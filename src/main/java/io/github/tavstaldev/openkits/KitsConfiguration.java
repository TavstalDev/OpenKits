package io.github.tavstaldev.openkits;

import io.github.tavstaldev.minecorelib.config.ConfigurationBase;
import org.bukkit.Material;

import java.util.List;

public class KitsConfiguration extends ConfigurationBase {
    public KitsConfiguration() {
        super(OpenKits.Instance, "config.yml", null);
    }

    // General
    public String locale, prefix;
    public boolean usePlayerLocale, checkForUpdates, debug;

    // Storage
    public String storageType, storageFilename, storageHost, storageDatabase, storageUsername, storagePassword, storageTablePrefix;
    public int storagePort;

    // Kit Config
    public boolean dropItemsOnFullInventory, allowPreviewingKits, requirePermissionForPreview;
    public String permissionToPreview, firstJoinKit;

    // Default values
    public double defaultPrice;
    public int defaultCooldown;
    public String defaultPermission;
    public Material defaultIcon;
    public boolean defaultRequirePermission, defaultOneTimeUse;


    @Override
    protected void loadDefaults() {
        //#region General
        locale = resolveGet("locale", "eng");
        resolveComment("locale", List.of(
                "The default locale to use. If 'usePlayerLocale' is true, this will be used as a fallback.",
                "Default locales: eng, hun"));
        usePlayerLocale = resolveGet("usePlayerLocale", true);
        resolveComment("usePlayerLocale", List.of(
                "If true, the plugin will attempt to use the player's locale for messages.",
                "If the player's locale is not supported, the default 'locale' will be used."));
        checkForUpdates = resolveGet("checkForUpdates", true);
        resolveComment("checkForUpdates", List.of(
                "If true, the plugin will check for updates on startup and notify server admins if a new version is available."));
        debug = resolveGet("debug", false);
        resolveComment("debug", List.of(
                "If true, the plugin will output additional debug information to the console."));
        prefix = resolveGet("prefix", "&bOpen&3Kits &8»");
        //#endregion

        //#region Storage
        storageType = resolveGet("storage.type", "sqlite");
        resolveComment("storage.type", List.of("Supported types: sqlite, mysql"));
        storageFilename = resolveGet("storage.filename", "database");
        resolveComment("storage.filename", List.of("Used only for SQLite storage type. The database file will be created in the plugin's folder."));
        storageHost = resolveGet("storage.host", "localhost");
        storagePort = resolveGet("storage.port", 3306);
        storageDatabase = resolveGet("storage.database", "minecraft");
        storageUsername = resolveGet("storage.username", "root");
        storagePassword = resolveGet("storage.password", "ascent");
        storageTablePrefix = resolveGet("storage.tablePrefix", "openkits");
        //#endregion

        //#region Kit Config
        dropItemsOnFullInventory = resolveGet("kitConfig.dropItemsOnFullInventory", true);
        resolveComment("kitConfig.dropItemsOnFullInventory", List.of(
                "If true, when a player redeems a kit and their inventory is full, the items will be dropped on the ground.",
                "If false, the items will not be given if there is no space in the inventory."));

        allowPreviewingKits = resolveGet("kitConfig.allowPreviewingKits", true);
        resolveComment("kitConfig.allowPreviewingKits", List.of(
                "If true, players can preview kits in a GUI before redeeming them."));

        requirePermissionForPreview = resolveGet("kitConfig.requirePermissionForPreview", false);
        resolveComment("kitConfig.requirePermissionForPreview", List.of(
                "If true, players will need a specific permission to preview kits."));

        permissionToPreview = resolveGet("kitConfig.permissionToPreview", "openkits.preview");
        resolveComment("kitConfig.permissionToPreview", List.of(
                "The permission node required to preview kits if 'requirePermissionForPreview' is true."));

        firstJoinKit = resolveGet("kitConfig.firstJoinKit", "none");
        resolveComment("kitConfig.firstJoinKit", List.of(
                "The kit that will be automatically given to players when they join for the first time.",
                "Set to 'none' to disable this feature."));
        //#endregion

        //#region Default values
        defaultPrice = resolveGet("kitConfig.defaultValues.price", 0.0);
        defaultCooldown = resolveGet("kitConfig.defaultValues.cooldown", 1800);
        defaultRequirePermission = resolveGet("kitConfig.defaultValues.requirePermission", false);
        defaultPermission = resolveGet("kitConfig.defaultValues.permission", "openkits.kit.%kit%");
        String temp = resolveGet("kitConfig.defaultValues.icon", "WOODEN_SWORD");
        defaultIcon = Material.getMaterial(temp.toUpperCase());
        if (defaultIcon == null) {
            OpenKits.Instance.getLogger().warning("Invalid material for default kit icon: " + temp + ". Using WOODEN_SWORD instead.");
            defaultIcon = Material.WOODEN_SWORD;
        }
        defaultOneTimeUse = resolveGet("kitConfig.defaultValues.oneTimeUse", false);

        resolveComment("kitsConfig.defaultValues", List.of(
                "Default values applied to kits if not explicitly set.",
                "These can be overridden on a per-kit basis."));
        //#endregion
    }
}
