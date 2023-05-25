package com.worldplugins.vip.controller;

import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.view.KeyMarketView;
import com.worldplugins.vip.view.data.KeyMarketOrder;
import me.post.lib.util.Scheduler;
import me.post.lib.view.Views;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VipKeyShopController {
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull Scheduler scheduler;

    public VipKeyShopController(@NotNull SellingKeyRepository sellingKeyRepository, @NotNull Scheduler scheduler) {
        this.sellingKeyRepository = sellingKeyRepository;
        this.scheduler = scheduler;
    }

    public void openView(@NotNull Player player, int page, @NotNull KeyMarketOrder order) {
        sellingKeyRepository.getAllKeys().thenAccept(keys -> scheduler.runTask(0, false, () -> {
            if (!player.isOnline()) {
                return;
            }

            Views.get().open(player, KeyMarketView.class, new KeyMarketView.InContext(
                page, order, keys
            ));
        }));
    }
}
