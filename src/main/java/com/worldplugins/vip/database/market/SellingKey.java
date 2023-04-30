package com.worldplugins.vip.database.market;

import com.worldplugins.vip.database.player.model.VipType;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class SellingKey {
    private final @NonNull String code;
    private final @NonNull UUID sellerId;
    private final double price;
    private final byte vipId;
    private final @NonNull VipType vipType;
    private final int vipDuration;
    private final short vipUsages;
    private final long postTimestamp;
}
