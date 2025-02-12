package io.github.tavstal.openkits;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstal.openkits.managers.MySqlManager;
import io.github.tavstal.openkits.managers.SqlLiteManager;
import io.github.tavstal.openkits.models.IDatabase;
import io.github.tavstal.openkits.utils.EconomyUtils;
import io.github.tavstal.openkits.utils.LocaleUtils;
import io.github.tavstal.openkits.utils.LoggerUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;

public class OpenKits extends JavaPlugin {
    //#region Constants
    public static final String PROJECT_NAME = "OpenKits";
    public static final String VERSION = "1.0.0";
    public static final String AUTHOR = "Tavstal";
    public static final String DOWNLOAD_URL = "https://github.com/TavstalDev/OpenKits/releases/latest";
    //#endregion
    public static OpenKits Instance;
    private static SpiGUI _spiGUI;
    /**
     * Gets the SpiGUI instance.
     *
     * @return The SpiGUI instance.
     */
    public static SpiGUI GetGUI() {
        return _spiGUI;
    }
    /**
     * Gets the plugin configuration.
     * @return The FileConfiguration object.
     */
    public static FileConfiguration GetConfig(){
        return Instance.getConfig();
    }
    public static IDatabase Database;

    @Override
    public void onEnable() {
        Instance = this;
        LoggerUtils.LogInfo("Loading OpenKits...");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            LoggerUtils.LogWarning("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        //new PAPIExpansion().register();
        LoggerUtils.LogInfo("Hooked into PlaceholderAPI.");

        // Register Events
        EventListener.init();

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!LocaleUtils.Load())
        {
            LoggerUtils.LogError("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Economy
        LoggerUtils.LogDebug("Hooking into Vault...");
        if (EconomyUtils.setupEconomy())
            LoggerUtils.LogInfo("Economy plugin found and hooked into Vault.");
        else
            LoggerUtils.LogWarning("Economy plugin not found. Disabling economy features.");

        // Create Database
        switch (this.getConfig().getString("storage.type").toLowerCase()) {
            case "mysql":
            {
                Database = new MySqlManager();
                break;
            }
            case "sqlite":
            default:
            {
                Database = new SqlLiteManager();
                break;
            }
        }
        Database.CheckSchema();

        // Register GUI
        LoggerUtils.LogDebug("Loading GUI...");
        _spiGUI = new SpiGUI(this);

        // Register Commands
        LoggerUtils.LogDebug("Registering commands...");
        var kitCommand = getCommand("kit");
        if (kitCommand != null) {
            //kitCommand.setExecutor(new CommandKit());
            //kitCommand.setTabCompleter(new CommandKitCompleter());
        }

        // Schedule a task to run every second
        LoggerUtils.LogInfo("OpenKits has been successfully loaded.");
        if (!isUpToDate())
            LoggerUtils.LogWarning("A new version of Aldas is available! Download it at: " + DOWNLOAD_URL);
    }

    @Override
    public void onDisable() {
        Database.Unload();
        LoggerUtils.LogInfo("OpenKits has been successfully unloaded.");
    }

    /**
     * Reloads the plugin configuration and localizations.
     */
    public void reload() {
        LoggerUtils.LogInfo("Reloading OpenKits...");
        LoggerUtils.LogDebug("Reloading localizations...");
        LocaleUtils.Load();
        LoggerUtils.LogDebug("Localizations reloaded.");
        LoggerUtils.LogDebug("Reloading configuration...");
        this.reloadConfig();
        LoggerUtils.LogDebug("Configuration reloaded.");
    }

    /**
     * Checks if the plugin is up to date by comparing the current version with the latest release version.
     * @return true if the plugin is up to date, false otherwise.
     */
    public boolean isUpToDate() {
        String version;
        LoggerUtils.LogDebug("Checking for updates...");
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            LoggerUtils.LogDebug("Sending request to GitHub...");
            HttpGet request = new HttpGet(DOWNLOAD_URL);
            HttpResponse response = httpClient.execute(request);
            LoggerUtils.LogDebug("Received response from GitHub.");
            String jsonResponse = EntityUtils.toString(response.getEntity());
            LoggerUtils.LogDebug("Parsing response...");
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            LoggerUtils.LogDebug("Parsing release version...");
            version = jsonObject.get("tag_name").toString();
        } catch (IOException e) {
            LoggerUtils.LogError("Failed to check for updates.");
            return false;
        } catch (ParseException e) {
            LoggerUtils.LogError("Failed to parse release version.");
            return false;
        }

        LoggerUtils.LogDebug("Current version: " + VERSION);
        LoggerUtils.LogDebug("Latest version: " + version);
        return version.equalsIgnoreCase(VERSION);
    }
}
