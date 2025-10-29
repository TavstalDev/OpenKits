package io.github.tavstaldev.openkits.managers;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.models.IDatabase;
import io.github.tavstaldev.openkits.models.Kit;
import io.github.tavstaldev.openkits.models.KitCooldown;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Manages MySQL database connections and operations for the OpenKits plugin.
 * Implements the IDatabase interface to provide methods for loading, unloading,
 * and managing the database schema.
 */
public class MySqlManager implements IDatabase {
    private static HikariDataSource _dataSource;
    private static FileConfiguration getConfig() { return OpenKits.Instance.getConfig(); }
    private static final PluginLogger _logger = OpenKits.logger().withModule(MySqlManager.class);
    private final Cache<@NotNull Long, Kit> _kitCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(3, TimeUnit.MINUTES)
            .build();
    private final Cache<@NotNull UUID, List<KitCooldown>> _cooldownCache = Caffeine.newBuilder()
            .maximumSize(1000)
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .build();

    /**
     * Initializes the database connection by creating a data source.
     */
    @Override
    public void load() {
        _dataSource = CreateDataSource();
    }

    /**
     * Closes the database connection and releases resources.
     */
    @Override
    public void unload() {
        if (_dataSource != null) {
            if (!_dataSource.isClosed())
                _dataSource.close();
        }
    }

    /**
     * Creates and configures a HikariCP data source for connecting to the MySQL database.
     *
     * @return A configured HikariDataSource instance, or null if an error occurs.
     */
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
            _logger.error(String.format("Unknown error happened during the creation of database connection...\n%s", ex.getMessage()));
            return null;
        }
    }

    /**
     * Ensures the required database schema exists by creating tables if they do not already exist.
     */
    @Override
    public void checkSchema() {
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
                            "End VARCHAR(200));",
                    getConfig().getString("storage.tablePrefix")
            );
            statement = connection.prepareStatement(sql);
            statement.executeUpdate();
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while creating tables...\n%s", ex.getMessage()));
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
    public void addKit(String name, Material icon, Double price, boolean requirePermission, String permission, long cooldown, boolean isOneTime, boolean enable, List<ItemStack> items) {
        try (Connection connection = _dataSource.getConnection())
        {
            byte[] serializedItems = OpenKits.ItemMetaSerializer.serializeItemStackListToBytes(items);
            String sql = String.format("INSERT INTO %s_kits (Name, Icon, Price, RequirePermission, Permission, Cooldown, IsOneTime, Enable, Items) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);",
                    getConfig().getString("storage.tablePrefix"));

            long id;
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

                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        id = generatedKeys.getLong("Id");
                    } else {
                        _logger.warn("Could not retrieve auto-incremented ID after INSERT.");
                        return;
                    }
                }
            }

            _kitCache.put(id, new Kit(id, name, icon.name(), price, requirePermission, permission, cooldown, isOneTime, enable, serializedItems));
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while adding tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the name of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param name The new name for the kit.
     */
    @Override
    public void updateKitName(long id, String name) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Name=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, name);
                statement.setLong(2, id);
                statement.executeUpdate();
            }

            var kitResult = _kitCache.getIfPresent(id);
            if (kitResult != null) {
                kitResult.Name = name;
                _kitCache.put(id, kitResult);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
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
    public void updateKitPermission(long id, boolean requirePermission, String permission) {
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

            var kitResult = _kitCache.getIfPresent(id);
            if (kitResult != null) {
                kitResult.RequirePermission = requirePermission;
                kitResult.Permission = permission;
                _kitCache.put(id, kitResult);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the items of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param items The new list of items for the kit.
     */
    @Override
    public void updateKitItems(long id, List<ItemStack> items) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Items=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));

            var serializedItems = OpenKits.ItemMetaSerializer.serializeItemStackListToBytes(items);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBytes(1, serializedItems);
                statement.setLong(2, id);
                statement.executeUpdate();
            }

            var kitResult = _kitCache.getIfPresent(id);
            if (kitResult != null) {
                kitResult.Items = serializedItems;
                _kitCache.put(id, kitResult);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the price of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param price The new price for the kit.
     */
    @Override
    public void updateKitPrice(long id, Double price) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Price=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setDouble(1, price);
                statement.setLong(2, id);
                statement.executeUpdate();
            }

            var kitResult = _kitCache.getIfPresent(id);
            if (kitResult != null) {
                kitResult.Price = price;
                _kitCache.put(id, kitResult);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the cooldown of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param cooldown The new cooldown time (in milliseconds) for the kit.
     */
    @Override
    public void updateKitCooldown(long id, long cooldown) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Cooldown=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, cooldown);
                statement.setLong(2, id);
                statement.executeUpdate();
            }

            var kitResult = _kitCache.getIfPresent(id);
            if (kitResult != null) {
                kitResult.Cooldown = cooldown;
                _kitCache.put(id, kitResult);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the enabled status of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param enable The new enabled status for the kit.
     */
    @Override
    public void updateKitEnabled(long id, boolean enable) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Enable=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBoolean(1, enable);
                statement.setLong(2, id);
                statement.executeUpdate();
            }

            var kitResult = _kitCache.getIfPresent(id);
            if (kitResult != null) {
                kitResult.Enable = enable;
                _kitCache.put(id, kitResult);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the icon of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param icon The new material icon for the kit.
     */
    @Override
    public void updateKitIcon(long id, Material icon) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET Icon=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, icon.name());
                statement.setLong(2, id);
                statement.executeUpdate();
            }

            var kitResult = _kitCache.getIfPresent(id);
            if (kitResult != null) {
                kitResult.Icon = icon.name();
                _kitCache.put(id, kitResult);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Updates the one-time usage status of a kit in the database.
     *
     * @param id The ID of the kit to update.
     * @param isOneTime The new one-time usage status for the kit.
     */
    @Override
    public void updateKitOneTime(long id, boolean isOneTime) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("UPDATE %s_kits SET IsOneTime=? WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setBoolean(1, isOneTime);
                statement.setLong(2, id);
                statement.executeUpdate();
            }

            var kitResult = _kitCache.getIfPresent(id);
            if (kitResult != null) {
                kitResult.IsOneTime = isOneTime;
                _kitCache.put(id, kitResult);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the kit table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a kit from the database.
     *
     * @param id The ID of the kit to remove.
     */
    @Override
    public void removeKit(long id) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("DELETE FROM %s_kits WHERE Id=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, id);
                statement.executeUpdate();
            }

            _kitCache.invalidate(id);
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Retrieves all kits from the database.
     *
     * @return A list of all kits in the database.
     */
    @Override
    public List<Kit> getKits() {
        var kitMap = _kitCache.asMap();
        if (!kitMap.isEmpty()) {
            return kitMap.values().stream().toList();
        }

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
            _logger.error(String.format("Unknown error happened while getting kits data...\n%s", ex.getMessage()));
            return null;
        }

        for (var kit : data) {;
            _kitCache.put(kit.Id, kit);
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
    public Kit findKit(long id) {
        if (_kitCache.asMap().containsKey(id)) {
            return _kitCache.getIfPresent(id);
        }

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
            _logger.error(String.format("Unknown error happened while finding kit data...\n%s", ex.getMessage()));
            return null;
        }

        if (data != null)
            _kitCache.put(data.Id, data);
        return data;
    }

    /**
     * Finds a kit in the database by its name.
     *
     * @param name The name of the kit to find.
     * @return The kit with the specified name, or null if not found.
     */
    @Override
    public Kit findKit(String name) {
        var kitMap = _kitCache.asMap();
        if (!kitMap.isEmpty()) {
            Optional<Kit> cachedKit = kitMap.values().stream()
                    .filter(kit -> kit.Name.equalsIgnoreCase(name))
                    .findFirst();
            if (cachedKit.isPresent()) {
                return cachedKit.get();
            }
        }

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
            _logger.error(String.format("Unknown error happened while finding kit data...\n%s", ex.getMessage()));
            return null;
        }

        if (data != null)
            _kitCache.put(data.Id, data);

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
    public void addKitCooldown(UUID playerId, long kitId, LocalDateTime end) {
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

            var cooldowns = _cooldownCache.getIfPresent(playerId);
            if (cooldowns != null) {
                cooldowns.add(new KitCooldown(playerId, kitId, end));
                _cooldownCache.put(playerId, cooldowns);
            }
            else {
                List<KitCooldown> newCooldowns = new ArrayList<>();
                newCooldowns.add(new KitCooldown(playerId, kitId, end));
                _cooldownCache.put(playerId, newCooldowns);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while adding cooldown...\n%s", ex.getMessage()));
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
    public void updateKitCooldown(UUID playerId, long kitId, LocalDateTime end) {
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

            var cooldowns = _cooldownCache.getIfPresent(playerId);
            if (cooldowns != null) {
                for (var cooldown : cooldowns) {
                    if (cooldown.KitId == kitId) {
                        cooldown.End = end;
                        break;
                    }
                }
                _cooldownCache.put(playerId, cooldowns);
            }
            else {
                List<KitCooldown> newCooldowns = new ArrayList<>();
                newCooldowns.add(new KitCooldown(playerId, kitId, end));
                _cooldownCache.put(playerId, newCooldowns);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened while updating the cooldowns table...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes a specific cooldown for a kit and player from the database.
     *
     * @param playerId The UUID of the player.
     * @param kitId The ID of the kit.
     */
    @Override
    public void removeKitCooldown(UUID playerId, long kitId) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("DELETE FROM %s_cooldowns WHERE PlayerId=? AND KitId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.setLong(2, kitId);
                statement.executeUpdate();
            }

            var cooldowns = _cooldownCache.getIfPresent(playerId);
            if (cooldowns != null) {
                cooldowns.removeIf(cooldown -> cooldown.KitId == kitId);
                _cooldownCache.put(playerId, cooldowns);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes all cooldowns for a specific player from the database.
     *
     * @param playerId The UUID of the player.
     */
    @Override
    public void removeKitCooldowns(UUID playerId) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("DELETE FROM %s_cooldowns WHERE PlayerId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, playerId.toString());
                statement.executeUpdate();
            }

            _cooldownCache.invalidate(playerId);
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Removes all cooldowns for a specific kit from the database.
     *
     * @param kitId The ID of the kit.
     */
    @Override
    public void removeKitCooldowns(long kitId) {
        try (Connection connection = _dataSource.getConnection())
        {
            String sql = String.format("DELETE FROM %s_cooldowns WHERE KitId=?;",
                    getConfig().getString("storage.tablePrefix"));
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setLong(1, kitId);
                statement.executeUpdate();
            }

            var cooldownMap = _cooldownCache.asMap();
            for (var entry : cooldownMap.entrySet()) {
                var cooldowns = entry.getValue();
                cooldowns.removeIf(cooldown -> cooldown.KitId == kitId);
                _cooldownCache.put(entry.getKey(), cooldowns);
            }
        }
        catch (Exception ex)
        {
            _logger.error(String.format("Unknown error happened during the deletion of tables...\n%s", ex.getMessage()));
        }
    }

    /**
     * Retrieves all cooldowns for a specific player from the database.
     *
     * @param playerId The UUID of the player.
     * @return A list of KitCooldown objects representing the player's cooldowns.
     */
    @Override
    public List<KitCooldown> getKitCooldowns(UUID playerId) {
        var cachedCooldowns = _cooldownCache.getIfPresent(playerId);
        if (cachedCooldowns != null) {
            return cachedCooldowns;
        }

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
            _logger.error(String.format("Unknown error happened while getting cooldowns data...\n%s", ex.getMessage()));
            return null;
        }

        _cooldownCache.put(playerId, data);
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
    public KitCooldown findKitCooldown(UUID playerId, long kitId) {
        var cachedCooldowns = _cooldownCache.getIfPresent(playerId);
        if (cachedCooldowns != null) {
            for (var cooldown : cachedCooldowns) {
                if (cooldown.KitId == kitId) {
                    return cooldown;
                }
            }
        }

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
            _logger.error(String.format("Unknown error happened while finding cooldown data...\n%s", ex.getMessage()));
            return null;
        }

        if (data != null) {
            if (cachedCooldowns == null)
                cachedCooldowns = new ArrayList<>(); // Make sure the list is initialized
            cachedCooldowns.add(data);
            _cooldownCache.put(playerId, cachedCooldowns);
        }

        return data;
    }
    //#endregion
}
