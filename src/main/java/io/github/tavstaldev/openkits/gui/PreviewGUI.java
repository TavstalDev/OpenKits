package io.github.tavstaldev.openkits.gui;

import io.github.tavstaldev.minecorelib.core.GuiDupeDetector;
import io.github.tavstaldev.minecorelib.managers.MenuManager;
import io.github.tavstaldev.minecorelib.models.gui.MenuBase;
import io.github.tavstaldev.minecorelib.models.gui.MenuButton;
import io.github.tavstaldev.minecorelib.shadow.spigui.buttons.SGButton;
import io.github.tavstaldev.minecorelib.shadow.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.managers.PlayerCacheManager;
import io.github.tavstaldev.openkits.models.Kit;
import io.github.tavstaldev.openkits.models.PlayerCache;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PreviewGUI extends MenuBase {
    public static String ID = "kitspreview";

    public PreviewGUI() {
        super(OpenKits.Instance, "preview.yml");
    }

    @Override
    protected void loadDefaults() {
        menuTitle = resolveGet("title", "GUI.KitPreviewTitle");
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
            add(new MenuButton(Material.SPRUCE_DOOR, null, 1, null, "GUI.Back", null, null, 45, null,  List.of("[OPEN] " + KitsGUI.ID)));
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
        SGMenu menu = menuManager.getSpiGUI().create(menuTitle, menuSize);
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
        int page = playerData.getPreviewPage();
        List<ItemStack> items = playerData.getPreviewKit().getItems();
        for (int i = 0; i < dynamicSlots.size(); i++) {
            int index = i + (page - 1) * dynamicSlots.size();
            int slot = dynamicSlots.get(i);

            if (index >= items.size()) {
                sgMenu.removeButton(0, slot);
                continue;
            }

            ItemStack itemStack = items.get(index);
            var meta = itemStack.getItemMeta();
            meta.getPersistentDataContainer().set(GuiDupeDetector.getDupeProtectedKey(), PersistentDataType.BOOLEAN, true);
            itemStack.setItemMeta(meta);
            sgMenu.setButton(0, slot, new SGButton(itemStack));
        }
        player.openInventory(sgMenu.getInventory());
    }

    @Override
    public void executeCommand(@NotNull Player player, @NotNull String command) {
        String[] parts = command.split("\\s+");
        switch (parts[0].toLowerCase()) {
            case "[next_page]" -> {
                PlayerCache playerData = PlayerCacheManager.get(player.getUniqueId());
                int maxPage = 1 + (playerData.getPreviewKit().getItems().size() / dynamicSlots.getOrDefault("kits_slots", new ArrayList<>()).size());
                if (playerData.getPreviewPage() + 1 > maxPage)
                    return;
                playerData.setPreviewPage(playerData.getPreviewPage() + 1);

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
                if (playerData.getPreviewPage() - 1 <= 0)
                    return;
                playerData.setPreviewPage(playerData.getPreviewPage() - 1);

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
        MenuManager manager = plugin.getMenuManager();
        if (manager != null) {
            SGMenu menu = manager.getMenu(player, ID);
            if (menu != null) {
                Kit kit = playerCache.getPreviewKit();
                if (isMenuTitleTranslated) {
                    menu.setName(plugin.localize(player, menuTitle, Map.of(
                            "kit", kit.Name.substring(0, 1).toUpperCase() + kit.Name.substring(1)
                    )));
                }
                refresh(player, menu);
            }
        }
    }
}