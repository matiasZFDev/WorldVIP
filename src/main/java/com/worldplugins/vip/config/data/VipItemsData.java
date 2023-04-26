package com.worldplugins.vip.config.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

@RequiredArgsConstructor
public class VipItemsData {
    @RequiredArgsConstructor
    @Getter
    public static class VipItems {
        private final @NonNull String vipName;
        private final @NonNull ItemStack[] data;
    }

    public final @NonNull Collection<VipItems> items;

    public VipItems getByName(@NonNull String vipName) {
        return items.stream()
            .filter(vipItems -> vipItems.getVipName().equals(vipName))
            .findFirst()
            .orElse(null);
    }
}
