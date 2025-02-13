package io.github.tavstal.openkits;

import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class EventListener implements Listener {
    public static void init() {
        LoggerUtils.LogDebug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new EventListener(), OpenKits.Instance);
        LoggerUtils.LogDebug("Event listener registered.");
    }

    @EventHandler
    public void onPlayerFirstJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
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
