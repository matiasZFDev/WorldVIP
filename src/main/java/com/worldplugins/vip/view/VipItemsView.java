package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.config.data.ItemDisplay;
import com.worldplugins.lib.extension.CollectionExtensions;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.menu.VipItemsMenuContainer;
import com.worldplugins.vip.controller.VipItemsController;
import com.worldplugins.vip.database.items.VipItems;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.util.ItemFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    CollectionExtensions.class,
    ItemExtensions.class,
    GenericExtensions.class,
    ViewExtensions.class
})

@RequiredArgsConstructor
@ViewSpec(menuContainer = VipItemsMenuContainer.class)
public class VipItemsView extends MenuDataView<VipItemsView.Context> {
    @RequiredArgsConstructor
    public static class Context implements ViewContext {
        private final @NonNull Collection<VipItems> itemsList;
    }

    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull VipItemsController vipItemsController;

    @Override
    public @NonNull ItemProcessResult processItems(
        @NonNull Player player,
        Context context,
        @NonNull MenuData menuData
    ) {
        final List<Integer> slots = menuData.getData("Slots");
        final ItemDisplay itemsDisplay = menuData.getData("Display-itens");
        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            session.addDynamics(() ->
                context.itemsList.zip(slots).stream()
                    .map(itemsPair -> {
                        final VipData.VIP configVip = vipConfig.data().getById(itemsPair.first().getVipId());
                        final ItemStack item = configVip.getItem()
                            .display(itemsDisplay)
                            .loreFormat("@quantia".to(String.valueOf(itemsPair.first().getAmount())));
                        return ItemFactory.dynamicOf("Itens", itemsPair.second(), item);
                    })
                    .collect(Collectors.toList())
            );
        }).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem item, @NonNull InventoryClickEvent event) {
        if (item.getId().equals("Voltar")) {
            player.openView(VipMenuView.class);
            return;
        }
    }
}
