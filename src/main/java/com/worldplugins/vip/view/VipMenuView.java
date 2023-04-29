package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberExtensions;
import com.worldplugins.lib.extension.ReplaceExtensions;
import com.worldplugins.lib.extension.TimeExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.controller.KeysController;
import com.worldplugins.vip.controller.VipItemsController;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.extension.ViewExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@ExtensionMethod({
    ItemExtensions.class,
    ReplaceExtensions.class,
    GenericExtensions.class,
    TimeExtensions.class,
    NumberExtensions.class,
    ResponseExtensions.class,
    ViewExtensions.class
})

@RequiredArgsConstructor
public class VipMenuView extends MenuDataView<ViewContext> {
    private final @NonNull PlayerService playerService;
    private final @NonNull KeysController keysController;
    private final @NonNull VipItemsController vipItemsController;

    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull ConfigCache<MainData> mainConfig;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, ViewContext context, @NonNull MenuData menuData) {
        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());
        final String activeVipState = menuData.getData("Formato-vip-ativo");
        final String inactiveVipState = menuData.getData("Formato-vip-inativo");

        final String vipState;

        if (vipPlayer == null || vipPlayer.getActiveVip() == null) {
            vipState = inactiveVipState;
        } else {
            final VIP activeVip = vipPlayer.getActiveVip();
            final VipData.VIP configVip = vipConfig.data().getById(activeVip.getId());
            vipState = activeVipState.formatReplace(
                "@vip".to(configVip.getDisplay()),
                "@tipo".to(activeVip.getType().getName().toUpperCase()),
                "@tempo".to(activeVip.getType() == VipType.PERMANENT
                    ? ((Integer) activeVip.getDuration()).toTime()
                    : GlobalValues.PERMANENT_DURATION
                )
            );
        }

        final String spentValue = vipPlayer == null ? "0" : ((Double) vipPlayer.getSpent()).plainFormat();

        return MenuItemsUtils.newSession(menuData.getItems(), session -> {
            session.modify("Perfil", item ->
                item.loreFormat(
                    "@estado-vip".to(vipState),
                    "@gasto".to(spentValue)
                )
            );
        }).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem menuItem, @NonNull InventoryClickEvent inventoryClickEvent) {
        switch (menuItem.getId()) {
            case "Coletar-itens":
                if (!mainConfig.data().storeItems()) {
                    player.closeInventory();
                    player.respond("Coleta-desabilitada");
                    return;
                }

                vipItemsController.openView(player);
                break;

            case "Vips-secondarios":
                // Abrir vips secondarios
                break;

            case "Keys":
                keysController.openView(player, 0);
                break;

            case "Top-vip":
                player.openView(VipTopView.class);
                break;

            case "Mercado-vip":
                // Abrir menu do mercado
                break;
        }
    }
}
