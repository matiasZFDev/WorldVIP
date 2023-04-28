package com.worldplugins.vip.database.key;

import com.worldplugins.vip.database.player.model.VipType;
import lombok.*;

@AllArgsConstructor
@Getter
public class ValidVipKey {
    private final String generatorName;
    private final @NonNull String code;
    private final byte vipId;
    private final @NonNull VipType vipType;
    private final int vipDuration;
    @Setter
    private short usages;

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ValidVipKey))
            return false;

        return this.code.equals(((ValidVipKey) other).code);
    }
}
