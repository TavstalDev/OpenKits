package io.github.tavstaldev.openkits;

import com.samjakob.spigui.SpiGUI;
import io.github.tavstaldev.minecorelib.PluginBase;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.ItemMetaSerializer;
import io.github.tavstaldev.openkits.commands.CommandKit;
import io.github.tavstaldev.openkits.commands.CommandKitCompleter;
import io.github.tavstaldev.openkits.commands.CommandKits;
import io.github.tavstaldev.openkits.events.PlayerEventListener;
import io.github.tavstaldev.openkits.managers.MySqlManager;
import io.github.tavstaldev.openkits.managers.SqlLiteManager;
import io.github.tavstaldev.openkits.metrics.Metrics;
import io.github.tavstaldev.openkits.models.IDatabase;
import io.github.tavstaldev.openkits.tasks.CacheCleanTask;
import io.github.tavstaldev.openkits.utils.EconomyUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Main class for the OpenKits plugin.
 * Extends the PluginBase class and provides the core functionality for the plugin.
 */
public class OpenKits extends PluginBase {
    // Singleton instance of the plugin
    public static OpenKits Instance;
    private static SpiGUI _spiGUI;
    public static ItemMetaSerializer ItemMetaSerializer;
    public static IDatabase Database;
    private CacheCleanTask cacheCleanTask; // Task for cleaning player caches.

    // Static logger accessor
    public static PluginLogger logger() {
        return Instance._logger;
    }

    /**
     * Gets the SpiGUI instance.
     *
     * @return The SpiGUI instance.
     */
    public static SpiGUI gui() {
        return _spiGUI;
    }

    /**
     * Gets the plugin configuration.
     *
     * @return The FileConfiguration object.
     */
    public static FileConfiguration config() {
        return Instance.getConfig();
    }

    /**
     * Constructor for the OpenKits plugin.
     * Initializes the plugin with its name, version, author, download URL, and supported languages.
     */
    public OpenKits() {
        super(true, "https://github.com/TavstalDev/OpenKits/releases/latest"
        );
    }

    /**
     * Called when the plugin is enabled.
     * Initializes the plugin, hooks into dependencies, and sets up commands, events, and storage.
     */
    @Override
    public void onEnable() {
        Instance = this;
        _config = new KitsConfiguration();
        _config.load(); // Fix load bug
        _translator = new PluginTranslator(this, new String[]{"eng", "hun"});
        _logger.info(String.format("Loading %s...", getProjectName()));

        // Check for PlaceholderAPI dependency
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            _logger.warn("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        _logger.info("Hooked into PlaceholderAPI.");

        // Register events
        PlayerEventListener.init();

        // Load localizations
        if (!_translator.load()) {
            _logger.error("Failed to load localizations... Unloading...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Register economy integration
        _logger.debug("Hooking into Vault...");
        if (EconomyUtils.setupEconomy()) {
            _logger.info("Economy plugin found and hooked into Vault.");
        } else {
            _logger.warn("Economy plugin not found. Disabling economy features.");
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
        Database.load();
        Database.checkSchema();

        // Initialize GUI
        _logger.debug("Loading GUI...");
        _spiGUI = new SpiGUI(this);

        // Register commands
        _logger.debug("Registering commands...");
        var command = getCommand("openkits");
        if (command != null) {
            command.setExecutor(new CommandKit());
            command.setTabCompleter(new CommandKitCompleter());
        }
        command = getCommand("kits");
        if (command != null) {
            command.setExecutor(new CommandKits());
        }

        // Metrics
        try {
            @SuppressWarnings("unused") Metrics metrics = new Metrics(this, 27764);
        }
        catch (Exception ex)
        {
            _logger.error("Failed to start Metrics: " + ex.getMessage());
        }

        // Initialize ItemMetaSerializer
        ItemMetaSerializer = new ItemMetaSerializer(this);

        // Register cache cleanup task.
        if (cacheCleanTask != null && !cacheCleanTask.isCancelled())
            cacheCleanTask.cancel();
        cacheCleanTask = new CacheCleanTask(); // Runs every 5 minutes
        cacheCleanTask.runTaskTimerAsynchronously(this, 0, 5 * 60 * 20);

        _logger.ok(String.format("%s has been successfully loaded.", getProjectName()));

        // Check for updates
        isUpToDate().thenAccept(upToDate -> {
            if (upToDate) {
                _logger.ok("Plugin is up to date!");
            } else {
                _logger.warn("A new version of the plugin is available: " + getDownloadUrl());
            }
        }).exceptionally(e -> {
            _logger.error("Failed to determine update status: " + e.getMessage());
            return null;
        });
    }

    /**
     * Called when the plugin is disabled.
     * Cleans up resources and unloads the database.
     */
    @Override
    public void onDisable() {
        Database.unload();
        _logger.info(String.format("%s has been successfully unloaded.", getProjectName()));
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
            result = result.replace("%currency_singular%", currencySingular == null ? localize("General.CurrencySingular") : currencySingular);
        }
        if (result.contains("%currency_plural%")) {
            String currencyPlural = EconomyUtils.currencyNamePlural();
            result = result.replace("%currency_plural%", currencyPlural == null ? localize("General.CurrencyPlural") : currencyPlural);
        }
        return result;
    }

    /**
     * Reloads the plugin configuration and localizations.
     */
    public void reload() {
        _logger.info(String.format("Reloading %s...", getProjectName()));
        _logger.debug("Reloading localizations...");
        _translator.load();
        _logger.debug("Localizations reloaded.");
        _logger.debug("Reloading configuration...");
        _config.load();
        _logger.debug("Configuration reloaded.");

        // Restart cache cleanup task
        if (cacheCleanTask != null && !cacheCleanTask.isCancelled())
            cacheCleanTask.cancel();
        cacheCleanTask = new CacheCleanTask(); // Runs every 5 minutes
        cacheCleanTask.runTaskTimerAsynchronously(this, 0, 5 * 60 * 20);
    }
}