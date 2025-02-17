package io.github.tavstal.openkits.utils;

import io.github.tavstal.openkits.OpenKits;
import org.bukkit.entity.Player;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.*;

/**
 * Utility class for handling localization using YAML files.
 */
public class LocaleUtils {
    private static Dictionary<String, Map<String, Object>> _localization;
    private static String _defaultLocale = "eng";

    /**
     * Loads the localization file based on the locale specified in the plugin's config.
     *
     * @return true if the localization file was successfully loaded, false otherwise.
     */
    public static Boolean Load() {
        InputStream inputStream;
        _localization = new Hashtable<>();
        _defaultLocale = OpenKits.Instance.getConfig().getString("locale");

        Path dirPath = Paths.get(OpenKits.Instance.getDataFolder().getPath(), "lang");
        if (!Files.exists(dirPath))
            try
            {
                Files.createDirectory(dirPath);

                // Copy default locales from resource
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                Enumeration<URL> resources = classLoader.getResources("/lang");

                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    Path path = Paths.get(resource.toURI());

                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
                        for (Path entry : stream) {
                            Files.copy(entry, Paths.get(dirPath.toString(), entry.getFileName().toString()));
                        }
                    }
                }
            }
            catch (IOException ex)
            {
                LoggerUtils.LogWarning("Failed to create lang directory.");
                LoggerUtils.LogError(ex.getMessage());
                return false;
            } catch (URISyntaxException ex) {
                LoggerUtils.LogError("Failed to get resource URL.");
                LoggerUtils.LogError(ex.getMessage());
                return false;
            }


        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dirPath)) {
            for (Path entry : stream) {
                String fileName = entry.getFileName().toString();
                if (!(fileName.endsWith(".yml") || fileName.endsWith(".yaml")))
                    continue;

                try
                {
                    inputStream = new FileInputStream(entry.toFile());
                }
                catch (FileNotFoundException ex)
                {
                    LoggerUtils.LogError(String.format("Failed to get localization file. Path: %s", entry));
                    return false;
                }
                catch (Exception ex)
                {
                    LoggerUtils.LogWarning("Unknown error happened while reading locale file.");
                    LoggerUtils.LogError(ex.getMessage());
                    return false;
                }

                Yaml yaml = new Yaml();
                Object yamlObject = yaml.load(inputStream);
                if (!(yamlObject instanceof Map))
                {
                    LoggerUtils.LogError("Failed to cast the yamlObject after reading the localization.");
                    return false;
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> localValue = (Map<String, Object>)yamlObject;
                _localization.put(fileName.split("\\.")[0], localValue); // Warning fix
            }
        } catch (IOException ex) {
            LoggerUtils.LogWarning("Failed to read the lang directory.");
            LoggerUtils.LogError(ex.getMessage());
            return false;
        }
        return true;
    }


    /**
     * Retrieves the player's locale in ISO 639-3 language code format.
     *
     * @param player The player whose locale is to be retrieved.
     * @return The ISO 639-3 language code of the player's locale, or "en" if an error occurs.
     */
    private static String GetPlayerLocale(Player player) {
        try {
            if (!OpenKits.GetConfig().getBoolean("usePlayerLocale"))
                return _defaultLocale;
            return player.locale().getISO3Language();
        }
        catch (Exception ex) {
            LoggerUtils.LogWarning("Failed to get the player's locale.");
            LoggerUtils.LogError(ex.getMessage());
            return "eng";
        }
    }

    /**
     * Localizes a given key to its corresponding value.
     *
     * @param key the key to be localized.
     * @return the localized string, or an empty string if the key is not found.
     */
    public static String Localize(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    LoggerUtils.LogWarning(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return value.toString();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning(String.format("Unknown error happened while translating '%s'.", key));
            LoggerUtils.LogError(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding list of values.
     *
     * @param key the key to be localized.
     * @return the localized list of strings, or an empty list if the key is not found.
     */
    public static List<String> LocalizeList(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    LoggerUtils.LogWarning(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new ArrayList<>();
                }
            }

            return (List<String>)value;
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning(String.format("Unknown error happened while translating '%s'.", key));
            LoggerUtils.LogError(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Localizes a given key to its corresponding array of values.
     *
     * @param key the key to be localized.
     * @return the localized array of strings, or an empty array if the key is not found.
     */
    public static String[] LocalizeArray(String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    LoggerUtils.LogWarning(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new String[0];
                }
            }

            return (String[])value;
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning(String.format("Unknown error happened while translating '%s'.", key));
            LoggerUtils.LogError(ex.getMessage());
            return new String[0];
        }
    }

    /**
     * Localizes a given key to its corresponding value and formats it with the provided arguments.
     *
     * @param key the key to be localized.
     * @param args the arguments to format the localized string.
     * @return the formatted localized string, or an empty string if the key is not found.
     */
    public static String Localize(String key, Object... args) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization;
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    LoggerUtils.LogWarning(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return MessageFormat.format(value.toString(), args);
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning(String.format("Unknown error happened while translating '%s'.", key));
            LoggerUtils.LogError(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding value for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized string, or an empty string if the key is not found.
     */
    public static String Localize(Player player, String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(GetPlayerLocale(player));
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    LoggerUtils.LogWarning(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return value.toString();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning(String.format("Unknown error happened while translating '%s'.", key));
            LoggerUtils.LogError(ex.getMessage());
            return "";
        }
    }

    /**
     * Localizes a given key to its corresponding list of values for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized list of strings, or an empty list if the key is not found.
     */
    public static List<String> LocalizeList(Player player, String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(GetPlayerLocale(player));
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    LoggerUtils.LogWarning(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new ArrayList<>();
                }
            }

            return (List<String>)value;
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning(String.format("Unknown error happened while translating '%s'.", key));
            LoggerUtils.LogError(ex.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Localizes a given key to its corresponding array of values for a specific player.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @return The localized array of strings, or an empty array if the key is not found.
     */
    public static String[] LocalizeArray(Player player,String key) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(GetPlayerLocale(player));
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    LoggerUtils.LogWarning(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return new String[0];
                }
            }

            return (String[])value;
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning(String.format("Unknown error happened while translating '%s'.", key));
            LoggerUtils.LogError(ex.getMessage());
            return new String[0];
        }
    }

    /**
     * Localizes a given key to its corresponding value for a specific player and formats it with the provided arguments.
     *
     * @param player The player whose locale is to be used for localization.
     * @param key The key to be localized.
     * @param args The arguments to format the localized string.
     * @return The formatted localized string, or an empty string if the key is not found.
     */
    public static String Localize(Player player,String key, Object... args) {
        try
        {
            String[] keys = key.split("\\.");
            Object value = _localization.get(GetPlayerLocale(player));
            for (String k : keys) {
                if (value instanceof Map) {
                    value = ((Map<?, ?>) value).get(k);
                } else {
                    LoggerUtils.LogWarning(String.format("Failed to get the translation for the '%s' translation key.", key));
                    return "";
                }
            }

            return MessageFormat.format(value.toString(), args);
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning(String.format("Unknown error happened while translating '%s'.", key));
            LoggerUtils.LogError(ex.getMessage());
            return "";
        }
    }
}
