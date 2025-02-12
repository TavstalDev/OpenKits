package io.github.tavstal.openkits.commands;

import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.utils.ChatUtils;
import io.github.tavstal.openkits.utils.LocaleUtils;
import io.github.tavstal.openkits.utils.LoggerUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/*
SubCommands:
- help
- version
- reload
- give
- list
- create
- delete
- edit
- info
- gui
- setPrice
- setCooldown
- setPermission
*/
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
                    if (args.length == 1) {
                        ChatUtils.sendLocalizedMsg(player, "Commands.Info.Usage");
                        return true;
                    }



                    return true;
                }
                case "give": {

                    return true;
                }
                case "create": {

                    return true;
                }
                case "delete": {

                    return true;
                }
                case "edit": {

                    return true;
                }
                case "gui": {

                    return true;
                }
                case "setprice": {

                    return true;
                }
                case "setcooldown": {

                    return true;
                }
                case "setpermission": {

                    return true;
                }
            }

            // Find kit by name
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
