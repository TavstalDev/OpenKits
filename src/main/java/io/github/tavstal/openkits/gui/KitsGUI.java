package io.github.tavstal.openkits.gui;

import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.utils.ChatUtils;
import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.helpers.GUIHelper;
import io.github.tavstal.openkits.managers.PlayerManager;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.PlayerData;
import io.github.tavstal.openkits.utils.EconomyUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Represents the GUI for displaying kits in the OpenKits plugin.
 * This class provides methods to create, open, close, and refresh the GUI for players.
 */
public class KitsGUI {
    private static final PluginLogger _logger = OpenKits.Logger().WithModule(KitsGUI.class);

    private static final Integer[] SlotPlaceholders = {
            0,  1,  2,  3,  4,  5,  6,  7,  8,
            9,                              17,
            18,                             26,
            27,                             35,
            36,                             44,
                46, 47,             51, 52, 53
    };

    /**
     * Creates the Kits GUI for the specified player.
     *
     * @param player The player for whom the GUI is being created.
     * @return The created SGMenu instance.
     */
    public static SGMenu create(@NotNull Player player) {
        try {
            SGMenu menu = OpenKits.GetGUI().create(OpenKits.Instance.Localize(player, "GUI.KitsTitle"), 6);

            // Create Placeholders
            SGButton placeholderButton = new SGButton(GUIHelper.createItem(Material.BLACK_STAINED_GLASS_PANE, " "));
            for (Integer slot : SlotPlaceholders) {
                menu.setButton(0, slot, placeholderButton);
            }

            // Close Button
            SGButton closeButton = new SGButton(
                    GUIHelper.createItem(Material.BARRIER, OpenKits.Instance.Localize(player, "GUI.Close")))
                    .withListener((InventoryClickEvent event) -> close(player));
            menu.setButton(0, 45, closeButton);

            // Previous Page Button
            SGButton prevPageButton = new SGButton(
                    GUIHelper.createItem(Material.ARROW, OpenKits.Instance.Localize(player, "GUI.PreviousPage")))
                    .withListener((InventoryClickEvent event) -> {
                        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
                        if (playerData.getKitsPage() - 1 <= 0)
                            return;
                        playerData.setKitsPage(playerData.getKitsPage() - 1);
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
                        int maxPage = 1 + (OpenKits.Database.GetKits().size() / 28);
                        if (playerData.getKitsPage() + 1 > maxPage)
                            return;
                        playerData.setKitsPage(playerData.getKitsPage() + 1);
                        refresh(player);
                    });
            menu.setButton(0, 50, nextPageButton);
            return menu;
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while creating the Kits GUI.");
            _logger.Error(ex);
            return null;
        }
    }

    /**
     * Opens the Kits GUI for the specified player.
     *
     * @param player The player for whom the GUI is being opened.
     */
    public static void open(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        // Show the GUI
        playerData.setGUIOpened(true);
        playerData.setKitsPage(1);
        player.openInventory(playerData.getKitsMenu().getInventory());
        refresh(player);
    }

    /**
     * Closes the Kits GUI for the specified player.
     *
     * @param player The player for whom the GUI is being closed.
     */
    public static void close(@NotNull Player player) {
        PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
        player.closeInventory();
        playerData.setGUIOpened(false);
    }

    /**
     * Refreshes the Kits GUI for the specified player.
     *
     * @param player The player for whom the GUI is being refreshed.
     */
    public static void refresh(@NotNull Player player) {
        try {
            PlayerData playerData = PlayerManager.getPlayerData(player.getUniqueId());
            SGButton pageButton = new SGButton(
                    GUIHelper.createItem(Material.PAPER, OpenKits.Instance.Localize(player, "GUI.Page")
                            .replace("%page%", String.valueOf(playerData.getKitsPage())))
            );
            playerData.getKitsMenu().setButton(0, 49, pageButton);

            var kits = OpenKits.Database.GetKits();
            int page = playerData.getKitsPage();
            String yesText = OpenKits.Instance.Localize(player, "Commands.Common.YesText");
            String noText = OpenKits.Instance.Localize(player, "Commands.Common.NoText");
            String freeText = OpenKits.Instance.Localize(player, "Commands.Common.Free");

            for (int i = 0; i < 28; i++) {
                int index = i + (page - 1) * 28;
                int slot = i + 10 + (2 * (i / 7));
                if (index >= kits.size()) {
                    playerData.getKitsMenu().removeButton(0, slot);
                    continue;
                }

                Kit kit = kits.get(index);
                List<Component> loreList = new ArrayList<>();

                long hours = kit.Cooldown / 3600;
                long minutes = (kit.Cooldown % 3600) / 60;
                long remainingSeconds = kit.Cooldown % 60;

                for (String rawLore : OpenKits.Instance.LocalizeList(player, "GUI.KitLore")) {
                    String lore = rawLore
                            .replace("%enabled%", kit.Enable ? yesText : noText)
                            .replace("%price%", kit.Price == 0 ? freeText : String.format("%.2f", kit.Price))
                            .replace("%cooldown%", String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds))
                            .replace("%onetime%", kit.IsOneTime ? yesText : noText)
                            .replace("%canget%", kit.CanGet(player) ? yesText : noText);

                    if (lore.contains("%currency_singular%")) {
                        String currencySingular = EconomyUtils.currencyNameSingular();
                        lore = lore.replace("%currency_singular%", kit.Price == 0 ? "" : currencySingular == null ? OpenKits.Instance.Localize("General.CurrencySingular") : currencySingular);
                    }
                    if (lore.contains("%currency_plural%")) {
                        String currencyPlural = EconomyUtils.currencyNamePlural();
                        lore = lore.replace("%currency_plural%", kit.Price == 0 ? "" : currencyPlural == null ? OpenKits.Instance.Localize("General.CurrencyPlural") : currencyPlural);
                    }
                    loreList.add(ChatUtils.translateColors(lore, true));
                }
                ItemStack stack = GUIHelper.createItem(kit.GetIcon(),
                        OpenKits.Instance.Localize(player, "GUI.KitName", new HashMap<>() {{
                            put("kit", kit.Name);
                        }}),
                        loreList
                );

                playerData.getKitsMenu().setButton(0, slot, new SGButton(stack).withListener((InventoryClickEvent event) -> {
                    if (event.isLeftClick())
                        player.performCommand("kit " + kit.Name);
                    if (event.isRightClick()) {
                        close(player);
                        PreviewGUI.open(player, kit);
                    }
                }));
                player.openInventory(playerData.getKitsMenu().getInventory());
            }
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while refreshing the Kits GUI.");
            _logger.Error(ex);
        }
    }
}
