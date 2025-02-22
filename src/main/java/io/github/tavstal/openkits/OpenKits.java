package io.github.tavstal.openkits;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstal.minecorelib.PluginBase;
import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.core.PluginTranslator;
import io.github.tavstal.openkits.commands.CommandKit;
import io.github.tavstal.openkits.commands.CommandKitCompleter;
import io.github.tavstal.openkits.commands.CommandKits;
import io.github.tavstal.openkits.managers.MySqlManager;
import io.github.tavstal.openkits.managers.SqlLiteManager;
import io.github.tavstal.openkits.models.IDatabase;
import io.github.tavstal.openkits.utils.EconomyUtils;
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

public class OpenKits extends PluginBase {
    public static OpenKits Instance;
    public static PluginLogger Logger() {
        return Instance.getCustomLogger();
    }
    public static PluginTranslator Translator() {
        return Instance.getTranslator();
    }
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

    public OpenKits() {
        super("OpenKits",
                "1.0.0",
                "Tavstal",
                "https://github.com/TavstalDev/OpenKits/releases/latest",
                new String[]{"eng", "hun"}
        );
    }

    @Override
    public void onEnable() {
        Instance = this;
        getCustomLogger().Info("Loading OpenKits...");

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getCustomLogger().Warn("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        //new PAPIExpansion().register();
        getCustomLogger().Info("Hooked into PlaceholderAPI.");

        // Register Events
        EventListener.init();

        // Generate config file
        saveDefaultConfig();

        // Load Localizations
        if (!getTranslator().Load())
        {
            getCustomLogger().Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register Economy
        getCustomLogger().Debug("Hooking into Vault...");
        if (EconomyUtils.setupEconomy())
            getCustomLogger().Info("Economy plugin found and hooked into Vault.");
        else
        {
            getCustomLogger().Warn("Economy plugin not found. Disabling economy features.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

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
        getCustomLogger().Debug("Loading GUI...");
        _spiGUI = new SpiGUI(this);

        // Register Commands
        getCustomLogger().Debug("Registering commands...");
        var command = getCommand("kit");
        if (command != null) {
            command.setExecutor(new CommandKit());
            command.setTabCompleter(new CommandKitCompleter());
        }
        command = getCommand("kits");
        if (command != null) {
            command.setExecutor(new CommandKits());
        }

        getCustomLogger().Info("OpenKits has been successfully loaded.");
        if (!isUpToDate())
            getCustomLogger().Warn("A new version of Aldas is available! Download it at: " + getDownloadUrl());
    }

    @Override
    public void onDisable() {
        Database.Unload();
        getCustomLogger().Info("OpenKits has been successfully unloaded.");
    }

    @Override
    protected String replacePlaceholders(String message) {
        String result = super.replacePlaceholders(message);
        if (result.contains("%currency_singular%")) {
            String currencySingular = EconomyUtils.currencyNameSingular();
            result = result.replace("%currency_singular%", currencySingular == null ? Localize("General.CurrencySingular") : currencySingular);
        }
        if (result.contains("%currency_plural%")) {
            String currencyPlural = EconomyUtils.currencyNamePlural();
            result = result.replace("%currency_plural%", currencyPlural == null ? Localize("General.CurrencyPlural") : currencyPlural);
        }
        return result;
    }

    /**
     * Reloads the plugin configuration and localizations.
     */
    public void reload() {
        getCustomLogger().Info("Reloading OpenKits...");
        getCustomLogger().Debug("Reloading localizations...");
        getTranslator().Load();
        getCustomLogger().Debug("Localizations reloaded.");
        getCustomLogger().Debug("Reloading configuration...");
        this.reloadConfig();
        getCustomLogger().Debug("Configuration reloaded.");
    }

    /**
     * Checks if the plugin is up to date by comparing the current version with the latest release version.
     * @return true if the plugin is up to date, false otherwise.
     */
    public boolean isUpToDate() {
        String version;
        getCustomLogger().Debug("Checking for updates...");
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            getCustomLogger().Debug("Sending request to GitHub...");
            HttpGet request = new HttpGet(getDownloadUrl());
            HttpResponse response = httpClient.execute(request);
            getCustomLogger().Debug("Received response from GitHub.");
            String jsonResponse = EntityUtils.toString(response.getEntity());
            getCustomLogger().Debug("Parsing response...");
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonResponse);
            getCustomLogger().Debug("Parsing release version...");
            version = jsonObject.get("tag_name").toString();
        } catch (IOException e) {
            getCustomLogger().Error("Failed to check for updates.");
            return false;
        } catch (ParseException e) {
            getCustomLogger().Error("Failed to parse release version.");
            return false;
        }

        getCustomLogger().Debug("Current version: " + getVersion());
        getCustomLogger().Debug("Latest version: " + version);
        return version.equalsIgnoreCase(getVersion());
    }
}
