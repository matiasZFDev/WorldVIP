package com.worldplugins.vip.database.player.model;

import lombok.Getter;
import lombok.NonNull;

public class OwningVIP extends VIP {
    @Getter
    private final short owningId;

    public OwningVIP(byte id, @NonNull VipType type, int duration, short owningId) {
        super(id, type, duration);
        this.owningId = owningId;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof OwningVIP))
            return false;

        return owningId == ((OwningVIP) other).owningId;
    }
}
