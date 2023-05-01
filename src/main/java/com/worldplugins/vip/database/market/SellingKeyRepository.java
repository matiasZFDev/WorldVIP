package com.worldplugins.vip.database.market;

import lombok.NonNull;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface SellingKeyRepository {
    @NonNull CompletableFuture<Collection<SellingKey>> getAllKeys();
    void addKey(@NonNull SellingKey key);
    void removeKey(@NonNull SellingKey key);
}
