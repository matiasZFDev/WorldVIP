package com.worldplugins.vip.handler;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.manager.PermissionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.entity.Player;

@RequiredArgsConstructor
public class VipRemovalHandler {
    private final @NonNull PlayerService playerService;
    private final @NonNull VipActivationHandler activationHandler;
    private final @NonNull PermissionManager permissionManager;
    private final @NonNull ConfigCache<VipData> vipConfig;

    public void removePrimary(@NonNull Player player, @NonNull VipPlayer vipPlayer) {
        final VipData.VIP configVip = vipConfig.data().getById(vipPlayer.getActiveVip().getId());

        permissionManager.removeGroup(player, configVip.getGroup());
        playerService.removeVip(player.getUniqueId());

        final OwningVIP primaryReplace = pickPrimaryReplacement(vipPlayer);

        if (primaryReplace == null) {
            return;
        }

        activationHandler.activate(player, primaryReplace, false);
        playerService.removeOwningVip(player.getUniqueId(), primaryReplace);
    }

    private OwningVIP pickPrimaryReplacement(@NonNull VipPlayer vipPlayer) {
        return vipPlayer.getOwningVips().getVips().stream()
            .findAny()
            .orElse(null);
    }

    public void removeOwningVip(
        @NonNull Player player,
        @NonNull VipPlayer vipPlayer,
        @NonNull OwningVIP owningVip
    ) {
        playerService.removeOwningVip(player.getUniqueId(), owningVip);

        final boolean existingSameVipCategory = vipPlayer.getOwningVips().getVips()
            .stream()
            .anyMatch(vip -> vip.getId() == owningVip.getId());

        if (existingSameVipCategory) {
            return;
        }

        final VipData.VIP configVip = vipConfig.data().getById(owningVip.getId());
        permissionManager.removeGroup(player, configVip.getGroup());
    }
}
