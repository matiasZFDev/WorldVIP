package com.worldplugins.vip.database.key;

import com.worldplugins.vip.database.player.model.VipType;
import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class ValidVipKey {
    private final UUID generatorId;
    private final @NonNull String code;
    private final byte vipId;
    private final @NonNull VipType vipType;
    private final int vipDuration;
    @Setter
    private short usages;

    public static final int MAX_CODE_LENGTH = 20;

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ValidVipKey))
            return false;

        return this.code.equals(((ValidVipKey) other).code);
    }
}
