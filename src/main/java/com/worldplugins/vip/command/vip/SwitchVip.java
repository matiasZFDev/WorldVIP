package com.worldplugins.vip.command.vip;

import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.handler.VipHandler;
import com.worldplugins.vip.handler.OwningVipHandler;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.database.cache.Cache;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

import static com.worldplugins.vip.Response.respond;
import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class SwitchVip implements CommandModule {
    private final @NotNull PlayerService playerService;
    private final @NotNull Cache<UUID, VipPlayer> cache;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull OwningVipHandler owningVipHandler;
    private final @NotNull VipHandler vipHandler;

    private final @NotNull Map<UUID, Long> onDelay = new HashMap<>();

    public SwitchVip(
        @NotNull PlayerService playerService,
        @NotNull Cache<UUID, VipPlayer> cache,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull OwningVipHandler owningVipHandler,
        @NotNull VipHandler vipHandler
    ) {
        this.playerService = playerService;
        this.cache = cache;
        this.mainConfig = mainConfig;
        this.vipConfig = vipConfig;
        this.owningVipHandler = owningVipHandler;
        this.vipHandler = vipHandler;
    }

    @Command(name = "trocarvip")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            respond(sender, "Comando-jogador");
            return;
        }

        if (!(args.length > 0 && args.length < 3)) {
            respond(sender, "Trocar-vip-uso");
            return;
        }

        final Player player = (Player) sender;

        if (onDelay.containsKey(player.getUniqueId())) {
            final long now = System.nanoTime();
            final int secondsElapsed = (int) ((now - onDelay.get(player.getUniqueId())) / 1000);

            if (secondsElapsed < mainConfig.data().switchVipDelay()) {
                respond(player, "Trocar-vip-delay");
                return;
            }

            onDelay.remove(player.getUniqueId());
        }

        final VipData.VIP configVip = vipConfig.data().getByName(args[0]);

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

        final VipPlayer vipPlayer = cache.get(player.getUniqueId());

        if (vipPlayer == null || vipPlayer.activeVip() == null) {
            respond(player, "Trocar-vip-inexistente");
            return;
        }

        final VIP activeVip = requireNonNull(vipPlayer.activeVip());
        final VipType vipType;

        if (args.length == 1) {
            vipType = getGreatestType(vipPlayer);

            if (vipType == null) {
                respond(sender, "Trocar-vip-sem-vips", message -> message.replace(
                    to("@vip", configVip.display())
                ));
                return;
            }
        } else {
            vipType = VipType.fromName(args[1]);

            if (vipType == null) {
                final Collection<String> vipTypes = Arrays.stream(VipType.values())
                    .map(type -> type.getName().toUpperCase())
                    .collect(Collectors.toList());
                respond(sender, "Tipo-vip-inexistente", message -> message.replace(
                    to("@nome", args[1]),
                    to("@tipos", vipTypes.toString())
                ));
                return;
            }
        }

        final VipData.VIP oldConfigVip = vipConfig.data().getById(activeVip.id());
        final OwningVIP newPrimaryVip = vipPlayer.owningVips().get(configVip.id(), vipType);

        if (mainConfig.data().stackVips()) {
            playerService.removeOwningVip(player.getUniqueId(), newPrimaryVip);
            playerService.setVip(player.getUniqueId(), newPrimaryVip);
            playerService.addOwningVip(player.getUniqueId(), activeVip);
        } else {
            owningVipHandler.remove(vipPlayer, newPrimaryVip);
            vipHandler.activate(player.getUniqueId(), newPrimaryVip, false);
        }

        respond(player, "Vip-trocado", message -> message.replace(
            to("@atual-vip", oldConfigVip.display()),
            to("@atual-tipo", activeVip.type().getName().toUpperCase()),
            to("@novo-vip", configVip.display()),
            to("@novo-tipo", vipType.getName().toUpperCase())
        ));
    }

    private VipType getGreatestType(@NotNull VipPlayer vipPlayer) {
        return vipPlayer.owningVips().vips().stream()
            .max(Comparator.comparingInt(owningVip -> owningVip.type().priority()))
            .map(OwningVIP::type)
            .orElse(null);
    }
}
