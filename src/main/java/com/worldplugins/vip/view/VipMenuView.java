package com.worldplugins.vip.view;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemBuilding;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.util.Strings;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.database.player.model.VipType;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.Heads;
import me.post.lib.util.Numbers;
import me.post.lib.util.Time;
import me.post.lib.view.View;
import me.post.lib.view.Views;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.helper.ClickHandler;
import me.post.lib.view.helper.ViewContext;
import me.post.lib.view.helper.impl.MapViewContext;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.worldplugins.vip.Response.respond;
import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Colors.color;
import static me.post.lib.util.Pairs.to;

public class VipMenuView implements View {
    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;

    public VipMenuView(
        @NotNull MenuModel menuModel,
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.playerService = playerService;
        this.vipConfig = vipConfig;
        this.mainConfig = mainConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());
        final String vipState = color(getVipState(vipPlayer));
        final String spentValue = vipPlayer == null
            ? "0"
            : Numbers.plainFormat(vipPlayer.spent());

        ConfigContextBuilder.withModel(menuModel)
            .editMenuItem("Perfil", item ->
                ItemBuilding.loreFormat(
                    Heads.fromOwner(player.getName(), item),
                    to("@estado-vip", vipState),
                    to("@gasto", spentValue)
                )
            )
            .handleMenuItemClick("Coletar-itens", click -> {
                if (!mainConfig.data().storeItems()) {
                    player.closeInventory();
                    respond(player, "Coleta-desabilitada");
                    return;
                }

                Views.get().open(player, VipItemsView.class);
            })
            .handleMenuItemClick(
                "Vips-secondarios",
                click -> Views.get().open(player, OwningVipsView.class)
            )
            .handleMenuItemClick(
                "Keys",
                click -> Views.get().open(player, KeysView.class)
            )
            .handleMenuItemClick(
                "Top-vip",
                click -> Views.get().open(player, VipTopView.class)
            )
            .handleMenuItemClick(
                "Mercado-vip",
                click -> Views.get().open(player, KeyMarketView.class)
            )
            .build(viewContext, player, data);
    }

    private @NotNull String getVipState(@Nullable VipPlayer player) {
        final String activeVipState = menuModel.data().getData("Formato-vip-ativo");
        final String inactiveVipState = menuModel.data().getData("Formato-vip-inativo");

        if (player == null || player.activeVip() == null) {
            return inactiveVipState;
        }

        final VIP activeVip = requireNonNull(player.activeVip());
        final VipData.VIP configVip = vipConfig.data().getById(activeVip.id());

        return Strings.replace(
            activeVipState,
            to("@vip", configVip.display()),
            to("@tipo", activeVip.type().getName().toUpperCase()),
            to("@tempo", activeVip.type() == VipType.PERMANENT
                ? Time.toFormat(activeVip.duration())
                : GlobalValues.PERMANENT_DURATION
            )
        );
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
