package com.worldplugins.vip.command.vip;

import com.worldplugins.lib.command.ArgsCheck;
import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.handler.OwningVipHandler;
import com.worldplugins.vip.handler.VipHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class RemoveVip implements CommandModule {
    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull PlayerService playerService;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull VipHandler vipHandler;
    private final @NonNull OwningVipHandler owningVipHandler;

    @Command(
        name = "removervip",
        permission = "worldvip.removervip",
        argsChecks = {
            @ArgsChecker(check = ArgsCheck.GREATER_SIZE),
            @ArgsChecker(size = 4, check = ArgsCheck.LOWER_SIZE)
        },
        usage = "&cArgumentos invalidos. Digite /removervip <jogador> [vip] [tipo]"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final String playerName = args[0];
        final Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.respond("Jogador-offline", message -> message.replace(
                "@jogador".to(playerName)
            ));
            return;
        }

        if (args.length == 1) {
            playerService.getById(player.getUniqueId()).thenAccept(vipPlayer ->
                scheduler.newTask(() -> {
                    if (vipPlayer == null || vipPlayer.getActiveVip() == null) {
                        sender.respond("Remover-vip-inexistente", message -> message.replace(
                            "@jogador".to(playerName)
                        ));
                        return;
                    }

                    vipHandler.remove(player, vipPlayer);
                }).run()
            );
            return;
        }

        final String vipName = args[1];
        final VipData.VIP configVip = vipConfig.data().getByName(vipName);

        if (configVip == null) {
            final Collection<String> vipList = vipConfig.data().all().stream()
                .map(VipData.VIP::getName)
                .collect(Collectors.toList());
            sender.respond("Vip-inexistente", message -> message.replace(
                "@nome".to(vipName),
                "@vips".to(vipList.toString())
            ));
            return;
        }

        final VipType type;

        if (args.length == 3) {
            type = VipType.fromName(args[2]);

            if (type == null) {
                final Collection<String> typeList = Arrays.stream(VipType.values())
                    .map(current -> current.getName().toUpperCase())
                    .collect(Collectors.toList());
                sender.respond("Tipo-vip-inexistente", message -> message.replace(
                    "@nome".to(args[2]),
                    "@tipos".to(typeList.toString())
                ));
                return;
            }
        } else {
            type = null;
        }

        playerService.getById(player.getUniqueId()).thenAccept(vipPlayer ->
            scheduler.newTask(() -> {
                if (vipPlayer == null) {
                    sender.respond("Remover-vip-inexistente", message -> message.replace(
                        "@jogador".to(playerName)
                    ));
                    return;
                }

                final AtomicInteger owningVipRemoveCount = new AtomicInteger(0);

                vipPlayer.getOwningVips().getVips().forEach(owningVip -> {
                    if (
                        owningVip.getId() == configVip.getId() &&
                        (type == null ||  owningVip.getType() == type)
                    ) {
                        owningVipHandler.remove(player, vipPlayer, owningVip);
                        owningVipRemoveCount.incrementAndGet();
                    }
                });

                final boolean removePrimaryVip =
                    vipPlayer.getActiveVip() != null &&
                    vipPlayer.getActiveVip().getId() == configVip.getId() &&
                    (type == null || vipPlayer.getActiveVip().getType() == type);
                final String typeFormat = type == null
                    ? "-/-"
                    : type.getName().toUpperCase();

                if (removePrimaryVip) {
                    vipHandler.remove(player, vipPlayer);
                    sender.respond("Vip-primario-removido");

                    if (owningVipRemoveCount.get() > 0) {
                        sender.respond("Vip-secundarios-removidos", message -> message.replace(
                            "@removidos".to(String.valueOf(owningVipRemoveCount.get())),
                            "@vip".to(configVip.getDisplay()),
                            "@tipo".to(typeFormat)
                        ));
                    }
                } else {
                    if (owningVipRemoveCount.get() == 0) {
                        sender.respond("Vip-removidos-nenhum", message -> message.replace(
                            "@vip".to(configVip.getDisplay()),
                            "@tipo".to(typeFormat)
                        ));
                        return;
                    }

                    sender.respond("Vip-secundarios-removidos", message -> message.replace(
                        "@removidos".to(String.valueOf(owningVipRemoveCount.get())),
                        "@vip".to(configVip.getDisplay()),
                        "@tipo".to(typeFormat)
                    ));
                }
            }).run()
        );
    }
}
