package com.worldplugins.vip.database.items;

import lombok.*;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class VipItems {
    private final @NonNull UUID playerId;
    private final byte vipId;
    @Setter
    private short amount;
}
