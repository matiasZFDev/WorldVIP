package com.worldplugins.vip.conversation;

import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.key.KeyManagement;
import com.worldplugins.vip.util.VipDuration;
import com.worldplugins.vip.view.ManageKeyView;
import me.post.lib.config.model.ConfigModel;
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

public class KeyPostConfirmConversation extends StringPrompt {
    private final @NotNull KeyManagement keyManagement;
    private final double price;
    private final @NotNull ManageKeyView.Context manageKeyViewContext;
    private final @NotNull Scheduler scheduler;
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;

    public KeyPostConfirmConversation(
        @NotNull KeyManagement keyManagement,
        double price,
        @NotNull ManageKeyView.Context manageKeyViewContext,
        @NotNull Scheduler scheduler,
        @NotNull SellingKeyRepository sellingKeyRepository,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.keyManagement = keyManagement;
        this.price = price;
        this.manageKeyViewContext = manageKeyViewContext;
        this.scheduler = scheduler;
        this.sellingKeyRepository = sellingKeyRepository;
        this.validKeyRepository = validKeyRepository;
        this.vipConfig = vipConfig;
        this.mainConfig = mainConfig;
    }

    @Override
    public String getPromptText(@NotNull ConversationContext context) {
        final ValidVipKey key = manageKeyViewContext.key();
        final VipData.VIP configVip = vipConfig.data().getById(key.vipId());
        final Player player = (Player) context.getForWhom();

        respond(player, "Vender-key-confirmar", message -> message.replace(
            to("@key", key.code()),
            to("@vip", configVip.display()),
            to("@tempo", VipDuration.format(key)),
            to("@tipo", key.vipType().getName().toUpperCase()),
            to("@usos", String.valueOf(key.usages())),
            to("@preco", NumberFormats.suffixed(price))
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

        if (!value.equalsIgnoreCase("CONFIRMAR")) {
            return null;
        }

        keyManagement.manage(
            player,
            manageKeyViewContext.key().code(),
            manageKeyViewContext.keysViewPage(),
            key ->
                sellingKeyRepository
                    .getAllKeys()
                    .thenAccept(keys -> scheduler.runTask(0, false, ()  -> {
                        if (!player.isOnline()) {
                            return;
                        }

                        final int sellingKeysCount = (int) keys.stream()
                            .filter(sellingKey -> sellingKey.sellerId().equals(player.getUniqueId()))
                            .count();

                        if (sellingKeysCount >= mainConfig.data().maxSellingKeysPerPlayer()) {
                            respond(player, "Maximo-de-keys-a-venda");
                            return;
                        }

                        final SellingKey sellingKey = new SellingKey(
                            key.code(),
                            player.getUniqueId(),
                            price,
                            key.vipId(),
                            key.vipType(),
                            key.vipDuration(),
                            key.usages(),
                            System.nanoTime()
                        );

                        validKeyRepository.removeKey(key);
                        sellingKeyRepository.addKey(sellingKey);
                        player.closeInventory();
                        respond(player, "Key-postada");
                    }))
        );

        return null;
    }
}
