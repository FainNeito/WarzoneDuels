package dev.minecraft.warzoneduels.adapter.bukkit.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Read-only public projection, independent of Bukkit and game-thread state. */
public final class WeeklyWinsQuery {
    private WeeklyWinsQuery() { }

    public static List<Map<String, Object>> topThree(Connection connection, long start, long end) throws SQLException {
        if (end <= start) {
            throw new IllegalArgumentException("Invalid weekly interval");
        }
        List<Map<String, Object>> result = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement("""
            SELECT wins.winner_id, wins.total,
                (SELECT r.winner_name FROM duel_records r
                 WHERE r.winner_id = wins.winner_id AND r.counted_as_match = TRUE AND r.end_reason <> 'DRAW'
                   AND r.ended_at >= ? AND r.ended_at < ?
                 ORDER BY r.ended_at DESC, r.reference ASC LIMIT 1) AS latest_name
            FROM (SELECT winner_id, COUNT(*) AS total FROM duel_records
                  WHERE counted_as_match = TRUE AND end_reason <> 'DRAW' AND winner_id IS NOT NULL AND winner_id <> ''
                    AND ended_at >= ? AND ended_at < ?
                  GROUP BY winner_id) wins
            ORDER BY wins.total DESC, wins.winner_id ASC LIMIT 3
            """)) {
            statement.setLong(1, start);
            statement.setLong(2, end);
            statement.setLong(3, start);
            statement.setLong(4, end);
            try (ResultSet rows = statement.executeQuery()) {
                while (rows.next()) {
                    result.add(Map.of("id", rows.getString("winner_id"),
                        "name", rows.getString("latest_name"), "wins", rows.getLong("total")));
                }
            }
        }
        return List.copyOf(result);
    }
}
