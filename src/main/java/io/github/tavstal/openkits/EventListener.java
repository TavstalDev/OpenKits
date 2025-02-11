package io.github.tavstal.openkits;

import io.github.tavstal.openkits.utils.LoggerUtils;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;

public class EventListener implements Listener {
    public static void init() {
        LoggerUtils.LogDebug("Registering event listener...");
        Bukkit.getPluginManager().registerEvents(new EventListener(), OpenKits.Instance);
        LoggerUtils.LogDebug("Event listener registered.");
    }
}
