package com.worldplugins.vip.database.key;

import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ValidKeyRepository {
    @NonNull CompletableFuture<Collection<ValidVipKey>> getKeys(@NonNull UUID generatorId);
    @NonNull CompletableFuture<ValidVipKey> getKeyByCode(@NonNull String code);
    void consumeKey(@NonNull String code);
    void addKey(@NonNull ValidVipKey key);
    void removeKey(@NonNull String code);
}
