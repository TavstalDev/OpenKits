package io.github.tavstal.openkits.models;

import io.github.tavstal.openkits.utils.LoggerUtils;
import org.bukkit.inventory.ItemStack;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a kit in the OpenKits plugin.
 */
public class Kit {
    public long Id;
    public String Name;
    public String Description;
    public Double Price;
    public boolean RequirePermission;
    public String Permission;
    public long Cooldown;
    public boolean IsOneTime;
    public boolean Enable;
    public byte[] Items;

    /**
     * Constructs a new Kit with the specified parameters.
     *
     * @param id                the unique identifier of the kit
     * @param name              the name of the kit
     * @param description       the description of the kit
     * @param price             the price of the kit
     * @param requirePermission whether the kit requires a permission
     * @param permission        the permission required to use the kit
     * @param cooldown          the cooldown time for the kit
     * @param isOneTime         whether the kit can be used only once
     * @param enable            whether the kit is enabled
     * @param items             the serialized items in the kit
     */
    public Kit(long id, String name, String description, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, byte[] items) {
        Id = id;
        Name = name;
        Description = description;
        Price = price;
        RequirePermission = requirePermission;
        Permission = permission;
        Cooldown = cooldown;
        IsOneTime = isOneTime;
        Enable = enable;
        Items = items;
    }

    /**
     * Constructs a new Kit with the specified parameters.
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
    public Kit(String name, String description, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, List<ItemStack> items) {
        Name = name;
        Description = description;
        Price = price;
        RequirePermission = requirePermission;
        Permission = permission;
        Cooldown = cooldown;
        IsOneTime = isOneTime;
        Enable = enable;
        Items = SerializeItems(items);
    }

    /**
     * Deserializes the items from the byte array.
     *
     * @return the list of deserialized items
     */
    @SuppressWarnings("unchecked")
    public List<ItemStack> GetItems() {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Items);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (List<ItemStack>) objectInputStream.readObject();
        }
        catch (IOException ex) {
            LoggerUtils.LogError("An IO error occurred while deserializing items.");
            LoggerUtils.LogError(ex.getMessage());
        }
        catch (ClassNotFoundException ex) {
            LoggerUtils.LogError("Failed to get class while deserializing items.");
            LoggerUtils.LogError(ex.getMessage());
        }
        catch (Exception ex) {
            LoggerUtils.LogError("Unexpected error happened while deserializing items..");
            LoggerUtils.LogError(ex.getMessage());
        }
        return new ArrayList<>();
    }

    /**
     * Deserializes the items from the given byte array.
     *
     * @param items the byte array containing the serialized items
     * @return the list of deserialized items
     */
    @SuppressWarnings("unchecked")
    public static List<ItemStack> GetItems(byte[] items) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(items);
             ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream)) {
            return (List<ItemStack>) objectInputStream.readObject();
        }
        catch (IOException ex) {
            LoggerUtils.LogError("An IO error occurred while deserializing items.");
            LoggerUtils.LogError(ex.getMessage());
        }
        catch (ClassNotFoundException ex) {
            LoggerUtils.LogError("Failed to get class while deserializing items.");
            LoggerUtils.LogError(ex.getMessage());
        }
        catch (Exception ex) {
            LoggerUtils.LogError("Unexpected error happened while deserializing items..");
            LoggerUtils.LogError(ex.getMessage());

        }
        return new ArrayList<>();
    }

    /**
     * Serializes the given list of items into a byte array.
     *
     * @param itemStacks the list of items to serialize
     * @return the byte array containing the serialized items
     */
    public static byte[] SerializeItems(List<ItemStack> itemStacks) {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(itemStacks);
            return byteArrayOutputStream.toByteArray();
        }
        catch (IOException ex) {
            LoggerUtils.LogError("An IO error occurred while serializing items.");
            LoggerUtils.LogError(ex.getMessage());
        }
        catch (Exception ex) {
            LoggerUtils.LogError("Unexpected error happened while serializing items..");
            LoggerUtils.LogError(ex.getMessage());

        }
        return new byte[0];
    }

}
