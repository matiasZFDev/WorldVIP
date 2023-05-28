package com.worldplugins.vip.handler;

import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.data.VipItemsData;
import com.worldplugins.vip.database.items.VipItems;
import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.*;
import com.worldplugins.vip.manager.PermissionManager;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.Players;
import me.post.lib.util.Time;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class VipHandler {
    private final @NotNull PlayerService playerService;
    private final @NotNull VipItemsRepository vipItemsRepository;
    private final @NotNull PermissionManager permissionManager;
    private final @NotNull OwningVipHandler owningVipHandler;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<VipItemsData> vipItemsConfig;

    public VipHandler(
        @NotNull PlayerService playerService,
        @NotNull VipItemsRepository vipItemsRepository,
        @NotNull PermissionManager permissionManager,
        @NotNull OwningVipHandler owningVipHandler,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ConfigModel<VipItemsData> vipItemsConfig
    ) {
        this.playerService = playerService;
        this.vipItemsRepository = vipItemsRepository;
        this.permissionManager = permissionManager;
        this.owningVipHandler = owningVipHandler;
        this.vipConfig = vipConfig;
        this.mainConfig = mainConfig;
        this.vipItemsConfig = vipItemsConfig;
    }

    public void activate(
        @NotNull UUID playerId,
        @NotNull VIP vip,
        boolean announceAndBenefits
    ) {
        final VipPlayer vipPlayer = playerService.getById(playerId);

        if (vipPlayer == null) {
            setVip(playerId, vip, announceAndBenefits);
            return;
        }

        if (vipPlayer.activeVip() == null) {
            setVip(vipPlayer.id(), vip, announceAndBenefits);
            return;
        }

        if (vip.type() == VipType.PERMANENT) {
            switchVips(vipPlayer, vip);
            return;
        }

        final OwningVIP matchingVip = vipPlayer.owningVips().vips().stream()
            .filter(owningVip -> owningVip.id() == vip.id() && owningVip.type() == vip.type())
            .findFirst()
            .orElse(null);

        if (matchingVip == null) {
            switchVips(vipPlayer, vip);
        } else {
            mergeAndSwitchVips(vipPlayer, vip, matchingVip);
        }
    }

    public void remove(@NotNull VipPlayer vipPlayer) {
        final VIP activeVip = vipPlayer.activeVip();

        if (activeVip == null) {
            return;
        }

        final VipData.VIP configVip = vipConfig.data().getById(activeVip.id());

        permissionManager.removeGroup(vipPlayer.id(), configVip.group());
        playerService.removeVip(vipPlayer.id());

        final OwningVIP primaryReplace = pickPrimaryReplacement(vipPlayer);

        if (primaryReplace == null) {
            return;
        }

        activate(vipPlayer.id(), primaryReplace, false);
        playerService.removeOwningVip(vipPlayer.id(), primaryReplace);
    }

    private OwningVIP pickPrimaryReplacement(@NotNull VipPlayer vipPlayer) {
        return vipPlayer.owningVips().vips().stream()
            .findAny()
            .orElse(null);
    }

    private void setVip(@NotNull UUID playerId, @NotNull VIP vip, boolean announceAndBenefits) {
        final VipPlayer vipPlayer = playerService.getById(playerId);
        final VIP oldVip = vipPlayer == null ? null : vipPlayer.activeVip();

        playerService.setVip(playerId, vip);
        setGroup(playerId, oldVip, vip);

        if (announceAndBenefits) {
            final Player player = Bukkit.getPlayer(playerId);

            announce(player, vip);
            giveBenefits(player);
        }
    }

    private void setGroup(@NotNull UUID playerId, VIP oldVip, @NotNull VIP vip) {
        final VipData.VIP configVip = vipConfig.data().getById(vip.id());

        permissionManager.addGroup(playerId, configVip.group());

        if (!mainConfig.data().stackVips() && oldVip != null) {
            final VipData.VIP configOldVip = vipConfig.data().getById(oldVip.id());

            permissionManager.removeGroup(playerId, configOldVip.group());
        }
    }

    private void announce(@NotNull Player player, @NotNull VIP vip) {
        final VipData.VIP configVip = vipConfig.data().getById(vip.id());
        final String durationDisplay = vip.type() == VipType.PERMANENT
            ? GlobalValues.PERMANENT_DURATION
            : Time.toFormat(vip.duration());

        respond(player, "Vip-ativado", message -> message.replace(
            to("@jogador", player.getName()),
            to("@vip", configVip.display()),
            to("@tipo", vip.type().name().toUpperCase()),
            to("@tempo", durationDisplay)
        ));
    }

    private void giveBenefits(@NotNull Player player) {
        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());

        if (vipPlayer == null) {
            return;
        }

        final VIP activeVip = vipPlayer.activeVip();

        if (activeVip == null) {
            return;
        }

        final VipData.VIP configVip = vipConfig.data().getById(activeVip.id());

        configVip.activationCommands().forEach(command ->
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                command.replace("@jogador", player.getName())
            )
        );

        if (mainConfig.data().storeItems()) {
            final VipItems items = new VipItems(vipPlayer.id(), activeVip.id(), (short) 1);

            vipItemsRepository.addItems(items);
        } else {
            final VipItemsData.VipItems configItems = vipItemsConfig.data().getByName(configVip.name());
            final ItemStack[] itemsContent = configItems == null
                ? new ItemStack[0]
                : configItems.data();

            Players.giveItems(player, itemsContent);
        }

        final int vipDaysDuration = (int) TimeUnit.SECONDS.toDays(activeVip.duration());
        final Double vipPrice = configVip.pricing().getPrice(vipDaysDuration);

        if (vipPrice == null || vipPrice == 0) {
            return;
        }

        playerService.addSpent(player.getUniqueId(), vipPrice);
    }

    private void switchVips(@NotNull VipPlayer vipPlayer, @NotNull VIP vip) {
        final VIP currentActiveVip = vipPlayer.activeVip();

        if (currentActiveVip == null) {
            return;
        }

        final VipData.VIP owningConfigVip = vipConfig.data().getById(currentActiveVip.id());

        setVip(vipPlayer.id(), vip, true);
        playerService.addOwningVip(vipPlayer.id(), currentActiveVip);

        if (!mainConfig.data().stackVips()) {
            permissionManager.removeGroup(vipPlayer.id(), owningConfigVip.group());
        }
    }

    private void mergeAndSwitchVips(
        @NotNull VipPlayer vipPlayer,
        @NotNull VIP primaryVip,
        @NotNull OwningVIP matchingVip
    ) {
        final int newDuration = primaryVip.duration() + matchingVip.duration();
        final VIP mergedVip = new VIP(primaryVip.id(), primaryVip.type(), newDuration);

        owningVipHandler.remove(vipPlayer, matchingVip);
        switchVips(vipPlayer, mergedVip);
    }
}
