package dev.minecraft.warzoneduels.adapter.bukkit.persistence;

import org.junit.jupiter.api.Test;
import java.sql.DriverManager;
import static org.junit.jupiter.api.Assertions.*;

class WeeklyWinsQueryTest {
    @Test
    void countsOnlyQualifiedWinsAndUsesLatestNameWithinHalfOpenWindow() throws Exception {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:weekly;DB_CLOSE_DELAY=-1")) {
            try (var statement = connection.createStatement()) {
                statement.execute("CREATE TABLE duel_records(reference VARCHAR, winner_id VARCHAR, winner_name VARCHAR, ended_at BIGINT, counted_as_match BOOLEAN, end_reason VARCHAR DEFAULT 'KILL')");
                statement.execute("INSERT INTO duel_records(reference,winner_id,winner_name,ended_at,counted_as_match) VALUES ('a','uuid-a','OldName',100,TRUE),('b','uuid-a','NewName',199,TRUE),"
                    + "('c','uuid-b','B',101,TRUE),('d','uuid-b','B',150,TRUE),('e','uuid-c','C',120,TRUE),"
                    + "('f','uuid-d','D',125,TRUE),('before','uuid-c','C',99,TRUE),('end','uuid-c','C',200,TRUE),"
                    + "('cancel','uuid-c','C',140,FALSE),('draw',NULL,NULL,140,TRUE)");
                statement.execute("INSERT INTO duel_records VALUES ('malformed-draw','uuid-c','C',145,TRUE,'DRAW')");
            }
            var results = WeeklyWinsQuery.topThree(connection, 100, 200);
            assertEquals(3, results.size());
            assertEquals("uuid-a", results.get(0).get("id"));
            assertEquals("NewName", results.get(0).get("name"));
            assertEquals(2L, results.get(0).get("wins"));
            assertEquals("uuid-b", results.get(1).get("id"));
            assertEquals(1L, results.get(2).get("wins"));
            assertEquals(java.util.Set.of("id", "name", "wins"), results.get(0).keySet());
            assertTrue(WeeklyWinsQuery.topThree(connection, 300, 400).isEmpty());
            assertThrows(IllegalArgumentException.class, () -> WeeklyWinsQuery.topThree(connection, 200, 100));
        }
    }
}
