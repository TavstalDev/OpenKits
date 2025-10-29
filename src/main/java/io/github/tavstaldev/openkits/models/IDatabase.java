package io.github.tavstaldev.openkits.models;

import org.bukkit.Material;
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
    void load();

    /**
     * Called when the database is unloaded.
     */
    void unload();

    /**
     * Checks and updates the database schema if necessary.
     */
    void checkSchema();

    /**
     * Adds a new kit to the database.
     *
     * @param name              the name of the kit
     * @param icon              the icon of the kit
     * @param price             the price of the kit
     * @param requirePermission whether the kit requires a permission
     * @param permission        the permission required to use the kit
     * @param cooldown          the cooldown time for the kit
     * @param isOneTime         whether the kit can be used only once
     * @param enable            whether the kit is enabled
     * @param items             the list of items in the kit
     */
    void addKit(String name, Material icon, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, List<ItemStack> items);

    /**
     * Updates the name and description of a kit.
     *
     * @param id The ID of the kit to update.
     * @param name The new name of the kit.
     */
    void updateKitName(long id, String name);

    /**
     * Updates the permission requirements of a kit.
     *
     * @param id The ID of the kit to update.
     * @param requirePermission Whether the kit requires permission.
     * @param permission The permission required for the kit.
     */
    void updateKitPermission(long id, boolean requirePermission, String permission);

    /**
     * Updates the items of a kit.
     *
     * @param id The ID of the kit to update.
     * @param items The new items of the kit.
     */
    void updateKitItems(long id, List<ItemStack> items);

    /**
     * Updates the price of a kit.
     *
     * @param id The ID of the kit to update.
     * @param price The new price of the kit.
     */
    void updateKitPrice(long id, Double price);

    /**
     * Updates the cooldown of a kit.
     *
     * @param id The ID of the kit to update.
     * @param cooldown The new cooldown of the kit.
     */
    void updateKitCooldown(long id, long cooldown);

    /**
     * Enables or disables a kit.
     *
     * @param id The ID of the kit to update.
     * @param enable Whether to enable or disable the kit.
     */
    void updateKitEnabled(long id, boolean enable);

    /**
     * Updates the icon of a kit.
     *
     * @param id The ID of the kit to update.
     * @param icon The new icon of the kit.
     */
    void updateKitIcon(long id, Material icon);

    /**
     * Sets whether a kit is one-time use.
     *
     * @param id The ID of the kit to update.
     * @param isOneTime Whether the kit is one-time use.
     */
    void updateKitOneTime(long id, boolean isOneTime);

    /**
     * Removes a kit from the database.
     *
     * @param id The ID of the kit to remove.
     */
    void removeKit(long id);

    /**
     * Retrieves all kits from the database.
     *
     * @return A list of all kits.
     */
    List<Kit> getKits();

    /**
     * Finds a kit by its ID.
     *
     * @param id The ID of the kit to find.
     * @return The kit with the specified ID, or null if not found.
     */
    Kit findKit(long id);

    /**
     * Finds a kit by its name.
     *
     * @param name The name of the kit to find.
     * @return The kit with the specified name, or null if not found.
     */
    Kit findKit(String name);

    /**
     * Adds a cooldown period for a kit assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     * @param end the end time of the cooldown period
     */
    void addKitCooldown(UUID playerId, long kitId, LocalDateTime end);

    /**
     * Updates the cooldown period for a kit assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     * @param end the new end time of the cooldown period
     */
    void updateKitCooldown(UUID playerId, long kitId, LocalDateTime end);

    /**
     * Removes the cooldown period for a kit assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     */
    void removeKitCooldown(UUID playerId, long kitId);

    /**
     * Removes all cooldown periods for kits assigned to a player.
     *
     * @param playerId the unique identifier of the player
     */
    void removeKitCooldowns(UUID playerId);

    /**
     * Removes all cooldown periods for a specific kit.
     *
     * @param kitId the unique identifier of the kit
     */
    void removeKitCooldowns(long kitId);

    /**
     * Retrieves all cooldown periods for kits assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @return a list of all kit cooldowns for the specified player
     */
    List<KitCooldown> getKitCooldowns(UUID playerId);

    /**
     * Finds the cooldown period for a specific kit assigned to a player.
     *
     * @param playerId the unique identifier of the player
     * @param kitId the unique identifier of the kit
     * @return the kit cooldown for the specified player and kit, or null if not found
     */
    KitCooldown findKitCooldown(UUID playerId, long kitId);
}
