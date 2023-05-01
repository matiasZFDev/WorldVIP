package com.worldplugins.vip.conversation;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.key.KeyManagement;
import com.worldplugins.vip.util.VipDuration;
import com.worldplugins.vip.view.ManageKeyView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberFormatExtensions.class,
    ViewExtensions.class
})

@RequiredArgsConstructor
public class KeyPostConfirmConversation extends StringPrompt {
    private final @NonNull KeyManagement keyManagement;
    private final double price;
    private final @NonNull ManageKeyView.Context manageKeyViewContext;
    private final @NonNull SellingKeyRepository sellingKeyRepository;
    private final @NonNull ValidKeyRepository validKeyRepository;

    private final @NonNull ConfigCache<VipData> vipConfig;

    @Override
    public String getPromptText(ConversationContext context) {
        final ValidVipKey key = manageKeyViewContext.getKey();
        final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());

        ((Player) context.getForWhom()).respond("Vender-key-confirmar", message -> message.replace(
            "@key".to(key.getCode()),
            "@vip".to(configVip.getDisplay()),
            "@tempo".to(VipDuration.format(key)),
            "@tipo".to(key.getVipType().getName().toUpperCase()),
            "@usos".to(String.valueOf(key.getUsages())),
            "@preco".to(((Double) price).suffixed())
        ));
        return "";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String value) {
        final Player player = (Player) context.getForWhom();

        if (!value.equalsIgnoreCase("CONFIRMAR")) {
            if (value.equalsIgnoreCase("CANCELAR")) {
                keyManagement.manage(
                    player,
                    manageKeyViewContext.getKey().getCode(),
                    manageKeyViewContext.getKeysViewPage(),
                    key -> player.openView(ManageKeyView.class, manageKeyViewContext)
                );
                return null;
            }

            return this;
        }

        keyManagement.manage(
            player,
            manageKeyViewContext.getKey().getCode(),
            manageKeyViewContext.getKeysViewPage(),
            key -> {
                final SellingKey sellingKey = new SellingKey(
                    key.getCode(), player.getUniqueId(), price, key.getVipId(), key.getVipType(),
                    key.getVipDuration(), key.getUsages(), System.nanoTime()
                );

                player.closeInventory();
                validKeyRepository.removeKey(key);
                sellingKeyRepository.addKey(sellingKey);

                player.respond("Key-postada");
            }
        );
        return null;
    }
}
