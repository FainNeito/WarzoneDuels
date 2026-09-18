package dev.minecraft.warzoneduels.adapter.bukkit.persistence;

import dev.minecraft.warzoneduels.WarzoneDuelsPlugin;
import dev.minecraft.warzoneduels.domain.DuelEndReason;
import dev.minecraft.warzoneduels.domain.DuelMatchType;
import dev.minecraft.warzoneduels.domain.analytics.DuelRecord;
import dev.minecraft.warzoneduels.domain.analytics.DuelRecordParticipant;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.Test;
import sun.misc.Unsafe;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class DuelAnalyticsTransactionTest {
    @Test
    void successStoresParentAndEveryParticipant() throws Exception {
        try (Connection connection = database()) {
            DuelAnalyticsStore store = store(connection);
            store.insert(record("success", false));
            assertEquals(1, rows(connection, "duel_records"));
            assertEquals(2, rows(connection, "duel_record_participants"));
            assertTrue(connection.getAutoCommit());
        }
    }

    @Test
    void participantFailureRollsBackParentAndPartialBatchAndAllowsRetry() throws Exception {
        try (Connection connection = database()) {
            DuelAnalyticsStore store = store(connection);
            store.insert(record("retry", true));
            assertEquals(0, rows(connection, "duel_records"));
            assertEquals(0, rows(connection, "duel_record_participants"));
            assertTrue(connection.getAutoCommit());
            store.insert(record("retry", false));
            assertEquals(1, rows(connection, "duel_records"));
            assertEquals(2, rows(connection, "duel_record_participants"));
        }
    }

    @Test
    void duplicateParentDoesNotDamageAnExistingRecord() throws Exception {
        try (Connection connection = database()) {
            DuelAnalyticsStore store = store(connection);
            store.insert(record("existing", false));
            store.insert(record("existing", false));
            assertEquals(1, rows(connection, "duel_records"));
            assertEquals(2, rows(connection, "duel_record_participants"));
            assertTrue(connection.getAutoCommit());
        }
    }

    @Test
    void failureInsideCallerTransactionPreservesEarlierWrites() throws Exception {
        try (Connection connection = database()) {
            DuelAnalyticsStore store = store(connection);
            connection.setAutoCommit(false);
            store.insert(record("earlier", false));
            store.insert(record("failed", true));
            assertEquals(1, rows(connection, "duel_records"));
            assertEquals(2, rows(connection, "duel_record_participants"));
            assertFalse(connection.getAutoCommit());
            connection.rollback();
            assertEquals(0, rows(connection, "duel_records"));
            assertEquals(0, rows(connection, "duel_record_participants"));
        }
    }

    private Connection database() throws Exception {
        return DriverManager.getConnection("jdbc:h2:mem:" + UUID.randomUUID());
    }

    private long rows(Connection connection, String table) throws Exception {
        try (var statement = connection.createStatement();
             var result = statement.executeQuery("SELECT COUNT(*) FROM " + table)) {
            result.next();
            return result.getLong(1);
        }
    }

    private DuelRecord record(String reference, boolean duplicateParticipant) {
        UUID first = UUID.randomUUID(), second = UUID.randomUUID();
        return new DuelRecord(reference, 1, 2, 1, first, "A", second, "B", first, "A", second, "B",
            "arena", "Arena", "default", "keep", DuelEndReason.DRAW, true, 0, 0,
            DuelMatchType.NORMAL, 1, List.of(new DuelRecordParticipant(first, "A", 1, true),
                new DuelRecordParticipant(duplicateParticipant ? first : second, "B", 2, false)));
    }

    // Keep the test on the real store/schema without booting a Paper server.
    private DuelAnalyticsStore store(Connection connection) throws Exception {
        Field unsafeField = Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        Unsafe unsafe = (Unsafe) unsafeField.get(null);
        WarzoneDuelsPlugin plugin = (WarzoneDuelsPlugin) unsafe.allocateInstance(WarzoneDuelsPlugin.class);
        Field logger = JavaPlugin.class.getDeclaredField("logger");
        logger.setAccessible(true);
        logger.set(plugin, Logger.getAnonymousLogger());
        DuelAnalyticsStore store = (DuelAnalyticsStore) unsafe.allocateInstance(DuelAnalyticsStore.class);
        Field pluginField = DuelAnalyticsStore.class.getDeclaredField("plugin");
        pluginField.setAccessible(true);
        pluginField.set(store, plugin);
        Field connectionField = DuelAnalyticsStore.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(store, connection);
        var initialize = DuelAnalyticsStore.class.getDeclaredMethod("initializeSchema");
        initialize.setAccessible(true);
        initialize.invoke(store);
        return store;
    }
}
