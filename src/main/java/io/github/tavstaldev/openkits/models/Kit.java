package io.github.tavstaldev.openkits.models;

import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.utils.EconomyUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Represents a kit in the OpenKits plugin.
 */
public class Kit {
    public long Id;
    public String Name;
    public String Icon;
    public Double Price;
    public boolean RequirePermission;
    public String Permission;
    public long Cooldown;
    public boolean IsOneTime;
    public boolean Enable;
    public byte[] Items;


    public Kit(long id, String name, String icon, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, byte[] items) {
        Id = id;
        Name = name;
        Icon = icon;
        Price = price;
        RequirePermission = requirePermission;
        Permission = permission;
        Cooldown = cooldown;
        IsOneTime = isOneTime;
        Enable = enable;
        Items = items;
    }


    public Kit(String name, String icon, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, List<ItemStack> items) {
        Name = name;
        Icon = icon;
        Price = price;
        RequirePermission = requirePermission;
        Permission = permission;
        Cooldown = cooldown;
        IsOneTime = isOneTime;
        Enable = enable;
        Items = OpenKits.ItemMetaSerializer.serializeItemStackListToBytes(items);
    }

    /**
     * Retrieves the icon material for the kit.
     * If the icon is not set or is empty, it returns the default icon material from the configuration.
     * If an error occurs, it logs the error and returns Material.CHEST.
     *
     * @return the Material representing the icon of the kit
     */
    public Material getIcon() {
        try
        {
            if (Icon == null || Icon.isEmpty())
                return Material.getMaterial(Objects.requireNonNull(OpenKits.config().getString("default.icon")));
            return Material.getMaterial(Icon);
        }
        catch (Exception ex) {
            OpenKits.logger().error("Failed to get kit icon.");
            OpenKits.logger().error(ex.getMessage());
            return Material.CHEST;
        }
    }

    /**
     * Deserializes the items from the byte array.
     *
     * @return the list of deserialized items
     */
    public List<ItemStack> getItems() {
        return OpenKits.ItemMetaSerializer.deserializeItemStackListFromBytes(Items);
    }


    /**
     * Gives the items from the kit to the specified player.
     *
     * @param player the player to whom the items will be given
     * @return true if the items were successfully given to the player
     */
    public boolean give(Player player) {
        List<ItemStack> items = getItems();
        for (ItemStack item : items) {
            Map<Integer, ItemStack> remainingItems = player.getInventory().addItem(item);
            if (!OpenKits.Instance.getConfig().getBoolean("dropItemsOnFullInventory"))
                continue;

            if (remainingItems.isEmpty())
                continue;

            for (ItemStack remainingItem : remainingItems.values()) {
                player.getWorld().dropItem(player.getLocation(), remainingItem);
            }
        }
        return true;
    }

    /**
     * Checks if the player can get the kit.
     *
     * @param player the player to check
     * @return true if the player can get the kit, false otherwise
     */
    public boolean canGet(Player player) {
        if (!Enable) {
            return false;
        }

        if (RequirePermission && !player.hasPermission(Permission))
            return false;

        KitCooldown cooldown = OpenKits.Database.findKitCooldown(player.getUniqueId(), Id);
        if (cooldown != null) {
            Duration duration = Duration.between(LocalDateTime.now(), cooldown.End);
            if (duration.getSeconds() > 0) {
                return false;
            }

            if (IsOneTime) {
                return false;
            }
        }

        return Price <= 0 || EconomyUtils.has(player, Price);
    }
}
