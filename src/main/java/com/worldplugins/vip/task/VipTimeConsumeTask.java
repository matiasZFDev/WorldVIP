package com.worldplugins.vip.task;

import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.handler.OwningVipHandler;
import com.worldplugins.vip.handler.VipHandler;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.database.cache.Cache;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class VipTimeConsumeTask implements Runnable {
    private final @NotNull Cache<UUID, VipPlayer> cache;
    private final @NotNull VipHandler vipHandler;
    private final @NotNull OwningVipHandler owningVipHandler;
    private final @NotNull ConfigModel<MainData> mainConfig;

    private final @NotNull Collection<VipPlayer> updatePlayers;
    private final @NotNull Map<UUID, Collection<OwningVIP>> updateOwningVips;

    public VipTimeConsumeTask(
        @NotNull Cache<UUID, VipPlayer> cache,
        @NotNull VipHandler vipHandler,
        @NotNull OwningVipHandler owningVipHandler,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.cache = cache;
        this.vipHandler = vipHandler;
        this.owningVipHandler = owningVipHandler;
        this.mainConfig = mainConfig;
        this.updatePlayers = new ArrayList<>(10);
        this.updateOwningVips = new HashMap<>(0);
    }

    @Override
    public void run() {
        cache.getValues().forEach(vipPlayer -> {
            final VIP activeVip = vipPlayer.activeVip();

            if (activeVip != null && activeVip.type() != VipType.PERMANENT) {
                if (!(activeVip.type() == VipType.ONLINE && Bukkit.getPlayer(vipPlayer.id()) == null)) {
                    activeVip.decrementDuration(1);

                    if (activeVip.duration() == -1) {
                        updatePlayers.add(vipPlayer);
                    }
                }
            }

            if (!mainConfig.data().simultaneousReduction()) {
                return;
            }

            vipPlayer.owningVips().vips().forEach(owningVip -> {
                if (owningVip.type() == VipType.PERMANENT) {
                    return;
                }

                if (!(owningVip.type() == VipType.ONLINE && Bukkit.getPlayer(vipPlayer.id()) == null)) {
                    owningVip.decrementDuration(1);

                    if (owningVip.duration() == -1) {
                        updateOwningVips
                            .computeIfAbsent(vipPlayer.id(), $ -> new ArrayList<>(1))
                            .add(owningVip);
                    }
                }
            });
        });

        updateOwningVips.forEach((playerId, vips) -> {
            final VipPlayer vipPlayer = cache.get(playerId);
            vips.forEach(owningVip -> owningVipHandler.remove(vipPlayer, owningVip));
        });
        updatePlayers.forEach(vipHandler::remove);

        updatePlayers.clear();
        updateOwningVips.clear();
    }
}
