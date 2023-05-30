package com.worldplugins.vip.command.vip;

import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.handler.OwningVipHandler;
import com.worldplugins.vip.handler.VipHandler;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class RemoveVip implements CommandModule {
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull PlayerService playerService;
    private final @NotNull VipHandler vipHandler;
    private final @NotNull OwningVipHandler owningVipHandler;

    public RemoveVip(
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull PlayerService playerService,
        @NotNull VipHandler vipHandler,
        @NotNull OwningVipHandler owningVipHandler
    ) {
        this.vipConfig = vipConfig;
        this.playerService = playerService;
        this.vipHandler = vipHandler;
        this.owningVipHandler = owningVipHandler;
    }

    @Command(name = "removervip")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldvip.removervip")) {
            respond(sender, "Remover-vip-permissoes");
            return;
        }

        if (!(args.length > 0 && args.length < 4)) {
            respond(sender, "Remover-vip-uso");
            return;
        }

        final String playerName = args[0];
        final Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            respond(sender, "Jogador-offline", message -> message.replace(
                to("@jogador", playerName)
            ));
            return;
        }

        if (args.length == 1) {
            final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());

            if (vipPlayer == null || vipPlayer.activeVip() == null) {
                respond(sender, "Remover-vip-inexistente", message -> message.replace(
                    to("@jogador", playerName)
                ));
                return;
            }

            vipHandler.remove(vipPlayer);
            respond(sender, "Vip-primario-removido", message -> message.replace(
                to("@jogador", player.getName())
            ));
            return;
        }

        final String vipName = args[1];
        final VipData.VIP configVip = vipConfig.data().getByName(vipName);

        if (configVip == null) {
            final Collection<String> vipList = vipConfig.data().all().stream()
                .map(VipData.VIP::name)
                .collect(Collectors.toList());

            respond(sender, "Vip-inexistente", message -> message.replace(
                to("@nome", vipName),
                to("@vips", vipList.toString())
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
                respond(sender, "Tipo-vip-inexistente", message -> message.replace(
                    to("@nome", args[2]),
                    to("@tipos", typeList.toString())
                ));
                return;
            }
        } else {
            type = null;
        }

        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());

        if (vipPlayer == null) {
            respond(sender, "Remover-vip-inexistente", message -> message.replace(
                to("@jogador", playerName)
            ));
            return;
        }

        final AtomicInteger owningVipRemoveCount = new AtomicInteger(0);
        final Collection<OwningVIP> owningVips = new ArrayList<>(vipPlayer.owningVips().vips());

        owningVips.forEach(owningVip -> {
            if (owningVip.id() == configVip.id() && (type == null || owningVip.type() == type)) {
                owningVipHandler.remove(vipPlayer, owningVip);
                owningVipRemoveCount.incrementAndGet();
            }
        });

        final VIP primaryVip = vipPlayer.activeVip();
        final boolean removePrimaryVip =
            primaryVip != null &&
            primaryVip.id() == configVip.id() &&
            (type == null || primaryVip.type() == type);
        final String typeFormat = type == null
            ? "-/-"
            : type.getName().toUpperCase();

        if (removePrimaryVip) {
            vipHandler.remove(vipPlayer);
            respond(sender, "Vip-primario-removido", message -> message.replace(
                to("@jogador", player.getName())
            ));

            if (owningVipRemoveCount.get() > 0) {
                respond(sender, "Vip-secundarios-removidos", message -> message.replace(
                    to("@jogador", player.getName()),
                    to("@removidos", String.valueOf(owningVipRemoveCount.get())),
                    to("@vip", configVip.display()),
                    to("@tipo", typeFormat)
                ));
            }
        } else {
            if (owningVipRemoveCount.get() == 0) {
                respond(sender, "Vip-removidos-nenhum", message -> message.replace(
                    to("@jogador", player.getName()),
                    to("@vip", configVip.display()),
                    to("@tipo", typeFormat)
                ));
                return;
            }

            respond(sender, "Vip-secundarios-removidos", message -> message.replace(
                to("@jogador", player.getName()),
                to("@removidos", String.valueOf(owningVipRemoveCount.get())),
                to("@vip", configVip.display()),
                to("@tipo", typeFormat)
            ));
        }
    }
}
