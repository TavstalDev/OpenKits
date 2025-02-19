package io.github.tavstal.openkits.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.models.IDatabase;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.KitCooldown;
import io.github.tavstal.openkits.utils.ItemUtils;
import io.github.tavstal.openkits.utils.LoggerUtils;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class MySqlManager implements IDatabase {
    private static HikariDataSource _dataSource;
    private static FileConfiguration getConfig() { return OpenKits.Instance.getConfig(); }

    @Override
    public void Load() {
        _dataSource = CreateDataSource();
    }

    @Override
    public void Unload() {
        if (_dataSource != null) {
            if (!_dataSource.isClosed())
                _dataSource.close();
        }
    }

    public HikariDataSource CreateDataSource() {
        try
        {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(String.format("jdbc:mysql://%s:%s/%s", getConfig().getString("storage.host"), getConfig().getString("storage.port"), getConfig().getString("storage.database"))); // Address of your running MySQL database
            config.setUsername(getConfig().getString("storage.username")); // Username
            config.setPassword(getConfig().getString("storage.password")); // Password
            config.setMaximumPoolSize(10); // Pool size defaults to 10
            config.setMaxLifetime(30000);
            return new HikariDataSource(config);
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened during the creation of database connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    @Override
    public void CheckSchema() {
        try (Connection connection = _dataSource.getConnection())
        {
            // Kits
            String sql = String.format("CREATE TABLE IF NOT EXISTS %s_kits (" +
                    "Id INTEGER AUTO_INCREMENT PRIMARY KEY, " +
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
                            "End DATETIME);",
                    getConfig().getString("storage.tablePrefix")
            );
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
        }
    }

    //#region Kits
    @Override
    public void AddKit(String name, Material icon, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, List<ItemStack> items) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while adding tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitName(long id, String name) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitPermission(long id, boolean requirePermission, String permission) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitItems(long id, List<ItemStack> items) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitPrice(long id, Double price) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitCooldown(long id, long cooldown) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitEnabled(long id, boolean enable) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitIcon(long id, Material icon) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitOneTime(long id, boolean isOneTime) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void RemoveKit(long id) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("DELETE FROM %s_kits WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public List<Kit> GetKits() {
        List<Kit> data = new ArrayList<>();
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while getting kits data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }

    @Override
    public Kit FindKit(long id) {
        Kit data = null;
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while finding kit data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }

    @Override
    public Kit FindKit(String name) {
        Kit data = null;
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while finding kit data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }
    //#endregion

    //#region Cooldowns
    @Override
    public void AddKitCooldown(UUID playerId, long kitId, LocalDateTime end) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while adding cooldown...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKitCooldown(UUID playerId, long kitId, LocalDateTime end) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened while updating the cooldowns table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void RemoveKitCooldown(UUID playerId, long kitId) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void RemoveKitCooldowns(UUID playerId) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void RemoveKitCooldowns(long kitId) {
        try (Connection connection = _dataSource.getConnection())
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
            LoggerUtils.LogError(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    @Override
    public List<KitCooldown> GetKitCooldowns(UUID playerId) {
        List<KitCooldown> data = new ArrayList<>();
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("SELECT * FROM %s_cooldowns WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        data.add(new KitCooldown(
                                result.getObject("PlayerId", UUID.class),
                                result.getLong("KitId"),
                                result.getObject("End", LocalDateTime.class)
                        ));
                    }
                }
            }
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while getting cooldowns data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }

    @Override
    public KitCooldown FindKitCooldown(UUID playerId, long kitId) {
        KitCooldown data = null;
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("SELECT * FROM %s_cooldowns WHERE PlayerId=? AND KitId=? LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setLong(2, kitId);
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        data = new KitCooldown(
                                result.getObject("PlayerId", UUID.class),
                                result.getLong("KitId"),
                                result.getObject("End", LocalDateTime.class)
                        );
                    }
                }
            }
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while finding cooldown data...\n%s", ex.getMessage()));
            return null;
        }

        return data;
    }
    //#endregion
}
