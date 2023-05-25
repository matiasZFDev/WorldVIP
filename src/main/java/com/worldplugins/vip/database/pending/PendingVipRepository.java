package com.worldplugins.vip.database.pending;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface PendingVipRepository {
    @NotNull CompletableFuture<Collection<PendingVIP>> getPendingVips(@NotNull String playerName);
    void addPending(@NotNull PendingVIP vip);
    void removePendings(@NotNull String playerName);
}
