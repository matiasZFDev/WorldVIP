package com.worldplugins.vip.database.player.model;

import lombok.Getter;
import lombok.NonNull;

public class VIP {
    @Getter
    private final byte id;
    @Getter
    private final @NonNull VipType type;
    @Getter
    private int duration;
    private boolean updated;

    public VIP(byte id, @NonNull VipType type, int duration) {
        this.id = id;
        this.type = type;
        this.duration = duration;
    }

    public void decrementDuration(int time) {
        updated = true;
        duration -= time;
    }

    public boolean updated() {
        return updated;
    }

    public void resetUpdated() {
        this.updated = false;
    }
}
