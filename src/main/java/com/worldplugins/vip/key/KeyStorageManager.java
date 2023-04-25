package com.worldplugins.vip.key;

import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.player.PlayerService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class KeyStorageManager {
    private final @NonNull PlayerService playerService;
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;

    /**
     * Firstable, it checks the keys in the requester cache, then in the actual
     * storage.
     * */
    public @NonNull CompletableFuture<ValidVipKey> get(UUID requesterId, @NonNull String code) {
        return requesterId != null
            ? playerService.getById(requesterId)
                .thenApply(vipPlayer ->
                    vipPlayer.getKeys().all().stream()
                        .filter(key -> key.getCode().equals(code))
                        .findFirst()
                        .orElse(null)
                )
                .thenCompose(key ->
                    key != null
                        ? CompletableFuture.completedFuture(key)
                        : validKeyRepository.getKeyByCode(code)
                )
            : validKeyRepository.getKeyByCode(code);
    }

    public void store(@NonNull ValidVipKey key) {
        validKeyRepository.addKey(key);

        if (key.getGeneratorId() != null) {
            playerService.getById(key.getGeneratorId()).thenAccept(vipPlayer ->
                scheduler.newTask(() -> vipPlayer.getKeys().add(key)).run()
            );
        }
    }

    public void remove(@NonNull ValidVipKey key) {
        validKeyRepository.removeKey(key.getCode());

        if (key.getGeneratorId() != null) {
            playerService.getById(key.getGeneratorId()).thenAccept(vipPlayer ->
                scheduler.newTask(() -> vipPlayer.getKeys().remove(key.getCode())).run()
            );
        }
    }
}
