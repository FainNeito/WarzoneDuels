package dev.minecraft.warzoneduels.app;

import dev.minecraft.warzoneduels.api.DuelStatusApi;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

/** Exposes {@link DuelService}'s match and watch state to other plugins (REQ-035). */
public final class DuelStatusApiService implements DuelStatusApi {
    private final Predicate<UUID> inMatch;
    private final Predicate<UUID> watching;
    private final BooleanSupplier mainThread;

    public DuelStatusApiService(Predicate<UUID> inMatch, Predicate<UUID> watching, BooleanSupplier mainThread) {
        this.inMatch = Objects.requireNonNull(inMatch, "inMatch");
        this.watching = Objects.requireNonNull(watching, "watching");
        this.mainThread = Objects.requireNonNull(mainThread, "mainThread");
    }

    @Override
    public boolean isInMatch(UUID player) {
        requireMainThread();
        return player != null && inMatch.test(player);
    }

    @Override
    public boolean isWatching(UUID player) {
        requireMainThread();
        return player != null && watching.test(player);
    }

    private void requireMainThread() {
        if (!mainThread.getAsBoolean()) {
            throw new IllegalStateException("DuelStatusApi must be called on the main thread");
        }
    }
}
