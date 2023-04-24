package com.worldplugins.vip.database.items;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@RequiredArgsConstructor
@Getter
public class VipItems {
    private final @NonNull UUID playerId;
    private final byte vipId;
    @Setter
    private short amount;
}
