package io.github.tavstal.openkits.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.models.IDatabase;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.KitCooldown;
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
                    "Id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
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
            String sql = String.format("INSERT INTO %s_kits (Name, Icon, Price, RequirePermission, Permission, Cooldown, IsOneTime, Enable, Items) " +
                            "VALUES ('%s','%s','%s','%s','%s','%s','%s','%s','%s');",
                    getConfig().getString("storage.tablePrefix"), name, icon.name(), price, requirePermission, permission, cooldown, isOneTime, enable, Arrays.toString(Kit.SerializeItems(items)));
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while adding kit...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKit(long id, String name) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Name='%s' WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), name, id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception ex)
        {
           LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKit(long id, boolean requirePermission, String permission) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET RequirePermission='%s' AND Permission='%s' WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), requirePermission, permission, id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKit(long id, List<ItemStack> items) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Items='%s' WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), Arrays.toString(Kit.SerializeItems(items)), id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKit(long id, Double price) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Price='%s' WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), price, id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKit(long id, long cooldown) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Cooldown='%s' WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), cooldown, id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKit(long id, boolean enable) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Enable='%s' WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), enable, id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
        }
        catch (Exception ex)
        {
            LoggerUtils.LogError(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    @Override
    public void UpdateKit(long id, Material icon) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Icon='%s' WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), icon.name(), id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
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
            String sql = String.format("UPDATE %s_kits SET IsOneTime='%s' WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), isOneTime, id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
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
            String sql = String.format("DELETE FROM %s_kits WHERE Id='%s';",
                    getConfig().getString("storage.tablePrefix"), id);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
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
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();


            while (result.next()) {
                data.add(new Kit(
                        result.getLong("Id"),
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

            if (!result.isClosed())
                result.close();

            statement.close();
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
            String sql = String.format("SELECT * FROM %s_kits WHERE Id='%s' LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"), id);
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();

            if (result.next())
            {
                data = new Kit(
                        result.getLong("Id"),
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

            if (!result.isClosed())
                result.close();

            statement.close();
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
            String sql = String.format("SELECT * FROM %s_kits WHERE LOWER(Name) LIKE LOWER('%%%s%%') LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"), name);
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();

            if (result.next())
            {
                data = new Kit(
                        result.getLong("Id"),
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

            if (!result.isClosed())
                result.close();

            statement.close();
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
                            "VALUES ('%s','%s','%s');",
                    getConfig().getString("storage.tablePrefix"), playerId, kitId, end);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
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
            String sql = String.format("UPDATE %s_cooldowns SET End='%s' WHERE PlayerId='%s' AND KitId='%s';",
                    getConfig().getString("storage.tablePrefix"), end, playerId, kitId);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
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
            String sql = String.format("DELETE FROM %s_cooldowns WHERE PlayerId='%s' AND KitId='%s';",
                    getConfig().getString("storage.tablePrefix"), playerId, kitId);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
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
            String sql = String.format("DELETE FROM %s_cooldowns WHERE PlayerId='%s';",
                    getConfig().getString("storage.tablePrefix"), playerId);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
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
            String sql = String.format("DELETE FROM %s_cooldowns WHERE KitId='%s';",
                    getConfig().getString("storage.tablePrefix"), kitId);
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.executeUpdate();
            statement.close();
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
            String sql = String.format("SELECT * FROM %s_cooldowns WHERE PlayerId='%s';",
                    getConfig().getString("storage.tablePrefix"), playerId);
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();


            while (result.next()) {
                data.add(new KitCooldown(
                        result.getObject("PlayerId", UUID.class),
                        result.getLong("KitId"),
                        result.getObject("End", LocalDateTime.class)
                ));
            }

            if (!result.isClosed())
                result.close();

            statement.close();
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
            String sql = String.format("SELECT * FROM %s_cooldowns WHERE PlayerId='%s' AND KitId='%s' LIMIT 1;",
                    getConfig().getString("storage.tablePrefix"), playerId, kitId);
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet result = statement.executeQuery();

            if (result.next())
            {
                data = new KitCooldown(
                        result.getObject("PlayerId", UUID.class),
                        result.getLong("KitId"),
                        result.getObject("End", LocalDateTime.class)
                );
            }

            if (!result.isClosed())
                result.close();

            statement.close();
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
