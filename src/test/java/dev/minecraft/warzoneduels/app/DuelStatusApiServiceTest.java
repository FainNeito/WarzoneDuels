package dev.minecraft.warzoneduels.app;

import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DuelStatusApiServiceTest {
    private final UUID fighter = UUID.randomUUID();
    private final UUID watcher = UUID.randomUUID();
    private final UUID idle = UUID.randomUUID();

    @Test void reportsMatchAndWatchStateOnTheMainThread() {
        var api = new DuelStatusApiService(Set.of(fighter)::contains, Set.of(watcher)::contains, () -> true);
        assertTrue(api.isInMatch(fighter));
        assertFalse(api.isInMatch(watcher));
        assertFalse(api.isInMatch(idle));
        assertTrue(api.isWatching(watcher));
        assertFalse(api.isWatching(fighter));
        assertFalse(api.isInMatch(null), "An unknown player is not in a match");
        assertFalse(api.isWatching(null));
    }

    @Test void refusesCallsOffTheMainThread() {
        var api = new DuelStatusApiService(Set.of(fighter)::contains, Set.of(watcher)::contains, () -> false);
        assertThrows(IllegalStateException.class, () -> api.isInMatch(fighter));
        assertThrows(IllegalStateException.class, () -> api.isWatching(watcher));
    }
}
