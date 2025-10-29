package io.github.tavstaldev.openkits.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.core.GuiDupeDetector;
import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.managers.PlayerCacheManager;
import io.github.tavstaldev.openkits.models.Kit;
import io.github.tavstaldev.openkits.models.PlayerCache;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/**
 * Represents the Preview GUI for the OpenKits plugin.
 * This class provides constants for logging and placeholder slots used in the GUI.
 */
public class PreviewGUI {
    private static final PluginLogger _logger = OpenKits.logger().withModule(PreviewGUI.class);
    private static final Integer[] SlotPlaceholders = {
            0,  1,  2,  3,  4,  5,  6,  7,  8,
            9,                              17,
            18,                             26,
            27,                             35,
            36,                             44,
                46, 47,             51, 52, 53
    };

    /**
     * Creates the Preview GUI for the specified player.
     *
     * @param player The player for whom the GUI is being created.
     * @return The created SGMenu instance.
     */
    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = OpenKits.gui().create(OpenKits.Instance.localize(player, "GUI.KitPreviewTitle"), 6);

            // Create Placeholders
            SGButton placeholderButton = new SGButton(GuiUtils.createItem(OpenKits.Instance, Material.BLACK_STAINED_GLASS_PANE, " "));
            for (Integer slot : SlotPlaceholders) {
                menu.setButton(0, slot, placeholderButton);
            }

            // Close Button
            SGButton closeButton = new SGButton(
                    GuiUtils.createItem(OpenKits.Instance, Material.BARRIER, OpenKits.Instance.localize(player, "GUI.Close")))
                    .withListener((InventoryClickEvent event) -> KitsGUI.open(player));
            menu.setButton(0, 45, closeButton);

            // Previous Page Button
            SGButton prevPageButton = new SGButton(
                    GuiUtils.createItem(OpenKits.Instance, Material.ARROW, OpenKits.Instance.localize(player, "GUI.PreviousPage")))
                    .withListener((InventoryClickEvent event) -> {
                        PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
                        if (playerCache.getPreviewPage() - 1 <= 0)
                            return;
                        playerCache.setPreviewPage(playerCache.getPreviewPage() - 1);
                        refresh(player);
                    });
            menu.setButton(0, 48, prevPageButton);

            // Page Indicator
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(OpenKits.Instance, Material.PAPER, OpenKits.Instance.localize(player, "GUI.Page").replace("%page%", "1"))
            );
            menu.setButton(0, 49, pageButton);

            // Next Page Button
            SGButton nextPageButton = new SGButton(
                    GuiUtils.createItem(OpenKits.Instance, Material.ARROW, OpenKits.Instance.localize(player, "GUI.NextPage")))
                    .withListener((InventoryClickEvent event) -> {
                        PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
                        int maxPage = 1 + (playerCache.getPreviewKit().getItems().size() / 28);
                        if (playerCache.getPreviewPage() + 1 > maxPage)
                            return;
                        playerCache.setPreviewPage(playerCache.getPreviewPage() + 1);
                        refresh(player);
                    });
            menu.setButton(0, 50, nextPageButton);
            return menu;
        }
        catch (Exception ex) {
            _logger.error("An error occurred while creating the Preview GUI.");
            _logger.error(ex);
            return null;
        }
    }

    /**
     * Opens the Preview GUI for the specified player and kit.
     *
     * @param player The player for whom the GUI is being opened.
     * @param kit    The kit to be previewed.
     */
    public static void open(@NotNull Player player, Kit kit) {
        PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
        // Show the GUI
        playerCache.setPreviewKit(kit);
        playerCache.setGUIOpened(true);
        playerCache.setPreviewPage(1);
        playerCache.getPreviewMenu().setName(OpenKits.Instance.localize(player, "GUI.KitPreviewTitle", new HashMap<>() {{
            put("kit", kit.Name.substring(0, 1).toUpperCase() + kit.Name.substring(1));
        }}));
        player.openInventory(playerCache.getPreviewMenu().getInventory());
        refresh(player);
    }

    /**
     * Closes the Preview GUI for the specified player.
     *
     * @param player The player for whom the GUI is being closed.
     */
    public static void close(@NotNull Player player) {
        PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
        player.closeInventory();
        playerCache.setGUIOpened(false);
    }

    /**
     * Refreshes the Preview GUI for the specified player.
     *
     * @param player The player for whom the GUI is being refreshed.
     */
    public static void refresh(@NotNull Player player) {
        try {
            PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
            SGButton pageButton = new SGButton(
                    GuiUtils.createItem(OpenKits.Instance, Material.PAPER, OpenKits.Instance.localize(player, "GUI.Page")
                            .replace("%page%", String.valueOf(playerCache.getPreviewPage())))
            );
            playerCache.getPreviewMenu().setButton(0, 49, pageButton);

            List<ItemStack> items = playerCache.getPreviewKit().getItems();
            int page = playerCache.getPreviewPage();
            for (int i = 0; i < 28; i++) {
                int index = i + (page - 1) * 28;
                int slot = i + 10 + (2 * (i / 7));
                if (index >= items.size()) {
                    playerCache.getPreviewMenu().removeButton(0, slot);
                    continue;
                }

                ItemStack itemStack = items.get(index).clone();
                var meta = itemStack.getItemMeta();
                meta.getPersistentDataContainer().set(GuiDupeDetector.getDupeProtectedKey(), PersistentDataType.BOOLEAN, true);
                itemStack.setItemMeta(meta);

                playerCache.getPreviewMenu().setButton(0, slot, new SGButton(itemStack));
            }
            player.openInventory(playerCache.getPreviewMenu().getInventory());
        }
        catch (Exception ex) {
            _logger.error("An error occurred while refreshing the Preview GUI.");
            _logger.error(ex);
        }
    }
}
