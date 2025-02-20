package io.github.tavstal.openkits;

import io.github.tavstal.openkits.managers.PlayerManager;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.PlayerData;
import io.github.tavstal.openkits.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Event listener for handling player-related events.
 */
public class EventListener implements Listener {
    /**
     * Initializes and registers the event listener.
     */
    public static void init() {
        LoggerUtils.LogDebug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new EventListener(), OpenKits.Instance);
        LoggerUtils.LogDebug("Event listener registered.");
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
