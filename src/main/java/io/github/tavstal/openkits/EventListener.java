package io.github.tavstal.openkits;

import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.openkits.managers.PlayerManager;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener for handling player-related events.
 */
public class EventListener implements Listener {
    private static final PluginLogger _logger = OpenKits.Logger().WithModule(EventListener.class);

    /**
     * Initializes and registers the event listener.
     */
    public static void init() {
        _logger.Debug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new EventListener(), OpenKits.Instance);
        _logger.Debug("Event listener registered.");
    }

    /**
     * Handles the event when a player joins the server for the first time.
     *
     * @param event The PlayerJoinEvent triggered when a player joins the server.
     */
    @EventHandler
    public void onPlayerFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = new PlayerData(player);
        PlayerManager.addPlayerData(player.getUniqueId(), playerData);
        if (player.hasPlayedBefore())
            return;

        String kitName = OpenKits.GetConfig().getString("firstJoinKit");
        if (kitName == null || kitName.isEmpty())
            return;

        Kit kit = OpenKits.Database.FindKit(kitName);
        if (kit == null)
            return;

        kit.Give(player);
    }
}
