package com.worldplugins.vip.handler;

import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.manager.PermissionManager;
import me.post.lib.config.model.ConfigModel;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class OwningVipHandler {
    private final @NotNull PlayerService playerService;
    private final @NotNull PermissionManager permissionManager;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;

    public OwningVipHandler(
        @NotNull PlayerService playerService,
        @NotNull PermissionManager permissionManager,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.playerService = playerService;
        this.permissionManager = permissionManager;
        this.vipConfig = vipConfig;
        this.mainConfig = mainConfig;
    }

    public void add(@NotNull UUID playerId, @NotNull OwningVIP owningVip) {
        final VipPlayer vipPlayer = playerService.getById(playerId);

        if (vipPlayer == null) {
            return;
        }

        final VipData.VIP configVip = vipConfig.data().getById(owningVip.id());
        playerService.addOwningVip(vipPlayer.id(), owningVip);

        if (mainConfig.data().stackVips()) {
            permissionManager.addGroup(vipPlayer.id(), configVip.group());
        }
    }

    public void remove(@NotNull VipPlayer vipPlayer, @NotNull OwningVIP owningVip) {
        playerService.removeOwningVip(vipPlayer.id(), owningVip);

        if (!mainConfig.data().stackVips()) {
            return;
        }

        final boolean existingSameVipCategory = vipPlayer.owningVips().vips().stream()
            .anyMatch(vip -> vip.id() == owningVip.id());

        if (existingSameVipCategory) {
            return;
        }

        final VipData.VIP configVip = vipConfig.data().getById(owningVip.id());

        permissionManager.removeGroup(vipPlayer.id(), configVip.group());
    }
}
