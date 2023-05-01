package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.util.ConversationProvider;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.menu.ManageKeyMenuContainer;
import com.worldplugins.vip.controller.KeysController;
import com.worldplugins.vip.conversation.KeyPostPriceConversation;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.key.KeyManagement;
import com.worldplugins.vip.util.VipDuration;
import lombok.Getter;
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
@ViewSpec(menuContainer = ManageKeyMenuContainer.class)
public class ManageKeyView extends MenuDataView<ManageKeyView.Context> {
    @RequiredArgsConstructor
    @Getter
    public static class Context implements ViewContext {
        private final @NonNull ValidVipKey key;
        private final int keysViewPage;
    }

    private final @NonNull KeysController keysController;
    private final @NonNull KeyManagement keyManagement;
    private final @NonNull ConversationProvider conversationProvider;
    private final @NonNull SellingKeyRepository sellingKeyRepository;
    private final @NonNull ValidKeyRepository validKeyRepository;

    private final @NonNull ConfigCache<VipData> vipConfig;

    @Override
    public @NonNull ItemProcessResult processItems(@NonNull Player player, Context context, @NonNull MenuData menuData) {
        final ValidVipKey key = context.key;
        final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());

        return MenuItemsUtils.newSession(menuData.getItems(), session ->
            session.modify("Info", item ->
                item
                    .nameFormat("@vip".to(configVip.getDisplay()))
                    .loreFormat(
                        "@tipo".to(key.getVipType().getName().toUpperCase()),
                        "@tempo".to(VipDuration.format(key)),
                        "@usos".to(String.valueOf(key.getUsages()))
                    )
            )
        ).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem item, @NonNull InventoryClickEvent event) {
        final Context context = getContext(player);

        switch (item.getId()) {
            case "Voltar": {
                keysController.openView(player, context.keysViewPage);
                return;
            }

            case "Ativar": {
                keyManagement.manage(
                    player,
                    context.getKey().getCode(),
                    context.getKeysViewPage(),
                    key -> player.openView(ConfirmKeyActivationView.class)
                );
                return;
            }

            case "Vender": {
                conversationProvider.create()
                    .withFirstPrompt(new KeyPostPriceConversation(
                        context, keyManagement, conversationProvider, sellingKeyRepository,
                        validKeyRepository, vipConfig
                    ))
                    .withTimeout(20)
                    .withLocalEcho(false)
                    .buildConversation(player)
                    .begin();
            }
        }
    }
}
