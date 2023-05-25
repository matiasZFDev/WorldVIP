package com.worldplugins.vip.database.pending;

import com.worldplugins.vip.database.player.model.VipType;
import org.jetbrains.annotations.NotNull;

public class PendingVIP {
    private final @NotNull String playerName;
    private final byte id;
    private final @NotNull VipType type;
    private final int duration;

    public PendingVIP(@NotNull String playerName, byte id, @NotNull VipType type, int duration) {
        this.playerName = playerName;
        this.id = id;
        this.type = type;
        this.duration = duration;
    }

    public @NotNull String playerName() {
        return playerName;
    }

    public byte id() {
        return id;
    }

    public @NotNull VipType type() {
        return type;
    }

    public int duration() {
        return duration;
    }
}
