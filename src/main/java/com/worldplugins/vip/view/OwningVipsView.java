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
import com.worldplugins.vip.config.menu.OwningVipsMenuContainer;
import com.worldplugins.vip.controller.OwningVipsController;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.util.ItemFactory;
import com.worldplugins.vip.util.VipDuration;
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
@ViewSpec(menuContainer = OwningVipsMenuContainer.class)
public class OwningVipsView extends MenuDataView<OwningVipsView.Context> {
    @RequiredArgsConstructor
    public static class Context implements ViewContext {
        private final int page;
        private final int totalPages;
        private final Collection<OwningVIP> pageVips;

        @Override
        public ViewContext viewDidOpen() {
            return new Context(page, totalPages, null);
        }
    }

    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull OwningVipsController owningVipsController;

    @Override
    public @NonNull ItemProcessResult processItems(
        @NonNull Player player,
        Context context,
        @NonNull MenuData menuData
    ) {
        final List<Integer> slots = menuData.getData("Slots");
        final ItemDisplay vipDisplay = menuData.getData("Display-vip");
        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            if (context.page == 0) {
                session.remove("Pagina-anterior");
            }

            if (context.page == context.totalPages - 1) {
                session.remove("Pagina-seguinte");
            }

            session.addDynamics(() ->
                context.pageVips.zip(slots).stream()
                    .map(vipPair -> {
                        final OwningVIP vip = vipPair.first();
                        final VipData.VIP configVip = vipConfig.data().getById(vip.getId());
                        return ItemFactory.dynamicOf(
                            "Vip", vipPair.second(), configVip.getItem()
                                .display(vipDisplay)
                                .nameFormat("@vip".to(configVip.getDisplay()))
                                .loreFormat(
                                    "@tipo".to(vip.getType().getName().toUpperCase()),
                                    "@tempo".to(VipDuration.format(vip))
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
    public void onClick(@NonNull Player player, @NonNull MenuItem item, @NonNull InventoryClickEvent event) {
        switch (item.getId()) {
            case "Voltar": {
                player.openView(VipMenuView.class);
                break;
            }

            case "Pagina-seguinte": {
                final Context context = getContext(player);
                owningVipsController.openView(player.getPlayer(), context.page + 1);
                break;
            }

            case "Pagina-anterior": {
                final Context context = getContext(player);
                owningVipsController.openView(player.getPlayer(), context.page - 1);
                break;
            }
        }
    }
}
