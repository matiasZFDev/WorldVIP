package com.worldplugins.vip.handler;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.manager.PermissionManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class OwningVipHandler {
    private final @NonNull PlayerService playerService;
    private final @NonNull PermissionManager permissionManager;
    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull ConfigCache<MainData> mainConfig;

    public void remove(@NonNull VipPlayer vipPlayer, @NonNull OwningVIP owningVip) {
        playerService.removeOwningVip(vipPlayer.getId(), owningVip);

        if (!mainConfig.data().stackVips()) {
            return;
        }

        final boolean existingSameVipCategory = vipPlayer.getOwningVips().getVips()
            .stream()
            .anyMatch(vip -> vip.getId() == owningVip.getId());

        if (existingSameVipCategory) {
            return;
        }

        final VipData.VIP configVip = vipConfig.data().getById(owningVip.getId());
        permissionManager.removeGroup(vipPlayer.getId(), configVip.getGroup());
    }
}
