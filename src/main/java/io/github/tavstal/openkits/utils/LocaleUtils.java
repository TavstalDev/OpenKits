package io.github.tavstal.openkits.utils;

import io.github.tavstal.openkits.OpenKits;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utility class for handling localization using YAML files.
 */
public class LocaleUtils {
    private static Map<String, Object> _localization;


    /**
     * Loads the localization file based on the locale specified in the plugin's config.
     *
     * @return true if the localization file was successfully loaded, false otherwise.
     */
    public static Boolean Load() {
        InputStream inputStream;
        LoggerUtils.LogDebug("Loading localization file.");
        String locale =  OpenKits.Instance.getConfig().getString("locale");
        LoggerUtils.LogDebug("Locale: " + locale);
        Path dirPath = Paths.get(OpenKits.Instance.getDataFolder().getPath(), "lang");
        LoggerUtils.LogDebug("Directory path: " + dirPath);
        if (!Files.exists(dirPath))
            try
            {
                Files.createDirectory(dirPath);
            }
            catch (IOException ex)
            {
                LoggerUtils.LogWarning("Failed to create lang directory.");
                LoggerUtils.LogError(ex.getMessage());
                return false;
            }
        LoggerUtils.LogDebug("Directory created.");
        Path filePath = Paths.get(dirPath.toString(), String.format("%s.yml", locale));
        LoggerUtils.LogDebug("File path: " + filePath);
        if (!Files.exists(filePath))
        {
            try {
                // Get the resource URL
                LoggerUtils.LogDebug("Resource file not found. Getting resource file.");
                String localePath = String.format("/lang/%s.yml", locale);
                inputStream = OpenKits.Instance.getClass().getResourceAsStream(localePath);
                if (inputStream == null) {
                    throw new IOException("Resource file not found: " + localePath);
                }
                // Copy the file
                Files.copy(inputStream, filePath);

                // Close the input stream
                inputStream.close();
            } catch (IOException e) {
                LoggerUtils.LogError("Failed to get resource file.");
            }
        }

        try
        {
            LoggerUtils.LogDebug("Reading localization file.");
            inputStream = new FileInputStream(filePath.toString());
        }
        catch (FileNotFoundException ex)
        {
            LoggerUtils.LogError(String.format("Failed to get localization file. Path: %s", filePath));
            return false;
        }
        catch (Exception ex)
        {
            LoggerUtils.LogWarning("Unknown error happened while reading locale file.");
            LoggerUtils.LogError(ex.getMessage());
            return false;
        }

        LoggerUtils.LogDebug("Loading yaml file.");
        Yaml yaml = new Yaml();
        Object yamlObject = yaml.load(inputStream);
        if (!(yamlObject instanceof Map))
        {
            LoggerUtils.LogError("Failed to cast the yamlObject after reading the localization.");
            return false;
        }

        LoggerUtils.LogDebug("Casting yaml object.");
        @SuppressWarnings("unchecked")
        Map<String, Object> localValue = (Map<String, Object>)yamlObject;
        _localization = localValue; // Warning fix
        return true;
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
}
