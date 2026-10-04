package dev.minecraft.warzoneduels.app;

import dev.minecraft.warzoneduels.domain.DuelCooldownPolicy;
import dev.minecraft.warzoneduels.port.DuelCooldownStore;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.LongSupplier;

/** Durable protection is committed before advancement-bearing evidence is emitted. */
public final class DuelCooldownService {
    private final DuelCooldownStore store;
    private final LongSupplier clock;
    private final Consumer<String> warning;
    private DuelCooldownPolicy policy = new DuelCooldownPolicy(DuelCooldownPolicy.Snapshot.empty());
    private long playerWindow = 300_000;
    private long pairWindow = 86_400_000;
    private boolean healthy;

    public DuelCooldownService(DuelCooldownStore store, LongSupplier clock, Consumer<String> warning) {
        this.store = store;
        this.clock = clock;
        this.warning = warning;
    }
    public void enable() {
        healthy = false;
        try {
            policy = new DuelCooldownPolicy(store.load());
            store.save(policy.snapshot());
            healthy = true;
        } catch (IOException ex) { fail(ex); }
    }
    public void configure(long playerSeconds, long pairSeconds) {
        playerWindow = DuelCooldownPolicy.windowMillis(playerSeconds);
        pairWindow = DuelCooldownPolicy.windowMillis(pairSeconds);
    }
    public boolean isHealthy() { return healthy; }
    public boolean ensureWritable() { return healthy && persist(); }
    public DuelCooldownPolicy.Block block(List<UUID> first, List<UUID> second) {
        return policy.block(first, second, clock.getAsLong(), playerWindow, pairWindow);
    }
    public boolean recordCompletion(List<UUID> first, List<UUID> second) {
        if (!healthy) return false;
        policy.recordCompletion(first, second, clock.getAsLong());
        return persist();
    }
    public boolean creditChallenge(List<UUID> first, List<UUID> second) {
        if (!healthy || !policy.creditChallenge(first, second, clock.getAsLong(), pairWindow)) return false;
        return persist();
    }
    private boolean persist() {
        try {
            store.save(policy.snapshot());
            return true;
        } catch (IOException ex) {
            fail(ex);
            return false;
        }
    }
    private void fail(IOException ex) {
        healthy = false;
        warning.accept("Duel cooldown protection unavailable; new duels and match/challenge advancement evidence blocked: " + ex.getMessage());
    }
}
