package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.extension.TimeExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.menu.KeyMarketPurchaseMenuContainer;
import com.worldplugins.vip.controller.VipKeyShopController;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.key.VipKeyGenerator;
import com.worldplugins.vip.manager.PointsManager;
import com.worldplugins.vip.util.BukkitUtils;
import com.worldplugins.vip.util.VipDuration;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.concurrent.TimeUnit;

@ExtensionMethod({
    ItemExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class,
    ResponseExtensions.class,
    TimeExtensions.class
})

@RequiredArgsConstructor
@ViewSpec(menuContainer = KeyMarketPurchaseMenuContainer.class)
public class KeyMarketPurchaseView extends MenuDataView<KeyMarketPurchaseView.Context> {
    @RequiredArgsConstructor
    @Getter
    public static class Context implements ViewContext {
        private final @NonNull SellingKey key;
        private final @NonNull KeyMarketView.Context marketContext;
    }

    private final @NonNull VipKeyShopController vipKeyShopController;
    private final @NonNull SellingKeyRepository sellingKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull PointsManager pointsManager;
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull VipKeyGenerator vipKeyGenerator;

    private final @NonNull ConfigCache<VipData> vipConfig;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, Context context, @NonNull MenuData menuData) {
        final SellingKey key = context.key;
        final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());

        return MenuItemsUtils.newSession(menuData.getItems(), session ->
            session.modify("Key", item ->
                item.loreFormat(
                    "@vip".to(configVip.getDisplay()),
                    "@tempo".to(VipDuration.format(key)),
                    "@tipo".to(key.getVipType().getName().toUpperCase()),
                    "@vendedor".to(BukkitUtils.getPlayerName(key.getSellerId())),
                    "@usos".to(String.valueOf(key.getVipUsages())),
                    "@preco".to(((Double) key.getPrice()).suffixed())
                )
            )
        ).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem item, @NonNull InventoryClickEvent event) {
        final Context context = getContext(player);

        if (item.getId().equals("Cancelar")) {
            vipKeyShopController.openView(
                player, context.marketContext.getPage(), context.marketContext.getOrder()
            );
            return;
        }

        if (!item.getId().equals("Confirmar")) {
            return;
        }

        sellingKeyRepository.getAllKeys().thenAccept(keys -> scheduler.newTask(() -> {
            if (!player.isOnline()) {
                return;
            }

            final SellingKey key = context.key;
            final boolean hasKey = keys.stream().anyMatch(current ->
                current.getCode().equals(key.getCode())
            );

            if (!hasKey) {
                player.respond("Mercado-key-inexistente");
                vipKeyShopController.openView(
                    player, context.marketContext.getPage(), context.marketContext.getOrder()
                );
                return;
            }

            if (!pointsManager.hasPoints(player.getUniqueId(), key.getPrice())) {
                player.respond("Comprar-key-cash-insuficiente");
                return;
            }

            final ValidVipKey newValidKey = new ValidVipKey(
                player.getName(), vipKeyGenerator.generate(), key.getVipId(), key.getVipType(),
                key.getVipDuration(), key.getVipUsages()
            );

            validKeyRepository.getKeyByCode(newValidKey.getCode()).thenAccept(validKey -> scheduler.newTask(() -> {
                if (!player.isOnline()) {
                    return;
                }

                if (validKey != null) {
                    player.respond("Key-gerada-duplicada");
                    vipKeyShopController.openView(
                        player, context.marketContext.getPage(), context.marketContext.getOrder()
                    );
                    return;
                }

                final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());
                final Player seller = Bukkit.getPlayer(key.getSellerId());

                pointsManager.withdrawPoints(player.getUniqueId(), key.getPrice());
                sellingKeyRepository.removeKey(key);
                validKeyRepository.addKey(newValidKey);

                player.closeInventory();
                player.respond("Key-comprada", message -> message.replace(
                    "@vip".to(configVip.getDisplay()),
                    "@tempo".to(VipDuration.format(key)),
                    "@tipo".to(key.getVipType().getName().toUpperCase()),
                    "@vendedor".to(BukkitUtils.getPlayerName(key.getSellerId())),
                    "@usos".to(String.valueOf(key.getVipUsages())),
                    "@preco".to(((Double) key.getPrice()).suffixed()),
                    "@key".to(newValidKey.getCode())
                ));

                if (seller != null) {
                    final Integer postTimeElapsed = (int) TimeUnit
                        .MILLISECONDS
                        .toSeconds(System.nanoTime() - key.getPostTimestamp());
                    seller.respond("Key-vendida", message -> message.replace(
                        "@vip".to(configVip.getDisplay()),
                        "@tempo".to(VipDuration.format(key)),
                        "@tipo".to(key.getVipType().getName().toUpperCase()),
                        "@comprador".to(player.getName()),
                        "@usos".to(String.valueOf(key.getVipUsages())),
                        "@preco".to(((Double) key.getPrice()).suffixed()),
                        "@tempo-postagem".to(postTimeElapsed.toTime())
                    ));
                }
            }).run());
        }).run());
    }
}
