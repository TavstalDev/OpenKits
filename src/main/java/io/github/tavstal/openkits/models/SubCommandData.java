package io.github.tavstal.openkits.models;

import io.github.tavstal.openkits.utils.ChatUtils;
import io.github.tavstal.openkits.utils.LocaleUtils;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data associated with a subcommand in the OpenKits plugin.
 */
public class SubCommandData {
    public String command;
    public String permission;
    public Map<String, Object> arguments;

    /**
     * Constructs a new SubCommandData object with the specified command, permission, and arguments.
     *
     * @param command    the subcommand
     * @param permission the permission required to execute the subcommand
     * @param arguments  the arguments for the subcommand
     */
    public SubCommandData(String command, String permission, Map<String, Object> arguments) {
        this.command = command;
        this.permission = permission;
        this.arguments = arguments;
    }

    /**
     * Checks if the player has the required permission to execute the subcommand.
     *
     * @param player the player to check
     * @return true if the player has the required permission, false otherwise
     */
    public boolean hasPermission(Player player) {
        if (this.permission == null || permission.isBlank())
            return true;
        return player.hasPermission(this.permission);
    }

    /**
     * Sends a localized message to the player with the subcommand details.
     *
     * @param player the player to send the message to
     */
    public void send(Player player) {
        if (arguments == null)
            return;

        Map<String, Object> args = new HashMap<>() {{
            put("subcommand", command);
        }};
        var keys = arguments.keySet();
        for (String key : keys) {
            Object param = arguments.get(key);
            if (param == null) {
                args.put(key, "");
                continue;
            }
            args.put(key, LocaleUtils.Localize(player, param.toString()));
        }

        ChatUtils.sendLocalizedMsg(player, LocaleUtils.Localize(player, "Commands.Help.Line"), args);
    }
}
