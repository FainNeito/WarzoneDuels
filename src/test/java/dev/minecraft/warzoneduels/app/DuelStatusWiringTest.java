package dev.minecraft.warzoneduels.app;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Source contract for REQ-035: DuelService is Bukkit-coupled, so the stages it counts are checked in its source. */
class DuelStatusWiringTest {
    private static String body(String source, String boundary) {
        int start = source.indexOf(boundary);
        assertTrue(start >= 0, boundary + " is missing");
        String body = source.substring(start);
        return body.substring(0, body.indexOf("\n    }") + 6);
    }

    @Test void matchStatusCoversEveryStageFromAcceptanceToElimination() throws Exception {
        String service = Files.readString(Path.of("src/main/java/dev/minecraft/warzoneduels/app/DuelService.java")).replace("\r\n", "\n");
        String inMatch = body(service, "public boolean isInMatch(");
        for (String stage : new String[]{"isInActiveDuel(", "preparingDuel", "queuedDuelStart", "readyChallengeFor("}) {
            assertTrue(inMatch.contains(stage), "isInMatch must count " + stage);
        }
        assertFalse(inMatch.contains("pendingRequest"), "An unaccepted request is not a match");
        assertTrue(body(service, "public boolean isWatching(").contains("isActiveWatcher("));
    }

    @Test void pluginRegistersTheStatusApiOnTheMainThread() throws Exception {
        String plugin = Files.readString(Path.of("src/main/java/dev/minecraft/warzoneduels/WarzoneDuelsPlugin.java"));
        assertTrue(plugin.contains("DuelStatusApi.class") && plugin.contains("new DuelStatusApiService("), "DuelStatusApi is registered");
        assertTrue(plugin.contains("isInMatch") && plugin.contains("isWatching") && plugin.contains("Bukkit::isPrimaryThread"));
    }
}
