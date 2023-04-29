package com.worldplugins.vip.config.data;

import com.worldplugins.lib.extension.CollectionExtensions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

@ExtensionMethod({
    CollectionExtensions.class
})

@RequiredArgsConstructor
public class VipData {

    @RequiredArgsConstructor
    @Getter
    public static class VIP {
        @RequiredArgsConstructor
        public static class Pricing {
            @RequiredArgsConstructor
            @Getter
            public static class PricePair {
                private final int days;
                private final double price;
            }

            private final @NonNull Collection<PricePair> prices;

            public Double getPrice(int days) {
                return prices.stream()
                    .filter(pair -> pair.days == days)
                    .findFirst()
                    .map(PricePair::getPrice)
                    .orElse(null);
            }
        }

        private final byte id;
        private final @NonNull String name;
        private final @NonNull String display;
        private final @NonNull String group;
        private final @NonNull List<String> activationCommands;
        private final @NonNull Pricing pricing;
        private final @NonNull ItemStack item;
    }

    private final @NonNull Collection<VIP> vips;

    public @NonNull Collection<VIP> all() {
        return vips;
    }

    public VIP getById(byte id) {
        return getMatching(vip -> vip.id == id);
    }

    public VIP getByName(@NonNull String name) {
        return getMatching(vip -> vip.name.equals(name));
    }

    private VIP getMatching(@NonNull Predicate<VIP> predicate) {
        return vips.stream().filter(predicate).findFirst().orElse(null);
    }
}
