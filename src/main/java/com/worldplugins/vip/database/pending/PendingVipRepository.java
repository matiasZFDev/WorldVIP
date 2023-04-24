package com.worldplugins.vip.database.pending;

import lombok.NonNull;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public interface PendingVipRepository {
    @NonNull CompletableFuture<Collection<PendingVIP>> getPendingVips(@NonNull String playerName);
    void addPending(@NonNull PendingVIP vip);
    void removePendings(@NonNull String playerName);
}
