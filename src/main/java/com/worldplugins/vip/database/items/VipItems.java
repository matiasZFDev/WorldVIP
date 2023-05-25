package com.worldplugins.vip.database.items;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class VipItems {
    private final @NotNull UUID playerId;
    private final byte vipId;
    private short amount;

    public VipItems(@NotNull UUID playerId, byte vipId, short amount) {
        this.playerId = playerId;
        this.vipId = vipId;
        this.amount = amount;
    }

    public UUID playerId() {
        return playerId;
    }

    public byte vipId() {
        return vipId;
    }

    public short amount() {
        return amount;
    }

    public void setAmount(short amount) {
        this.amount = amount;
    }
}
