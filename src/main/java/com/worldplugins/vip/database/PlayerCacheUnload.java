package com.worldplugins.vip.database;

import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.util.ExpiringCollection;
import lombok.NonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerCacheUnload implements CacheUnloader<UUID> {
    private final @NonNull ExpiringCollection<UUID> unloadCountdown;

    public PlayerCacheUnload(
        @NonNull SchedulerBuilder scheduler,
        @NonNull Cache<UUID, VipPlayer> cache,
        @NonNull PlayerService playerService
    ) {
        unloadCountdown = new ExpiringCollection<>(
            scheduler,
            60,
            false,
            playerId -> {
                final VipPlayer vipPlayer = cache.get(playerId);
                cache.remove(playerId);

                if (vipPlayer.getActiveVip() != null && vipPlayer.getActiveVip().updated()) {
                    playerService.updatePrimaryVip(Collections.singleton(vipPlayer));
                }

                final Collection<OwningVIP> updatedOwningVips = vipPlayer.getOwningVips().getVips().stream()
                    .filter(OwningVIP::updated)
                    .collect(Collectors.toList());

                if (!updatedOwningVips.isEmpty()) {
                    playerService.updateOwningVips(Collections.singletonMap(playerId, updatedOwningVips));
                }
            }
        );
    }

    @Override
    public void prepareUnload(@NonNull UUID playerId) {
        unloadCountdown.add(playerId);
    }

    public void cancel(@NonNull UUID playerId) {
        unloadCountdown.remove(playerId);
    }
}
