package com.worldplugins.vip.util;

import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VIP;
import org.jetbrains.annotations.NotNull;

public class VipTransition {
    public static @NotNull OwningVIP toOwning(@NotNull VIP vip) {
        return new OwningVIP(
            vip.id(),
            vip.type(),
            vip.duration()
        );
    }
}
