package com.worldplugins.vip.command;

import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.util.Time;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.vip.Response.respond;
import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class VipDurationLeft implements CommandModule {
    private final @NotNull PlayerService playerService;

    public VipDurationLeft(@NotNull PlayerService playerService) {
        this.playerService = playerService;
    }

    @Command(name = "tempovip")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length >= 1) {
            if (!sender.hasPermission("worldvip.tempovip")) {
                respond(sender, "Tempo-vip-permissoes");
                return;
            }

            final Player player = Bukkit.getPlayer(args[0]);

            if (player == null) {
                respond(sender, "Jogador-offline", message -> message.replace(
                    to("@jogador", args[0])
                ));
                return;
            }

            final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());

            if (vipPlayer == null || vipPlayer.activeVip() == null) {
                respond(sender, "Tempo-jogador-sem-vip", message -> message.replace(
                    to("@jogador", player.getName())
                ));
                return;
            }

            final VIP activeVip = requireNonNull(vipPlayer.activeVip());
            final String durationFormat = activeVip.type() == VipType.PERMANENT
                ? GlobalValues.PERMANENT_DURATION
                : Time.toFormat(activeVip.duration());

            respond(sender, "Tempo-vip-jogador", message -> message.replace(
                to("@jogador", player.getName()),
                to("@tempo", durationFormat)
            ));
        }

        if (!(sender instanceof Player)) {
            respond(sender, "Comando-jogador");
            return;
        }

        final Player player = (Player) sender;
        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());

        if (vipPlayer == null || vipPlayer.activeVip() == null) {
            respond(sender, "Tempo-sem-vip");
            return;
        }

        final VIP activeVip = requireNonNull(vipPlayer.activeVip());
        final String durationFormat = activeVip.type() == VipType.PERMANENT
            ? GlobalValues.PERMANENT_DURATION
            : Time.toFormat(activeVip.duration());

        respond(sender, "Tempo-vip", message -> message.replace(
            to("@tempo", durationFormat)
        ));
    }
}
