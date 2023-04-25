package com.worldplugins.vip.key;

import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.player.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KeyStorageHandler {
    private final @NonNull PlayerService playerService;
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;

    public void store(@NonNull ValidVipKey key) {
        validKeyRepository.addKey(key);

        if (key.getGeneratorId() != null) {
            playerService.getById(key.getGeneratorId()).thenAccept(vipPlayer ->
                scheduler.newTask(() -> vipPlayer.getKeys().add(key)).run()
            );
        }
    }
}
