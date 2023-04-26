package com.worldplugins.vip.database.player.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class OwningVIPs {
    @Getter
    private final @NonNull List<OwningVIP> vips;

    public OwningVIP get(byte id, @NonNull VipType type) {
        return vips.stream()
            .filter(owningVip -> owningVip.getId() == id && owningVip.getType() == type)
            .findFirst()
            .orElse(null);
    }

    public @NonNull OwningVIP add(@NonNull VIP vip) {
        final OwningVIP owningVip  = new OwningVIP(vip.getId(), vip.getType(), vip.getDuration());
        vips.add(owningVip);
        return owningVip;
    }

    public boolean remove(byte id, @NonNull VipType type) {
        final OwningVIP owningVip =  vips.stream()
            .filter(current -> current.getId() == id && current.getType() == type)
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
