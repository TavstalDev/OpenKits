package io.github.tavstaldev.openkits.managers;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.models.IDatabase;
import io.github.tavstaldev.openkits.models.Kit;
import io.github.tavstaldev.openkits.models.KitCooldown;
import io.github.tavstaldev.openkits.utils.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Manages SQLite database connections and operations for the OpenKits plugin.
 * Implements the IDatabase interface to provide methods for loading, unloading,
 * and managing the database schema.
 */
public class SqlLiteManager implements IDatabase {
    private static FileConfiguration getConfig() { return OpenKits.Instance.getConfig(); }
    private static final PluginLogger _logger = OpenKits.Logger().WithModule(SqlLiteManager.class);

    /**
     * Loads the database manager. No operation is performed for SQLite.
     */
    @Override
    public void Load() {}

    /**
     * Unloads the database manager. No operation is performed for SQLite.
     */
    @Override
    public void Unload() {}

    /**
     * Creates a connection to the SQLite database.
     *
     * @return A Connection object to the SQLite database, or null if an error occurs.
     */
    public Connection CreateConnection() {
        try
        {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(String.format("jdbc:sqlite:plugins/OpenKits/%s.db", getConfig().getString("storage.filename")));
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while creating db connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    /**
     * Ensures the required database schema exists by creating tables if they do not already exist.
     */
    @Override
    public void CheckSchema() {
        try (Connection connection = CreateConnection())
        {
            // Kits
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_kits (" +
                            "Id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "Name VARCHAR(35), " +
                            "Icon VARCHAR(200), " +
                            "Price DECIMAL, " +
                            "RequirePermission BOOLEAN, " +
                            "Permission VARCHAR(200), " +
                            "Cooldown BIGINT, " +
                            "IsOneTime BOOLEAN, " +
                            "Enable BOOLEAN, " +
                            "Items BLOB);",
                    getConfig().getString("storage.tablePrefix")
            );
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();

            // Cooldowns
            sql = String.format("CREATE TABLE IF NOT EXISTS %s_cooldowns (" +
                            "PlayerId VARCHAR(36), " +
                            "KitId BIGINT, " +
                            "End VARCHAR(200));",
                    getConfig().getString("storage.tablePrefix")
            );
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    //#region Kits
    /**
     * Adds a new kit to the database with the specified attributes.
     *
     * @param name The name of the kit.
     * @param icon The material icon representing the kit.
     * @param price The price of the kit.
     * @param requirePermission Whether the kit requires a permission to be used.
     * @param permission The permission string required to use the kit.
     * @param cooldown The cooldown time (in milliseconds) for the kit.
     * @param isOneTime Whether the kit can only be used once.
     * @param enable Whether the kit is enabled.
     * @param items The list of items included in the kit.
     */
    @Override
    public void AddKit(String name, Material icon, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, List<ItemStack> items) {
        try (Connection connection = CreateConnection())
        {
            byte[] serializedItems = ItemUtils.serializeItemStackList(items);
            String sql = String.format("INSERT INTO %s_kits (Name, Icon, Price, RequirePermission, Permission, Cooldown, IsOneTime, Enable, Items) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    getConfig().getString("storage.tablePrefix"));

            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                // Set parameters for the prepared statement
                statement.setString(1, name);  // Kit name
                statement.setString(2, icon.name());  // Material icon as a string
                statement.setDouble(3, price);  // Price
                statement.setBoolean(4, requirePermission);  // Require Permission
                statement.setString(5, permission);  // Permission (string)
                statement.setLong(6, cooldown);  // Cooldown
                statement.setBoolean(7, isOneTime);  // Is One Time
                statement.setBoolean(8, enable);  // Is Enabled
                statement.setBytes(9, serializedItems);  // Serialized Items (e.g., JSON or Base64 string)

                // Execute the query
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while adding tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the name of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param name The new name for the kit.
     */
    @Override
    public void UpdateKitName(long id, String name) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Name=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the permission requirements for a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param requirePermission Whether the kit requires a permission.
     * @param permission The new permission string for the kit.
     */
    @Override
    public void UpdateKitPermission(long id, boolean requirePermission, String permission) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_kits SET RequirePermission=?, Permission=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBoolean(1, requirePermission);
                statement.setString(2, permission);
                statement.setLong(3, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the items of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param items The new list of items for the kit.
     */
    @Override
    public void UpdateKitItems(long id, List<ItemStack> items) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Items=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBytes(1, ItemUtils.serializeItemStackList(items));
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the price of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param price The new price for the kit.
     */
    @Override
    public void UpdateKitPrice(long id, Double price) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Price=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, price);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the cooldown of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param cooldown The new cooldown time (in milliseconds) for the kit.
     */
    @Override
    public void UpdateKitCooldown(long id, long cooldown) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Cooldown=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, cooldown);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the enabled status of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param enable The new enabled status for the kit.
     */
    @Override
    public void UpdateKitEnabled(long id, boolean enable) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Enable=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBoolean(1, enable);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the icon of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param icon The new material icon for the kit.
     */
    @Override
    public void UpdateKitIcon(long id, Material icon) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Icon=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, icon.name());
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the one-time usage status of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param isOneTime The new one-time usage status for the kit.
     */
    @Override
    public void UpdateKitOneTime(long id, boolean isOneTime) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_kits SET IsOneTime=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBoolean(1, isOneTime);
                statement.setLong(2, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a kit from the database.
     *
     * @param id The ID of the kit to remove.
     */
    @Override
    public void RemoveKit(long id) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("DELETE FROM %s_kits WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                int rowsAffected = statement.executeUpdate();
                if (rowsAffected == 0) {
                    _logger.Warn("No kit found with the specified ID: " + id);
                }
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Retrieves all kits from the database.
     *
     * @return A list of all kits in the database.
     */
    @Override
    public List<Kit> GetKits() {
        List<Kit> data = new ArrayList<>();
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("SELECT * FROM %s_kits;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql);
                 ResultSet result = statement.executeQuery()) {

                while (result.next()) {
                    data.add(new Kit(
                            result.getInt("Id"),
                            result.getString("Name"),
                            result.getString("Icon"),
                            result.getDouble("Price"),
                            result.getBoolean("RequirePermission"),
                            result.getString("Permission"),
                            result.getLong("Cooldown"),
                            result.getBoolean("IsOneTime"),
                            result.getBoolean("Enable"),
                            result.getBytes("Items")
                    ));
                }
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while getting kits data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }

    /**
     * Finds a kit in the database by its ID.
     *
     * @param id The ID of the kit to find.
     * @return The kit with the specified ID, or null if not found.
     */
    @Override
    public Kit FindKit(long id) {
        Kit data = null;
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("SELECT * FROM %s_kits WHERE Id=? LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new Kit(
                                result.getInt("Id"),
                                result.getString("Name"),
                                result.getString("Icon"),
                                result.getDouble("Price"),
                                result.getBoolean("RequirePermission"),
                                result.getString("Permission"),
                                result.getLong("Cooldown"),
                                result.getBoolean("IsOneTime"),
                                result.getBoolean("Enable"),
                                result.getBytes("Items")
                        );
                    }
                }
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while finding kit data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }

    /**
     * Finds a kit in the database by its name.
     *
     * @param name The name of the kit to find.
     * @return The kit with the specified name, or null if not found.
     */
    @Override
    public Kit FindKit(String name) {
        Kit data = null;
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("SELECT * FROM %s_kits WHERE LOWER(Name) LIKE LOWER(?) LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, "%" + name + "%");
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new Kit(
                                result.getInt("Id"),
                                result.getString("Name"),
                                result.getString("Icon"),
                                result.getDouble("Price"),
                                result.getBoolean("RequirePermission"),
                                result.getString("Permission"),
                                result.getLong("Cooldown"),
                                result.getBoolean("IsOneTime"),
                                result.getBoolean("Enable"),
                                result.getBytes("Items")
                        );
                    }
                }
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while finding kit data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }
    //#endregion

    //#region Cooldowns
    /**
     * Adds a cooldown for a specific kit and player in the database.
     *
     * @param playerId The UUID of the player.
     * @param kitId The ID of the kit.
     * @param end The end time of the cooldown as a LocalDateTime.
     */
    @Override
    public void AddKitCooldown(UUID playerId, long kitId, LocalDateTime end) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("INSERT INTO %s_cooldowns (PlayerId, KitId, End) " +
                            "VALUES (?, ?, ?);",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setLong(2, kitId);
                statement.setString(3, end.toString());
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while adding cooldown...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the cooldown for a specific kit and player in the database.
     *
     * @param playerId The UUID of the player.
     * @param kitId The ID of the kit.
     * @param end The new end time of the cooldown as a LocalDateTime.
     */
    @Override
    public void UpdateKitCooldown(UUID playerId, long kitId, LocalDateTime end) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("UPDATE %s_cooldowns SET End=? WHERE PlayerId=? AND KitId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, end.toString());
                statement.setString(2, playerId.toString());
                statement.setLong(3, kitId);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while updating the cooldowns table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a specific cooldown for a kit and player from the database.
     *
     * @param playerId The UUID of the player.
     * @param kitId The ID of the kit.
     */
    @Override
    public void RemoveKitCooldown(UUID playerId, long kitId) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("DELETE FROM %s_cooldowns WHERE PlayerId=? AND KitId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setLong(2, kitId);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes all cooldowns for a specific player from the database.
     *
     * @param playerId The UUID of the player.
     */
    @Override
    public void RemoveKitCooldowns(UUID playerId) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("DELETE FROM %s_cooldowns WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes all cooldowns for a specific kit from the database.
     *
     * @param kitId The ID of the kit.
     */
    @Override
    public void RemoveKitCooldowns(long kitId) {
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("DELETE FROM %s_cooldowns WHERE KitId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, kitId);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Retrieves all cooldowns for a specific player from the database.
     *
     * @param playerId The UUID of the player.
     * @return A list of KitCooldown objects representing the player's cooldowns.
     */
    @Override
    public List<KitCooldown> GetKitCooldowns(UUID playerId) {
        List<KitCooldown> data = new ArrayList<>();
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("SELECT * FROM %s_cooldowns WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        data.add(new KitCooldown(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getLong("KitId"),
                                LocalDateTime.parse(result.getString("End"))
                        ));
                    }
                }
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while getting cooldowns data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }

    /**
     * Finds a specific cooldown for a kit and player in the database.
     *
     * @param playerId The UUID of the player.
     * @param kitId The ID of the kit.
     * @return A KitCooldown object representing the cooldown, or null if not found.
     */
    @Override
    public KitCooldown FindKitCooldown(UUID playerId, long kitId) {
        KitCooldown data = null;
        try (Connection connection = CreateConnection())
        {
            String sql = String.format("SELECT * FROM %s_cooldowns WHERE PlayerId=? AND KitId=? LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setLong(2, kitId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new KitCooldown(
                                UUID.fromString(result.getString("PlayerId")),
                                result.getLong("KitId"),
                                LocalDateTime.parse(result.getString("End"))
                        );
                    }
                }
            }
        }
        catch (Exception ex)
        {
            _logger.Error(String.format("Unknown error happened while finding cooldown data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }
    //#endregion
}
