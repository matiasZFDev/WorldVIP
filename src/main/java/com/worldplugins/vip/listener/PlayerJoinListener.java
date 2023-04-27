package com.worldplugins.vip.listener;

import com.worldplugins.vip.database.CacheUnloader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

@RequiredArgsConstructor
public class PlayerJoinListener implements Listener {
    private final @NonNull CacheUnloader<UUID> cacheUnloader;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        cacheUnloader.cancel(event.getPlayer().getUniqueId());
    }
}
