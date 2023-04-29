package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.config.data.ItemDisplay;
import com.worldplugins.lib.extension.CollectionExtensions;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.ReplaceExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.menu.KeysMenuContainer;
import com.worldplugins.vip.controller.KeysController;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.util.ItemFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    ReplaceExtensions.class,
    GenericExtensions.class,
    CollectionExtensions.class,
    ItemExtensions.class,
    ViewExtensions.class
})

@RequiredArgsConstructor
@ViewSpec(menuContainer = KeysMenuContainer.class)
public class KeysView extends MenuDataView<KeysView.Context> {
    @RequiredArgsConstructor
    public static class Context implements ViewContext {
        private final int page;
        private final int totalPages;
        private final Collection<ValidVipKey> pageKeys;

        @Override
        public ViewContext viewDidOpen() {
            return new Context(page, totalPages, null);
        }
    }

    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull KeysController keysController;

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
                        final ValidVipKey key = keyPair.first();
                        final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());
                        return ItemFactory.dynamicOf(
                            "Key", keyPair.second(), configVip.getItem()
                                .display(keyDisplay)
                                .nameFormat("@nome".to(configVip.getDisplay()))
                                .loreFormat(
                                    "@tipo".to(key.getVipType().getName().toUpperCase()),
                                    "@tempo".to(VipDuration.format(key))
                                )
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
    public void onClick(@NonNull Player player, @NonNull MenuItem menuItem, @NonNull InventoryClickEvent inventoryClickEvent) {
        switch (menuItem.getId()) {
            case "Voltar": {
                player.openView(VipMenuView.class);
                break;
            }

            case "Pagina-seguinte": {
                final Context context = getContext(player);
                keysController.openView(player.getPlayer(), context.page + 1);
                break;
            }

            case "Pagina-anterior": {
                final Context context = getContext(player);
                keysController.openView(player.getPlayer(), context.page - 1);
                break;
            }
        }
    }
}
