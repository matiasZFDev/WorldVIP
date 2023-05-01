package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.menu.ConfirmKeyActivationMenuContainer;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.handler.VipHandler;
import com.worldplugins.vip.key.KeyManagement;
import com.worldplugins.vip.util.VipDuration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

@ExtensionMethod(value = {
    ItemExtensions.class,
    GenericExtensions.class,
    ViewExtensions.class
}, suppressBaseMethods = false)

@RequiredArgsConstructor
@ViewSpec(menuContainer = ConfirmKeyActivationMenuContainer.class)
public class ConfirmKeyActivationView extends MenuDataView<ManageKeyView.Context> {
    private final @NonNull KeyManagement keyManagement;
    private final @NonNull VipHandler vipHandler;
    private final @NonNull ValidKeyRepository validKeyRepository;

    private final @NonNull ConfigCache<VipData> vipConfig;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, ManageKeyView.Context context, @NonNull MenuData menuData) {
        final ValidVipKey key = context.getKey();
        final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());

        return MenuItemsUtils.newSession(menuData.getItems(), session ->
            session.modify("Key", item ->
                item
                    .loreFormat(
                        "@vip".to(configVip.getDisplay()),
                        "@tipo".to(key.getVipType().getName().toUpperCase()),
                        "@tempo".to(VipDuration.format(key)),
                        "@usos".to(String.valueOf(key.getUsages()))
                    )
            )
        ).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem menuItem, @NonNull InventoryClickEvent inventoryClickEvent) {
        if (menuItem.getId().equals("Cancelar")) {
            final ManageKeyView.Context context = getContext(player);
            keyManagement.manage(
                player,
                context.getKey().getCode(),
                context.getKeysViewPage(),
                key -> player.openView(ManageKeyView.class, getContext(player))
            );
            return;
        }

        if (menuItem.getId().equals("Confirmar")) {
            final ManageKeyView.Context context = getContext(player);

            keyManagement.manage(
                player,
                context.getKey().getCode(),
                context.getKeysViewPage(),
                key -> {
                    final VIP vip = new VIP(key.getVipId(), key.getVipType(), key.getVipDuration());

                    player.closeInventory();
                    vipHandler.activate(player.getUniqueId(), vip, true);

                    if (key.getUsages() > 1) {
                        validKeyRepository.consumeKey(key);
                    } else {
                        validKeyRepository.removeKey(key);
                    }
                }
            );
        }
    }
}
