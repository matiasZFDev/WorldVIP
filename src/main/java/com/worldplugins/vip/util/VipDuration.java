package com.worldplugins.vip.util;

import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipType;
import me.post.lib.util.Time;
import org.jetbrains.annotations.NotNull;

public class VipDuration {
    public static @NotNull String format(@NotNull VIP vip) {
        return format(vip.type(), vip.duration());
    }

    public static @NotNull String format(@NotNull ValidVipKey key) {
        return format(key.vipType(), key.vipDuration());
    }

    public static @NotNull String format(@NotNull SellingKey key) {
        return format(key.vipType(), key.vipDuration());
    }

    private static @NotNull String format(@NotNull VipType type, int duration) {
        return type == VipType.PERMANENT
            ? GlobalValues.PERMANENT_DURATION
            : Time.toFormat(duration);
    }
}
