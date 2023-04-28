package com.worldplugins.vip.command.vip;

import com.worldplugins.lib.command.ArgsCheck;
import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.pending.PendingVIP;
import com.worldplugins.vip.database.pending.PendingVipRepository;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.handler.VipHandler;
import com.worldplugins.vip.util.TimeParser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class GiveVip implements CommandModule {
    private final @NonNull PendingVipRepository pendingVipRepository;
    private final @NonNull VipHandler activationHandler;
    private final @NonNull ConfigCache<VipData> vipConfig;

    @Command(
        name = "darvip",
        permission = "worldvip.darvip",
        usage = "&cArgumentos invalidos. Digite /darkvip <jogador> <vip> <tipo> [tempo]",
        argsChecks = {
                @ArgsChecker(size = 2, check = ArgsCheck.GREATER_SIZE),
                @ArgsChecker(size = 5, check = ArgsCheck.LOWER_SIZE)
        }
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final String playerName = args[0];
        final Player player = Bukkit.getPlayer(playerName);
        final VipData.VIP configVip = vipConfig.data().getByName(args[1]);

        if (configVip == null) {
            final Collection<String> vipList = vipConfig.data().all().stream()
                .map(VipData.VIP::getName)
                .collect(Collectors.toList());
            sender.respond("Vip-inexistente", message -> message.replace(
                "@nome".to(args[1]),
                "@vips".to(vipList.toString())
            ));
            return;
        }

        final VipType vipType = VipType.fromName(args[2]);

        if (vipType == null) {
            final Collection<String> vipTypes = Arrays.stream(VipType.values())
                .map(type -> type.getName().toUpperCase())
                .collect(Collectors.toList());
            sender.respond("Tipo-vip-inexistente", message -> message.replace(
                "@nome".to(args[2]),
                "@tipos".to(vipTypes.toString())
            ));
            return;
        }

        if (vipType != VipType.PERMANENT && args.length == 3) {
            sender.respond("Vip-tempo-inexistente");
            return;
        }

        final Integer duration = vipType == VipType.PERMANENT
            ? Integer.valueOf(-1)
            : TimeParser.parseTime(args[3]);

        final String durationFormat = duration == null || duration != 1
            ? args[3]
            : GlobalValues.PERMANENT_DURATION;

        if (duration == null) {
            sender.respond("Vip-duracao-invalida", message -> message.replace(
                "@valor".to(durationFormat)
            ));
            return;
        }

        if (player == null) {
            final PendingVIP pendingVip = new PendingVIP(
                playerName, configVip.getId(), vipType, duration
            );
            pendingVipRepository.addPending(pendingVip);
            sender.respond("Vip-givado-pendente", message -> message.replace(
                "@jogador".to(playerName),
                "@vip".to(configVip.getDisplay()),
                "@tipo".to(vipType.getName().toUpperCase()),
                "@tempo".to(durationFormat)
            ));
            return;
        }

        final VIP vip = new VIP(configVip.getId(), vipType, duration);
        activationHandler.activate(player.getUniqueId(), vip, true);
        sender.respond("Vip-givado", message -> message.replace(
            "@jogador".to(player.getName()),
            "@vip".to(configVip.getDisplay()),
            "@tipo".to(vipType.getName().toUpperCase()),
            "@tempo".to(durationFormat)
        ));
    }
}
