package com.worldplugins.vip.task;

import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.database.cache.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VipDatabaseUpdate implements Runnable {
    private final @NotNull Cache<UUID, VipPlayer> playersCache;
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<MainData> mainConfig;

    public VipDatabaseUpdate(
        @NotNull Cache<UUID, VipPlayer> playersCache,
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.playersCache = playersCache;
        this.playerService = playerService;
        this.mainConfig = mainConfig;
    }

    @Override
    public void run() {
        Collection<VipPlayer> updatePrimaryVips = new ArrayList<>(20);
        Map<UUID, Collection<OwningVIP>> updateOwningVips = new HashMap<>(15);

        if (mainConfig.data().simultaneousReduction()) {
            playersCache.getValues().forEach(player -> {
                if (player.activeVip() != null && player.activeVip().updated()) {
                    updatePrimaryVips.add(player);
                    player.activeVip().resetUpdated();
                }

                player.owningVips().vips().forEach(owningVip -> {
                    if (owningVip.updated()) {
                        updateOwningVips
                            .computeIfAbsent(player.id(), $ -> new ArrayList<>(1))
                            .add(owningVip);
                        owningVip.resetUpdated();
                    }
                });
            });
        } else {
            playersCache.getValues().forEach(player -> {
                if (player.activeVip() != null && player.activeVip().updated()) {
                    updatePrimaryVips.add(player);
                    player.activeVip().resetUpdated();
                }
            });
        }

        playerService.updatePrimaryVip(updatePrimaryVips);
        playerService.updateOwningVips(updateOwningVips);
    }
}
