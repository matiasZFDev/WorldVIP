package com.worldplugins.vip.database.player.model;

import lombok.NonNull;

public class OwningVIP extends VIP {
    public OwningVIP(byte id, @NonNull VipType type, int duration) {
        super(id, type, duration);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof OwningVIP))
            return false;

        return getId() == ((OwningVIP) other).getId() && getType() == ((OwningVIP) other).getType();
    }
}
