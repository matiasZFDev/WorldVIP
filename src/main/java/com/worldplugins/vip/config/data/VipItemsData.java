package com.worldplugins.vip.config.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class VipItemsData {
    public static class VipItems {
        private final @NotNull String vipName;
        private final @NotNull ItemStack[] data;

        public VipItems(@NotNull String vipName, @NotNull ItemStack[] data) {
            this.vipName = vipName;
            this.data = data;
        }

        public @NotNull String vipName() {
            return vipName;
        }

        public @Nullable ItemStack @NotNull [] data() {
            return data;
        }
    }

    public final @NotNull Collection<VipItems> items;

    public VipItemsData(@NotNull Collection<VipItems> items) {
        this.items = items;
    }

    public @NotNull Collection<VipItems> items() {
        return items;
    }

    public @Nullable VipItems getByName(@NotNull String vipName) {
        return items.stream()
            .filter(vipItems -> vipItems.vipName.equals(vipName))
            .findFirst()
            .orElse(null);
    }
}
