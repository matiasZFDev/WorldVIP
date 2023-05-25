package com.worldplugins.vip.config.data;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import static java.util.Objects.requireNonNull;

public class VipData {
    public static class VIP {
        public static class Pricing {
            public static class PricePair {
                private final int days;
                private final double price;

                public PricePair(int days, double price) {
                    this.days = days;
                    this.price = price;
                }

                public int days() {
                    return days;
                }

                public double price() {
                    return price;
                }
            }

            public Pricing(@NotNull Collection<PricePair> prices) {
                this.prices = prices;
            }

            private final @NotNull Collection<PricePair> prices;

            public @Nullable Double getPrice(int days) {
                return prices.stream()
                    .filter(pair -> pair.days == days)
                    .findFirst()
                    .map(PricePair::price)
                    .orElse(null);
            }
        }

        private final byte id;
        private final @NotNull String name;
        private final @NotNull String display;
        private final @NotNull String group;
        private final @NotNull List<String> activationCommands;
        private final @NotNull Pricing pricing;
        private final @NotNull ItemStack item;

        public VIP(byte id, @NotNull String name, @NotNull String display, @NotNull String group, @NotNull List<String> activationCommands, @NotNull Pricing pricing, @NotNull ItemStack item) {
            this.id = id;
            this.name = name;
            this.display = display;
            this.group = group;
            this.activationCommands = activationCommands;
            this.pricing = pricing;
            this.item = item;
        }

        public byte id() {
            return id;
        }

        public @NotNull String name() {
            return name;
        }

        public @NotNull String display() {
            return display;
        }

        public @NotNull String group() {
            return group;
        }

        public @NotNull List<String> activationCommands() {
            return activationCommands;
        }

        public @NotNull Pricing pricing() {
            return pricing;
        }

        public @NotNull ItemStack item() {
            return item;
        }
    }

    private final @NotNull Collection<VIP> vips;

    public VipData(@NotNull Collection<VIP> vips) {
        this.vips = vips;
    }

    public @NotNull Collection<VIP> all() {
        return vips;
    }

    /**
     * The provided id must be related to a VIP.
     * @throws NullPointerException If the id does not match with any vip.
     * */
    public @NotNull VIP getById(byte id) {
        return requireNonNull(
            getMatching(vip -> vip.id == id),
            "VIP 'id: " + id + "' não encontrado."
        );
    }

    public @Nullable VIP getByName(@NotNull String name) {
        return getMatching(vip -> vip.name.equals(name));
    }

    private @Nullable VIP getMatching(@NotNull Predicate<VIP> predicate) {
        return vips.stream().filter(predicate).findFirst().orElse(null);
    }
}
