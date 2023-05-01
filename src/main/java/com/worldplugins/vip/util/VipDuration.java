package com.worldplugins.vip.util;

import com.worldplugins.lib.extension.TimeExtensions;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipType;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({
    TimeExtensions.class
})

public class VipDuration {
    public static @NonNull String format(@NonNull VIP vip) {
        return format(vip.getType(), vip.getDuration());
    }

    public static @NonNull String format(@NonNull ValidVipKey key) {
        return format(key.getVipType(), key.getVipDuration());
    }

    public static @NonNull String format(@NonNull SellingKey key) {
        return format(key.getVipType(), key.getVipDuration());
    }

    private static @NonNull String format(@NonNull VipType type, int duration) {
        return type == VipType.PERMANENT
            ? GlobalValues.PERMANENT_DURATION
            : Integer.valueOf(duration).toTime();
    }
}
