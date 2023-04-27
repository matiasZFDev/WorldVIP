package com.worldplugins.vip.command.vip;

import com.worldplugins.lib.command.ArgsCheck;
import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.CommandTarget;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.handler.VipHandler;
import com.worldplugins.vip.handler.OwningVipHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class SwitchVip implements CommandModule {
    private final @NonNull PlayerService playerService;
    private final @NonNull Cache<UUID, VipPlayer> cache;
    private final @NonNull ConfigCache<MainData> mainConfig;
    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull OwningVipHandler owningVipHandler;
    private final @NonNull VipHandler vipHandler;

    private final @NonNull Map<UUID, Long> onDelay = new HashMap<>();

    @Command(
        name = "trocarvip",
        target = CommandTarget.PLAYER,
        argsChecks = {
            @ArgsChecker(check = ArgsCheck.GREATER_SIZE),
            @ArgsChecker(size = 3, check = ArgsCheck.LOWER_SIZE)
        },
        usage = "&cArgumentos invalidos. Digite /trocarvip <vip> [tipo]"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = (Player) sender;

        if (onDelay.containsKey(player.getUniqueId())) {
            final long now = System.nanoTime();
            final int secondsElapsed = (int) ((now - onDelay.get(player.getUniqueId())) / 1000);

            if (secondsElapsed < mainConfig.data().getSwitchVipDelay()) {
                player.respond("Trocar-vip-delay");
                return;
            }

            onDelay.remove(player.getUniqueId());
        }

        final VipData.VIP configVip = vipConfig.data().getByName(args[0]);

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

        final VipPlayer vipPlayer = cache.get(player.getUniqueId());

        if (vipPlayer == null || vipPlayer.getActiveVip() == null) {
            player.respond("Trocar-vip-inexistente");
            return;
        }

        final VipType vipType;

        if (args.length == 1) {
            vipType = getGreatestType(vipPlayer);

            if (vipType == null) {
                sender.respond("Trocar-vip-sem-vips", message -> message.replace(
                    "@vip".to(configVip.getDisplay())
                ));
                return;
            }
        } else {
            vipType = VipType.fromName(args[1]);

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
        }

        final VipData.VIP oldConfigVip = vipConfig.data().getById(vipPlayer.getActiveVip().getId());
        final VIP newOwningVip = vipPlayer.getActiveVip();
        final OwningVIP newPrimaryVip = vipPlayer.getOwningVips().get(configVip.getId(), vipType);

        if (mainConfig.data().stackVips()) {
            playerService.removeOwningVip(player.getUniqueId(), newPrimaryVip);
            playerService.setVip(player.getUniqueId(), newPrimaryVip);
            playerService.addOwningVip(player.getUniqueId(), newOwningVip);
        } else {
            owningVipHandler.remove(player, vipPlayer, newPrimaryVip);
            vipHandler.activate(player, newPrimaryVip, false);
        }

        player.respond("Vip-trocado", message -> message.replace(
            "@atual-vip".to(oldConfigVip.getDisplay()),
            "@atual-tipo".to(newOwningVip.getType().getName().toUpperCase()),
            "@novo-vip".to(configVip.getDisplay()),
            "@novo-tipo".to(vipType.getName().toUpperCase())
        ));
    }

    private VipType getGreatestType(@NonNull VipPlayer vipPlayer) {
        return vipPlayer.getOwningVips().getVips().stream()
            .max(Comparator.comparingInt(owningVip -> owningVip.getType().getPriority()))
            .map(OwningVIP::getType)
            .orElse(null);
    }
}
