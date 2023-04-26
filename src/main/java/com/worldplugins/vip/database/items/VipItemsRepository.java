package com.worldplugins.vip.database.items;

import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface VipItemsRepository {
    @NonNull CompletableFuture<Collection<VipItems>> getItems(@NonNull UUID playerId);
    void addItems(@NonNull VipItems items);
    void removeItems(@NonNull UUID playerId, byte vipId, short amount);
}
