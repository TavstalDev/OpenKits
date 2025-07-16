package io.github.tavstal.openkits.managers;

import io.github.tavstal.openkits.models.PlayerData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages player data for the OpenKits plugin.
 * Provides methods to add, remove, and retrieve player data.
 */
public class PlayerManager {
    private static final Map<UUID, PlayerData> _playerData = new HashMap<>();

    /**
     * Adds player data to the manager.
     *
     * @param playerId   the UUID of the player
     * @param playerData the data associated with the player
     */
    public static void addPlayerData(UUID playerId, PlayerData playerData) {
        _playerData.put(playerId, playerData);
    }

    /**
     * Removes player data from the manager.
     *
     * @param playerId the UUID of the player
     */
    public static void removePlayerData(UUID playerId) {
        _playerData.remove(playerId);
    }

    /**
     * Retrieves player data from the manager.
     *
     * @param playerId the UUID of the player
     * @return the data associated with the player, or null if not found
     */
    public static PlayerData getPlayerData(UUID playerId) {
        return _playerData.get(playerId);
    }
}
