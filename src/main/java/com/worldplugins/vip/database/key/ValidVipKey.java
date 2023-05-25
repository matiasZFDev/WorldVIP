package com.worldplugins.vip.database.key;

import com.worldplugins.vip.database.player.model.VipType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ValidVipKey {
    private final @Nullable String generatorName;
    private final @NotNull String code;
    private final byte vipId;
    private final @NotNull VipType vipType;
    private final int vipDuration;
    private short usages;

    public ValidVipKey(
        @Nullable String generatorName,
        @NotNull String code,
        byte vipId,
        @NotNull VipType vipType,
        int vipDuration,
        short usages
    ) {
        this.generatorName = generatorName;
        this.code = code;
        this.vipId = vipId;
        this.vipType = vipType;
        this.vipDuration = vipDuration;
        this.usages = usages;
    }

    public @Nullable String generatorName() {
        return generatorName;
    }

    public @NotNull String code() {
        return code;
    }

    public byte vipId() {
        return vipId;
    }

    public @NotNull VipType vipType() {
        return vipType;
    }

    public int vipDuration() {
        return vipDuration;
    }

    public short usages() {
        return usages;
    }

    public void setUsages(short usages) {
        this.usages = usages;
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ValidVipKey))
            return false;

        return this.code.equals(((ValidVipKey) other).code);
    }
}
