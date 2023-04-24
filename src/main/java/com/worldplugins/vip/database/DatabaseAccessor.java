package com.worldplugins.vip.database;

import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.database.pending.PendingVipRepository;
import com.worldplugins.vip.database.player.PlayerService;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class DatabaseAccessor {
    private final @NonNull PlayerService playerService;
    private final @NonNull PendingVipRepository pendingVipRepository;
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull VipItemsRepository vipItemsRepository;
    private final @NonNull SellingKeyRepository sellingKeyRepository;
}
