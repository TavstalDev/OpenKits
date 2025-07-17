package io.github.tavstaldev.openkits;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import io.github.tavstaldev.openkits.managers.PlayerManager;
import io.github.tavstaldev.openkits.models.Kit;
import io.github.tavstaldev.openkits.models.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.InventoryHolder;

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
    public void onPlayerJoin(PlayerJoinEvent event) {
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

    /**
     * Handles the InventoryPickupItemEvent to prevent picking up duped items.
     *
     * @param event The InventoryPickupItemEvent.
     */
    @EventHandler
    public void onItemPickup(InventoryPickupItemEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof Player player))
            return;

        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        if (playerData == null)
            return;

        if (playerData.isGUIOpened())
            return;

        if (!GuiUtils.isDuped(event.getItem().getItemStack(), OpenKits.Instance))
            return;

        event.setCancelled(true);
        event.getItem().remove();
    }

    /**
     * Handles the InventoryMoveItemEvent to prevent moving duped items.
     *
     * @param event The InventoryMoveItemEvent.
     */
    @EventHandler
    public void onItemMoved(InventoryMoveItemEvent event) {
        InventoryHolder holder = event.getSource().getHolder();
        if (!(holder instanceof Player player))
            return;

        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        if (playerData == null)
            return;

        if (playerData.isGUIOpened())
            return;

        if (!GuiUtils.isDuped(event.getItem(), OpenKits.Instance))
            return;

        event.setCancelled(true);
        event.getItem().setAmount(0);
    }

    /**
     * Handles the PlayerDropItemEvent to prevent dropping duped items.
     *
     * @param event The PlayerDropItemEvent.
     */
    @EventHandler
    public void  onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        if (playerData == null)
            return;

        if (playerData.isGUIOpened())
            return;

        if (!GuiUtils.isDuped(event.getItemDrop().getItemStack(), OpenKits.Instance))
            return;

        event.setCancelled(true);
        event.getItemDrop().remove();
    }
}
