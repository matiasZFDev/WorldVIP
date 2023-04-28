package com.worldplugins.vip.handler;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.TimeExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.data.VipItemsData;
import com.worldplugins.vip.database.items.VipItems;
import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.*;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.manager.PermissionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    TimeExtensions.class,
    PlayerExtensions.class
})

@RequiredArgsConstructor
public class VipHandler {
    private final @NonNull PlayerService playerService;
    private final @NonNull VipItemsRepository vipItemsRepository;
    private final @NonNull PermissionManager permissionManager;
    private final @NonNull OwningVipHandler owningVipHandler;

    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull ConfigCache<MainData> mainConfig;
    private final @NonNull ConfigCache<VipItemsData> vipItemsConfig;

    public void activate(@NonNull UUID playerId, @NonNull VIP vip, boolean announceAndBenefits) {
        final VipPlayer vipPlayer = playerService.getById(playerId);

        if (vipPlayer == null) {
            final VipPlayer newVipPlayer = new VipPlayer(
                playerId,
                null,
                new OwningVIPs(new ArrayList<>(0))
            );
            setVip(newVipPlayer, vip, announceAndBenefits);
            return;
        }

        if (vipPlayer.getActiveVip() == null) {
            setVip(vipPlayer, vip, announceAndBenefits);
            return;
        }

        if (vip.getType() == VipType.PERMANENT) {
            switchVips(vipPlayer, vip);
        } else {
            final OwningVIP matchingVip = vipPlayer.getOwningVips().getVips().stream()
                .filter(owningVip ->
                    owningVip.getId() == vip.getId() && owningVip.getType() == vip.getType()
                )
                .findFirst()
                .orElse(null);

            if (matchingVip == null) {
                switchVips(vipPlayer, vip);
            } else {
                mergeAndSwitchVips(vipPlayer, vip, matchingVip);
            }
        }
    }

    public void remove(@NonNull VipPlayer vipPlayer) {
        final VipData.VIP configVip = vipConfig.data().getById(vipPlayer.getActiveVip().getId());

        permissionManager.removeGroup(vipPlayer.getId(), configVip.getGroup());
        playerService.removeVip(vipPlayer.getId());

        final OwningVIP primaryReplace = pickPrimaryReplacement(vipPlayer);

        if (primaryReplace == null) {
            return;
        }

        activate(vipPlayer.getId(), primaryReplace, false);
        playerService.removeOwningVip(vipPlayer.getId(), primaryReplace);
    }

    private OwningVIP pickPrimaryReplacement(@NonNull VipPlayer vipPlayer) {
        return vipPlayer.getOwningVips().getVips().stream()
            .findAny()
            .orElse(null);
    }

    private void setVip(VipPlayer vipPlayer, @NonNull VIP vip, boolean announceAndBenefits) {
        final VIP oldVip = vipPlayer.getActiveVip();
        playerService.setVip(vipPlayer.getId(), vip);
        setGroup(vipPlayer.getId(), oldVip, vip);

        if (announceAndBenefits) {
            final Player player = Bukkit.getPlayer(vipPlayer.getId());
            announce(player, vip);
            giveBenefits(player, vipPlayer);
        }
    }

    private void setGroup(@NonNull UUID playerId, VIP oldVip, @NonNull VIP vip) {
        final VipData.VIP configVip = vipConfig.data().getById(vip.getId());
        permissionManager.addGroup(playerId, configVip.getGroup());

        if (!mainConfig.data().stackVips() && oldVip != null) {
            final VipData.VIP configOldVip = vipConfig.data().getById(oldVip.getId());
            permissionManager.removeGroup(playerId, configOldVip.getGroup());
        }
    }

    private void announce(@NonNull Player player, @NonNull VIP vip) {
        final VipData.VIP configVip = vipConfig.data().getById(vip.getId());
        final String durationDisplay = vip.getType() == VipType.PERMANENT
            ? GlobalValues.PERMANENT_DURATION
            : ((Integer) vip.getDuration()).toTime();

        player.respond("Vip-ativado", message -> message.replace(
            "@jogador".to(player.getName()),
            "@vip".to(configVip.getDisplay()),
            "@tipo".to(vip.getType().getName().toUpperCase()),
            "@tempo".to(durationDisplay)
        ));
    }

    private void giveBenefits(@NonNull Player player, @NonNull VipPlayer vipPlayer) {
        final VipData.VIP configVip = vipConfig.data().getById(vipPlayer.getActiveVip().getId());

        configVip.getActivationCommands().forEach(command ->
            Bukkit.dispatchCommand(
                Bukkit.getConsoleSender(),
                command.replace("@jogador", player.getName())
            )
        );

        if (mainConfig.data().storeItems()) {
            final VipItems items = new VipItems(
                vipPlayer.getId(), vipPlayer.getActiveVip().getId(), (short) 1
            );
            vipItemsRepository.addItems(items);
        } else {
            player.giveItems(vipItemsConfig.data().getByName(configVip.getName()).getData());
        }
    }

    private void switchVips(@NonNull VipPlayer vipPlayer, @NonNull VIP vip) {
        final VIP currentActiveVip = vipPlayer.getActiveVip();
        final VipData.VIP owningConfigVIp = vipConfig.data().getById(currentActiveVip.getId());
        setVip(vipPlayer, vip, true);
        playerService.addOwningVip(vipPlayer.getId(), currentActiveVip);

        if (!mainConfig.data().stackVips()) {
            permissionManager.removeGroup(vipPlayer.getId(), owningConfigVIp.getGroup());
        }
    }

    private void mergeAndSwitchVips(
        @NonNull VipPlayer vipPlayer,
        @NonNull VIP primaryVip,
        @NonNull OwningVIP matchingVip
    ) {
        final int newDuration = primaryVip.getDuration() + matchingVip.getDuration();
        final VIP mergedVip = new VIP(primaryVip.getId(), primaryVip.getType(), newDuration);
        owningVipHandler.remove(vipPlayer, matchingVip);
        switchVips(vipPlayer, mergedVip);
    }
}
