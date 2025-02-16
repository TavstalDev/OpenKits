package io.github.tavstal.openkits.utils;

import io.github.tavstal.openkits.OpenKits;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.Player;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Dictionary;
import java.util.Enumeration;

public class ChatUtils {

    private static String replacePlaceholders(String message) {
        String currencySingular = EconomyUtils.currencyNameSingular();
        String currencyPlural = EconomyUtils.currencyNamePlural();

        return message
                .replace("%prefix%", LocaleUtils.Localize("General.Prefix"))
                .replaceAll("%currency_plural%", currencyPlural == null ? LocaleUtils.Localize("General.CurrencyPlural") : currencyPlural)
                .replaceAll("%currency_singular%", currencySingular == null ? LocaleUtils.Localize("General.CurrencySingular") : currencySingular);
    }

    /**
     * Sends a colored message to a player.
     *
     * @param player  The player to send the message to.
     * @param message The raw message containing '&' color codes.
     */
    public static void sendRichMsg(Player player, String message) {
        player.sendMessage(translateColors(message, true));
    }

    /**
     * Retrieves a localized message, translates color codes, and sends it to a player.
     *
     * @param player     The player to send the message to.
     * @param key        The localization key.
     */
    public static void sendLocalizedMsg(Player player, String key) {
        String rawMessage = LocaleUtils.Localize(key);
        sendRichMsg(player, rawMessage);
    }

    /**
     * Retrieves a localized message, replaces placeholders with the provided parameters, translates color codes, and sends it to a player.
     *
     * @param player     The player to send the message to.
     * @param key        The localization key.
     * @param parameters The dictionary containing placeholder keys and their corresponding values.
     */
    public static void sendLocalizedMsg(Player player, String key, Dictionary<String, Object> parameters) {
        String rawMessage = LocaleUtils.Localize(key);

        // Get the keys
        Enumeration<String> keys = parameters.keys();
        while (keys.hasMoreElements()) {
            String dirKey = keys.nextElement();
            rawMessage = rawMessage.replace("%" + dirKey + "%", parameters.get(dirKey).toString());
        }

        sendRichMsg(player, rawMessage);
    }

    /**
     * Builds a Component message with buttons by replacing placeholders with the provided parameters.
     *
     * @param message    The raw message containing placeholders.
     * @param parameters The dictionary containing placeholder keys and their corresponding Component values.
     * @return The Component message with buttons.
     */
    public static Component buildWithButtons(String message, Dictionary<String, Component> parameters) {
        Component result = translateColors(message, true);
        // Get the keys
        Enumeration<String> keys = parameters.keys();
        while (keys.hasMoreElements()) {
            @RegExp String dirKey = keys.nextElement();
            Component dirElem = parameters.get(dirKey);
            if (!message.contains("%" + dirKey + "%"))
                continue;

            result = result.replaceText(TextReplacementConfig.builder()
                    .match(dirKey)
                    .replacement(dirElem)
                    .build());
        }
        return result;
    }

    /**
     * Translates color codes using MiniMessage format.
     *
     * @param message The raw message using MiniMessage syntax.
     * @return The translated Component message.
     */
    public static Component translateColors(@NotNull String message, boolean checkLegacy) {
        if (!checkLegacy)
            return MiniMessage.miniMessage().deserialize(replacePlaceholders(message));

        // Convert '&' to '§' first (since ChatColor.stripColor requires '§')
        String legacyColor = translateAlternateColorCodes(message);
        return MiniMessage.miniMessage().deserialize(replacePlaceholders(legacyToMiniMessage(legacyColor)));
    }

    /**
     * Translates alternate color codes ('&' followed by a color code character) in a given string to the Minecraft color code character '§'.
     *
     * @param textToTranslate The string containing the alternate color codes to be translated.
     * @return The translated string with Minecraft color codes.
     */
    public static @NotNull String translateAlternateColorCodes(@NotNull String textToTranslate) {
        char[] b = textToTranslate.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if (b[i] == '&' && "0123456789AaBbCcDdEeFfKkLlMmNnOoRrXx".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

    /**
     * Converts legacy '§' codes to MiniMessage tags.
     */
    private static String legacyToMiniMessage(String message) {
        return message
                .replace("§0", "<black>")
                .replace("§1", "<dark_blue>")
                .replace("§2", "<dark_green>")
                .replace("§3", "<dark_aqua>")
                .replace("§4", "<dark_red>")
                .replace("§5", "<dark_purple>")
                .replace("§6", "<gold>")
                .replace("§7", "<gray>")
                .replace("§8", "<dark_gray>")
                .replace("§9", "<blue>")
                .replace("§a", "<green>")
                .replace("§b", "<aqua>")
                .replace("§c", "<red>")
                .replace("§d", "<light_purple>")
                .replace("§e", "<yellow>")
                .replace("§f", "<white>")
                .replace("§l", "<bold>")
                .replace("§o", "<italic>")
                .replace("§n", "<underlined>")
                .replace("§m", "<strikethrough>")
                .replace("§k", "<obfuscated>")
                .replace("§r", "<reset>");
    }
}
