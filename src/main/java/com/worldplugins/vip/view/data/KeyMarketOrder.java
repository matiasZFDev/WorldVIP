package com.worldplugins.vip.view.data;

import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.player.model.VipType;
import lombok.NonNull;

import java.util.Comparator;

public enum KeyMarketOrder {
    NONE {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return (a, b) -> 0;
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return BETTER_TYPE;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return this;
        }
    },
    BETTER_TYPE {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return WORST_TYPE.comparator().reversed();
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return EXPENSIVE;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return WORST_TYPE;
        }
    },
    WORST_TYPE {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return Comparator.comparingInt(SellingKey::getVipId);
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return EXPENSIVE;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return BETTER_TYPE;
        }
    },
    EXPENSIVE {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return CHEAPER.comparator().reversed();
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return MORE_USAGES;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return CHEAPER;
        }
    },
    CHEAPER {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return Comparator.comparingDouble(SellingKey::getPrice);
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return MORE_USAGES;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return EXPENSIVE;
        }
    },
    MORE_USAGES {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return LESS_USAGES.comparator().reversed();
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return LONGER_DURATION;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return LESS_USAGES;
        }
    },
    LESS_USAGES {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return Comparator.comparingInt(SellingKey::getVipUsages);
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return LONGER_DURATION;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return MORE_USAGES;
        }
    },
    LONGER_DURATION {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return (a, b) ->
                a.getVipType() == VipType.PERMANENT
                    ? 1
                    : a.getVipDuration() - b.getVipDuration();
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return RECENT_POST;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return SHORTER_DURATION;
        }
    },
    SHORTER_DURATION {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return SHORTER_DURATION.comparator().reversed();
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return RECENT_POST;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return LONGER_DURATION;
        }
    },
    RECENT_POST {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return OLDER_POST.comparator().reversed();
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return NONE;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return OLDER_POST;
        }
    },
    OLDER_POST {
        @Override
        public @NonNull Comparator<SellingKey> comparator() {
            return Comparator.comparingLong(SellingKey::getPostTimestamp);
        }

        @Override
        public @NonNull KeyMarketOrder next() {
            return NONE;
        }

        @Override
        public @NonNull KeyMarketOrder alternate() {
            return RECENT_POST;
        }
    };

    public abstract @NonNull Comparator<SellingKey> comparator();
    public abstract @NonNull KeyMarketOrder next();
    public abstract @NonNull KeyMarketOrder alternate();
}
