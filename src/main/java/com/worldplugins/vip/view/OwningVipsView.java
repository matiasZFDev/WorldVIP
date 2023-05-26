package com.worldplugins.vip.view;

import com.worldplugins.lib.config.common.ItemDisplay;
import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.util.Strings;
import com.worldplugins.lib.view.PageConfigContextBuilder;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.util.VipDuration;
import me.post.lib.config.model.ConfigModel;
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

import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class OwningVipsView implements View {
    public static class Context {
        private final int page;

        public Context(int page) {
            this.page = page;
        }
    }

    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public OwningVipsView(
        @NotNull MenuModel menuModel,
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.playerService = playerService;
        this.vipConfig = vipConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final Context context = data == null
            ? new Context(0)
            : (Context) requireNonNull(data);
        final List<Integer> slots = menuModel.data().getData("Slots");
        final ItemDisplay vipDisplay = menuModel.data().getData("Display-vip");
        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());
        final List<OwningVIP> owningVips = vipPlayer == null
            ? Collections.emptyList()
            : vipPlayer.owningVips().vips();

        PageConfigContextBuilder.of(
                menuModel,
                currentPage -> Views.get().open(player, OwningVipsView.class, new Context(currentPage)),
                context.page
            )
            .editTitle((pageInfo, title) -> Strings.replace(
                title,
                to("@atual", String.valueOf(pageInfo.page() + 1)),
                to("@totais", String.valueOf(pageInfo.totalPages())))
            )
            .handleMenuItemClick(
                "Voltar",
                click -> Views.get().open(click.whoClicked(), VipMenuView.class)
            )
            .nextPageButtonAs("Pagina-seguinte")
            .previousPageButtonAs("Pagina-anterior")
            .withSlots(slots)
            .fill(
                owningVips,
                vip -> {
                    final VipData.VIP configVip = vipConfig.data().getById(vip.id());
                    return ItemTransformer.of(configVip.item())
                        .display(vipDisplay)
                        .nameFormat(to("@vip", configVip.display()))
                        .loreFormat(
                            to("@tipo", vip.type().getName().toUpperCase()),
                            to("@tempo", VipDuration.format(vip))
                        )
                        .transform();
                }
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
