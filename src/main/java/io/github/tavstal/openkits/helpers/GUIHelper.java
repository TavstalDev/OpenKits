package io.github.tavstal.openkits.helpers;

import io.github.tavstal.minecorelib.utils.ChatUtils;
import io.github.tavstal.openkits.OpenKits;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for creating and managing GUI-related ItemStacks in the OpenKits plugin.
 */
public class GUIHelper {
    public static final NamespacedKey DupeKey = new NamespacedKey(OpenKits.Instance, "ProbablyDuped");

    /**
     * Creates an ItemStack with the specified material and display name.
     *
     * @param material The material of the item.
     * @param displayName The display name of the item.
     * @return The created ItemStack.
     */
    public static ItemStack createItem(@NotNull Material material, @NotNull String displayName) {
        return createItem(material, displayName, new ArrayList<>(), 1, null, null);
    }

    /**
     * Creates an ItemStack with the specified material, display name, and lore.
     *
     * @param material The material of the item.
     * @param displayName The display name of the item.
     * @param lore The lore of the item.
     * @return The created ItemStack.
     */
    public static ItemStack createItem(@NotNull Material material, @NotNull String displayName, List<Component> lore) {
        return createItem(material, displayName, lore, 1, null, null);
    }

    /**
     * Creates an ItemStack with the specified material, display name, and NBT data.
     *
     * @param material The material of the item.
     * @param displayName The display name of the item.
     * @param nbtKey The key for the NBT data.
     * @param nbtValue The value for the NBT data.
     * @return The created ItemStack.
     */
    public static ItemStack createItem(@NotNull Material material, @NotNull String displayName, @Nullable String nbtKey, @Nullable String nbtValue) {
        return createItem(material, displayName, new ArrayList<>(), 1, nbtKey, nbtValue);
    }

    /**
     * Creates an ItemStack with the specified material, display name, lore, and NBT data.
     *
     * @param material The material of the item.
     * @param displayName The display name of the item.
     * @param lore The lore of the item.
     * @param nbtKey The key for the NBT data.
     * @param nbtValue The value for the NBT data.
     * @return The created ItemStack.
     */
    public static ItemStack createItem(@NotNull Material material, @NotNull String displayName, List<Component> lore,  @Nullable String nbtKey, @Nullable String nbtValue) {
        return createItem(material, displayName, lore, 1, nbtKey, nbtValue);
    }

    /**
     * Creates an ItemStack with the specified material, display name, lore, amount, and NBT data.
     *
     * @param material The material of the item.
     * @param displayName The display name of the item.
     * @param lore The lore of the item.
     * @param amount The amount of the item.
     * @param nbtKey The key for the NBT data.
     * @param nbtValue The value for the NBT data.
     * @return The created ItemStack.
     */
    public static ItemStack createItem(@NotNull Material material, @NotNull String displayName, List<Component> lore, int amount, @Nullable String nbtKey, @Nullable String nbtValue) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();

        // Set display name
        meta.displayName(ChatUtils.translateColors(displayName, true));
        // Set lore
        meta.lore(lore);

        // Set NBT tag
        if (nbtKey != null && nbtValue != null) {
            NamespacedKey key = new NamespacedKey(OpenKits.Instance, nbtKey);
            meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, nbtValue);
        }

        meta.getPersistentDataContainer().set(DupeKey, PersistentDataType.BOOLEAN, true);

        // Apply the meta to the item
        item.setItemMeta(meta);

        return item;
    }

    /**
     * Gets an item from the player's inventory with the specified NBT key and value.
     *
     * @param player the player to search the inventory of.
     * @param key the key to search for.
     * @param value the value to search for.
     * @return the item with the specified NBT key and value, or null if not found.
     */
    public static ItemStack getItemWithNBT(Player player, NamespacedKey key, String value) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null)
                continue;

            ItemMeta meta = item.getItemMeta();
            if (meta == null)
                continue;

            PersistentDataContainer container = meta.getPersistentDataContainer();
            if (!container.has(key, PersistentDataType.STRING))
                continue;

            String nbtValue = container.get(key, PersistentDataType.STRING);
            if (!value.equals(nbtValue))
                continue;

            return item;
        }
        return null;
    }

    /**
     * Checks if the given ItemStack has the specified NBT key and value.
     *
     * @param item The ItemStack to check.
     * @param key The NamespacedKey to look for.
     * @param value The value associated with the key.
     * @return true if the ItemStack has the specified NBT key and value, false otherwise.
     */
    public static boolean hasNBT(@NotNull ItemStack item, @NotNull NamespacedKey key, @NotNull String value) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return false;

        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (!container.has(key, PersistentDataType.STRING))
            return false;

        String nbtValue = container.get(key, PersistentDataType.STRING);
        return value.equals(nbtValue);
    }

    /**
     * Checks if the given ItemStack is marked as duped.
     *
     * @param item The ItemStack to check.
     * @return true if the ItemStack is marked as duped, false otherwise.
     */
    public static boolean isDuped(@NotNull ItemStack item) {
        return hasNBT(item, DupeKey, "true");
    }
}