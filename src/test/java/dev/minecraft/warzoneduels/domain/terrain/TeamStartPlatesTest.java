package dev.minecraft.warzoneduels.domain.terrain;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class TeamStartPlatesTest {
    private final List<FootprintBlock> blocks = new ArrayList<>();
    private final List<String> data = new ArrayList<>();

    private List<FootprintBlock> team(int z, String floor, String surface) {
        var slots = new ArrayList<FootprintBlock>();
        for (int x : List.of(0, 2, -2)) {
            blocks.add(new FootprintBlock(x, 63, z));
            data.add(x == 0 ? floor : "minecraft:sand");
            var feet = new FootprintBlock(x, 64, z);
            blocks.add(feet);
            slots.add(feet);
            data.add(x == 0 ? surface : "minecraft:air");
        }
        return slots;
    }

    @Test
    void copiesOnlyOccupiedPlatesFromEachSideAndPreservesTheMap() {
        var red = team(0, "minecraft:red_concrete", "minecraft:stone_pressure_plate[powered=false]");
        var blue = team(20, "minecraft:blue_concrete", "minecraft:blue_carpet");
        var map = new ArenaMapSnapshot("desert", "world", data.size(), List.copyOf(data));
        for (int size : List.of(1, 2, 3)) {
            var prepared = map.withTeamPlates(blocks, List.of(red.subList(0, size), blue.subList(0, size)));
            for (int side : List.of(0, 1)) {
                int primary = side * 6;
                for (int slot = 0; slot < 3; slot++) {
                    int offset = primary + slot * 2;
                    int expected = slot < size ? primary : offset;
                    assertEquals(data.subList(expected, expected + 2), prepared.blockDataEntries().subList(offset, offset + 2));
                }
            }
        }
        assertEquals(data, map.blockDataEntries());
    }

    @Test
    void rejectsOverlappingOrUnmappedPlatesBeforeChangingTheSnapshot() {
        var red = team(0, "a", "b");
        var blue = team(20, "c", "d");
        var map = new ArenaMapSnapshot("desert", "world", data.size(), List.copyOf(data));
        for (var invalid : List.of(red.get(0), new FootprintBlock(50, 64, 10))) {
            assertThrows(IllegalArgumentException.class, () -> map.withTeamPlates(blocks,
                List.of(List.of(red.get(0), red.get(1)), List.of(blue.get(0), invalid))));
        }
        assertEquals(data, map.blockDataEntries());
    }
}
