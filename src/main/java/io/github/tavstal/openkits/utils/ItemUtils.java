package io.github.tavstal.openkits.utils;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.profile.PlayerTextures;

import java.io.*;
import java.net.URL;
import java.util.*;

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
                    if (meta.hasDisplayName())
                        itemData.put("name", GsonComponentSerializer.gson().serialize(meta.displayName()));
                    if (meta.hasLore() && meta.lore() != null) {
                        List<String> lore = new ArrayList<>();
                        for (Component line : meta.lore()) {
                            lore.add(GsonComponentSerializer.gson().serialize(line));
                        }

                        itemData.put("lore", lore);
                    }

                    // Add durability
                    if (meta instanceof Damageable) {
                        itemData.put("durability", ((Damageable) meta).getDamage());
                    }

                    // Add nbt tags
                    if (meta.hasCustomModelData()) {
                        itemData.put("customModelData", meta.getCustomModelData());
                    }

                    // Enchants
                    serializeEnchants(meta, itemData);
                    // Banners
                    serializeBannerMeta(meta, itemData);
                    // Books
                    serializeBookMeta(meta, itemData);
                    // BlockData
                    serializeBlockDataMeta(meta, itemData);
                    // Crossbow
                    serializeCrossbowMeta(meta, itemData);
                    // Fireworks
                    //serializeFireworkMeta(meta, itemData);
                    // Leather Armor
                    serializeLeatherArmorMeta(meta, itemData);
                    // Maps
                    serializeMapMeta(meta, itemData);
                    // Potions
                    //serializePotionMeta(meta, itemData);
                    // Skulls
                    serializeSkullMeta(meta, itemData);
                    // Spawn Eggs
                    serializeSpawnEggMeta(meta, itemData);
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
                    ItemMeta meta = item.getItemMeta();

                    // if there is no name, then it does not have metadata.
                    if (itemData.containsKey("name")) {
                        meta.displayName(GsonComponentSerializer.gson().deserialize((String) itemData.get("name")));
                    }

                    // Lore
                    if (itemData.containsKey("lore")) {
                        List<String> lore = (List<String>) itemData.get("lore");
                        List<Component> loreList = new ArrayList<>();
                        for (String line : lore) {
                            loreList.add(GsonComponentSerializer.gson().deserialize(line));
                        }
                        meta.lore(loreList);
                    }

                    // Durability
                    if (itemData.containsKey("durability")) {
                        ((Damageable) meta).setDamage((int) itemData.get("durability"));
                    }

                    // customModelData
                    if (itemData.containsKey("customModelData")) {
                        meta.setCustomModelData((int) itemData.get("customModelData"));
                    }

                    // Enchants
                    deserializeEnchants(meta, itemData);
                    // Banners
                    deserializeBannerMeta(meta, itemData);
                    // Books
                    deserializeBookMeta(meta, itemData);
                    // BlockData
                    deserializeBlockDataMeta(meta, itemData);
                    // Crossbow
                    deserializeCrossbowMeta(meta, itemData);
                    // Fireworks
                    //deserializeFireworkMeta(meta, itemData);
                    // Leather Armor
                    deserializeLeatherArmorMeta(meta, itemData);
                    // Maps
                    deserializeMapMeta(meta, itemData);
                    // Potions
                    //deserializePotionMeta(meta, itemData);
                    // Skulls
                    deserializeSkullMeta(meta, itemData);
                    // Spawn Eggs
                    deserializeSpawnEggMeta(meta, itemData);

                    item.setItemMeta(meta);  // Set the meta data to the item
                    items.add(item);  // Add the deserialized ItemStack to the list
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            LoggerUtils.LogError("An error occurred while deserializing items: " + ex.getMessage());
        }
        return items;
    }

    /**
     * Serializes the enchantments of an item into a map.
     *
     * @param meta     The ItemMeta of the item to serialize.
     * @param itemData The map to store the serialized enchantments.
     */
    private static void serializeEnchants(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta.hasEnchants()) {
                Map<String, Integer> enchantments = new HashMap<>();
                for (var entry : meta.getEnchants().entrySet()) {
                    enchantments.put(entry.getKey().getKey().toString(), entry.getValue());  // Store enchantment names and levels
                }
                itemData.put("enchantments", enchantments);
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing enchantments: " + ex.getMessage());
        }
    }

    /**
     * Deserializes the enchantments of an item from a map.
     *
     * @param meta     The ItemMeta of the item to deserialize.
     * @param itemData The map containing the serialized enchantments.
     */
    private static void deserializeEnchants(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (itemData.containsKey("enchantments")) {
                Map<String, Integer> enchantments = (Map<String, Integer>) itemData.get("enchantments");
                for (var entry : enchantments.entrySet()) {
                    Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.fromString(entry.getKey()));
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, entry.getValue(), true);
                    }
                }
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing enchantments: " + ex.getMessage());
        }
    }

    /**
     * Serializes the metadata of a book item into a map.
     *
     * @param meta     The ItemMeta of the book to serialize.
     * @param itemData The map to store the serialized data.
     */
    private static void serializeBookMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof BookMeta bookMeta) {
                itemData.put("title", bookMeta.getTitle());
                itemData.put("author", bookMeta.getAuthor());
                List<String> pages = new ArrayList<>();
                for (Component page : bookMeta.pages()) {
                    pages.add(GsonComponentSerializer.gson().serialize(page));
                }
                itemData.put("pages", pages);
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing book meta: " + ex.getMessage());
        }
    }

    /**
     * Deserializes the metadata of a book item from a map.
     *
     * @param meta     The ItemMeta of the book to deserialize.
     * @param itemData The map containing the serialized data.
     */
    private static void deserializeBookMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof BookMeta bookMeta) {
                if (itemData.containsKey("title"))
                    bookMeta.setTitle((String) itemData.get("title"));
                if (itemData.containsKey("author"))
                    bookMeta.setAuthor((String) itemData.get("author"));
                if (itemData.containsKey("pages")) {
                    for (String page : (List<String>) itemData.get("pages")) {
                        bookMeta.addPages(GsonComponentSerializer.gson().deserialize(page));
                    }
                }
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing book meta: " + ex.getMessage());
        }
    }

    private static void serializePotionMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof PotionMeta potionMeta) {
                // Custom Potion Name
                if (potionMeta.hasCustomPotionName())
                    itemData.put("customPotionName", potionMeta.getCustomPotionName());
                // Color
                if (potionMeta.hasColor()) {
                    if (potionMeta.getColor() != null) {
                        var color = potionMeta.getColor();
                        itemData.put("color", String.format("%s;%s;%s;%s", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
                    }
                }
                // Base Potion Type
                if (potionMeta.getBasePotionType() != null)
                    itemData.put("basePotionType", potionMeta.getBasePotionType().getKey().getKey());
                // Custom Effects
                if (!potionMeta.getCustomEffects().isEmpty()) {
                    Map<String, Object> effects = new HashMap<>();
                    for (var customEffect : potionMeta.getCustomEffects()) {
                        effects.put(customEffect.getType().getKey().getKey(), customEffect.serialize());
                    }
                    itemData.put("customEffects", effects);
                }
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing potion meta: " + ex.getMessage());
        }
    }

    private static void deserializePotionMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof PotionMeta potionMeta) {
                // Custom Potion Name
                if (itemData.containsKey("customPotionName")) {
                    potionMeta.setCustomPotionName((String) itemData.get("customPotionName"));
                }
                // Color
                if (itemData.containsKey("color")) {
                    String[] colorData = ((String) itemData.get("color")).split(";");
                    potionMeta.setColor(Color.fromARGB(Integer.parseInt(colorData[3]), Integer.parseInt(colorData[0]), Integer.parseInt(colorData[1]), Integer.parseInt(colorData[2])));
                }
                // Base Potion Type
                if (itemData.containsKey("basePotionType")) {
                    String potion = (String) itemData.get("basePotionType");
                    var potionKey = NamespacedKey.fromString(potion);
                    if (potionKey != null)
                        potionMeta.setBasePotionType(RegistryAccess.registryAccess().getRegistry(RegistryKey.POTION).get(potionKey));
                    else
                        LoggerUtils.LogDebug("Potion key type not found: " + potion);
                }
                // Custom Effects
                if (itemData.containsKey("customEffects")) {
                    Map<String, Object> effects = (Map<String, Object>) itemData.get("customEffects");
                    for (var entry : effects.entrySet()) {
                        var effectKey = NamespacedKey.fromString(entry.getKey());
                        if (effectKey != null) {
                            var data = (Map<String, Object>) entry.getValue();
                            PotionEffectType type = RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(effectKey);
                            if (type != null) {
                                int duration = (int) data.getOrDefault("duration", 200);
                                int amplifier = (int) data.getOrDefault("amplifier", 0);
                                boolean ambient = (boolean) data.getOrDefault("ambient", false);
                                boolean particles = (boolean) data.getOrDefault("particles", true);
                                potionMeta.addCustomEffect(new PotionEffect(type, duration, amplifier, ambient, particles), true);
                            } else
                                LoggerUtils.LogDebug("Potion effect type not found: " + effectKey);
                        } else
                            LoggerUtils.LogDebug("Potion effect key not found: " + entry.getKey());
                    }
                }
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing potion meta: " + ex.getMessage());
        }
    }

    private static void serializeFireworkMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof FireworkMeta fireworkMeta) {
                if (fireworkMeta.hasEffects()) {
                    List<Map<String, Object>> effects = new ArrayList<>();
                    int index = 0;
                    for (var effect : fireworkMeta.getEffects()) {
                        var serialized = effect.serialize();
                        effects.add(index, serialized);
                        index++;
                    }
                    itemData.put("effects", effects);
                }

                if (fireworkMeta.hasPower()) {
                    itemData.put("power", fireworkMeta.getPower());
                }
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing firework meta: " + ex.getMessage());
        }
    }

    private static void deserializeFireworkMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof FireworkMeta fireworkMeta) {
                if (itemData.containsKey("effects")) {
                    List<Map<String, Object>> effects = (List<Map<String, Object>>) itemData.get("effects");
                    for (var effect : effects) {
                        var builder = FireworkEffect.builder();
                        if (effect.containsKey("flicker"))
                            builder.flicker((boolean) effect.get("flicker"));
                        if (effect.containsKey("trail"))
                            builder.trail((boolean) effect.get("trail"));
                        if (effect.containsKey("type"))
                            builder.with(FireworkEffect.Type.valueOf((String) effect.get("type")));
                        if (effect.containsKey("colors")) {
                            List<Color> colors = new ArrayList<>();
                            for (var color : (List<Map<String, Object>>) effect.get("colors")) {
                                colors.add(Color.deserialize(color));
                            }
                            builder.withColor(colors);
                        }
                        if (effect.containsKey("fadeColors")) {
                            List<Color> fadeColors = new ArrayList<>();
                            for (var color : (List<Map<String, Object>>) effect.get("fadeColors")) {
                                fadeColors.add(Color.deserialize(color));
                            }
                            builder.withFade(fadeColors);
                        }
                        fireworkMeta.addEffect(builder.build());
                    }
                }
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing firework meta: " + ex.getMessage());
        }
    }

    /**
     * Serializes the metadata of a leather armor item into a map.
     *
     * @param meta     The ItemMeta of the leather armor to serialize.
     * @param itemData The map to store the serialized data.
     */
    private static void serializeLeatherArmorMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
                var color = leatherArmorMeta.getColor();
                itemData.put("color", String.format("%s;%s;%s;%s", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing leather armor meta: " + ex.getMessage());
        }
    }

    /**
     * Deserializes the metadata of a leather armor item from a map.
     *
     * @param meta     The ItemMeta of the leather armor to deserialize.
     * @param itemData The map containing the serialized data.
     */
    private static void deserializeLeatherArmorMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof LeatherArmorMeta leatherArmorMeta) {
                if (!itemData.containsKey("color"))
                    return;

                String[] colorData = ((String) itemData.get("color")).split(";");
                leatherArmorMeta.setColor(Color.fromARGB(Integer.parseInt(colorData[3]), Integer.parseInt(colorData[0]), Integer.parseInt(colorData[1]), Integer.parseInt(colorData[2])));
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing leather armor meta: " + ex.getMessage());
        }
    }

    /**
     * Serializes the metadata of a skull item into a map.
     *
     * @param meta     The ItemMeta of the skull to serialize.
     * @param itemData The map to store the serialized data.
     */
    private static void serializeSkullMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof SkullMeta skullMeta) {
                if (skullMeta.hasOwner() && skullMeta.getOwningPlayer() != null)
                    itemData.put("owner", skullMeta.getOwningPlayer().getUniqueId());

                if (skullMeta.getPlayerProfile() != null) {
                    var profile = skullMeta.getPlayerProfile();
                    if (profile.hasTextures())
                        itemData.put("profileUrl", profile.getTextures().getSkin());
                    itemData.put("profile", profile.getId());
                }
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing skull meta: " + ex.getMessage());
        }
    }

    /**
     * Deserializes the metadata of a skull item from a map.
     *
     * @param meta     The ItemMeta of the skull to deserialize.
     * @param itemData The map containing the serialized data.
     */
    private static void deserializeSkullMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof SkullMeta skullMeta) {
                if (itemData.containsKey("owner")) {
                    var player = Bukkit.getOfflinePlayer((UUID) itemData.get("owner"));
                    skullMeta.setOwningPlayer(player);
                }

                // Restore player profile (for custom skins)
                if (itemData.containsKey("profile")) {
                    UUID profileUUID = (UUID) itemData.get("profile");
                    var profile = Bukkit.createProfile(profileUUID);
                    var textures = profile.getTextures();
                    if (itemData.containsKey("profileUrl")) {
                        textures.setSkin((URL)itemData.get("profileUrl"));
                        profile.setTextures(textures);
                    }
                    skullMeta.setPlayerProfile(profile);
                }

            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing skull meta: " + ex.getMessage());
        }
    }

    private static void serializeBannerMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof BannerMeta bannerMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing banner meta: " + ex.getMessage());
        }
    }

    private static void deserializeBannerMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof BannerMeta bannerMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing banner meta: " + ex.getMessage());
        }
    }

    private static void serializeMapMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof MapMeta mapMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing map meta: " + ex.getMessage());
        }
    }

    private static void deserializeMapMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof MapMeta mapMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing map meta: " + ex.getMessage());
        }
    }

    private static void serializeSpawnEggMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof SpawnEggMeta spawnEggMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing spawn egg meta: " + ex.getMessage());
        }
    }

    private static void deserializeSpawnEggMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof SpawnEggMeta spawnEggMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing spawn egg meta: " + ex.getMessage());
        }
    }

    private static void serializeBlockDataMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof BlockDataMeta blockDataMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing block data meta: " + ex.getMessage());
        }
    }

    private static void deserializeBlockDataMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof BlockDataMeta blockDataMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing block data meta: " + ex.getMessage());
        }
    }

    private static void serializeCrossbowMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof CrossbowMeta crossbowMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while serializing crossbow meta: " + ex.getMessage());
        }
    }

    private static void deserializeCrossbowMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof CrossbowMeta crossbowMeta) {
                // TODO
            }
        }
        catch (Exception ex) {
            LoggerUtils.LogError("An error occurred while deserializing crossbow meta: " + ex.getMessage());
        }
    }
}
