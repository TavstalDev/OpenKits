package io.github.tavstal.openkits.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemUtils {
    public static byte[] serializeItemStackList(List<ItemStack> items) {
        List<Map<String, Object>> itemDataList = new ArrayList<>();
        for (ItemStack item : items) {
            Map<String, Object> itemData = new HashMap<>();

            itemData.put("material", item.getType().toString());  // Store the material (type) of the item
            itemData.put("amount", item.getAmount());  // Store the amount of the item

            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    // Add item meta data like display name, lore, etc. (Optional)
                    itemData.put("name", meta.displayName());
                    itemData.put("lore", meta.hasLore() ? meta.lore() : new ArrayList<String>());

                    // Add durability
                    if (meta instanceof Damageable) {
                        itemData.put("durability", ((Damageable) meta).getDamage());
                    }

                    // Add enchants
                    if (meta.hasEnchants()) {
                        Map<String, Integer> enchantments = new HashMap<>();
                        for (var entry : meta.getEnchants().entrySet()) {
                            enchantments.put(entry.getKey().getKey().toString(), entry.getValue());  // Store enchantment names and levels
                        }
                        itemData.put("enchantments", enchantments);
                    }

                    // Add nbt tags
                    if (meta.hasCustomModelData()) {
                        itemData.put("customModelData", meta.getCustomModelData());
                    }

                    // Book
                    serializeBookMeta(meta, itemData);

                    // TODO
                }
            }

            itemDataList.add(itemData);  // Add the map to the list
        }

        // Serialize the list of maps into byte[]
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        try (ObjectOutputStream objectStream = new ObjectOutputStream(byteStream)) {
            objectStream.writeObject(itemDataList);
        } catch (IOException ex) {
            LoggerUtils.LogError("An error occurred while serializing items: " + ex.getMessage());
        }
        return byteStream.toByteArray();
    }

    public static List<ItemStack> deserializeItemStackList(byte[] data) {
        List<ItemStack> items = new ArrayList<>();
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {
            List<Map<String, Object>> itemDataList = (List<Map<String, Object>>) objectStream.readObject();

            for (Map<String, Object> itemData : itemDataList) {
                String materialString = (String) itemData.get("material");
                Material material = Material.getMaterial(materialString);  // Convert material string to Material enum
                int amount = (int) itemData.get("amount");  // Get the item amount

                if (material != null) {
                    ItemStack item = new ItemStack(material, amount);

                    if (itemData.containsKey("name")) {
                        ItemMeta meta = item.getItemMeta();
                        meta.displayName((Component) itemData.get("name"));

                        if (itemData.containsKey("lore")) {
                            List<Component> lore = (List<Component>) itemData.get("lore");
                            meta.lore(lore);
                        }

                        if (itemData.containsKey("durability")) {
                            ((Damageable) meta).setDamage((int) itemData.get("durability"));
                        }

                        if (itemData.containsKey("enchantments")) {
                            Map<String, Integer> enchantments = (Map<String, Integer>) itemData.get("enchantments");
                            for (var entry : enchantments.entrySet()) {
                                Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.fromString(entry.getKey()));
                                if (enchantment != null) {
                                    meta.addEnchant(enchantment, entry.getValue(), true);
                                }
                            }
                        }

                        if (itemData.containsKey("customModelData")) {
                            meta.setCustomModelData((int) itemData.get("customModelData"));
                        }

                        // TODO

                        item.setItemMeta(meta);  // Set the meta data to the item
                    }

                    items.add(item);  // Add the deserialized ItemStack to the list
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            LoggerUtils.LogError("An error occurred while deserializing items: " + ex.getMessage());
        }
        return items;
    }

    private static void serializeBookMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof BookMeta bookMeta) {
            itemData.put("title", bookMeta.getTitle());
            itemData.put("author", bookMeta.getAuthor());
            itemData.put("pages", bookMeta.pages());
        }
    }

    private static void deserializeBookMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof BookMeta bookMeta) {
            bookMeta.setTitle((String) itemData.get("title"));
            bookMeta.setAuthor((String) itemData.get("author"));
            bookMeta.pages((List<Component>) itemData.get("pages"));
        }
    }

    private static void serializePotionMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof PotionMeta potionMeta) {
            // TODO
        }
    }

    private static void deserializePotionMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof PotionMeta potionMeta) {
            // TODO
        }
    }

    private static void serializeFireworkMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof PotionMeta potionMeta) {
            // TODO
        }
    }

    private static void deserializeFireworkMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof PotionMeta potionMeta) {
            // TODO
        }
    }

    private static void serializeLeatherArmorMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
            // TODO
        }
    }

    private static void deserializeLeatherArmorMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
            // TODO
        }
    }

    private static void serializeSkullMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof SkullMeta skullMeta) {
            // TODO
        }
    }

    private static void deserializeSkullMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof SkullMeta skullMeta) {
            // TODO
        }
    }

    private static void serializeBannerMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof BannerMeta bannerMeta) {
            // TODO
        }
    }

    private static void deserializeBannerMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof BannerMeta bannerMeta) {
            // TODO
        }
    }

    private static void serializeMapMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof MapMeta mapMeta) {
            // TODO
        }
    }

    private static void deserializeMapMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof MapMeta mapMeta) {
            // TODO
        }
    }

    private static void serializeSpawnEggMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof SpawnEggMeta spawnEggMeta) {
            // TODO
        }
    }

    private static void deserializeSpawnEggMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof SpawnEggMeta spawnEggMeta) {
            // TODO
        }
    }

    private static void serializeBlockDataMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof BlockDataMeta blockDataMeta) {
            // TODO
        }
    }

    private static void deserializeBlockDataMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof BlockDataMeta blockDataMeta) {
            // TODO
        }
    }

    private static void serializeCrossbowMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof CrossbowMeta crossbowMeta) {
            // TODO
        }
    }

    private static void deserializeCrossbowMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof CrossbowMeta crossbowMeta) {
            // TODO
        }
    }
}
