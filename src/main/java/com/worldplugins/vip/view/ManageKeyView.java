package com.worldplugins.vip.view;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.conversation.KeyPostPriceConversation;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.key.KeyGeneratorMatcher;
import com.worldplugins.vip.util.VipDuration;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.ConversationProvider;
import me.post.lib.util.Scheduler;
import me.post.lib.view.View;
import me.post.lib.view.Views;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.context.ClickHandler;
import me.post.lib.view.context.ViewContext;
import me.post.lib.view.context.impl.MapViewContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class ManageKeyView implements View {
    public static class Context {
        private final @NotNull ValidVipKey key;
        private final int keysViewPage;

        public Context(@NotNull ValidVipKey key, int keysViewPage) {
            this.key = key;
            this.keysViewPage = keysViewPage;
        }

        public @NotNull ValidVipKey key() {
            return key;
        }

        public int keysViewPage() {
            return keysViewPage;
        }
    }

    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull KeyGeneratorMatcher keyGeneratorMatcher;
    private final @NotNull ConversationProvider conversationProvider;
    private final @NotNull Scheduler scheduler;
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;

    public ManageKeyView(
        @NotNull MenuModel menuModel,
        @NotNull KeyGeneratorMatcher keyGeneratorMatcher,
        @NotNull ConversationProvider conversationProvider,
        @NotNull Scheduler scheduler,
        @NotNull SellingKeyRepository sellingKeyRepository,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.keyGeneratorMatcher = keyGeneratorMatcher;
        this.conversationProvider = conversationProvider;
        this.scheduler = scheduler;
        this.sellingKeyRepository = sellingKeyRepository;
        this.validKeyRepository = validKeyRepository;
        this.vipConfig = vipConfig;
        this.mainConfig = mainConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final Context context = (Context) requireNonNull(data);
        final ValidVipKey key = context.key;
        final VipData.VIP configVip = vipConfig.data().getById(key.vipId());

        ConfigContextBuilder.withModel(menuModel)
            .editMenuItem("Info", item ->
                ItemTransformer.of(item)
                    .nameFormat(to("@vip", configVip.display()))
                    .loreFormat(
                        to("@tipo", key.vipType().getName().toUpperCase()),
                        to("@tempo", VipDuration.format(key)),
                        to("@usos", String.valueOf(key.usages()))
                    )
            )
            .handleMenuItemClick(
                "Voltar",
                click -> Views.get().open(
                    player,
                    KeysView.class,
                    new KeysView.Context(context.keysViewPage)
                )
            )
            .handleMenuItemClick(
                "Ativar",
                click -> keyGeneratorMatcher.tryMatch(
                    player,
                    key.code(),
                    context.keysViewPage,
                    $ -> Views.get().open(player, ConfirmKeyActivationView.class)
                )
            )
            .handleMenuItemClick(
                "Vender",
                click -> conversationProvider.create()
                    .withFirstPrompt(new KeyPostPriceConversation(
                        context,
                        keyGeneratorMatcher,
                        conversationProvider,
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
            )
            .build(viewContext, player, data);
    }

    @Override
    public void onClick(@NotNull ViewClick click) {
        ClickHandler.handleTopNonNull(viewContext, click);
    }

    @Override
    public void onClose(@NotNull ViewClose close) {
        viewContext.removeViewer(close.whoCloses().getUniqueId());
    }
}
