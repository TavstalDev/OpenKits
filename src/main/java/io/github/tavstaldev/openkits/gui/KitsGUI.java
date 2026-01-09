package io.github.tavstaldev.openkits.gui;

import io.github.tavstaldev.minecorelib.managers.MenuManager;
import io.github.tavstaldev.minecorelib.models.gui.MenuBase;
import io.github.tavstaldev.minecorelib.models.gui.MenuButton;
import io.github.tavstaldev.minecorelib.shadow.spigui.buttons.SGButton;
import io.github.tavstaldev.minecorelib.shadow.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.managers.PlayerCacheManager;
import io.github.tavstaldev.openkits.models.Kit;
import io.github.tavstaldev.openkits.models.PlayerCache;
import io.github.tavstaldev.openkits.utils.EconomyUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class KitsGUI extends MenuBase {
    public static String ID = "kits";

    public KitsGUI() {
        super(OpenKits.Instance, "kits.yml");
    }

    @Override
    protected void loadDefaults() {
        menuTitle = resolveGet("title", "GUI.KitsTitle");
        isMenuTitleTranslated = resolveGet("title_translated", true);
        menuSize = resolveGet("size", 6);
        dynamicSlots = resolveDynamicSlots(new LinkedHashMap<>() {{
            put("kits_slots", new ArrayList<>() {{
                add("0-44");
            }});
        }});
        menuButtons = resolveButtons(new LinkedHashSet<>() {{
            // Placeholder
            add(new MenuButton(Material.BLACK_STAINED_GLASS_PANE, null, 1, "§r", null, null, null, null, List.of("0-9", "17-18", "26-27", "35-36", "44-47", "51-53"), null));
            // Back button
            add(new MenuButton(Material.SPRUCE_DOOR, null, 1, null, "GUI.Close", null, null, 45, null,  List.of("[CLOSE]")));
            // Previous button
            add(new MenuButton(Material.ARROW, null, 1, null, "GUI.PreviousPage", null, null, 48, null, List.of("[PREV_PAGE]")));
            // Page button, NOTE: should be updated on refresh
            add(new MenuButton(Material.PAPER, null, 1, "{PAGE}", null, null, null, 49, null, null));
            // Next button
            add(new MenuButton(Material.ARROW, null, 1, null, "GUI.NextPage", null, null, 50, null, List.of("[NEXT_PAGE]")));
        }});
    }

    @Override
    public SGMenu create(@NotNull Player player) {
        MenuManager menuManager = plugin.getMenuManager();
        if (menuManager == null)
            throw new RuntimeException("Menu manager was not initialized.");
        SGMenu menu = menuManager.getSpiGUI().create(isMenuTitleTranslated ? translator.localize(player, menuTitle) : menuTitle, menuSize);
        for (MenuButton button : menuButtons) {
            button.apply(player, translator, menu, this);
        }
        return menu;
    }

    @Override
    public void refresh(@NotNull Player player, @NotNull SGMenu sgMenu) {
        PlayerCache playerData = PlayerCacheManager.get(player.getUniqueId());

        // 1. Find page button
        MenuButton pageButton = null;
        for (MenuButton btn : menuButtons) {
            if (btn.getTitle() != null && btn.getTitle().equalsIgnoreCase("{PAGE}")) {
                pageButton = btn;
                break;
            }
        }

        // 2. Update page button
        if (pageButton != null) {
            String pageText = translator.localize(player,  "GUI.Page", Map.of(
                    "page", String.valueOf(playerData.getKitsPage()) // Localize the page number
            ));
            Component pageComp = ChatUtils.translateColors(pageText, true);

            for (Integer slot : pageButton.getSlots()) {
                SGButton btn = sgMenu.getButton(0, slot);
                if (btn == null)
                    continue;

                ItemStack icon = btn.getIcon();
                ItemMeta meta = icon.getItemMeta();
                if (meta != null) {
                    meta.displayName(pageComp);
                    icon.setItemMeta(meta);
                }
                btn.setIcon(icon);
            }
        }

        // 3. Handle dynamic slots
        List<Integer> dynamicSlots = this.dynamicSlots.getOrDefault("kits_slots", new ArrayList<>());
        int page = playerData.getKitsPage();
        List<Kit> kits = OpenKits.Database.getKits();
        String yesText = plugin.localize(player, "Commands.Common.YesText");
        String noText = plugin.localize(player, "Commands.Common.NoText");
        String freeText = plugin.localize(player, "Commands.Common.Free");
        String currencySingular = Optional.ofNullable(EconomyUtils.currencyNameSingular()).orElse(OpenKits.Instance.localize("General.CurrencySingular"));
        String currencyPlural = Optional.ofNullable(EconomyUtils.currencyNamePlural()).orElse(OpenKits.Instance.localize("General.CurrencyPlural"));

        for (int i = 0; i < dynamicSlots.size(); i++) {
            int index = i + (page - 1) * dynamicSlots.size();
            int slot = dynamicSlots.get(i);

            if (index >= kits.size()) {
                sgMenu.removeButton(0, slot);
                continue;
            }

            Kit kit = kits.get(index);
            List<Component> loreList = new ArrayList<>();

            long hours = kit.Cooldown / 3600;
            long minutes = (kit.Cooldown % 3600) / 60;
            long remainingSeconds = kit.Cooldown % 60;
            String enabledText = kit.Enable ? yesText : noText;
            String priceText = kit.Price == 0 ? freeText : String.format("%.2f", kit.Price);
            String cooldownText = String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds);
            String oneTimeText = kit.IsOneTime ? yesText : noText;
            String canGetText = kit.canGet(player) ? yesText : noText;
            String currencySingularText = kit.Price == 0 ? "" : currencySingular;
            String currencyPluralText = kit.Price == 0 ? "" : currencyPlural;

            for (String rawLore : OpenKits.Instance.localizeList(player, "GUI.KitLore")) {
                String lore = rawLore
                        .replace("%enabled%",enabledText)
                        .replace("%price%", priceText)
                        .replace("%cooldown%", cooldownText)
                        .replace("%onetime%", oneTimeText)
                        .replace("%canget%", canGetText)
                        .replace("%currency_singular%", currencySingularText)
                        .replace("%currency_plural%", currencyPluralText);
                loreList.add(ChatUtils.translateColors(lore, true));
            }

            ItemStack stack = GuiUtils.createItem(plugin, kit.getIcon(),
                    plugin.localize(player, "GUI.KitName", Map.of("kit", kit.Name)),
                    loreList
            );

            sgMenu.setButton(0, slot, new SGButton(stack).withListener(event ->
            {
                // Handle left-click events: request the kit
                if (event.isLeftClick()) {
                    player.performCommand("kit " + kit.Name);
                    return;
                }

                // Handle right-click events: preview the kit
                if (event.isRightClick()) {
                   MenuManager menuManager = plugin.getMenuManager();
                     if (menuManager == null)
                         return;
                     playerData.setPreviewPage(1);
                     playerData.setPreviewKit(kit);
                     menuManager.open(player, PreviewGUI.ID);
                }
            }));
        }
        player.openInventory(sgMenu.getInventory());
    }

    @Override
    public void executeCommand(@NotNull Player player, @NotNull String command) {
        String[] parts = command.split("\\s+");
        switch (parts[0].toLowerCase()) {
            case "[next_page]" -> {
                PlayerCache playerData = PlayerCacheManager.get(player.getUniqueId());
                int maxPage = 1 + (OpenKits.Database.getKits().size() / dynamicSlots.getOrDefault("kits_slots", new ArrayList<>()).size());
                if (playerData.getKitsPage() + 1 > maxPage)
                    return;
                playerData.setKitsPage(playerData.getKitsPage() + 1);

                MenuManager manager = plugin.getMenuManager();
                if (manager == null)
                    break;
                SGMenu menu = manager.getMenu(player, ID);
                if (menu == null)
                    break;
                refresh(player, menu);
            }
            case "[prev_page]" -> {
                PlayerCache playerData = PlayerCacheManager.get(player.getUniqueId());
                if (playerData.getKitsPage() - 1 <= 0)
                    return;
                playerData.setKitsPage(playerData.getKitsPage() - 1);

                MenuManager manager = plugin.getMenuManager();
                if (manager == null)
                    break;
                SGMenu menu = manager.getMenu(player, ID);
                if (menu == null)
                    break;
                refresh(player, menu);
            }
            case "[open]" -> {
                if (parts.length < 2)
                    return;
                String menuId = parts[1];
                MenuManager manager = plugin.getMenuManager();
                if (manager != null)
                    manager.open(player, menuId);
            }
            case "[close]" -> {
                MenuManager manager = plugin.getMenuManager();
                if (manager != null)
                    manager.close(player, false);
            }
        }
    }

    @Override
    public void onOpen(@NotNull Player player) {
        PlayerCache playerCache = PlayerCacheManager.get(player.getUniqueId());
        playerCache.setKitsPage(1);

        MenuManager manager = plugin.getMenuManager();
        if (manager != null) {
            SGMenu menu = manager.getMenu(player, ID);
            if (menu != null)
                refresh(player, menu);
        }
    }
}