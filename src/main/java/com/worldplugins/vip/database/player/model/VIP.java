package com.worldplugins.vip.database.player.model;

import org.jetbrains.annotations.NotNull;

public class VIP {
    private final byte id;
    private final @NotNull VipType type;
    private int duration;
    private boolean updated;

    public VIP(byte id, @NotNull VipType type, int duration) {
        this.id = id;
        this.type = type;
        this.duration = duration;
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

    public void decrementDuration(int time) {
        updated = true;
        duration -= time;
    }

    public void incrementDuration(int time) {
        duration += time;
    }

    public boolean updated() {
        return updated;
    }

    public void resetUpdated() {
        this.updated = false;
    }
}
