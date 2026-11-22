import java.util.*;
import java.lang.reflect.Proxy;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.text.TextComponent;
import dev.minecraft.warzoneduels.adapter.bukkit.hud.DuelHud;
public class HudProbe extends JavaPlugin {
    static class Client {
        final List<String> frames = new ArrayList<>();
        final UUID id = UUID.randomUUID();
        final List<Float> pitches = new ArrayList<>();
        final List<Integer> ticks = new ArrayList<>();
        boolean dead;
        final Player player;
        Client(boolean pack) {
            player=(Player)Proxy.newProxyInstance(Player.class.getClassLoader(),new Class[]{Player.class},(p,m,a)->{
                return switch(m.getName()) {
                    case "getUniqueId" -> id;
                    case "isOnline" -> true;
                    case "isDead" -> dead;
                    case "hasResourcePack" -> pack;
                    case "playSound" -> {
                        if (!Sound.BLOCK_NOTE_BLOCK_BASS.equals(a[1])) throw new AssertionError("unexpected sound");
                        pitches.add((Float)a[3]);
                        ticks.add(Bukkit.getCurrentTick());
                        yield null;
                    }
                    case "getLocation" -> new Location(Bukkit.getWorlds().getFirst(),0,80,0);
                    case "showTitle" -> {
                        var title=(TextComponent)((Title)a[0]).title();
                        frames.add(title.content());
                        if(title.content().charAt(0)>=0xe100 && !"warzoneduels:duel_hud".equals(title.font().asString())) throw new AssertionError("font");
                        yield null;
                    }
                    default -> throw new UnsupportedOperationException(m.toString());
                };
            });
        }
        void verify(int row) {
            var expected=new ArrayList<String>();
            for(int i=0;i<8;i++)expected.add(Character.toString(0xe100+8*row+i));
            if(!expected.equals(frames))throw new AssertionError("row "+row+" frames="+frames);
        }
    }
    public void onEnable() {
        var plugin=(JavaPlugin)Bukkit.getPluginManager().getPlugin("WarzoneDuels");
        var hud=new DuelHud(plugin);
        Bukkit.getScheduler().runTask(this,()->{
            try {
                var countdown=new Client(true); var start=new Client(true); var loss=new Client(true);
                var win=new Client(true); var plain=new Client(false); var cancelled=new Client(true);
                hud.countdown(countdown.player,3,"3v3 · Desert"); hud.fight(start.player);
                loss.dead=true; hud.result(loss.player,false,true);
                if(!loss.frames.isEmpty() || !loss.pitches.isEmpty())throw new AssertionError("premature defeat");
                loss.dead=false; hud.respawn(loss.player); hud.result(win.player,true,true);
                var fallbackLoss=new Client(false); hud.result(fallbackLoss.player,false,false);
                var stoppedLoss=new Client(true); hud.result(stoppedLoss.player,false,true); hud.stop(stoppedLoss.id);
                hud.fight(plain.player); hud.fight(cancelled.player); hud.stop(cancelled.id);
                Bukkit.getScheduler().runTaskLater(this,()->{
                    try {
                        for (Client loser : List.of(fallbackLoss,loss)) {
                            if (!loser.pitches.equals(List.of(0.78f,0.58f)) || loser.ticks.get(1)-loser.ticks.get(0)!=5)
                                throw new AssertionError("defeat notes: "+loser.pitches+" ticks "+loser.ticks);
                        }
                        if (!win.pitches.isEmpty() || !stoppedLoss.pitches.equals(List.of(0.78f))) throw new AssertionError("recipient/cancellation");
                        countdown.verify(2); start.verify(5); loss.verify(6); win.verify(7);
                        if(!plain.frames.equals(List.of("FIGHT!")) || !cancelled.frames.isEmpty())throw new AssertionError("fallback / cancellation");
                        getLogger().info("PASS all four eight-frame title sequences, no-pack fallback, cancellation, respawn");
                    } finally {hud.close();Bukkit.shutdown();}
                },24);
            } catch(Throwable t) {t.printStackTrace();Bukkit.shutdown();}
        });
    }
}

