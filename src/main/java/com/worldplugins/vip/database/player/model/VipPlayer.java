package com.worldplugins.vip.database.player.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class VipPlayer {
    private final @NonNull UUID id;
    @Setter
    private VIP activeVip;
    private final @NonNull OwningVIPs owningVips;
    private final @NonNull PlayerKeys keys;
    private final @NonNull PlayerItems items;
}
