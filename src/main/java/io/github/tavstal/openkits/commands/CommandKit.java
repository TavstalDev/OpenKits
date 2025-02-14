package io.github.tavstal.openkits.commands;

import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.KitCooldown;
import io.github.tavstal.openkits.utils.ChatUtils;
import io.github.tavstal.openkits.utils.EconomyUtils;
import io.github.tavstal.openkits.utils.LocaleUtils;
import io.github.tavstal.openkits.utils.LoggerUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class CommandKit implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender)
        {
            LoggerUtils.LogInfo(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player)sender;

        if (!player.hasPermission("openkits.commands.kit")) {
            ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        if (args.length == 0) {
            help(player);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "help":
                case "?": {
                    help(player);
                    return true;
                }
                case "version": {
                    Dictionary<String, Object> parameters = new Hashtable<>();
                    parameters.put("version", OpenKits.VERSION);
                    ChatUtils.sendLocalizedMsg(player, "Commands.Version.Current", parameters);

                    boolean isUpToDate = OpenKits.Instance.isUpToDate();
                    if (isUpToDate) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.Version.UpToDate");
                        return true;
                    }

                    parameters = new Hashtable<>();
                    parameters.put("link", OpenKits.DOWNLOAD_URL);
                    ChatUtils.sendLocalizedMsg(player, "Commands.Version.Outdated");
                    return true;
                }
                case "reload": {
                    if (!player.hasPermission("openkits.commands.kit.reload")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    OpenKits.Instance.reload();
                    ChatUtils.sendLocalizedMsg(player, "Commands.Reload.Done");
                    return true;
                }
                case "list": {
                    if (!player.hasPermission("openkits.commands.kit.list")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    int page = 1;
                    if (args.length > 1) {
                        try {
                            page = Integer.parseInt(args[1]);
                        }
                        catch (Exception ex) {
                            ChatUtils.sendLocalizedMsg(player, "Commands.Common.InvalidPage");
                            return true;
                        }
                    }

                    ChatUtils.sendLocalizedMsg(player, "Commands.List.Title");

                    boolean reachedEnd = false;
                    List<Kit> kits = OpenKits.Database.GetKits();
                    int maxPage = 1 + (kits.size() / 15);
                    for (int i = 0; i < 15; i++) {
                        int index = i + (page - 1) * 15;

                        if (index >= kits.size()) {
                            reachedEnd = true;
                            break;
                        }

                        Kit kit = kits.get(index);
                        String msg = LocaleUtils.Localize("Commands.List.Line")
                                .replace("%kit%", kit.Name)
                                .replace("%description%", kit.Description);

                        Component result = ChatUtils.buildWithButtons(msg, new Hashtable<>() {{
                            put("info_button",
                                    ChatUtils.translateColors(LocaleUtils.Localize("Commands.List.InfoBtn"), true).clickEvent(ClickEvent.runCommand("/kit info " + kit.Name)));
                            put("get_button",
                                    ChatUtils.translateColors(LocaleUtils.Localize("Commands.List.GetBtn"), true).clickEvent(ClickEvent.runCommand("/kit " + kit.Name)));
                        }});

                        player.sendMessage(result);
                    }

                    // Bottom message
                    String previousBtn = LocaleUtils.Localize("Commands.List.PrevBtn");
                    String nextBtn = LocaleUtils.Localize("Commands.List.NextBtn");
                    String bottomMsg = LocaleUtils.Localize("Commands.List.Bottom")
                            .replace("%page%", String.valueOf(page))
                            .replace("%maxPage%", String.valueOf(maxPage));

                    Dictionary<String, Component> bottomParams = new Hashtable<>();
                    if (page > 1)
                        bottomParams.put("previous_btn", Component.text(previousBtn).clickEvent(ClickEvent.runCommand("/aldas list " + (page - 1))));
                    else
                        bottomParams.put("previous_btn", Component.text(previousBtn));

                    if (!reachedEnd && maxPage >= page + 1)
                        bottomParams.put("next_btn", Component.text(nextBtn).clickEvent(ClickEvent.runCommand("/aldas list " + (page + 1))));
                    else
                        bottomParams.put("next_btn", Component.text(nextBtn));

                    Component bottomComp = ChatUtils.buildWithButtons(bottomMsg, bottomParams);
                    player.sendMessage(bottomComp);

                    return true;
                }
                case "info": {
                    if (!player.hasPermission("openkits.commands.kit.info")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.Info.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.FindKit(args[1]);
                    if (kit == null) {
                        ChatUtils.sendLocalizedMsg(player, "General.KitNotFound", new Hashtable<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    if (kit.RequirePermission && !player.hasPermission(kit.Permission)) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoKitPermission", kit.Name);
                        return true;
                    }

                    ChatUtils.sendLocalizedMsg(player, "Commands.Info.Title", new Hashtable<>() {{
                        put("kit", kit.Name);
                    }});

                    ChatUtils.sendRichMsg(player, LocaleUtils.Localize("Commands.Info.Description").replace("%description%", kit.Description));
                    String kitPermission = LocaleUtils.Localize("Commands.Common.None");
                    if (!kit.Permission.isEmpty())
                        kitPermission = kit.Permission;
                    String kitRequired = LocaleUtils.Localize("Commands.Common.No");
                    if (kit.RequirePermission)
                        kitRequired = LocaleUtils.Localize("Commands.Common.Yes");

                    ChatUtils.sendRichMsg(player, LocaleUtils.Localize("Commands.Info.Price").replace("%price%", String.format("%.2f", kit.Price)));

                    long hours = kit.Cooldown / 3600;
                    long minutes = (kit.Cooldown % 3600) / 60;
                    long remainingSeconds = kit.Cooldown % 60;

                    ChatUtils.sendRichMsg(player, LocaleUtils.Localize("Commands.Info.Cooldown")
                            .replace("%cooldown%", String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)));
                    ChatUtils.sendRichMsg(player, LocaleUtils.Localize("Commands.Info.Permission")
                            .replace("%permission%", kitPermission)
                            .replace("%required%", kitRequired));
                    ChatUtils.sendRichMsg(player, LocaleUtils.Localize("Commands.Info.OneTime")
                            .replace("%onetime%", kit.IsOneTime ? LocaleUtils.Localize("Commands.Common.Yes") : LocaleUtils.Localize("Commands.Common.No")));


                    return true;
                }
                case "gui": {
                    if (!player.hasPermission("openkits.commands.kit.gui")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    // TODO: Open GUI

                    return true;
                }
                case "give": {
                    if (!player.hasPermission("openkits.commands.kit.give")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.Give.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.FindKit(args[1]);
                    if (kit == null) {
                        ChatUtils.sendLocalizedMsg(player, "General.KitNotFound", new Hashtable<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    Player target = OpenKits.Instance.getServer().getPlayer(args[2]);
                    if (target == null) {
                        ChatUtils.sendLocalizedMsg(player, "General.PlayerNotFound", new Hashtable<>() {{
                            put("player", args[2]);
                        }});
                        return true;
                    }

                    kit.Give(target);
                    ChatUtils.sendLocalizedMsg(player, "Commands.Give.Success", new Hashtable<>() {{
                        put("kit", kit.Name);
                        put("player", target.getName());
                    }});
                    ChatUtils.sendRichMsg(target, LocaleUtils.Localize("Commands.Get.Success").replace("%kit%", kit.Name));

                    return true;
                }
                case "create": {
                    if (!player.hasPermission("openkits.commands.kit.create")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    // TODO

                    return true;
                }
                case "delete": {
                    if (!player.hasPermission("openkits.commands.kit.delete")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.Delete.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.FindKit(args[1]);
                    if (kit == null) {
                        ChatUtils.sendLocalizedMsg(player, "General.KitNotFound", new Hashtable<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    OpenKits.Database.RemoveKit(kit.Id);
                    OpenKits.Database.RemoveKitCooldowns(kit.Id);
                    ChatUtils.sendLocalizedMsg(player, "Commands.Delete.Success", new Hashtable<>() {{
                        put("kit", kit.Name);
                    }});

                    return true;
                }
                case "edit": {
                    if (!player.hasPermission("openkits.commands.kit.edit")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 2) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.Edit.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.FindKit(args[1]);
                    if (kit == null) {
                        ChatUtils.sendLocalizedMsg(player, "General.KitNotFound", new Hashtable<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    // TODO

                    return true;
                }
                case "setprice": {
                    if (!player.hasPermission("openkits.commands.kit.setprice")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.SetPrice.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.FindKit(args[1]);
                    if (kit == null) {
                        ChatUtils.sendLocalizedMsg(player, "General.KitNotFound", new Hashtable<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    double price;
                    try {
                        price = Double.parseDouble(args[2]);
                    }
                    catch (Exception ex) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.Common.InvalidPrice");
                        return true;
                    }
                    if (price < 0)
                        price = 0;

                    // Required because of the Hashtable
                    double finalPrice = price;
                    OpenKits.Database.UpdateKit(kit.Id, finalPrice);
                    ChatUtils.sendLocalizedMsg(player, "Commands.SetPrice.Success", new Hashtable<>() {{
                        put("kit", kit.Name);
                        put("price", String.format("%.2f", finalPrice));
                    }});
                    return true;
                }
                case "setcooldown": {
                    if (!player.hasPermission("openkits.commands.kit.setcooldown")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length != 3) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.SetCooldown.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.FindKit(args[1]);
                    if (kit == null) {
                        ChatUtils.sendLocalizedMsg(player, "General.KitNotFound", new Hashtable<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    long cooldown;
                    try {
                        cooldown = Long.parseLong(args[2]);
                    }
                    catch (Exception ex) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.Common.InvalidCooldown");
                        return true;
                    }

                    if (cooldown < 0)
                        cooldown = 0;

                    // Required because of the Hashtable
                    long finalCooldown = cooldown;
                    OpenKits.Database.UpdateKit(kit.Id, finalCooldown);
                    ChatUtils.sendLocalizedMsg(player, "Commands.SetCooldown.Success", new Hashtable<>() {{
                        put("kit", kit.Name);
                        put("cooldown", finalCooldown);
                    }});
                    return true;
                }
                case "setpermission": {
                    if (!player.hasPermission("openkits.commands.kit.setpermission")) {
                        ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
                        return true;
                    }

                    if (args.length < 3) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.SetPermission.Usage");
                        return true;
                    }

                    Kit kit = OpenKits.Database.FindKit(args[1]);
                    if (kit == null) {
                        ChatUtils.sendLocalizedMsg(player, "General.KitNotFound", new Hashtable<>() {{
                            put("kit", args[1]);
                        }});
                        return true;
                    }

                    if (args[2].equalsIgnoreCase("none")) {
                        OpenKits.Database.UpdateKit(kit.Id, false, "");
                        ChatUtils.sendLocalizedMsg(player, "Commands.SetPermission.Success", new Hashtable<>() {{
                            put("kit", kit.Name);
                            put("permission", LocaleUtils.Localize("Commands.Common.None"));
                        }});
                        return true;
                    }

                    if (args.length != 4) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.SetPermission.Usage");
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
                            ChatUtils.sendLocalizedMsg(player, "Commands.Common.InvalidBoolean");
                            return true;
                        }
                    }

                    OpenKits.Database.UpdateKit(kit.Id, requirePermission, args[2]);
                    ChatUtils.sendLocalizedMsg(player, "Commands.SetPermission.Success", new Hashtable<>() {{
                        put("kit", kit.Name);
                        put("permission", args[2]);
                    }});

                    return true;
                }
            }

            // Find kit by name
            //#region Get Kit
            Kit kit = OpenKits.Database.FindKit(args[0]);
            if (kit == null) {
                ChatUtils.sendLocalizedMsg(player, "General.KitNotFound", new Hashtable<>() {{
                    put("kit", args[0]);
                }});
                return true;
            }

            if (kit.RequirePermission && !player.hasPermission(kit.Permission)) {
                ChatUtils.sendLocalizedMsg(player, "General.NoKitPermission", new Hashtable<>() {{
                    put("kit", kit.Name);
                }});
                return true;
            }

            KitCooldown cooldown = OpenKits.Database.FindKitCooldown(player.getUniqueId(), kit.Id);
            if (cooldown != null) {
                Duration duration = Duration.between(LocalDateTime.now(), cooldown.End);
                if (duration.getSeconds() > 0) {
                    ChatUtils.sendLocalizedMsg(player, "Commands.Get.Cooldown", new Hashtable<>() {{
                        put("kit", kit.Name);
                        put("time", String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart()));
                    }});
                    return true;
                }

                if (kit.IsOneTime) {
                    ChatUtils.sendLocalizedMsg(player, "Commands.Get.OneTime", new Hashtable<>() {{
                        put("kit", kit.Name);
                    }});
                    return true;
                }
            }

            if (kit.Price > 0 && !EconomyUtils.has(player, kit.Price)) {
                ChatUtils.sendLocalizedMsg(player, "Commands.Get.NoMoney", new Hashtable<>() {{
                    put("kit", kit.Name);
                }});
                return true;
            }

            kit.Give(player);
            if (cooldown == null)
                OpenKits.Database.AddKitCooldown(player.getUniqueId(), kit.Id, LocalDateTime.now().plusSeconds(kit.Cooldown));
            else
                OpenKits.Database.UpdateKitCooldown(player.getUniqueId(), kit.Id, LocalDateTime.now().plusSeconds(kit.Cooldown));

            if (kit.Price > 0) {
                EconomyUtils.withdraw(player, kit.Price);
                ChatUtils.sendLocalizedMsg(player, "Commands.Get.Purchase", new Hashtable<>() {{
                    put("kit", kit.Name);
                    put("price", String.format("%.2f", kit.Price));
                }});
                return true;
            }

            ChatUtils.sendLocalizedMsg(player, "Commands.Get.Success", new Hashtable<>() {{
                put("kit", kit.Name);
            }});
            //#endregion
        }
        catch (Exception ex) {
            ChatUtils.sendLocalizedMsg(player, "Commands.UnknownError");
            LoggerUtils.LogWarning("Error while executing aldas command:");
            LoggerUtils.LogError(ex.getMessage());
        }

        return true;
    }

    private void help(Player player) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("currentpage", 1);
        parameters.put("maxpage", 1);
        ChatUtils.sendLocalizedMsg(player, "Commands.Help.Title", parameters);
        ChatUtils.sendLocalizedMsg(player, "Commands.Help.Info");

        // Help
        parameters = new HashMap<>();
        parameters.put("subcommand", "help");
        parameters.put("syntax", "");
        parameters.put("description", LocaleUtils.Localize("Commands.Help.Desc"));
        ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);

        // Version
        parameters = new HashMap<>();
        parameters.put("subcommand", "version");
        parameters.put("syntax", "");
        parameters.put("description", LocaleUtils.Localize("Commands.Version.Desc"));
        ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);

        // Reload
        if (player.hasPermission("openkits.commands.kit.reload")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "reload");
            parameters.put("syntax", "");
            parameters.put("description", LocaleUtils.Localize("Commands.Reload.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // List
        if (player.hasPermission("openkits.commands.kit.list")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "list");
            parameters.put("syntax", LocaleUtils.Localize("Commands.List.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.List.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // Info
        if (player.hasPermission("openkits.commands.kit.info")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "info");
            parameters.put("syntax", LocaleUtils.Localize("Commands.Info.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.Info.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // Get
        parameters = new HashMap<>();
        parameters.put("subcommand", "");
        parameters.put("syntax", LocaleUtils.Localize("Commands.Get.Syntax"));
        parameters.put("description", LocaleUtils.Localize("Commands.Get.Desc"));
        ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);

        // Give
        if (player.hasPermission("openkits.commands.kit.give")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "give");
            parameters.put("syntax", LocaleUtils.Localize("Commands.Give.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.Give.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // Create
        if (player.hasPermission("openkits.commands.kit.create")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "create");
            parameters.put("syntax", LocaleUtils.Localize("Commands.Create.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.Create.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // Delete
        if (player.hasPermission("openkits.commands.kit.delete")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "delete");
            parameters.put("syntax", LocaleUtils.Localize("Commands.Delete.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.Delete.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // Edit
        if (player.hasPermission("openkits.commands.kit.edit")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "edit");
            parameters.put("syntax", LocaleUtils.Localize("Commands.Edit.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.Edit.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // GUI
        if (player.hasPermission("openkits.commands.kit.gui")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "gui");
            parameters.put("syntax", LocaleUtils.Localize("Commands.Gui.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.Gui.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // SetPrice
        if (player.hasPermission("openkits.commands.kit.setprice")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "setprice");
            parameters.put("syntax", LocaleUtils.Localize("Commands.SetPrice.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.SetPrice.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // SetCooldown
        if (player.hasPermission("openkits.commands.kit.setcooldown")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "setcooldown");
            parameters.put("syntax", LocaleUtils.Localize("Commands.SetCooldown.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.SetCooldown.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }

        // SetPermission
        if (player.hasPermission("openkits.commands.kit.setpermission")) {
            parameters = new HashMap<>();
            parameters.put("subcommand", "setpermission");
            parameters.put("syntax", LocaleUtils.Localize("Commands.SetPermission.Syntax"));
            parameters.put("description", LocaleUtils.Localize("Commands.SetPermission.Desc"));
            ChatUtils.sendLocalizedMsg(player, "Commands.Help.Line", parameters);
        }
    }
}
