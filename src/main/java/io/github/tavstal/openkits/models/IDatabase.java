package io.github.tavstal.openkits.models;

import org.bukkit.inventory.ItemStack;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Interface for database operations related to kits.
 */
public interface IDatabase {

    /**
     * Called when the database is loaded.
     */
    void Load();

    /**
     * Called when the database is unloaded.
     */
    void Unload();

    /**
     * Checks and updates the database schema if necessary.
     */
    void CheckSchema();

    /**
     * Adds a new kit to the database.
     *
     * @param name              the name of the kit
     * @param description       the description of the kit
     * @param price             the price of the kit
     * @param requirePermission whether the kit requires a permission
     * @param permission        the permission required to use the kit
     * @param cooldown          the cooldown time for the kit
     * @param isOneTime         whether the kit can be used only once
     * @param enable            whether the kit is enabled
     * @param items             the list of items in the kit
     */
    void AddKit(String name, String description, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, List<ItemStack> items);

    /**
     * Updates the name and description of a kit.
     *
     * @param id The ID of the kit to update.
     * @param name The new name of the kit.
     * @param description The new description of the kit.
     */
    void UpdateKit(long id, String name, String description);

    /**
     * Updates the permission requirements of a kit.
     *
     * @param id The ID of the kit to update.
     * @param requirePermission Whether the kit requires permission.
     * @param permission The permission required for the kit.
     */
    void UpdateKit(long id, boolean requirePermission, String permission);

    /**
     * Updates the items of a kit.
     *
     * @param id The ID of the kit to update.
     * @param items The new items of the kit.
     */
    void UpdateKit(long id, List<ItemStack> items);

    /**
     * Updates the price of a kit.
     *
     * @param id The ID of the kit to update.
     * @param price The new price of the kit.
     */
    void UpdateKit(long id, Double price);

    /**
     * Updates the cooldown of a kit.
     *
     * @param id The ID of the kit to update.
     * @param cooldown The new cooldown of the kit.
     */
    void UpdateKit(long id, long cooldown);

    /**
     * Enables or disables a kit.
     *
     * @param id The ID of the kit to update.
     * @param enable Whether to enable or disable the kit.
     */
    void UpdateKit(long id, boolean enable);

    /**
     * Sets whether a kit is one-time use.
     *
     * @param id The ID of the kit to update.
     * @param isOneTime Whether the kit is one-time use.
     */
    void UpdateKitOneTime(long id, boolean isOneTime);

    /**
     * Removes a kit from the database.
     *
     * @param id The ID of the kit to remove.
     */
    void RemoveKit(long id);

    /**
     * Retrieves all kits from the database.
     *
     * @return A list of all kits.
     */
    List<Kit> GetKits();

    /**
     * Finds a kit by its ID.
     *
     * @param id The ID of the kit to find.
     * @return The kit with the specified ID, or null if not found.
     */
    Kit FindKit(long id);

    /**
     * Finds a kit by its name.
     *
     * @param name The name of the kit to find.
     * @return The kit with the specified name, or null if not found.
     */
    Kit FindKit(String name);

    /**
     * Adds a cooldown period for a kit assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     * @param end the end time of the cooldown period
     */
    void AddKitCooldown(UUID playerId, long kitId, LocalDateTime end);

    /**
     * Updates the cooldown period for a kit assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     * @param end the new end time of the cooldown period
     */
    void UpdateKitCooldown(UUID playerId, long kitId, LocalDateTime end);

    /**
     * Removes the cooldown period for a kit assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     */
    void RemoveKitCooldown(UUID playerId, long kitId);

    /**
     * Removes all cooldown periods for kits assigned to a player.
     *
     * @param playerId the unique identifier of the player
     */
    void RemoveKitCooldowns(UUID playerId);

    /**
     * Removes all cooldown periods for a specific kit.
     *
     * @param kitId the unique identifier of the kit
     */
    void RemoveKitCooldowns(long kitId);

    /**
     * Retrieves all cooldown periods for kits assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @return a list of all kit cooldowns for the specified player
     */
    List<KitCooldown> GetKitCooldowns(UUID playerId);

    /**
     * Finds the cooldown period for a specific kit assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     * @return the kit cooldown for the specified player and kit, or null if not found
     */
    KitCooldown FindKitCooldown(UUID playerId, long kitId);
}
