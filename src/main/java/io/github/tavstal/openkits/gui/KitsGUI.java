package io.github.tavstal.openkits.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.helpers.GUIHelper;
import io.github.tavstal.openkits.managers.PlayerManager;
import io.github.tavstal.openkits.models.PlayerData;
import io.github.tavstal.openkits.utils.LocaleUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class KitsGUI {
    private static final Integer[] SlotPlaceholders = {
            0,  1,  2,  3,  4,  5,  6,  7,  8,
            9,                              17,
            18,                             26,
            27,                             35,
            36,                             44,
                46, 47,             51, 52, 53
    };

    public static SGMenu create(@NotNull Player player) {
        SGMenu menu = OpenKits.GetGUI().create(LocaleUtils.Localize(player, "GUI.KitsTitle"), 6);

        // Create Placeholders
        SGButton placeholderButton = new SGButton(GUIHelper.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
        for (Integer slot : SlotPlaceholders) {
            menu.setButton(0, slot, placeholderButton);
        }

        // Close Button
        SGButton closeButton = new SGButton(
                GUIHelper.createItem(Material.BARRIER, LocaleUtils.Localize(player, "GUI.Close")))
                .withListener((InventoryClickEvent event) -> close(player));
        menu.setButton(0, 45, closeButton);

        // Previous Page Button
        SGButton prevPageButton = new SGButton(
                GUIHelper.createItem(Material.ARROW, LocaleUtils.Localize(player, "GUI.PreviousPage")))
                .withListener((InventoryClickEvent event) -> {
                    PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                    if (playerData.getKitsPage() - 1 < 0)
                        return;
                    playerData.setKitsPage(playerData.getKitsPage() - 1);
                    refresh(player);
                });
        menu.setButton(0, 48, prevPageButton);

        // Page Indicator
        SGButton pageButton = new SGButton(
                GUIHelper.createItem(Material.PAPER, LocaleUtils.Localize(player, "GUI.Page").replace("%page%", "1"))
        );
        menu.setButton(0, 49, pageButton);

        // Next Page Button
        SGButton nextPageButton = new SGButton(
                GUIHelper.createItem(Material.ARROW, LocaleUtils.Localize(player, "GUI.NextPage")))
                .withListener((InventoryClickEvent event) -> {
                    PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                    int maxPage = 1 + (OpenKits.Database.GetKits().size() / 28);
                    if (playerData.getKitsPage()+ 1 > maxPage)
                        return;
                    playerData.setKitsPage(playerData.getKitsPage() + 1);
                    refresh(player);
                });
        menu.setButton(0, 50, nextPageButton);

        refresh(player);
        return menu;
    }

    public static void open(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        // Show the GUI
        player.openInventory(playerData.getKitsMenu().getInventory());
        playerData.setGUIOpened(true);
    }

    public static void close(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        player.closeInventory();
        playerData.setGUIOpened(false);
    }

    public static void refresh(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        SGButton pageButton = new SGButton(
                GUIHelper.createItem(Material.PAPER, LocaleUtils.Localize(player,"GUI.Page")
                        .replace("%page%", String.valueOf(playerData.getKitsPage())))
        );
        playerData.getKitsMenu().setButton(0, 49, pageButton);


    }
}
