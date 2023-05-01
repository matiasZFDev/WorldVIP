package com.worldplugins.vip.controller;

import com.worldplugins.lib.config.cache.menu.MenuContainer;
import com.worldplugins.lib.manager.view.MenuContainerManager;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.config.menu.KeyMarketMenuContainer;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.view.KeyMarketView;
import com.worldplugins.vip.view.data.KeyMarketOrder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    ViewExtensions.class
})

@RequiredArgsConstructor
public class VipKeyShopController {
    private final @NonNull SellingKeyRepository sellingKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull MenuContainerManager menuContainerManager;

    public void openView(@NonNull Player player, int page, @NonNull KeyMarketOrder order) {
        final MenuContainer vipKeyShopMenuContainer = menuContainerManager.get(KeyMarketMenuContainer.class);
        final List<Integer> slots = vipKeyShopMenuContainer.getData().getData("Slots");

        sellingKeyRepository.getAllKeys().thenAccept(keys -> scheduler.newTask(() -> {
            if (!player.isOnline()) {
                return;
            }

            final int totalPages = keys.size() <= slots.size()
                ? 1
                : keys.size() / slots.size();
            final Collection<SellingKey> pageKeys = keys.stream()
                .sorted(order.comparator())
                .skip((long) page * slots.size())
                .limit(slots.size())
                .collect(Collectors.toList());
            player.openView(KeyMarketView.class, new KeyMarketView.Context(
                page, totalPages, order, pageKeys
            ));
        }).run());
    }
}
