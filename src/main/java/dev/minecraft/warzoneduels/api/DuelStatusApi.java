package dev.minecraft.warzoneduels.api;

import java.util.UUID;

/**
 * Match status for other plugins (for example EnthusiaFriends' Join, which must not move a player out of a duel),
 * registered with Bukkit's ServicesManager. Calls must be made on the main thread. This API performs no permission
 * checks and sends no player messages.
 */
public interface DuelStatusApi {

    /**
     * Whether {@code player} is in a match: from the moment their request or party challenge is accepted by every
     * player it needs, through a queued start, arena preparation and the start countdown, until the duel ends or they
     * are eliminated. A sent but unaccepted challenge, an eliminated player and a watcher are not in a match.
     *
     * @throws IllegalStateException if called off the main thread
     */
    boolean isInMatch(UUID player);

    /**
     * Whether {@code player} is watching a duel (an active watch session).
     *
     * @throws IllegalStateException if called off the main thread
     */
    boolean isWatching(UUID player);
}
