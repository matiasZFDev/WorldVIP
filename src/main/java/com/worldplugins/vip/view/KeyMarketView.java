package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.config.data.ItemDisplay;
import com.worldplugins.lib.extension.*;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.NBTExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.vip.NBTKeys;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.menu.KeyMarketMenuContainer;
import com.worldplugins.vip.controller.VipKeyShopController;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.util.BukkitUtils;
import com.worldplugins.vip.util.ItemFactory;
import com.worldplugins.vip.util.VipDuration;
import com.worldplugins.vip.view.data.KeyMarketOrder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ExtensionMethod({
    ReplaceExtensions.class,
    GenericExtensions.class,
    CollectionExtensions.class,
    ItemExtensions.class,
    ViewExtensions.class,
    TimeExtensions.class,
    NumberFormatExtensions.class,
    NBTExtensions.class,
    ResponseExtensions.class
})

@RequiredArgsConstructor
@ViewSpec(menuContainer = KeyMarketMenuContainer.class)
public class KeyMarketView extends MenuDataView<KeyMarketView.Context> {
    @RequiredArgsConstructor
    public static class Context implements ViewContext {
        @Getter
        private final int page;
        private final int totalPages;
        @Getter
        private final @NonNull KeyMarketOrder order;
        private final Collection<SellingKey> pageKeys;

        @Override
        public ViewContext viewDidOpen() {
            return new Context(page, totalPages, order, null);
        }
    }

    private final @NonNull SellingKeyRepository sellingKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull VipKeyShopController vipKeyShopController;

    private final @NonNull ConfigCache<VipData> vipConfig;

    @Override
    public @NonNull ItemProcessResult processItems(
        @NonNull Player player,
        Context context,
        @NonNull MenuData menuData
    ) {


        final List<Integer> slots = menuData.getData("Slots");
        final ItemDisplay keyDisplay = menuData.getData("Display-key");
        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            if (context.page == 0) {
                session.remove("Pagina-anterior");
            }

            if (context.page == context.totalPages - 1) {
                session.remove("Pagina-seguinte");
            }

            session.addDynamics(() ->
                context.pageKeys.zip(slots).stream()
                    .map(keyPair -> {
                        final SellingKey key = keyPair.first();
                        final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());
                        final Integer postTimeElapsed = (int) TimeUnit
                            .MILLISECONDS
                            .toSeconds(System.nanoTime() - key.getPostTimestamp());
                        return ItemFactory.dynamicOf(
                            "Key", keyPair.second(), configVip.getItem()
                                .display(keyDisplay)
                                .nameFormat("@vip".to(configVip.getDisplay()))
                                .loreFormat(
                                    "@vendedor".to(BukkitUtils.getPlayerName(key.getSellerId())),
                                    "@preco".to(((Double) key.getPrice()).suffixed()),
                                    "@tipo".to(key.getVipType().getName().toUpperCase()),
                                    "@tempo".to(VipDuration.format(key)),
                                    "@usos".to(String.valueOf(key.getVipUsages())),
                                    "@data-postagem".to(postTimeElapsed.toTime())
                                )
                                .addReference(NBTKeys.SELLING_KEY, key.getCode())
                        );
                    })
                    .collect(Collectors.toList())
            );
        }).build();
    }

    @Override
    public @NonNull String getTitle(@NonNull String title, Context data, @NonNull MenuData menuData) {
        return title.formatReplace(
            "@atual".to(String.valueOf(data.page + 1)),
            "@totais".to(String.valueOf(data.totalPages))
        );
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem menuItem, @NonNull InventoryClickEvent event) {
        switch (menuItem.getId()) {
            case "Voltar": {
                player.openView(VipMenuView.class);
                break;
            }

            case "Pagina-seguinte": {
                final Context context = getContext(player);
                vipKeyShopController.openView(player.getPlayer(), context.page + 1, context.order);
                break;
            }

            case "Pagina-anterior": {
                final Context context = getContext(player);
                vipKeyShopController.openView(player.getPlayer(), context.page - 1, context.order);
                break;
            }

            case "Ordem": {
                final Context context = getContext(player);

                if (event.isRightClick()) {
                    vipKeyShopController.openView(player.getPlayer(), context.page, context.order.next());
                }

                if (event.isLeftClick()) {
                    vipKeyShopController.openView(player.getPlayer(), context.page, context.order.alternate());
                }
                break;
            }

            case "Key": {
                final Context context = getContext(player);
                final String code = menuItem.getItem().getReference(NBTKeys.SELLING_KEY);

                sellingKeyRepository.getAllKeys().thenAccept(keys -> scheduler.newTask(() -> {
                    final SellingKey matchingKey = keys.stream()
                        .filter(key -> key.getCode().equals(code))
                        .findFirst()
                        .orElse(null);

                    if (matchingKey == null) {
                        player.respond("Mercado-key-inexistente");
                        vipKeyShopController.openView(player, context.page, context.order);
                        return;
                    }

                    player.openView(KeyMarketPurchaseView.class, new KeyMarketPurchaseView.Context(
                        matchingKey, context
                    ));
                }).run());
            }
        }
    }
}
