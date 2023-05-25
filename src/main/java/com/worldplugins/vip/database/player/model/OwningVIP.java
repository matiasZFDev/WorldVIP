package com.worldplugins.vip.database.player.model;

import org.jetbrains.annotations.NotNull;

public class OwningVIP extends VIP {
    public OwningVIP(byte id, @NotNull VipType type, int duration) {
        super(id, type, duration);
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof OwningVIP))
            return false;

        return id() == ((OwningVIP) other).id() && type() == ((OwningVIP) other).type();
    }
}
