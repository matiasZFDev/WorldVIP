package com.worldplugins.vip.command;

import com.worldplugins.lib.command.ArgsCheck;
import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.TimeExtensions;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    TimeExtensions.class
})

@RequiredArgsConstructor
public class VipDurationLeft implements CommandModule {
    private final @NonNull PlayerService playerService;

    @Command(
        name = "tempovip",
        usage = "&cArgumentos invalidos. Digite /tempovip [jogador]",
        argsChecks = {@ArgsChecker(size = 2, check = ArgsCheck.LOWER_SIZE)}
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        if (args.length == 1) {
            if (!sender.hasPermission("worldvip.tempovip")) {
                sender.respond("Tempo-vip-permissoes");
                return;
            }

            final Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                sender.respond("Jogador-offline", message -> message.replace(
                    "@jogador".to(args[0])
                ));
                return;
            }

            final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());

            if (vipPlayer == null || vipPlayer.getActiveVip() == null) {
                sender.respond("Tempo-jogador-sem-vip", message -> message.replace(
                    "@jogador".to(player.getName())
                ));
                return;
            }

            final String durationFormat = vipPlayer.getActiveVip().getType() == VipType.PERMANENT
                ? GlobalValues.PERMANENT_DURATION
                : ((Integer) vipPlayer.getActiveVip().getDuration()).toTime();
            sender.respond("Tempo-vip-jogador", message -> message.replace(
                "@jogador".to(player.getName()),
                "@tempo".to(durationFormat)
            ));
        }

        final Player player = Bukkit.getPlayer(args[0]);
        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());

        if (vipPlayer == null || vipPlayer.getActiveVip() == null) {
            sender.respond("Tempo-sem-vip");
            return;
        }

        final String durationFormat = vipPlayer.getActiveVip().getType() == VipType.PERMANENT
            ? GlobalValues.PERMANENT_DURATION
            : ((Integer) vipPlayer.getActiveVip().getDuration()).toTime();
        sender.respond("Tempo-vip", message -> message.replace(
            "@tempo".to((durationFormat)
        )));
    }
}
