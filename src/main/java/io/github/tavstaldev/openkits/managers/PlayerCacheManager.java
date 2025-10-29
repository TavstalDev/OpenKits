package io.github.tavstaldev.openkits.managers;

import io.github.tavstaldev.openkits.models.PlayerCache;

import java.util.*;

/**
 * Manages player data for the OpenKits plugin.
 * Provides methods to add, remove, and retrieve player data.
 */
public class PlayerCacheManager {
    private static final Map<UUID, PlayerCache> _playerData = new HashMap<>();
    private static final Set<UUID> _markedForRemoval = new HashSet<>();

    /**
     * Adds player data to the manager.
     *
     * @param playerId   the UUID of the player
     * @param playerCache the data associated with the player
     */
    public static void add(UUID playerId, PlayerCache playerCache) {
        _playerData.put(playerId, playerCache);
    }

    /**
     * Removes player data from the manager.
     *
     * @param playerId the UUID of the player
     */
    public static void remove(UUID playerId) {
        _playerData.remove(playerId);
    }

    /**
     * Retrieves player data from the manager.
     *
     * @param playerId the UUID of the player
     * @return the data associated with the player, or null if not found
     */
    public static PlayerCache get(UUID playerId) {
        return _playerData.get(playerId);
    }

    /**
     * Marks a player for removal by adding their UUID to the removal set.
     *
     * @param playerId The UUID of the player to mark for removal.
     */
    public static void markForRemoval(UUID playerId) {
        _markedForRemoval.add(playerId);
    }

    /**
     * Unmarks a player for removal by removing their UUID from the removal set.
     *
     * @param playerId The UUID of the player to unmark for removal.
     */
    public static void unmarkForRemoval(UUID playerId) {
        _markedForRemoval.remove(playerId);
    }

    /**
     * Checks if a player is marked for removal.
     *
     * @param playerId The UUID of the player to check.
     * @return true if the player is marked for removal, false otherwise.
     */
    public static boolean isMarkedForRemoval(UUID playerId) {
        return _markedForRemoval.contains(playerId);
    }

    /**
     * Checks if the set of players marked for removal is empty.
     *
     * @return true if no players are marked for removal, false otherwise.
     */
    public static boolean isMarkedForRemovalEmpty() {
        return _markedForRemoval.isEmpty();
    }

    /**
     * Retrieves the set of UUIDs representing players marked for removal.
     *
     * @return A Set of UUIDs of players marked for removal.
     */
    public static Set<UUID> getMarkedForRemovalSet() {
        return new HashSet<>(_markedForRemoval); // Return a copy to prevent external modification
    }
}
