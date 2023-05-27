package com.worldplugins.vip.view;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemBuilding;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.handler.VipHandler;
import com.worldplugins.vip.key.KeyGeneratorMatcher;
import com.worldplugins.vip.util.VipDuration;
import me.post.lib.config.model.ConfigModel;
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

public class ConfirmKeyActivationView implements View {
    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull KeyGeneratorMatcher keyGeneratorMatcher;
    private final @NotNull VipHandler vipHandler;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public ConfirmKeyActivationView(
        @NotNull MenuModel menuModel,
        @NotNull KeyGeneratorMatcher keyGeneratorMatcher,
        @NotNull VipHandler vipHandler,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.keyGeneratorMatcher = keyGeneratorMatcher;
        this.vipHandler = vipHandler;
        this.validKeyRepository = validKeyRepository;
        this.vipConfig = vipConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final ManageKeyView.Context context = (ManageKeyView.Context) requireNonNull(data);
        final ValidVipKey key = context.key();
        final VipData.VIP configVip = vipConfig.data().getById(key.vipId());

        ConfigContextBuilder.withModel(menuModel)
            .editMenuItem("Key", item ->
                ItemBuilding.loreFormat(
                    item,
                    to("@vip", configVip.display()),
                    to("@tipo", key.vipType().getName().toUpperCase()),
                    to("@tempo", VipDuration.format(key)),
                    to("@usos", String.valueOf(key.usages()))
                )
            )
            .handleMenuItemClick(
                "Cancelar",
                click -> keyGeneratorMatcher.tryMatch(
                    player,
                    context.key().code(),
                    context.keysViewPage(),
                    $ -> Views.get().open(player, ManageKeyView.class, context)
                )
            )
            .handleMenuItemClick(
                "Confirmar",
                click -> keyGeneratorMatcher.tryMatch(
                    player,
                    context.key().code(),
                    context.keysViewPage(),
                    $ -> {
                        final VIP vip = new VIP(key.vipId(), key.vipType(), key.vipDuration());

                        player.closeInventory();
                        vipHandler.activate(player.getUniqueId(), vip, true);

                        if (key.usages() > 1) {
                            validKeyRepository.consumeKey(key);
                        } else {
                            validKeyRepository.removeKey(key);
                        }
                    }
                )
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
