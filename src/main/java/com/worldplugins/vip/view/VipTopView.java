package com.worldplugins.vip.view;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.vip.manager.VipTopManager;
import me.post.lib.util.CollectionHelpers;
import me.post.lib.util.Numbers;
import me.post.lib.view.View;
import me.post.lib.view.Views;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.helper.ClickHandler;
import me.post.lib.view.helper.ViewContext;
import me.post.lib.view.helper.impl.MapViewContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static me.post.lib.util.Pairs.to;

public class VipTopView implements View {
    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull VipTopManager topManager;

    public VipTopView(@NotNull MenuModel menuModel, @NotNull VipTopManager topManager) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.topManager = topManager;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final List<Integer> slots = menuModel.data().getData("Slots");
        final ItemStack topItem = menuModel.data().getData("Iten-top");

        ConfigContextBuilder.withModel(menuModel)
            .handleMenuItemClick(
                "Voltar",
                click -> Views.get().open(click.whoClicked(), VipMenuView.class)
            )
            .apply(builder -> {
                final AtomicInteger position = new AtomicInteger(0);
                CollectionHelpers.zip(topManager.getTop(), slots).forEach(topPair -> {
                    final ItemStack item = ItemTransformer.of(topItem)
                        .nameFormat(
                            to("@posicao", String.valueOf(position.getAndIncrement())),
                            to("@jogador", Bukkit.getOfflinePlayer(topPair.first().playerId()).getName()))
                        .loreFormat(
                            to("@gasto", Numbers.plainFormat(topPair.first().spent()))
                        )
                        .transform();
                    builder.item(topPair.second(), item);
                });
            })
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
