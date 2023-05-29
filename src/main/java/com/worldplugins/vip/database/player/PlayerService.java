package com.worldplugins.vip.database.player;

import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PlayerService {
    @Nullable VipPlayer getById(@NotNull UUID playerId);

    void addSpent(@NotNull UUID playerId, double value);

    void setVip(@NotNull UUID playerId, @NotNull VIP vip);

    void removeVip(@NotNull UUID playerId);

    void updatePrimaryVip(@NotNull Collection<VipPlayer> players);

    void addOwningVip(@NotNull UUID playerId, @NotNull VIP vip);

    void removeOwningVip(@NotNull UUID playerId, @NotNull OwningVIP owningVip);

    void updateOwningVips(@NotNull Map<UUID, Collection<OwningVIP>> vips);
}
