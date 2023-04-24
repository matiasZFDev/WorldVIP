package com.worldplugins.vip.database.player.model;

import com.worldplugins.vip.database.items.VipItems;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
public class PlayerItems {
    private final @NonNull Collection<VipItems> data;

    public @NonNull Collection<VipItems> all() {
        return data;
    }

    public void add(@NonNull VipItems items) {
        final VipItems vipMatch = data.stream()
            .filter(current -> current.getVipId() == items.getVipId())
            .findFirst()
            .orElse(null);

        if (vipMatch == null) {
            data.add(items);
            return;
        }

        vipMatch.setAmount((short) (vipMatch.getAmount() + items.getAmount()));
    }
}
