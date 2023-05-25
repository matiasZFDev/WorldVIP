package com.worldplugins.vip.command.vip;

import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.pending.PendingVIP;
import com.worldplugins.vip.database.pending.PendingVipRepository;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.handler.VipHandler;
import com.worldplugins.vip.util.TimeParser;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class GiveVip implements CommandModule {
    private final @NotNull PendingVipRepository pendingVipRepository;
    private final @NotNull VipHandler activationHandler;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public GiveVip(
        @NotNull PendingVipRepository pendingVipRepository,
        @NotNull VipHandler activationHandler,
        @NotNull ConfigModel<VipData> vipConfig) {
        this.pendingVipRepository = pendingVipRepository;
        this.activationHandler = activationHandler;
        this.vipConfig = vipConfig;
    }

    @Command(name = "darvip")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldvip.darvip")) {
            return;
        }

        if (!(args.length > 2 && args.length < 5)) {
            respond(sender, "Dar-vip-uso");
            return;
        }

        final String playerName = args[0];
        final Player player = Bukkit.getPlayer(playerName);
        final VipData.VIP configVip = vipConfig.data().getByName(args[1]);

        if (configVip == null) {
            final Collection<String> vipList = vipConfig.data().all().stream()
                .map(VipData.VIP::name)
                .collect(Collectors.toList());

            respond(sender, "Vip-inexistente", message -> message.replace(
                to("@nome", args[1]),
                to("@vips", vipList.toString())
            ));
            return;
        }

        final VipType vipType = VipType.fromName(args[2]);

        if (vipType == null) {
            final Collection<String> vipTypes = Arrays.stream(VipType.values())
                .map(type -> type.getName().toUpperCase())
                .collect(Collectors.toList());

            respond(sender, "Tipo-vip-inexistente", message -> message.replace(
                to("@nome", args[2]),
                to("@tipos", vipTypes.toString())
            ));
            return;
        }

        if (vipType != VipType.PERMANENT && args.length == 3) {
            respond(sender, "Vip-tempo-inexistente");
            return;
        }

        final Integer duration = vipType == VipType.PERMANENT
            ? Integer.valueOf(-1)
            : TimeParser.parseTime(args[3]);

        final String durationFormat = duration == null || duration != 1
            ? args[3]
            : GlobalValues.PERMANENT_DURATION;

        if (duration == null) {
            respond(sender, "Vip-duracao-invalida", message -> message.replace(
                to("@valor", durationFormat)
            ));
            return;
        }

        if (player == null) {
            final PendingVIP pendingVip = new PendingVIP(
                playerName, configVip.id(), vipType, duration
            );

            pendingVipRepository.addPending(pendingVip);
            respond(sender, "Vip-givado-pendente", message -> message.replace(
                to("@jogador", playerName),
                to("@vip", configVip.display()),
                to("@tipo", vipType.getName().toUpperCase()),
                to("@tempo", durationFormat)
            ));
            return;
        }

        final VIP vip = new VIP(configVip.id(), vipType, duration);

        activationHandler.activate(player.getUniqueId(), vip, true);
        respond(sender, "Vip-givado", message -> message.replace(
            to("@jogador", player.getName()),
            to("@vip", configVip.display()),
            to("@tipo", vipType.getName().toUpperCase()),
            to("@tempo", durationFormat)
        ));
    }
}
