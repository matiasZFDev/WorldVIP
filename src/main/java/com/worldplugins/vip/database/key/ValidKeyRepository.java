package com.worldplugins.vip.database.key;

import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ValidKeyRepository {
    @NonNull CompletableFuture<Collection<ValidVipKey>> getKeys(@NonNull String generatorName);
    @NonNull CompletableFuture<ValidVipKey> getKeyByCode(@NonNull String code);
    void consumeKey(@NonNull ValidVipKey key);
    void addKey(@NonNull ValidVipKey key);
    void removeKey(@NonNull ValidVipKey key);
}
