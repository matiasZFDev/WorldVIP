package com.worldplugins.vip.database.player;

import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import lombok.NonNull;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public interface PlayerService {
    VipPlayer getById(@NonNull UUID playerId);
    void addSpent(@NonNull UUID playerId, double value);
    void setVip(@NonNull UUID playerId, @NonNull VIP vip);
    void removeVip(@NonNull UUID playerId);
    void updatePrimaryVip(@NonNull Collection<VipPlayer> players);
    void addOwningVip(@NonNull UUID playerId, @NonNull VIP vip);
    void removeOwningVip(@NonNull UUID playerId, @NonNull OwningVIP owningVip);
    void updateOwningVips(@NonNull Map<UUID, Collection<OwningVIP>> vips);
}
