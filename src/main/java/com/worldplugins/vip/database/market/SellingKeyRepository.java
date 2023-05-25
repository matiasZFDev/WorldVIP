package com.worldplugins.vip.database.market;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface SellingKeyRepository {
    @NotNull CompletableFuture<List<SellingKey>> getAllKeys();
    void addKey(@NotNull SellingKey key);
    void removeKey(@NotNull SellingKey key);
}
