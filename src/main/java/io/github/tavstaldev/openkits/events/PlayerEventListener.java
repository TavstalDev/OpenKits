package io.github.tavstaldev.openkits.events;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.managers.PlayerCacheManager;
import io.github.tavstaldev.openkits.models.Kit;
import io.github.tavstaldev.openkits.models.PlayerCache;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Event listener for handling player-related events.
 */
public class PlayerEventListener implements Listener {
    private static final PluginLogger _logger = OpenKits.logger().withModule(PlayerEventListener.class);

    /**
     * Initializes and registers the event listener.
     */
    public static void init() {
        _logger.debug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new PlayerEventListener(), OpenKits.Instance);
        _logger.debug("Event listener registered.");
    }

    /**
     * Handles the event when a player joins the server for the first time.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        var playerId = player.getUniqueId();

        if (PlayerCacheManager.isMarkedForRemoval(playerId))
            PlayerCacheManager.unmarkForRemoval(playerId);

        if (PlayerCacheManager.get(playerId) == null) {
            PlayerCache playerCache = new PlayerCache(player);
            PlayerCacheManager.add(playerId, playerCache);
        }

        if (player.hasPlayedBefore())
            return;

        String kitName = OpenKits.config().getString("firstJoinKit");
        if (kitName == null || kitName.isEmpty())
            return;

        Kit kit = OpenKits.Database.findKit(kitName);
        if (kit == null)
            return;

        kit.give(player);
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerCacheManager.markForRemoval(player.getUniqueId());
    }
}
