package com.worldplugins.vip.conversation;

import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.key.KeyManagement;
import com.worldplugins.vip.util.VipDuration;
import com.worldplugins.vip.view.ManageKeyView;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.ConversationProvider;
import me.post.lib.util.NumberFormats;
import me.post.lib.view.Views;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class KeyPostPriceConversation extends StringPrompt {
    private final @NotNull ManageKeyView.Context manageKeyViewContext;
    private final @NotNull KeyManagement keyManagement;
    private final @NotNull ConversationProvider conversationProvider;
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public KeyPostPriceConversation(
        @NotNull ManageKeyView.Context manageKeyViewContext,
        @NotNull KeyManagement keyManagement,
        @NotNull ConversationProvider conversationProvider,
        @NotNull SellingKeyRepository sellingKeyRepository,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.manageKeyViewContext = manageKeyViewContext;
        this.keyManagement = keyManagement;
        this.conversationProvider = conversationProvider;
        this.sellingKeyRepository = sellingKeyRepository;
        this.validKeyRepository = validKeyRepository;
        this.vipConfig = vipConfig;
    }

    @Override
    public String getPromptText(ConversationContext context) {
        final ValidVipKey key = manageKeyViewContext.key();
        final VipData.VIP configVip = vipConfig.data().getById(key.vipId());
        respond(((Player) context.getForWhom()), "Vender-key-preco", message -> message.replace(
            to("@key", key.code()),
            to("@vip", configVip.display()),
            to("@tempo", VipDuration.format(key)),
            to("@tipo", key.vipType().getName().toUpperCase()),
            to("@usos", String.valueOf(key.usages()))
        ));
        return "";
    }

    @Override
    public Prompt acceptInput(ConversationContext context, String value) {
        final Player player = (Player) context.getForWhom();

        if (value.equalsIgnoreCase("CANCELAR")) {
            keyManagement.manage(
                player,
                manageKeyViewContext.key().code(),
                manageKeyViewContext.keysViewPage(),
                key -> Views.get().open(player, ManageKeyView.class, manageKeyViewContext)
            );
            return null;
        }

        if (!NumberFormats.isValidValue(value)) {
            return this;
        }

        final double price = Double.parseDouble(NumberFormats.numerify(value));

        keyManagement.manage(
            player,
            manageKeyViewContext.key().code(),
            manageKeyViewContext.keysViewPage(),
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
