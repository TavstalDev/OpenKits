package io.github.tavstal.openkits.commands;

import io.github.tavstal.minecorelib.core.PluginLogger;
import io.github.tavstal.minecorelib.utils.ChatUtils;
import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.KitCooldown;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

public class CommandKits implements CommandExecutor {
    private final PluginLogger _logger = OpenKits.Logger().WithModule(CommandKits.class);
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
             _logger.Info(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("openkits.commands.kits")) {
            OpenKits.Instance.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        String message = OpenKits.Instance.Localize(player, "Commands.Kits.Format", true);
        StringBuilder kits = new StringBuilder();

        for (Kit kit : OpenKits.Database.GetKits()) {
            if (!kits.toString().isBlank()) {
                kits.append(OpenKits.Instance.Localize(player,"Commands.Kits.Separator"));
            }

            if (kit.RequirePermission && !player.hasPermission(kit.Permission)) {
                kits.append(OpenKits.Instance.Localize(player,"Commands.Kits.UnavailableKit", true)
                        .replace("%kit%", kit.Name));
                continue;
            }

            KitCooldown cooldown = OpenKits.Database.FindKitCooldown(player.getUniqueId(), kit.Id);
            if (cooldown != null) {
                Duration duration = Duration.between(LocalDateTime.now(), cooldown.End);
                if (duration.getSeconds() > 0) {
                    kits.append(OpenKits.Instance.Localize(player,"Commands.Kits.CooldownKit", true)
                            .replace("%kit%", kit.Name)
                            .replace("%cooldown%", String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())));
                    continue;
                }
            }

            if (kit.Price > 0)
                kits.append(OpenKits.Instance.Localize(player,"Commands.Kits.Paid", true)
                        .replace("%kit%", kit.Name)
                        .replace("%price%", String.format("%.2f", kit.Price)));
            else
                kits.append(OpenKits.Instance.Localize(player,"Commands.Kits.Free", true)
                        .replace("%kit%", kit.Name));
        }


        OpenKits.Instance.sendRichMsg(player, message
                .replace("%kits%", kits.toString())
                .replace("%count%", String.valueOf(OpenKits.Database.GetKits().size())));

        return true;
    }
}
