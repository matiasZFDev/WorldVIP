package com.worldplugins.vip.conversation;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberFormatExtensions;
import com.worldplugins.lib.util.ConversationProvider;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
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
    ViewExtensions.class,
    NumberFormatExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class KeyPostPriceConversation extends StringPrompt {
    private final @NonNull ManageKeyView.Context manageKeyViewContext;
    private final @NonNull KeyManagement keyManagement;
    private final @NonNull ConversationProvider conversationProvider;
    private final @NonNull SellingKeyRepository sellingKeyRepository;
    private final @NonNull ValidKeyRepository validKeyRepository;

    private final @NonNull ConfigCache<VipData> vipConfig;

    @Override
    public String getPromptText(ConversationContext context) {
        final ValidVipKey key = manageKeyViewContext.getKey();
        final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());
        ((Player) context.getForWhom()).respond("Vender-key-preco", message -> message.replace(
            "@key".to(key.getCode()),
            "@vip".to(configVip.getDisplay()),
            "@tempo".to(VipDuration.format(key)),
            "@tipo".to(key.getVipType().getName().toUpperCase()),
            "@usos".to(String.valueOf(key.getUsages()))
        ));
        return "";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String value) {
        final Player player = (Player) context.getForWhom();

        if (value.equalsIgnoreCase("CANCELAR")) {
            keyManagement.manage(
                player,
                manageKeyViewContext.getKey().getCode(),
                manageKeyViewContext.getKeysViewPage(),
                key -> player.openView(ManageKeyView.class, manageKeyViewContext)
            );
            return null;
        }

        if (!value.isValidValue()) {
            return this;
        }

        final double price = Double.parseDouble(value.numerify());

        keyManagement.manage(
            player,
            manageKeyViewContext.getKey().getCode(),
            manageKeyViewContext.getKeysViewPage(),
            key -> conversationProvider.create()
                .withFirstPrompt(new KeyPostConfirmConversation(
                    keyManagement, price, manageKeyViewContext, sellingKeyRepository,
                    validKeyRepository, vipConfig
                ))
                .withTimeout(20)
                .withLocalEcho(false)
                .buildConversation(player)
                .begin()
        );
        return null;
    }
}
