package com.worldplugins.vip.database.items;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface VipItemsRepository {
    @NotNull CompletableFuture<Collection<VipItems>> getItems(@NotNull UUID playerId);
    void addItems(@NotNull VipItems items);
    void removeItems(@NotNull UUID playerId, byte vipId, short amount);
}
