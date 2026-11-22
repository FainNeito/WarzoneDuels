package dev.minecraft.warzoneduels.adapter.bukkit.hud;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DuelTitlePackTest {
    @Test
    void everyCueHasEightDistinctVisibleFrames() throws Exception {
        Path assets = Path.of("resource-pack/assets/warzoneduels");
        String font = Files.readString(assets.resolve("font/duel_hud.json"));
        for (int row = 0; row < 8; row++) {
            String name = row < 5 ? "countdown_" + (row + 1) : new String[]{"fight", "defeat", "victory"}[row - 5];
            assertTrue(font.contains("warzoneduels:font/" + name + ".png"));
            BufferedImage atlas = ImageIO.read(assets.resolve("textures/font/" + name + ".png").toFile());
            int width = atlas.getWidth() / 8;
            var images = new java.util.HashSet<Integer>();
            for (int frame = 0; frame < 8; frame++) {
                String glyph = Character.toString(0xE100 + row * 8 + frame);
                assertTrue(font.contains(glyph), glyph);
                int[] pixels = atlas.getRGB(frame * width, 0, width, atlas.getHeight(), null, 0, width);
                assertTrue(java.util.Arrays.stream(pixels).anyMatch(pixel -> (pixel >>> 24) != 0));
                assertTrue(images.add(java.util.Arrays.hashCode(pixels)), "Repeated frame in " + name);
            }
        }
    }
}
