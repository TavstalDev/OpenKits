package io.github.tavstaldev.openkits.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.minecorelib.models.command.SubCommandData;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.gui.KitsGUI;
import io.github.tavstaldev.openkits.models.Kit;
import io.github.tavstaldev.openkits.models.KitCooldown;
import io.github.tavstaldev.openkits.utils.EconomyUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class CommandKit implements CommandExecutor {
    private final PluginLogger _logger = OpenKits.logger().withModule(CommandKit.class);
    private final String baseCommand = "openkits";
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            _logger.info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("openkits.commands.kit")) {
            OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        if (args.length == 0) {
            help(player, 1);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    help(player, page);
                    return true;
                }
                case "version": {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("version", OpenKits.Instance.getVersion());
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Version.Current", parameters);

                    OpenKits.Instance.isUpToDate().thenAccept(upToDate -> {
                        if (upToDate) {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Version.UpToDate");
                        } else {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Version.Outdated", Map.of("link", OpenKits.Instance.getDownloadUrl()));
                        }
                    }).exceptionally(e -> {
                        _logger.error("Failed to determine update status: " + e.getMessage());
                        return null;
                    });
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("openkits.commands.kit.reload")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    OpenKits.Instance.reload();
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Reload.Done");
                    return true;
                }
                case "list": {
                    if (!player.hasPermission("openkits.commands.kit.list")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        } catch (Exception ex) {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    List<Kit> kits = OpenKits.Database.getKits();
                    if (kits.isEmpty()) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.List.None");
                        return true;
                    }

                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.List.Title");

                    boolean reachedEnd = false;
                    int maxPage = 1 + (kits.size() / 15);
                    int kitSentCount = 0;
                    for (int i = 0; i < 15; i++) {
                        int index = i + (page - 1) * 15;

                        if (index >= kits.size()) {
                            reachedEnd = true;
                            break;
                        }

                        Kit kit = kits.get(index);
                        String msg = OpenKits.Instance.localize(player, "Commands.List.Line")
                                .replace("%kit%", kit.Name);

                        Component result = ChatUtils.buildWithButtons(msg, new HashMap<>() {{
                            put("info_button",
                                    ChatUtils.translateColors(OpenKits.Instance.localize(player, "Commands.List.InfoBtn"), true).clickEvent(ClickEvent.runCommand("/kit info " + kit.Name)));
                            put("get_button",
                                    ChatUtils.translateColors(OpenKits.Instance.localize(player, "Commands.List.GetBtn"), true).clickEvent(ClickEvent.runCommand("/kit " + kit.Name)));
                        }});

                        player.sendMessage(result);
                        kitSentCount++;
                    }

                    if (kitSentCount == 0) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.List.None");
                        return true;
                    }

                    // Bottom message
                    String previousBtn = OpenKits.Instance.localize(player, "Commands.List.PrevBtn");
                    String nextBtn = OpenKits.Instance.localize(player, "Commands.List.NextBtn");
                    String bottomMsg = OpenKits.Instance.localize(player, "Commands.List.Bottom")
                            .replace("%current_page%", String.valueOf(page))
                            .replace("%max_page%", String.valueOf(maxPage));

                    Map<String, Component> bottomParams = new HashMap<>();
                    if (page > 1)
                        bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true).clickEvent(ClickEvent.runCommand("/kit list " + (page - 1))));
                    else
                        bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true));

                    if (!reachedEnd && maxPage >= page + 1)
                        bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true).clickEvent(ClickEvent.runCommand("/kit list " + (page + 1))));
                    else
                        bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true));

                    Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
                    player.sendMessage(bottomComp);

                    return true;
                }
                case "info": {
                    if (!player.hasPermission("openkits.commands.kit.info")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Info.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    if (kit.RequirePermission && !player.hasPermission(kit.Permission)) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoKitPermission", new HashMap<>() {{
                            put("kit", kit.Name);
                        }});
                        return true;
                    }

                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Info.Title", new HashMap<>() {{
                        put("kit", kit.Name);
                    }});

                    String kitPermission = OpenKits.Instance.localize(player, "Commands.Common.NoText");
                    if (!kit.Permission.isEmpty())
                        kitPermission = kit.Permission;
                    String kitRequired = OpenKits.Instance.localize(player, "Commands.Common.NoText");
                    if (kit.RequirePermission)
                        kitRequired = OpenKits.Instance.localize(player, "Commands.Common.YesText");

                    long hours = kit.Cooldown / 3600;
                    long minutes = (kit.Cooldown % 3600) / 60;
                    long remainingSeconds = kit.Cooldown % 60;

                    OpenKits.Instance.sendRichMsg(player, OpenKits.Instance.localize(player, "Commands.Info.Enabled")
                            .replace("%enabled%", kit.Enable ? OpenKits.Instance.localize(player, "Commands.Common.YesText") : OpenKits.Instance.localize(player, "Commands.Common.NoText")));
                    OpenKits.Instance.sendRichMsg(player, OpenKits.Instance.localize(player, "Commands.Info.Price")
                            .replace("%price%", String.format("%.2f", kit.Price)));
                    OpenKits.Instance.sendRichMsg(player, OpenKits.Instance.localize(player, "Commands.Info.Cooldown")
                            .replace("%cooldown%", String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)));
                    OpenKits.Instance.sendRichMsg(player, OpenKits.Instance.localize(player, "Commands.Info.Permission")
                            .replace("%permission%", kitPermission)
                            .replace("%required%", kitRequired));
                    OpenKits.Instance.sendRichMsg(player, OpenKits.Instance.localize(player, "Commands.Info.OneTime")
                            .replace("%onetime%", kit.IsOneTime ? OpenKits.Instance.localize(player, "Commands.Common.YesText") : OpenKits.Instance.localize(player, "Commands.Common.NoText")));

                    return true;
                }
                case "gui": {
                    if (!player.hasPermission("openkits.commands.kit.gui")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    KitsGUI.open(player);
                    return true;
                }
                case "give": {
                    if (!player.hasPermission("openkits.commands.kit.give")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Give.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    if (!kit.Enable) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Get.Disabled", new HashMap<>() {{
                            put("kit", kit.Name);
                        }});
                        return true;
                    }

                    Player target = OpenKits.Instance.getServer().getPlayer(args[2]);
                    if (target == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.PlayerNotFound", new HashMap<>() {{
                            put("player", args[2]);
                        }});
                        return true;
                    }

                    kit.give(target);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Give.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                        put("player", target.getName());
                    }});
                    OpenKits.Instance.sendRichMsg(target, OpenKits.Instance.localize(player, "Commands.Get.Success").replace("%kit%", kit.Name));

                    return true;
                }
                case "create": {
                    if (!player.hasPermission("openkits.commands.kit.create")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length < 3 || args.length > 8) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Create.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit != null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Create.KitAlreadyExists", new HashMap<>() {{
                            put("kit", kit.Name);
                        }});
                        return true;
                    }
                    double price = OpenKits.config().getDouble("default.price");
                    long cooldown = OpenKits.config().getLong("default.cooldown");
                    String permission = OpenKits.config().getString("default.permission");
                    if (permission != null)
                        permission = permission.replace("%kit%", args[1]);
                    boolean requirePermission = OpenKits.config().getBoolean("default.isPermissionRequired");
                    boolean isOneTime = OpenKits.config().getBoolean("default.oneTime");
                    Material icon;
                    try
                    {
                        icon = Material.getMaterial(args[2].toUpperCase());
                        if ( icon == null) {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidMaterial", new HashMap<>() {{
                                put("material", args[2]);
                            }});
                            return true;
                        }
                    }
                    catch (Exception ex)
                    {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidMaterial");
                        return true;
                    }

                    if (args.length >= 4) {
                        try {
                            cooldown = Long.parseLong(args[3]);
                        } catch (Exception ex) {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidCooldown");
                            return true;
                        }
                    }

                    if (args.length >= 5) {
                        try {
                            price = Double.parseDouble(args[4]);
                        } catch (Exception ex) {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidPrice");
                            return true;
                        }
                    }

                    if (args.length >= 6) {
                        permission = args[5];
                        requirePermission = true;
                    }

                    if (args.length >= 7) {
                        switch (args[6].toLowerCase()) {
                            case "yes":
                            case "y":
                            case "true":
                            case "1":
                            case "on": {
                                isOneTime = true;
                                break;
                            }
                            case "no":
                            case "n":
                            case "false":
                            case "0":
                            case "off": {
                                isOneTime = false;
                                break;
                            }
                            default: {
                                OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidBoolean");
                                return true;
                            }
                        }
                    }

                    if (args.length == 8) {
                        switch (args[7].toLowerCase()) {
                            case "yes":
                            case "y":
                            case "true":
                            case "1":
                            case "on": {
                                isOneTime = true;
                                break;
                            }
                            case "no":
                            case "n":
                            case "false":
                            case "0":
                            case "off": {
                                isOneTime = false;
                                break;
                            }
                            default: {
                                OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidBoolean");
                                return true;
                            }
                        }
                    }

                    List<ItemStack> items = new ArrayList<>();
                    for (ItemStack itemStack : player.getInventory().getContents()) {
                        if (itemStack != null && itemStack.getType() != Material.AIR)
                            items.add(itemStack);
                    }

                    OpenKits.Database.addKit(args[1], icon, price, requirePermission, permission, cooldown, isOneTime, true, items);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Create.Success", new HashMap<>() {{
                        put("kit", args[1]);
                    }});

                    return true;
                }
                case "delete": {
                    if (!player.hasPermission("openkits.commands.kit.delete")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Delete.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    OpenKits.Database.removeKit(kit.Id);
                    OpenKits.Database.removeKitCooldowns(kit.Id);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Delete.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                    }});

                    return true;
                }
                case "edit": {
                    if (!player.hasPermission("openkits.commands.kit.edit")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Edit.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    ItemStack[] items = player.getInventory().getContents();
                    OpenKits.Database.updateKitItems(kit.Id, Arrays.stream(items).toList());
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Edit.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                    }});
                    return true;
                }
                case "setprice": {
                    if (!player.hasPermission("openkits.commands.kit.setprice")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetPrice.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    double price;
                    try {
                        price = Double.parseDouble(args[2]);
                    } catch (Exception ex) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidPrice");
                        return true;
                    }
                    if (price < 0)
                        price = 0;

                    // Required because of the Hashtable
                    double finalPrice = price;
                    OpenKits.Database.updateKitPrice(kit.Id, finalPrice);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetPrice.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                        put("price", String.format("%.2f", finalPrice));
                    }});
                    return true;
                }
                case "setcooldown": {
                    if (!player.hasPermission("openkits.commands.kit.setcooldown")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetCooldown.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    long cooldown;
                    try {
                        cooldown = Long.parseLong(args[2]);
                    } catch (Exception ex) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidCooldown");
                        return true;
                    }

                    if (cooldown < 0)
                        cooldown = 0;

                    // Required because of the Hashtable
                    long finalCooldown = cooldown;
                    OpenKits.Database.updateKitCooldown(kit.Id, finalCooldown);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetCooldown.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                        put("cooldown", finalCooldown);
                    }});
                    return true;
                }
                case "setpermission": {
                    if (!player.hasPermission("openkits.commands.kit.setpermission")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length < 3) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetPermission.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    if (args[2].equalsIgnoreCase("none")) {
                        OpenKits.Database.updateKitPermission(kit.Id, false, "");
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetPermission.Success", new HashMap<>() {{
                            put("kit", kit.Name);
                            put("permission", OpenKits.Instance.localize(player, "Commands.Common.NoTextne"));
                        }});
                        return true;
                    }

                    if (args.length != 4) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetPermission.Usage");
                        return true;
                    }

                    boolean requirePermission;
                    switch (args[3].toLowerCase()) {
                        case "yes":
                        case "y":
                        case "true":
                        case "1":
                        case "on": {
                            requirePermission = true;
                            break;
                        }
                        case "no":
                        case "n":
                        case "false":
                        case "0":
                        case "off": {
                            requirePermission = false;
                            break;
                        }
                        default: {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidBoolean");
                            return true;
                        }
                    }

                    OpenKits.Database.updateKitPermission(kit.Id, requirePermission, args[2]);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetPermission.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                        put("permission", args[2]);
                    }});

                    return true;
                }
                case "setonetime": {
                    if (!player.hasPermission("openkits.commands.kit.setonetime")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetOneTime.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    boolean isOneTime;
                    switch (args[2].toLowerCase()) {
                        case "yes":
                        case "y":
                        case "true":
                        case "1":
                        case "on": {
                            isOneTime = true;
                            break;
                        }
                        case "no":
                        case "n":
                        case "false":
                        case "0":
                        case "off": {
                            isOneTime = false;
                            break;
                        }
                        default: {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidBoolean");
                            return true;
                        }
                    }

                    OpenKits.Database.updateKitOneTime(kit.Id, isOneTime);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetOneTime.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                        put("onetime", isOneTime ? OpenKits.Instance.localize(player, "Commands.Common.YesText") : OpenKits.Instance.localize(player, "Commands.Common.NoText"));
                    }});

                    return true;
                }
                case "setenabled": {
                    if (!player.hasPermission("openkits.commands.kit.setenabled")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetEnabled.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    boolean enabled;
                    switch (args[2].toLowerCase()) {
                        case "yes":
                        case "y":
                        case "true":
                        case "1":
                        case "on": {
                            enabled = true;
                            break;
                        }
                        case "no":
                        case "n":
                        case "false":
                        case "0":
                        case "off": {
                            enabled = false;
                            break;
                        }
                        default: {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidBoolean");
                            return true;
                        }
                    }

                    OpenKits.Database.updateKitEnabled(kit.Id, enabled);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetEnabled.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                        put("enabled", enabled ? OpenKits.Instance.localize(player, "Commands.Common.YesText") : OpenKits.Instance.localize(player, "Commands.Common.NoText"));
                    }});

                    return true;
                }
                case "setname": {
                    if (!player.hasPermission("openkits.commands.kit.setname")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetName.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    Kit newKit = OpenKits.Database.findKit(args[2]);
                    if (newKit != null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Create.KitAlreadyExists", new HashMap<>() {{
                            put("kit", newKit.Name);
                        }});
                        return true;
                    }

                    OpenKits.Database.updateKitName(kit.Id, args[2]);
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetName.Success", new HashMap<>() {{
                        put("kit", kit.Name);
                        put("new_name", args[2]);
                    }});

                    return true;
                }
                case "seticon": {
                    if (!player.hasPermission("openkits.commands.kit.seticon")) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.SetIcon.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.findKit(args[1]);
                    if (kit == null) {
                        OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    Material icon;
                    try {
                        icon = Material.getMaterial(args[2].toUpperCase());
                        if (icon == null)
                        {
                            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidMaterial", new HashMap<>() {{
                                put("material", args[2]);
                            }});
                            return true;
                        }
                    } catch (Exception ex) {
                        OpenKits.Instance.sendLocalizedMsg(player, "Commands.Common.InvalidMaterial", new HashMap<>() {{
                            put("material", args[2]);
                        }});
                        return true;
                    }

                    OpenKits.Database.updateKitIcon(kit.Id, icon);
                    player.sendMessage(ChatUtils.buildWithButtons(OpenKits.Instance.localize(player, "Commands.SetIcon.Success"), new HashMap<>() {{
                        put("kit", ChatUtils.translateColors(kit.Name, true));
                        put("icon", Component.translatable(icon.translationKey()));
                    }}));
                    return true;
                }
            }

            // Find kit by name
            //#region Get Kit
            Kit kit = OpenKits.Database.findKit(args[0]);
            if (kit == null) {
                OpenKits.Instance.sendLocalizedMsg(player, "General.KitNotFound", new HashMap<>() {{
                    put("kit", args[0]);
                }});
                return true;
            }

            if (!kit.Enable) {
                OpenKits.Instance.sendLocalizedMsg(player, "Commands.Get.Disabled", new HashMap<>() {{
                    put("kit", kit.Name);
                }});
                return true;
            }

            if (kit.RequirePermission && !player.hasPermission(kit.Permission)) {
                OpenKits.Instance.sendLocalizedMsg(player, "General.NoKitPermission", new HashMap<>() {{
                    put("kit", kit.Name);
                }});
                return true;
            }

            KitCooldown cooldown = OpenKits.Database.findKitCooldown(player.getUniqueId(), kit.Id);
            if (cooldown != null) {
                Duration duration = Duration.between(LocalDateTime.now(), cooldown.End);
                if (duration.getSeconds() > 0) {
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Get.Cooldown", new HashMap<>() {{
                        put("kit", kit.Name);
                        put("time", String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()));
                    }});
                    return true;
                }

                if (kit.IsOneTime) {
                    OpenKits.Instance.sendLocalizedMsg(player, "Commands.Get.OneTime", new HashMap<>() {{
                        put("kit", kit.Name);
                    }});
                    return true;
                }
            }

            if (kit.Price > 0 && !EconomyUtils.has(player, kit.Price)) {
                OpenKits.Instance.sendLocalizedMsg(player, "Commands.Get.NoMoney", new HashMap<>() {{
                    put("kit", kit.Name);
                }});
                return true;
            }

            kit.give(player);
            if (cooldown == null)
                OpenKits.Database.addKitCooldown(player.getUniqueId(), kit.Id, LocalDateTime.now().plusSeconds(kit.Cooldown));
            else
                OpenKits.Database.updateKitCooldown(player.getUniqueId(), kit.Id, LocalDateTime.now().plusSeconds(kit.Cooldown));

            if (kit.Price > 0) {
                EconomyUtils.withdraw(player, kit.Price);
                OpenKits.Instance.sendLocalizedMsg(player, "Commands.Get.Purchase", new HashMap<>() {{
                    put("kit", kit.Name);
                    put("price", String.format("%.2f", kit.Price));
                }});
                return true;
            }

            OpenKits.Instance.sendLocalizedMsg(player, "Commands.Get.Success", new HashMap<>() {{
                put("kit", kit.Name);
            }});
            //#endregion
        } catch (Exception ex) {
            OpenKits.Instance.sendLocalizedMsg(player, "Commands.UnknownError");
            _logger.warn("Error while executing kits command:");
            _logger.error(ex.getMessage());
        }

        return true;
    }

    private final List<SubCommandData> _subCommands = new ArrayList<>() {
        {
            // HELP
            add(new SubCommandData("help", "", new HashMap<>() {{
                put("syntax", null);
                put("description", "Commands.Help.Desc");
            }}));
            // VERSION
            add(new SubCommandData("version", "", new HashMap<>() {{
                put("syntax", null);
                put("description", "Commands.Version.Desc");
            }}));
            // RELOAD
            add(new SubCommandData("reload", "openkits.commands.kit.reload", new HashMap<>() {{
                put("syntax", null);
                put("description", "Commands.Reload.Desc");
            }}));
            // LIST
            add(new SubCommandData("list", "openkits.commands.kit.list", new HashMap<>() {{
                put("syntax", "Commands.List.Syntax");
                put("description", "Commands.List.Desc");
            }}));
            // INFO
            add(new SubCommandData("info", "openkits.commands.kit.info", new HashMap<>() {{
                put("syntax", "Commands.Info.Syntax");
                put("description", "Commands.Info.Desc");
            }}));
            // GET
            add(new SubCommandData("", "", new HashMap<>() {{
                put("syntax", "Commands.Get.Syntax");
                put("description", "Commands.Get.Desc");
            }}));
            // GIVE
            add(new SubCommandData("give", "openkits.commands.kit.give", new HashMap<>() {{
                put("syntax", "Commands.Give.Syntax");
                put("description", "Commands.Give.Desc");
            }}));
            // GUI
            add(new SubCommandData("gui", "openkits.commands.kit.gui", new HashMap<>() {{
                put("syntax", null);
                put("description", "Commands.Gui.Desc");
            }}));
            // CREATE
            add(new SubCommandData("create", "openkits.commands.kit.create", new HashMap<>() {{
                put("syntax", "Commands.Create.Syntax");
                put("description", "Commands.Create.Desc");
            }}));
            // DELETE
            add(new SubCommandData("delete", "openkits.commands.kit.delete", new HashMap<>() {{
                put("syntax", "Commands.Delete.Syntax");
                put("description", "Commands.Delete.Desc");
            }}));
            // EDIT
            add(new SubCommandData("edit", "openkits.commands.kit.edit", new HashMap<>() {{
                put("syntax", "Commands.Edit.Syntax");
                put("description", "Commands.Edit.Desc");
            }}));
            // SET NAME
            add(new SubCommandData("setname", "openkits.commands.kit.setname", new HashMap<>() {{
                put("syntax", "Commands.SetName.Syntax");
                put("description", "Commands.SetName.Desc");
            }}));
            // SET ENABLED
            add(new SubCommandData("setenabled", "openkits.commands.kit.setenabled", new HashMap<>() {{
                put("syntax", "Commands.SetEnabled.Syntax");
                put("description", "Commands.SetEnabled.Desc");
            }}));
            // SET PRICE
            add(new SubCommandData("setprice", "openkits.commands.kit.setprice", new HashMap<>() {{
                put("syntax", "Commands.SetPrice.Syntax");
                put("description", "Commands.SetPrice.Desc");
            }}));
            // SET COOLDOWN
            add(new SubCommandData("setcooldown", "openkits.commands.kit.setcooldown", new HashMap<>() {{
                put("syntax", "Commands.SetCooldown.Syntax");
                put("description", "Commands.SetCooldown.Desc");
            }}));
            // SET PERMISSION
            add(new SubCommandData("setpermission", "openkits.commands.kit.setpermission", new HashMap<>() {{
                put("syntax", "Commands.SetPermission.Syntax");
                put("description", "Commands.SetPermission.Desc");
            }}));
            // SET ONE-TIME
            add(new SubCommandData("setonetime", "openkits.commands.kit.setonetime", new HashMap<>() {{
                put("syntax", "Commands.SetOneTime.Syntax");
                put("description", "Commands.SetOneTime.Desc");
            }}));
            // SET ICON
            add(new SubCommandData("seticon", "openkits.commands.kit.seticon", new HashMap<>() {{
                put("syntax", "Commands.SetIcon.Syntax");
                put("description", "Commands.SetIcon.Desc");
            }}));
        }
    };

    private void help(CommandSender sender, int page) {
        int maxPage = 1 + (_subCommands.size() / 15);

        // Ensure the page number is within valid bounds
        if (page > maxPage)
            page = maxPage;
        if (page < 1)
            page = 1;
        int finalPage = page;

        // Send the help menu title and info
        OpenKits.Instance.sendCommandReply(sender, "Commands.Help.Title", Map.of(
                        "current_page", finalPage,
                        "max_page", maxPage
                )
        );
        OpenKits.Instance.sendCommandReply(sender, "Commands.Help.Info");

        boolean reachedEnd = false;
        int itemIndex = 0;

        // Display up to 15 subcommands per page
        for (int i = 0; i < 15; i++) {
            int index = itemIndex + (page - 1) * 15;
            if (index >= _subCommands.size()) {
                reachedEnd = true;
                break;
            }
            itemIndex++;

            SubCommandData subCommand = _subCommands.get(index);
            if (!subCommand.hasPermission(sender)) {
                i--;
                continue;
            }

            subCommand.send(OpenKits.Instance, sender, baseCommand);
        }

        // Display navigation buttons for the help menu
        String previousBtn, nextBtn, bottomMsg;
        if (sender instanceof Player player)
        {
            previousBtn = OpenKits.Instance.localize(player, "Commands.Help.PrevBtn");
            nextBtn = OpenKits.Instance.localize(player, "Commands.Help.NextBtn");
            bottomMsg = OpenKits.Instance.localize(player, "Commands.Help.Bottom", Map.of(
                    "current_page", page,
                    "max_page", maxPage
            ));
        }
        else {
            previousBtn = OpenKits.Instance.localize("Commands.Help.PrevBtn");
            nextBtn = OpenKits.Instance.localize("Commands.Help.NextBtn");
            bottomMsg = OpenKits.Instance.localize("Commands.Help.Bottom", Map.of(
                    "current_page", page,
                    "max_page", maxPage
            ));
        }

        Map<String, Component> bottomParams = new HashMap<>();
        if (page > 1)
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page - 1))));
        else
            bottomParams.put("previous_btn", ChatUtils.translateColors(previousBtn, true));

        if (!reachedEnd && maxPage >= page + 1)
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true)
                    .clickEvent(ClickEvent.runCommand(String.format("/%s help %s", baseCommand, page + 1))));
        else
            bottomParams.put("next_btn", ChatUtils.translateColors(nextBtn, true));

        Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
        sender.sendMessage(bottomComp);
    }
}
