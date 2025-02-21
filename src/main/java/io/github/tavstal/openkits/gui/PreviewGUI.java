package io.github.tavstal.openkits.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.helpers.GUIHelper;
import io.github.tavstal.openkits.managers.PlayerManager;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PreviewGUI {
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
        SGMenu menu = OpenKits.GetGUI().create(OpenKits.Instance.Localize(player, "GUI.KitPreviewTitle"), 6);

        // Create Placeholders
        SGButton placeholderButton = new SGButton(GUIHelper.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        for (Integer slot : SlotPlaceholders) {
            menu.setButton(0, slot, placeholderButton);
        }

        // Close Button
        SGButton closeButton = new SGButton(
                GUIHelper.createItem(Material.BARRIER, OpenKits.Instance.Localize(player, "GUI.Close")))
                .withListener((InventoryClickEvent event) -> KitsGUI.open(player));
        menu.setButton(0, 45, closeButton);

        // Previous Page Button
        SGButton prevPageButton = new SGButton(
                GUIHelper.createItem(Material.ARROW, OpenKits.Instance.Localize(player, "GUI.PreviousPage")))
                .withListener((InventoryClickEvent event) -> {
                    PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                    if (playerData.getPreviewPage() - 1 < 0)
                        return;
                    playerData.setPreviewPage(playerData.getPreviewPage() - 1);
                    refresh(player);
                });
        menu.setButton(0, 48, prevPageButton);

        // Page Indicator
        SGButton pageButton = new SGButton(
                GUIHelper.createItem(Material.PAPER, OpenKits.Instance.Localize(player, "GUI.Page").replace("%page%", "1"))
        );
        menu.setButton(0, 49, pageButton);

        // Next Page Button
        SGButton nextPageButton = new SGButton(
                GUIHelper.createItem(Material.ARROW, OpenKits.Instance.Localize(player, "GUI.NextPage")))
                .withListener((InventoryClickEvent event) -> {
                    PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                    int maxPage = 1 + (playerData.getPreviewKit().GetItems().size() / 28);
                    if (playerData.getPreviewPage()+ 1 > maxPage)
                        return;
                    playerData.setPreviewPage(playerData.getPreviewPage() + 1);
                    refresh(player);
                });
        menu.setButton(0, 50, nextPageButton);

        refresh(player);
        return menu;
    }

    /**
     * Opens the Preview GUI for the specified player and kit.
     *
     * @param player The player for whom the GUI is being opened.
     * @param kit    The kit to be previewed.
     */
    public static void open(@NotNull Player player, Kit kit) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        // Show the GUI
        playerData.setPreviewKit(kit);
        playerData.setGUIOpened(true);
        playerData.setPreviewPage(1);
        player.openInventory(playerData.getKitsMenu().getInventory());
        refresh(player);
    }

    /**
     * Closes the Preview GUI for the specified player.
     *
     * @param player The player for whom the GUI is being closed.
     */
    public static void close(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        player.closeInventory();
        playerData.setGUIOpened(false);
    }

    /**
     * Refreshes the Preview GUI for the specified player.
     *
     * @param player The player for whom the GUI is being refreshed.
     */
    public static void refresh(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        SGButton pageButton = new SGButton(
                GUIHelper.createItem(Material.PAPER, OpenKits.Instance.Localize(player,"GUI.Page")
                        .replace("%page%", String.valueOf(playerData.getPreviewPage())))
        );
        playerData.getPreviewMenu().setButton(0, 49, pageButton);

        List<ItemStack> items = playerData.getPreviewKit().GetItems();
        int page =  playerData.getPreviewPage();
        for (int i = 0; i < 28; i++) {
            int index = i + page * 28;
            int slot = i + 10 + (2 * (i / 7));
            if (index >= items.size()) {
                playerData.getPreviewMenu().setButton(0, null);
                continue;
            }

            ItemStack itemStack = items.get(index);
            var meta = itemStack.getItemMeta();
            meta.getPersistentDataContainer().set(GUIHelper.DupeKey, PersistentDataType.BOOLEAN, true);
            itemStack.setItemMeta(meta);

            playerData.getPreviewMenu().setButton(0, slot, new SGButton(itemStack));
        }
    }
}
