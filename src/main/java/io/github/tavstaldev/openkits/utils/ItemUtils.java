package io.github.tavstaldev.openkits.utils;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.TypeUtils;
import io.github.tavstaldev.openkits.OpenKits;
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

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Utility class for item-related operations.
 */
public class ItemUtils {
    private static final PluginLogger _logger = OpenKits.Logger().WithModule(ItemUtils.class);
    private static final PluginLogger _typeUtilsLogger = _logger.WithModule(TypeUtils.class);

    /**
     * Serializes a list of ItemStack objects into a byte array.
     *
     * @param items The list of ItemStack objects to serialize.
     * @return A byte array representing the serialized list of ItemStack objects.
     */
    public static byte[] serializeItemStackList(List<ItemStack> items) {
        List<Map<String, Object>> itemDataList = new ArrayList<>();
        for (ItemStack item : items) {
            Map<String, Object> itemData = new HashMap<>();
            _logger.Debug("Serializing item: " + item.getType().name());
            itemData.put("material", item.getType().toString());  // Store the material (type) of the item
            itemData.put("amount", item.getAmount());  // Store the amount of the item


            if (item.hasItemMeta()) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {

                    // Add item meta data like display name, lore, etc. (Optional)
                    var displayName = meta.displayName();
                    if (displayName != null)
                        itemData.put("name", GsonComponentSerializer.gson().serialize(displayName));
                    var metaLore = meta.lore();
                    if (metaLore != null) {
                        List<String> lore = new ArrayList<>();
                        for (Component line : metaLore) {
                            lore.add(GsonComponentSerializer.gson().serialize(line));
                        }

                        itemData.put("lore", lore);
                    }

                    // Add durability
                    if (meta instanceof Damageable) {
                        itemData.put("durability", ((Damageable) meta).getDamage());
                    }

                    // Add nbt tags
                    // TODO: Replace when its replacement is stable
                    if (meta.hasCustomModelData()) {
                        var customModelData = meta.getCustomModelData();
                        itemData.put("customModelData", customModelData);
                    }

                    // Enchants
                    serializeEnchants(meta, itemData);
                    // Books
                    serializeBookMeta(meta, itemData);
                    // Crossbow
                    serializeCrossbowMeta(meta, itemData);
                    // Firework Effect
                    serializeFireworkEffectMeta(meta, itemData);
                    // Fireworks
                    serializeFireworkMeta(meta, itemData);
                    // Leather Armor
                    serializeLeatherArmorMeta(meta, itemData);
                    // Potions
                    serializePotionMeta(meta, itemData);
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
            _logger.Error("An error occurred while serializing items: " + ex.getMessage());
        }
        return byteStream.toByteArray();
    }

    /**
     * Deserializes a byte array into a list of ItemStack objects.
     *
     * @param data The byte array representing the serialized list of ItemStack objects.
     * @return A list of deserialized ItemStack objects.
     */
    public static List<ItemStack> deserializeItemStackList(byte[] data) {
        List<ItemStack> items = new ArrayList<>();
        try (ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
             ObjectInputStream objectStream = new ObjectInputStream(byteStream)) {

            var streamObject = objectStream.readObject();
            if (!(streamObject instanceof List)) {
                _logger.Error("Deserialized object is not a List.");
                return items;
            }

            @SuppressWarnings("unchecked") List<Map<String, Object>> itemDataList = (List<Map<String, Object>>) streamObject;

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
                        var loreData = itemData.get("lore");
                        if (loreData instanceof List) {
                            //noinspection unchecked
                            List<String> lore = (List<String>)loreData;
                            List<Component> loreList = new ArrayList<>();
                            for (String line : lore) {
                                loreList.add(GsonComponentSerializer.gson().deserialize(line));
                            }
                            meta.lore(loreList);
                        }
                    }

                    // Durability
                    if (itemData.containsKey("durability")) {
                        ((Damageable) meta).setDamage((int) itemData.get("durability"));
                    }

                    // customModelData
                    if (itemData.containsKey("customModelData")) {
                        // TODO: Replace when its replacement is stable
                        meta.setCustomModelData((int) itemData.get("customModelData"));
                    }

                    // Enchants
                    deserializeEnchants(meta, itemData);
                    // Books
                    deserializeBookMeta(meta, itemData);
                    // Crossbow
                    deserializeCrossbowMeta(meta, itemData);
                    // FireworkEffect
                    deserializeFireworkEffectMeta(meta, itemData);
                    // Fireworks
                    deserializeFireworkMeta(meta, itemData);
                    // Leather Armor
                    deserializeLeatherArmorMeta(meta, itemData);
                    // Potions
                    deserializePotionMeta(meta, itemData);
                    // Skulls
                    deserializeSkullMeta(meta, itemData);
                    // Spawn Eggs
                    deserializeSpawnEggMeta(meta, itemData);

                    item.setItemMeta(meta);  // Set the meta data to the item
                    items.add(item);  // Add the deserialized ItemStack to the list
                }
            }
        } catch (IOException | ClassNotFoundException ex) {
            _logger.Error("An error occurred while deserializing items: " + ex.getMessage());
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
            _logger.Error("An error occurred while serializing enchantments: " + ex.getMessage());
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
                @SuppressWarnings("unchecked") Map<String, Integer> enchantments = (Map<String, Integer>) itemData.get("enchantments");
                for (var entry : enchantments.entrySet()) {
                    var namespacedKey = NamespacedKey.fromString(entry.getKey());
                    if (namespacedKey == null)
                        continue;
                    Enchantment enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(namespacedKey);
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, entry.getValue(), true);
                    }
                }
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while deserializing enchantments: " + ex.getMessage());
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
            _logger.Error("An error occurred while serializing book meta: " + ex.getMessage());
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
                    //noinspection unchecked
                    for (String page : (List<String>) itemData.get("pages")) {
                        bookMeta.addPages(GsonComponentSerializer.gson().deserialize(page));
                    }
                }
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while deserializing book meta: " + ex.getMessage());
        }
    }

    /**
     * Serializes the metadata of a potion item into a map.
     *
     * @param meta     The ItemMeta of the potion to serialize.
     * @param itemData The map to store the serialized data.
     */
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
            _logger.Error("An error occurred while serializing potion meta: " + ex.getMessage());
        }
    }

    /**
     * Deserializes the metadata of a potion item from a map.
     *
     * @param meta     The ItemMeta of the potion to deserialize.
     * @param itemData The map containing the serialized data.
     */
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
                        _logger.Debug("Potion key type not found: " + potion);
                }
                // Custom Effects
                if (itemData.containsKey("customEffects")) {
                    //noinspection unchecked
                    Map<String, Object> effects = (Map<String, Object>) itemData.get("customEffects");
                    for (var entry : effects.entrySet()) {
                        var effectKey = NamespacedKey.fromString(entry.getKey());
                        if (effectKey != null) {
                            //noinspection unchecked
                            var data = (Map<String, Object>) entry.getValue();
                            PotionEffectType type = RegistryAccess.registryAccess().getRegistry(RegistryKey.MOB_EFFECT).get(effectKey);
                            if (type != null) {
                                int duration = (int) data.getOrDefault("duration", 200);
                                int amplifier = (int) data.getOrDefault("amplifier", 0);
                                boolean ambient = (boolean) data.getOrDefault("ambient", false);
                                boolean particles = (boolean) data.getOrDefault("particles", true);
                                potionMeta.addCustomEffect(new PotionEffect(type, duration, amplifier, ambient, particles), true);
                            } else
                                _logger.Debug("Potion effect type not found: " + effectKey);
                        } else
                            _logger.Debug("Potion effect key not found: " + entry.getKey());
                    }
                }
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while deserializing potion meta: " + ex.getMessage());
        }
    }

    /**
     * Serializes the metadata of a firework item into a map.
     *
     * @param meta     The ItemMeta of the firework to serialize.
     * @param itemData The map to store the serialized data.
     */
    private static void serializeFireworkMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof FireworkMeta fireworkMeta) {
                if (fireworkMeta.hasEffects()) {
                    List<Map<String, Object>> effects = new ArrayList<>();
                    for (var effect : fireworkMeta.getEffects()) {
                        Map<String, Object> effectData = new HashMap<>();
                        effectData.put("type", effect.getType().name());
                        effectData.put("flicker", effect.hasFlicker());
                        effectData.put("trail", effect.hasTrail());

                        // Colors
                        var colorList =  effect.getColors();
                        List<String> colors = new ArrayList<>();
                        for (Color color : colorList) {
                            colors.add(String.format("%s;%s;%s;%s", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
                        }
                        effectData.put("colors", colors);

                        // FadeColors
                        var fadeColor =  effect.getFadeColors();
                        List<String> fadeColors = new ArrayList<>();
                        for (Color color : fadeColor) {
                            fadeColors.add(String.format("%s;%s;%s;%s", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
                        }
                        effectData.put("fadeColors", fadeColors);
                        effects.add(effectData);
                    }
                    itemData.put("effects", effects);
                }

                if (fireworkMeta.hasPower()) {
                    itemData.put("power", fireworkMeta.getPower());
                }
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while serializing firework meta: " + ex.getMessage());
        }
    }

    /**
     * Deserializes the metadata of a firework item from a map.
     *
     * @param meta     The ItemMeta of the firework to deserialize.
     * @param itemData The map containing the serialized data.
     */
    private static void deserializeFireworkMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (meta instanceof FireworkMeta fireworkMeta) {
                if (itemData.containsKey("effects")) {
                    //noinspection unchecked
                    List<Map<String, Object>> effects = (List<Map<String, Object>>) itemData.get("effects");
                    for (var effectData : effects) {
                        FireworkEffect.Type type = FireworkEffect.Type.valueOf((String) effectData.get("type"));
                        boolean flicker = (boolean) effectData.get("flicker");
                        boolean trail = (boolean) effectData.get("trail");

                        List<Color> colors = new ArrayList<>();
                        //noinspection unchecked
                        for (String colorData : (List<String>) effectData.get("colors")) {
                            String[] color = colorData.split(";");
                            colors.add(Color.fromARGB(Integer.parseInt(color[3]), Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
                        }

                        List<Color> fadeColors = new ArrayList<>();
                        //noinspection unchecked
                        for (String colorData : (List<String>) effectData.get("fadeColors")) {
                            String[] color = colorData.split(";");
                            fadeColors.add(Color.fromARGB(Integer.parseInt(color[3]), Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
                        }

                        fireworkMeta.addEffect(FireworkEffect.builder().flicker(flicker).trail(trail).with(type).withColor(colors).withFade(fadeColors).build());
                    }
                }

                if (itemData.containsKey("power")) {
                    fireworkMeta.setPower((int) itemData.get("power"));
                }
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while deserializing firework meta: " + ex.getMessage());
        }
    }

    /**
     * Serializes the metadata of a firework effect item into a map.
     *
     * @param meta     The ItemMeta of the firework effect to serialize.
     * @param itemData The map to store the serialized data.
     */
    private static void serializeFireworkEffectMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof FireworkEffectMeta fireworkEffectMeta) {
            if (fireworkEffectMeta.hasEffect() && fireworkEffectMeta.getEffect() != null) {
                var effect = fireworkEffectMeta.getEffect();
                Map<String, Object> effectData = new HashMap<>();
                effectData.put("type", effect.getType().name());
                effectData.put("flicker", effect.hasFlicker());
                effectData.put("trail", effect.hasTrail());

                // Colors
                var colorList = effect.getColors();
                List<String> colors = new ArrayList<>();
                for (Color color : colorList) {
                    colors.add(String.format("%s;%s;%s;%s", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
                }
                effectData.put("colors", colors);

                // FadeColors
                var fadeColor = effect.getFadeColors();
                List<String> fadeColors = new ArrayList<>();
                for (Color color : fadeColor) {
                    fadeColors.add(String.format("%s;%s;%s;%s", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha()));
                }
                effectData.put("fadeColors", fadeColors);

                itemData.put("effect", effectData);
            }
        }
    }

    /**
     * Deserializes the metadata of a firework effect item from a map.
     *
     * @param meta     The ItemMeta of the firework effect to deserialize.
     * @param itemData The map containing the serialized data.
     */
    private static void deserializeFireworkEffectMeta(ItemMeta meta, Map<String, Object> itemData) {
        if (meta instanceof FireworkEffectMeta fireworkEffectMeta) {
            if (itemData.containsKey("effect")) {
                //noinspection unchecked
                Map<String, Object> effectData = (Map<String, Object>) itemData.get("effect");

                FireworkEffect.Type type = FireworkEffect.Type.valueOf((String) effectData.get("type"));
                boolean flicker = (boolean) effectData.get("flicker");
                boolean trail = (boolean) effectData.get("trail");

                List<Color> colors = new ArrayList<>();
                //noinspection unchecked
                for (String colorData : (List<String>) effectData.get("colors")) {
                    String[] color = colorData.split(";");
                    colors.add(Color.fromARGB(Integer.parseInt(color[3]), Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
                }

                List<Color> fadeColors = new ArrayList<>();
                //noinspection unchecked
                for (String colorData : (List<String>) effectData.get("fadeColors")) {
                    String[] color = colorData.split(";");
                    fadeColors.add(Color.fromARGB(Integer.parseInt(color[3]), Integer.parseInt(color[0]), Integer.parseInt(color[1]), Integer.parseInt(color[2])));
                }

                fireworkEffectMeta.setEffect(FireworkEffect.builder().flicker(flicker).trail(trail).with(type).withColor(colors).withFade(fadeColors).build());
            }
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
            if (!(meta instanceof LeatherArmorMeta leatherArmorMeta)) {
                _logger.Debug("ItemMeta is not an instance of LeatherArmorMeta.");
                return;
            }

            var color = leatherArmorMeta.getColor();
            itemData.put("color", String.format("%s;%s;%s;%s",
                    color.getRed(),
                    color.getGreen(),
                    color.getBlue(),
                    color.getAlpha())
            );
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while serializing leather armor meta: " + ex.getMessage());
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
            if (!(meta instanceof LeatherArmorMeta leatherArmorMeta)) {
                _logger.Debug("ItemMeta is not an instance of LeatherArmorMeta.");
                return;
            }
            if (!itemData.containsKey("color"))
                return;

            String[] colorData = ((String) itemData.get("color")).split(";");
            int red = Integer.parseInt(colorData[0]);
            int green = Integer.parseInt(colorData[1]);
            int blue = Integer.parseInt(colorData[2]);
            int alpha = Integer.parseInt(colorData[3]);
            leatherArmorMeta.setColor(Color.fromARGB(alpha, red, green, blue));
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while deserializing leather armor meta: " + ex.getMessage());
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
            if (!(meta instanceof SkullMeta skullMeta)) {
                _logger.Debug("ItemMeta is not an instance of SkullMeta.");
                return;
            }

            if (skullMeta.hasOwner() && skullMeta.getOwningPlayer() != null)
                itemData.put("owner", skullMeta.getOwningPlayer().getUniqueId());

            if (skullMeta.getPlayerProfile() != null) {
                var profile = skullMeta.getPlayerProfile();
                if (profile.hasTextures())
                    itemData.put("profileUrl", profile.getTextures().getSkin());
                itemData.put("profile", profile.getId());
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while serializing skull meta: " + ex.getMessage());
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
            if (!(meta instanceof SkullMeta skullMeta)) {
                _logger.Debug("ItemMeta is not an instance of SkullMeta.");
                return;
            }

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
                    textures.setSkin((URL) itemData.get("profileUrl"));
                    profile.setTextures(textures);
                }
                skullMeta.setPlayerProfile(profile);
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while deserializing skull meta: " + ex.getMessage());
        }
    }

    /**
     * Serializes the metadata of a spawn egg item into a map.
     *
     * @param meta     The ItemMeta of the spawn egg to serialize.
     * @param itemData The map to store the serialized data.
     */
    private static void serializeSpawnEggMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (!(meta instanceof SpawnEggMeta spawnEggMeta)) {
                _logger.Debug("ItemMeta is not an instance of SpawnEggMeta.");
                return;
            }

            if (spawnEggMeta.getSpawnedEntity() == null)
                return;

            if (spawnEggMeta.getCustomSpawnedType() == null)
                return;

            itemData.put("customEntityType", spawnEggMeta.getCustomSpawnedType().getKey().getKey());
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while serializing spawn egg meta: " + ex.getMessage());
        }
    }

    /**
     * Deserializes the metadata of a spawn egg item from a map.
     *
     * @param meta     The ItemMeta of the spawn egg to deserialize.
     * @param itemData The map containing the serialized data.
     */
    private static void deserializeSpawnEggMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (!(meta instanceof SpawnEggMeta spawnEggMeta)) {
                _logger.Debug("ItemMeta is not an instance of SpawnEggMeta.");
                return;
            }

            if (!itemData.containsKey("customEntityType")) {
                return;
            }

            String entityType = (String) itemData.get("customEntityType");
            var key = NamespacedKey.fromString(entityType);
            if (key != null) {
                spawnEggMeta.setCustomSpawnedType(RegistryAccess.registryAccess().getRegistry(RegistryKey.ENTITY_TYPE).get(key));
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while deserializing spawn egg meta: " + ex.getMessage());
        }
    }

    /**
     * Serializes the metadata of a crossbow item into a map.
     *
     * @param meta     The ItemMeta of the crossbow to serialize.
     * @param itemData The map to store the serialized data.
     */
    private static void serializeCrossbowMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (!(meta instanceof CrossbowMeta crossbowMeta)) {
                _logger.Debug("ItemMeta is not an instance of CrossbowMeta.");
                return;
            }

            if (!crossbowMeta.hasChargedProjectiles()) {
                return;
            }

            List<ItemStack> projectiles = crossbowMeta.getChargedProjectiles();
            var projectileData = serializeItemStackList(projectiles);
            itemData.put("projectiles", projectileData);
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while serializing crossbow meta: " + ex.getMessage());
        }
    }

    /**
     * Deserializes the metadata of a crossbow item from a map.
     *
     * @param meta     The ItemMeta of the crossbow to deserialize.
     * @param itemData The map containing the serialized data.
     */
    private static void deserializeCrossbowMeta(ItemMeta meta, Map<String, Object> itemData) {
        try {
            if (!(meta instanceof CrossbowMeta crossbowMeta)) {
                _logger.Debug("ItemMeta is not an instance of CrossbowMeta.");
                return;
            }

            if (!itemData.containsKey("projectiles"))
                return;

            var rawProjectiles = itemData.get("projectiles");
            if (!(rawProjectiles instanceof byte[])) {
                _logger.Error("Expected projectiles data to be a byte array.");
                return;
            }

            List<ItemStack> projectiles = deserializeItemStackList((byte[])rawProjectiles);
            crossbowMeta.setChargedProjectiles(projectiles);
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while deserializing crossbow meta: " + ex.getMessage());
        }
    }
}
