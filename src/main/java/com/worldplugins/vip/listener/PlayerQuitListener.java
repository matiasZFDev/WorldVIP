package com.worldplugins.vip.listener;

import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.vip.database.CacheUnloader;
import com.worldplugins.vip.database.player.model.VipPlayer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerQuitListener implements Listener {
    private final @NonNull Cache<UUID, VipPlayer> cache;
    private final @NonNull CacheUnloader<UUID> cacheUnloader;
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        if (!cache.containsKey(event.getPlayer().getUniqueId())) {
            return;
        }

        cacheUnloader.prepareUnload(event.getPlayer().getUniqueId());
    }
}
