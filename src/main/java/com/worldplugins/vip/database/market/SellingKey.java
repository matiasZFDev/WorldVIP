package com.worldplugins.vip.database.market;

import com.worldplugins.vip.database.player.model.VipType;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class SellingKey {
    private final @NotNull String code;
    private final @NotNull UUID sellerId;
    private final double price;
    private final byte vipId;
    private final @NotNull VipType vipType;
    private final int vipDuration;
    private final short vipUsages;
    private final long postTimestamp;

    public SellingKey(
        @NotNull String code,
        @NotNull UUID sellerId,
        double price,
        byte vipId,
        @NotNull VipType vipType,
        int vipDuration,
        short vipUsages,
        long postTimestamp
    ) {
        this.code = code;
        this.sellerId = sellerId;
        this.price = price;
        this.vipId = vipId;
        this.vipType = vipType;
        this.vipDuration = vipDuration;
        this.vipUsages = vipUsages;
        this.postTimestamp = postTimestamp;
    }

    public @NotNull String code() {
        return code;
    }

    public @NotNull UUID sellerId() {
        return sellerId;
    }

    public double price() {
        return price;
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

    public short vipUsages() {
        return vipUsages;
    }

    public long postTimestamp() {
        return postTimestamp;
    }
}
