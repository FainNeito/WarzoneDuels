package dev.minecraft.warzoneduels.adapter.bukkit.hud;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/** Sends server-timed bitmap frames from the optional duel HUD resource pack. */
public final class DuelHud {
    private static final Key FONT = Key.key("warzoneduels", "duel_hud");
    private static final int FRAME_COUNT = 8;
    private final JavaPlugin plugin;
    private final Map<UUID, BukkitTask> playing = new HashMap<>();
    private record Result(boolean won, boolean team) {}
    private final Map<UUID, Result> respawnResults = new HashMap<>();

    public DuelHud(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void countdown(Player player, int seconds, String match) {
        show(player, seconds <= 5 ? seconds - 1 : -1, Integer.toString(seconds), match, NamedTextColor.GOLD, 6, 0);
    }

    public void fight(Player player) {
        show(player, 5, "FIGHT!", "", NamedTextColor.GREEN, 1, 3);
    }

    public void result(Player player, boolean won, boolean team) {
        if (player.isDead()) {
            stop(player.getUniqueId());
            // Encode the complete result so it survives the match being cleaned up before respawn.
            respawnResults.put(player.getUniqueId(), new Result(won, team));
            return;
        }
        show(player, won ? 7 : 6, won ? "VICTORY" : "DEFEAT",
            team ? (won ? "Your team won the duel" : "Your team was defeated") :
                (won ? "You won the duel" : "You were defeated"),
            won ? NamedTextColor.GOLD : NamedTextColor.RED, 24, 7);
    }

    public void respawn(Player player) {
        var result = respawnResults.get(player.getUniqueId());
        if (result != null) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (respawnResults.remove(player.getUniqueId(), result) && player.isOnline()) {
                    result(player, result.won(), result.team());
                }
            });
        }
    }

    public void stop(UUID id) {
        var task = playing.remove(id);
        if (task != null) task.cancel();
        respawnResults.remove(id);
    }

    public void close() {
        playing.values().forEach(BukkitTask::cancel);
        playing.clear();
        respawnResults.clear();
    }

    private void show(Player player, int row, String text, String subtitle, NamedTextColor color, int hold, int fade) {
        UUID id = player.getUniqueId();
        stop(id);
        if (!player.isOnline()) return;
        Component caption = Component.text(subtitle, NamedTextColor.WHITE);
        if (row < 0 || !plugin.getConfig().getBoolean("settings.animated-hud-pack", false) || !player.hasResourcePack()) {
            player.showTitle(Title.title(Component.text(text, color), caption, times(14 + hold, fade)));
            return;
        }
        BukkitTask animation = new BukkitRunnable() {
            private int frame;
            @Override
            public void run() {
                if (!player.isOnline() || player.isDead()) {
                    playing.remove(id);
                    cancel();
                    return;
                }
                boolean last = frame == FRAME_COUNT - 1;
                Component glyph = Component.text(Character.toString(0xE100 + row * FRAME_COUNT + frame)).font(FONT);
                player.showTitle(Title.title(glyph, caption, last ? times(hold, fade) : times(4, 0)));
                frame++;
                if (last) {
                    playing.remove(id);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
        playing.put(id, animation);
    }

    private static Title.Times times(int hold, int fade) {
        return Title.Times.times(Duration.ZERO, Duration.ofMillis(50L * hold), Duration.ofMillis(50L * fade));
    }
}
