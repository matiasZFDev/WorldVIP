package com.worldplugins.vip.database.pending;

import com.worldplugins.vip.database.player.model.VipType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class PendingVIP {
    private final @NonNull String playerName;
    private final byte id;
    private final @NonNull VipType type;
    private final int duration;
}
