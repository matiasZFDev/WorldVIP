package com.worldplugins.vip.view.data;

import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.player.model.VipType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

public enum KeyMarketOrder {
    NONE(
        "Ordenar-nenhum",
        (a, b) -> 0
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return BETTER_TYPE;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return this;
        }
    },
    BETTER_TYPE(
        "Ordenar-melhor-tipo",
        Comparator.comparingInt(SellingKey::vipId).reversed()
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return EXPENSIVE;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return WORST_TYPE;
        }
    },
    WORST_TYPE(
        "Ordenar-pior-tipo",
        Comparator.comparingInt(SellingKey::vipId)
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return EXPENSIVE;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return BETTER_TYPE;
        }
    },
    EXPENSIVE(
        "Ordenar-maior-preço",
        Comparator.comparingDouble(SellingKey::price).reversed()
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return MORE_USAGES;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return CHEAPER;
        }
    },
    CHEAPER(
        "Ordenar-menor-preço",
        Comparator.comparingDouble(SellingKey::price)
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return MORE_USAGES;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return EXPENSIVE;
        }
    },
    MORE_USAGES(
        "Ordenar-mais-usos",
        Comparator.comparingInt(SellingKey::vipUsages).reversed()
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return LONGER_DURATION;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return LESS_USAGES;
        }
    },
    LESS_USAGES(
        "Ordenar-menos-usos",
        Comparator.comparingInt(SellingKey::vipUsages)
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return LONGER_DURATION;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return MORE_USAGES;
        }
    },
    LONGER_DURATION(
        "Ordenar-maior-duração",
        (a, b) -> a.vipType() == VipType.PERMANENT ? 1 : a.vipDuration() - b.vipDuration()
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return RECENT_POST;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return SHORTER_DURATION;
        }
    },
    SHORTER_DURATION(
        "Ordenar-menor-duração",
        (a, b) -> a.vipType() == VipType.PERMANENT ? 0 : b.vipDuration() - a.vipDuration()
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return RECENT_POST;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return LONGER_DURATION;
        }
    },
    RECENT_POST(
        "Ordenar-mais-recente",
        Comparator.comparingLong(SellingKey::postTimestamp).reversed()
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return NONE;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return OLDER_POST;
        }
    },
    OLDER_POST(
        "Ordenar-mais-antiga",
        Comparator.comparingLong(SellingKey::postTimestamp)
    ) {
        @Override
        public @NotNull KeyMarketOrder next() {
            return NONE;
        }

        @Override
        public @NotNull KeyMarketOrder alternate() {
            return RECENT_POST;
        }
    };

    private final @NotNull String configItemId;
    private final @NotNull Comparator<SellingKey> comparator;

    private static final @NotNull Collection<KeyMarketOrder> orders = Arrays.asList(values());

    KeyMarketOrder(@NotNull String configItemId, @NotNull Comparator<SellingKey> comparator) {
        this.configItemId = configItemId;
        this.comparator = comparator;
    }

    public @NotNull String configItemId() {
        return configItemId;
    }

    public @NotNull Comparator<SellingKey> comparator() {
        return comparator;
    }

    public abstract @NotNull KeyMarketOrder next();
    public abstract @NotNull KeyMarketOrder alternate();

    public static @NotNull Collection<KeyMarketOrder> orders() {
        return orders;
    }
}
