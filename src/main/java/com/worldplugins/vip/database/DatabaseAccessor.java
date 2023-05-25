package com.worldplugins.vip.database;

import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.database.pending.PendingVipRepository;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.VipPlayer;
import me.post.lib.database.cache.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class DatabaseAccessor {
    private final @NotNull PlayerService playerService;
    private final @NotNull PendingVipRepository pendingVipRepository;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull VipItemsRepository vipItemsRepository;
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull Cache<UUID, VipPlayer> playerCache;

    public DatabaseAccessor(
        @NotNull PlayerService playerService,
        @NotNull PendingVipRepository pendingVipRepository,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull VipItemsRepository vipItemsRepository,
        @NotNull SellingKeyRepository sellingKeyRepository,
        @NotNull Cache<UUID, VipPlayer> playerCache
    ) {
        this.playerService = playerService;
        this.pendingVipRepository = pendingVipRepository;
        this.validKeyRepository = validKeyRepository;
        this.vipItemsRepository = vipItemsRepository;
        this.sellingKeyRepository = sellingKeyRepository;
        this.playerCache = playerCache;
    }

    public @NotNull PlayerService playerService() {
        return playerService;
    }

    public @NotNull PendingVipRepository pendingVipRepository() {
        return pendingVipRepository;
    }

    public @NotNull ValidKeyRepository validKeyRepository() {
        return validKeyRepository;
    }

    public @NotNull VipItemsRepository vipItemsRepository() {
        return vipItemsRepository;
    }

    public @NotNull SellingKeyRepository sellingKeyRepository() {
        return sellingKeyRepository;
    }

    public @NotNull Cache<UUID, VipPlayer> playerCache() {
        return playerCache;
    }
}
