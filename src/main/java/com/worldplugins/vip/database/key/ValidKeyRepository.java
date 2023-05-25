package com.worldplugins.vip.database.key;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ValidKeyRepository {
    @NotNull CompletableFuture<List<ValidVipKey>> getKeys(@NotNull String generatorName);
    @NotNull CompletableFuture<ValidVipKey> getKeyByCode(@NotNull String code);
    void consumeKey(@NotNull ValidVipKey key);
    void addKey(@NotNull ValidVipKey key);
    void removeKey(@NotNull ValidVipKey key);
}
