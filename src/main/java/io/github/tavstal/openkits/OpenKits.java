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
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Main class for the OpenKits plugin.
 * Extends the PluginBase class and provides the core functionality for the plugin.
 */
public class OpenKits extends PluginBase {
    // Singleton instance of the plugin
    public static OpenKits Instance;

    // Logger for the plugin
    private final PluginLogger _logger;

    // Translator for handling localizations
    private final PluginTranslator _translator;

    // Static logger accessor
    public static PluginLogger Logger() {
        return Instance._logger;
    }

    // SpiGUI instance for GUI management
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
     *
     * @return The FileConfiguration object.
     */
    public static FileConfiguration GetConfig() {
        return Instance.getConfig();
    }

    // Database instance for managing storage
    public static IDatabase Database;

    /**
     * Constructor for the OpenKits plugin.
     * Initializes the plugin with its name, version, author, download URL, and supported languages.
     */
    public OpenKits() {
        super("OpenKits",
                "1.0.0",
                "Tavstal",
                "https://github.com/TavstalDev/OpenKits/releases/latest",
                new String[]{"eng", "hun"}
        );
        _logger = getCustomLogger();
        _translator = getTranslator();
    }

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin, hooks into dependencies, and sets up commands, events, and storage.
     */
    @Override
    public void onEnable() {
        Instance = this;
        _logger.Info(String.format("Loading %s...", getProjectName()));

        // Check for PlaceholderAPI dependency
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            _logger.Warn("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        _logger.Info("Hooked into PlaceholderAPI.");

        // Register events
        EventListener.init();

        // Generate default configuration file
        saveDefaultConfig();

        // Load localizations
        if (!_translator.Load()) {
            _logger.Error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register economy integration
        _logger.Debug("Hooking into Vault...");
        if (EconomyUtils.setupEconomy()) {
            _logger.Info("Economy plugin found and hooked into Vault.");
        } else {
            _logger.Warn("Economy plugin not found. Disabling economy features.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize database based on configuration
        String databaseType = this.getConfig().getString("storage.type");
        if (databaseType == null) {
            databaseType = "sqlite";
        }
        switch (databaseType.toLowerCase()) {
            case "mysql": {
                Database = new MySqlManager();
                break;
            }
            case "sqlite":
            default: {
                Database = new SqlLiteManager();
                break;
            }
        }
        Database.CheckSchema();

        // Initialize GUI
        _logger.Debug("Loading GUI...");
        _spiGUI = new SpiGUI(this);

        // Register commands
        _logger.Debug("Registering commands...");
        var command = getCommand("kit");
        if (command != null) {
            command.setExecutor(new CommandKit());
            command.setTabCompleter(new CommandKitCompleter());
        }
        command = getCommand("kits");
        if (command != null) {
            command.setExecutor(new CommandKits());
        }

        _logger.Info(String.format("%s has been successfully loaded.", getProjectName()));

        // Check for updates
        if (!isUpToDate()) {
            _logger.Warn(String.format("A new version of %s is available! Download it at %s", getProjectName(), getDownloadUrl()));
        }
    }

    /**
     * Called when the plugin is disabled.
     * Cleans up resources and unloads the database.
     */
    @Override
    public void onDisable() {
        Database.Unload();
        _logger.Info(String.format("%s has been successfully unloaded.", getProjectName()));
    }

    /**
     * Replaces placeholders in a message with their corresponding values.
     *
     * @param message The message containing placeholders.
     * @return The message with placeholders replaced.
     */
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
        _logger.Info(String.format("Reloading %s...", getProjectName()));
        _logger.Debug("Reloading localizations...");
        _translator.Load();
        _logger.Debug("Localizations reloaded.");
        _logger.Debug("Reloading configuration...");
        this.reloadConfig();
        _logger.Debug("Configuration reloaded.");
    }
}