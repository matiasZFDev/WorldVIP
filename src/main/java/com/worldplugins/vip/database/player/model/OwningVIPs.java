package com.worldplugins.vip.database.player.model;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OwningVIPs {
    private final @NotNull List<OwningVIP> vips;

    public OwningVIPs(@NotNull List<OwningVIP> vips) {
        this.vips = vips;
    }

    public @NotNull List<OwningVIP> vips() {
        return vips;
    }

    public OwningVIP get(byte id, @NotNull VipType type) {
        return vips.stream()
            .filter(owningVip -> owningVip.id() == id && owningVip.type() == type)
            .findFirst()
            .orElse(null);
    }

    public @NotNull OwningVIP add(@NotNull VIP vip) {
        final OwningVIP owningVip  = new OwningVIP(vip.id(), vip.type(), vip.duration());
        vips.add(owningVip);
        return owningVip;
    }

    public boolean remove(byte id, @NotNull VipType type) {
        final OwningVIP owningVip =  vips.stream()
            .filter(current -> current.id() == id && current.type() == type)
            .findFirst()
            .orElse(null);

        if (owningVip == null) {
            return false;
        }

        vips.remove(owningVip);
        return true;
    }

    public boolean updated() {
        return vips.stream().anyMatch(VIP::updated);
    }
}
