package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.CollectionExtensions;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.vip.config.menu.VipTopMenuContainer;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.manager.VipTopManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@ExtensionMethod({
    CollectionExtensions.class,
    ItemExtensions.class,
    GenericExtensions.class,
    NumberExtensions.class,
    ViewExtensions.class
})

@RequiredArgsConstructor
@ViewSpec(menuContainer = VipTopMenuContainer.class)
public class VipTopView extends MenuDataView<ViewContext> {
    private final @NonNull VipTopManager topManager;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, ViewContext context, @NonNull MenuData menuData) {
        final List<Integer> slots = menuData.getData("Slots");
        final ItemStack topItem = menuData.getData("Iten-top");

        return MenuItemsUtils.newSession(menuData.getItems(), session ->
            session.addDynamics(() ->
                topManager.getTop().zip(slots).mapIndexed((index, topPair) -> {
                        final ItemStack item = topItem
                            .nameFormat(
                                "@posicao".to(String.valueOf(index + 1)),
                                "@jogador".to(Bukkit.getOfflinePlayer(topPair.first().getPlayerId()).getName()))
                            .loreFormat(
                                "@gasto".to(((Double) topPair.first().getSpent()).plainFormat())
                            );
                        return new MenuItem("Jogador-top", topPair.second(), item, null);
                    })
            )
        ).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem menuItem, @NonNull InventoryClickEvent inventoryClickEvent) {
        if (menuItem.getId().equals("Voltar")) {
            player.openView(VipMenuView.class);
        }
    }
}
