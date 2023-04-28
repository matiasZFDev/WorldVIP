package com.worldplugins.vip.task;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.handler.OwningVipHandler;
import com.worldplugins.vip.handler.VipHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;

import java.util.UUID;

@RequiredArgsConstructor
public class VipTimeConsumeTask implements Runnable {
    private final @NonNull Cache<UUID, VipPlayer> cache;
    private final @NonNull VipHandler vipHandler;
    private final @NonNull OwningVipHandler owningVipHandler;

    private final @NonNull ConfigCache<MainData> mainConfig;

    @Override
    public void run() {
        cache.getValues().forEach(vipPlayer -> {
            final VIP activeVip = vipPlayer.getActiveVip();

            if (activeVip != null && activeVip.getType() != VipType.PERMANENT) {
                if (!(activeVip.getType() == VipType.ONLINE && Bukkit.getPlayer(vipPlayer.getId()) == null)) {
                    activeVip.decrementDuration(1);

                    if (activeVip.getDuration() == -1) {
                        vipHandler.remove(vipPlayer);
                    }
                }
            }

            if (!mainConfig.data().simultaneousReduction()) {
                return;
            }

            vipPlayer.getOwningVips().getVips().forEach(owningVip -> {
                if (owningVip.getType() == VipType.PERMANENT) {
                    return;
                }

                if (!(owningVip.getType() == VipType.ONLINE && Bukkit.getPlayer(vipPlayer.getId()) == null)) {
                    owningVip.decrementDuration(1);

                    if (owningVip.getDuration() == -1) {
                        owningVipHandler.remove(vipPlayer, owningVip);
                    }
                }
            });
        });
    }
}
