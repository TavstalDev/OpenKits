package io.github.tavstal.openkits.commands;

import io.github.tavstal.openkits.OpenKits;
import io.github.tavstal.openkits.models.Kit;
import io.github.tavstal.openkits.models.KitCooldown;
import io.github.tavstal.openkits.utils.ChatUtils;
import io.github.tavstal.openkits.utils.LocaleUtils;
import io.github.tavstal.openkits.utils.LoggerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.LocalDateTime;

public class CommandKits implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (sender instanceof ConsoleCommandSender) {
            LoggerUtils.LogInfo(ChatUtils.translateColors("Commands.ConsoleCaller", true).toString());
            return true;
        }
        Player player = (Player) sender;

        if (!player.hasPermission("openkits.commands.kits")) {
            ChatUtils.sendLocalizedMsg(player, "General.NoPermission");
            return true;
        }

        String message = LocaleUtils.Localize(player, "Commands.Kits.Format", true);
        StringBuilder kits = new StringBuilder();

        for (Kit kit : OpenKits.Database.GetKits()) {
            if (!kits.toString().isBlank()) {
                kits.append(LocaleUtils.Localize(player,"Commands.Kits.Separator"));
            }

            if (kit.RequirePermission && !player.hasPermission(kit.Permission)) {
                kits.append(LocaleUtils.Localize(player,"Commands.Kits.UnavailableKit", true)
                        .replace("%kit%", kit.Name));
                continue;
            }

            KitCooldown cooldown = OpenKits.Database.FindKitCooldown(player.getUniqueId(), kit.Id);
            if (cooldown != null) {
                Duration duration = Duration.between(LocalDateTime.now(), cooldown.End);
                if (duration.getSeconds() > 0) {
                    kits.append(LocaleUtils.Localize(player,"Commands.Kits.CooldownKit", true)
                            .replace("%kit%", kit.Name)
                            .replace("%cooldown%", String.format("%02d:%02d:%02d", duration.toHoursPart(), duration.toMinutesPart(), duration.toSecondsPart())));
                    continue;
                }
            }

            if (kit.Price > 0)
                kits.append(LocaleUtils.Localize(player,"Commands.Kits.Paid", true)
                        .replace("%kit%", kit.Name)
                        .replace("%price%", String.format("%.2f", kit.Price)));
            else
                kits.append(LocaleUtils.Localize(player,"Commands.Kits.Free", true)
                        .replace("%kit%", kit.Name));
        }


        ChatUtils.sendRichMsg(player, message
                .replace("%kits%", kits.toString())
                .replace("%count%", String.valueOf(OpenKits.Database.GetKits().size())));

        return true;
    }
}
