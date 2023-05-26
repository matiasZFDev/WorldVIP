package com.worldplugins.vip.conversation;

import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.key.KeyGeneratorMatcher;
import com.worldplugins.vip.util.VipDuration;
import com.worldplugins.vip.view.ManageKeyView;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.ConversationProvider;
import me.post.lib.util.NumberFormats;
import me.post.lib.util.Scheduler;
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
    private final @NotNull KeyGeneratorMatcher keyGeneratorMatcher;
    private final @NotNull ConversationProvider conversationProvider;
    private final @NotNull Scheduler scheduler;
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;

    public KeyPostPriceConversation(
        @NotNull ManageKeyView.Context manageKeyViewContext,
        @NotNull KeyGeneratorMatcher keyGeneratorMatcher,
        @NotNull ConversationProvider conversationProvider,
        @NotNull Scheduler scheduler,
        @NotNull SellingKeyRepository sellingKeyRepository,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.manageKeyViewContext = manageKeyViewContext;
        this.keyGeneratorMatcher = keyGeneratorMatcher;
        this.conversationProvider = conversationProvider;
        this.scheduler = scheduler;
        this.sellingKeyRepository = sellingKeyRepository;
        this.validKeyRepository = validKeyRepository;
        this.vipConfig = vipConfig;
        this.mainConfig = mainConfig;
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
            keyGeneratorMatcher.tryMatch(
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

        keyGeneratorMatcher.tryMatch(
            player,
            manageKeyViewContext.key().code(),
            manageKeyViewContext.keysViewPage(),
            key -> conversationProvider.create()
                .withFirstPrompt(new KeyPostConfirmConversation(
                    keyGeneratorMatcher,
                    price,
                    manageKeyViewContext,
                    scheduler,
                    sellingKeyRepository,
                    validKeyRepository,
                    vipConfig,
                    mainConfig
                ))
                .withTimeout(20)
                .withLocalEcho(false)
                .buildConversation(player)
                .begin()
        );
        return null;
    }
}
