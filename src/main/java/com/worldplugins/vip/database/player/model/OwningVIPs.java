package com.worldplugins.vip.database.player.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class OwningVIPs {
    @Getter
    private final @NonNull List<OwningVIP> vips;

    public OwningVIP get(short owningId) {
        return vips.stream()
            .filter(owningVip -> owningVip.getOwningId() == owningId)
            .findFirst()
            .orElse(null);
    }

    public @NonNull OwningVIP add(@NonNull VIP vip) {
        final short nextId = nextOwningId();
        final OwningVIP owningVip  = new OwningVIP(vip.getId(), vip.getType(), vip.getDuration(), nextId);
        vips.add(nextId, owningVip);
        return owningVip;
    }

    private short nextOwningId() {
        if (vips.isEmpty()) {
            return 0;
        }

        short nextId = -1;

        for (int i = 0; i < vips.size(); i++, nextId++) {
            if (nextId + 1 == vips.get(i).getOwningId()) {
                continue;
            }

            return (short) (nextId + 1);
        }

        return (short) (nextId + 1);
    }

    public boolean remove(short owningId) {
        final OwningVIP owningVip =  vips.stream()
            .filter(current -> current.getOwningId() == owningId)
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
