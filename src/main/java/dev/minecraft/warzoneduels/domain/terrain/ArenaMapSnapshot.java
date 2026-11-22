package dev.minecraft.warzoneduels.domain.terrain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public record ArenaMapSnapshot(
    String mapId,
    String worldName,
    int footprintBlockCount,
    List<String> blockDataEntries
) {
    public ArenaMapSnapshot withTeamPlates(List<FootprintBlock> footprint, List<List<FootprintBlock>> teams) {
        if (teams.stream().allMatch(team -> team.size() <= 1)) {
            return this;
        }
        var occupied = new HashSet<FootprintBlock>();
        for (var team : teams) {
            for (var feet : team) {
                if (!occupied.add(feet) || !occupied.add(floorBelow(feet))) {
                    throw new IllegalArgumentException("Starting plates overlap at " + feet);
                }
            }
        }

        var indices = new HashMap<FootprintBlock, Integer>();
        for (int i = 0; i < footprint.size(); i++) {
            var block = footprint.get(i);
            if (occupied.contains(block)) {
                indices.put(block, i);
            }
        }
        for (var block : occupied) {
            if (!indices.containsKey(block)) {
                throw new IllegalArgumentException("Starting plate outside the saved footprint at " + block);
            }
        }
        var prepared = new ArrayList<>(blockDataEntries);
        for (var team : teams) {
            var primary = team.getFirst();
            for (int slot = 1; slot < team.size(); slot++) {
                var destination = team.get(slot);
                prepared.set(indices.get(destination), blockDataEntries.get(indices.get(primary)));
                prepared.set(indices.get(floorBelow(destination)), blockDataEntries.get(indices.get(floorBelow(primary))));
            }
        }
        return new ArenaMapSnapshot(mapId, worldName, footprintBlockCount, List.copyOf(prepared));
    }

    private static FootprintBlock floorBelow(FootprintBlock feet) {
        return new FootprintBlock(feet.x(), feet.y() - 1, feet.z());
    }
}
