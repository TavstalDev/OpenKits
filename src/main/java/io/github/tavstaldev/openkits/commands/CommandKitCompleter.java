package io.github.tavstaldev.openkits.commands;

import io.github.tavstaldev.minecorelib.core.PluginLogger;
import io.github.tavstaldev.openkits.OpenKits;
import io.github.tavstaldev.openkits.models.Kit;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandKitCompleter implements TabCompleter {
    private final PluginLogger _logger = OpenKits.Logger().WithModule(CommandKitCompleter.class);

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String @NotNull [] args) {
        try {
            if (sender instanceof ConsoleCommandSender) {
                return null;
            }
            Player player = (Player) sender;
            List<String> commandList = new ArrayList<>();

            switch (args.length) {
                case 0:
                case 1: {
                    commandList.add("help");
                    commandList.add("version");
                    if (player.hasPermission("openkits.commands.kit.reload"))
                        commandList.add("reload");
                    if (player.hasPermission("openkits.commands.kit.list"))
                        commandList.add("list");
                    if (player.hasPermission("openkits.commands.kit.info"))
                        commandList.add("info");
                    if (player.hasPermission("openkits.commands.kit.give"))
                        commandList.add("give");
                    if (player.hasPermission("openkits.commands.kit.gui"))
                        commandList.add("gui");
                    if (player.hasPermission("openkits.commands.kit.create"))
                        commandList.add("create");
                    if (player.hasPermission("openkits.commands.kit.delete"))
                        commandList.add("delete");
                    if (player.hasPermission("openkits.commands.kit.edit"))
                        commandList.add("edit");
                    if (player.hasPermission("openkits.commands.kit.setname"))
                        commandList.add("setname");
                    if (player.hasPermission("openkits.commands.kit.setenabled"))
                        commandList.add("setenabled");
                    if (player.hasPermission("openkits.commands.kit.setpermission"))
                        commandList.add("setpermission");
                    if (player.hasPermission("openkits.commands.kit.setprice"))
                        commandList.add("setprice");
                    if (player.hasPermission("openkits.commands.kit.setcooldown"))
                        commandList.add("setcooldown");
                    if (player.hasPermission("openkits.commands.kit.setpermission"))
                        commandList.add("setpermission");
                    if (player.hasPermission("openkits.commands.kit.setonetime"))
                        commandList.add("setonetime");
                    if (player.hasPermission("openkits.commands.kit.seticon"))
                        commandList.add("seticon");


                    for (Kit kit : OpenKits.Database.GetKits()) {
                        if (!kit.Enable)
                            continue;

                        if (!kit.RequirePermission || player.hasPermission(kit.Permission))
                            commandList.add(kit.Name);
                    }

                    commandList.removeIf(cmd -> !cmd.toLowerCase().startsWith(args[0].toLowerCase()));
                    break;
                }
                case 2: {
                    switch (args[0].toLowerCase()) {
                        case "help":
                        case "list": {
                            commandList.add("1");
                            commandList.add("5");
                            commandList.add("10");
                            break;
                        }
                        case "info":
                        case "give":
                        case "delete":
                        case "edit":
                        case "setprice":
                        case "setcooldown":
                        case "setpermission":
                        case "setonetime":
                        case "seticon":
                        case "setname":
                        case "setenabled": {

                            for (Kit kit : OpenKits.Database.GetKits()) {
                                if (!kit.Enable)
                                    continue;

                                if (!kit.RequirePermission || player.hasPermission(kit.Permission))
                                    commandList.add(kit.Name);
                            }
                            commandList.removeIf(cmd -> !cmd.toLowerCase().startsWith(args[1].toLowerCase()));
                            break;
                        }
                        case "create": {
                            commandList.add("<kit_name>");
                            commandList.removeIf(cmd -> !cmd.toLowerCase().startsWith(args[1].toLowerCase()));
                            break;
                        }
                    }

                    break;
                }
                case 3: {
                    switch (args[0].toLowerCase()) {
                        case "give": {
                            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                commandList.add(onlinePlayer.getName());
                            }
                            commandList.removeIf(cmd -> !cmd.toLowerCase().startsWith(args[2].toLowerCase()));
                            break;
                        }
                        case "setprice": {
                            commandList.add("1.0");
                            commandList.add("5.0");
                            commandList.add("10.0");
                            break;
                        }
                        case "setcooldown": {
                            commandList.add("30");
                            commandList.add("60");
                            commandList.add("120");
                            break;
                        }
                        case "setpermission": {
                            String permission = OpenKits.GetConfig().getString("default.permission");
                            if (permission != null)
                                commandList.add(permission.replace("%kit%", args[1].toLowerCase()));
                        }
                        case "setonetime":
                        case "setenabled":{
                            commandList.add("true");
                            commandList.add("false");
                            break;
                        }
                        case "create":
                        case "seticon":{
                            for (Material material : Material.values()) {
                                commandList.add(material.name());
                            }
                            commandList.removeIf(cmd -> !cmd.toLowerCase().startsWith(args[2].toLowerCase()));
                            break;
                        }
                    }
                    break;
                }
                case 4: {
                    switch (args[0].toLowerCase()) {
                        case "setpermission": {
                            commandList.add("none");
                            commandList.add("true");
                            commandList.add("false");
                        }
                        case "create": {
                            commandList.add("30");
                            commandList.add("60");
                            commandList.add("120");
                            break;
                        }
                    }
                    break;
                }
                case 5: {
                    if (args[0].equalsIgnoreCase("create")) {
                        commandList.add("1.0");
                        commandList.add("5.0");
                        commandList.add("10.0");
                    }
                    break;
                }
                case 6: {
                    if (args[0].equalsIgnoreCase("create")) {
                        String permission = OpenKits.GetConfig().getString("default.permission");
                        if (permission != null)
                            commandList.add(permission.replace("%kit%", args[1].toLowerCase()));
                    }
                    break;
                }
                case 7: {
                    if (args[0].equalsIgnoreCase("create")) {
                        commandList.add("none");
                        commandList.add("true");
                        commandList.add("false");
                    }
                    break;
                }
                case 8: {
                    if (args[0].equalsIgnoreCase("create")) {
                        commandList.add("true");
                        commandList.add("false");
                    }
                    break;
                }
            }

            Collections.sort(commandList);
            return commandList;
        }
        catch (Exception ex) {
            _logger.Error("An error occurred while trying to tab complete the portallock command.");
            _logger.Error(ex.getMessage());
            return new ArrayList<>();
        }
    }
}
