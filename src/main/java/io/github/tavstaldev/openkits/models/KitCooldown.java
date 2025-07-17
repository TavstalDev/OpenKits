package io.github.tavstaldev.openkits.models;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a cooldown period for a kit assigned to a player.
 */
public class KitCooldown {
    /**
     * The unique identifier of the player.
     */
    public UUID PlayerId;
    /**
     * The unique identifier of the kit.
     */
    public long KitId;
    /**
     * The end time of the cooldown period.
     */
    public LocalDateTime End;

    /**
     * Constructs a new KitCooldown instance.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     * @param end the end time of the cooldown period
     */
    public KitCooldown(UUID playerId, long kitId, LocalDateTime end) {
        PlayerId = playerId;
        KitId = kitId;
        End = end;
    }
}
