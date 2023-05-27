package com.worldplugins.vip.view;

import com.worldplugins.lib.config.common.ItemDisplay;
import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.data.VipItemsData;
import com.worldplugins.vip.database.items.VipItems;
import com.worldplugins.vip.database.items.VipItemsRepository;
import me.post.deps.nbt_api.nbtapi.NBTCompound;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.CollectionHelpers;
import me.post.lib.util.NBTs;
import me.post.lib.util.Players;
import me.post.lib.util.Scheduler;
import me.post.lib.view.View;
import me.post.lib.view.Views;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.context.ClickHandler;
import me.post.lib.view.context.ViewContext;
import me.post.lib.view.context.impl.MapViewContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

import static com.worldplugins.vip.Response.respond;
import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class VipItemsView implements View {
    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull VipItemsRepository vipItemsRepository;
    private final @NotNull Scheduler scheduler;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<VipItemsData> vipItemsConfig;

    private static final @NotNull String ITEMS_TAG = "wvip_vip_items_id";

    public VipItemsView(
        @NotNull MenuModel menuModel,
        @NotNull VipItemsRepository vipItemsRepository,
        @NotNull Scheduler scheduler,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ConfigModel<VipItemsData> vipItemsConfig
    ) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.vipItemsRepository = vipItemsRepository;
        this.scheduler = scheduler;
        this.mainConfig = mainConfig;
        this.vipConfig = vipConfig;
        this.vipItemsConfig = vipItemsConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        vipItemsRepository
            .getItems(player.getUniqueId())
            .thenAccept(itemsList -> scheduler.runTask(0, false, () -> {
                if (viewContext.getViewer(player.getUniqueId()) == null) {
                    return;
                }

                openWithItems(player, data, itemsList);
            }));
    }

    public void openWithItems(
        @NotNull Player player,
        @Nullable Object data,
        @NotNull Collection<VipItems> itemsList
    ) {
        final List<Integer> slots = menuModel.data().getData("Slots");
        final ItemDisplay itemsDisplay = menuModel.data().getData("Display-itens");

        ConfigContextBuilder.withModel(menuModel)
            .handleMenuItemClick(
                "Voltar",
                click -> Views.get().open(click.whoClicked(), VipMenuView.class)
            )
            .apply(builder -> {
                if (!itemsList.isEmpty()) {
                    builder.removeMenuItem("Vazio");
                    return;
                }

                CollectionHelpers.zip(itemsList, slots).forEach(itemsPair -> {
                    final VipData.VIP configVip = requireNonNull(
                        vipConfig.data().getById(itemsPair.first().vipId())
                    );
                    final ItemStack item = ItemTransformer.of(configVip.item())
                        .display(itemsDisplay)
                        .loreFormat(to("@quantia", String.valueOf(itemsPair.first().amount())))
                        .addNBT(ITEMS_TAG, configVip.id())
                        .transform();
                    builder.item(itemsPair.second(), item, this::handleVipItemsClick);
                });
            })
            .build(viewContext, player, data);
    }

    private void handleVipItemsClick(@NotNull ViewClick click) {
        final Player player = click.whoClicked();

        if (!mainConfig.data().storeItems()) {
            respond(player, "Coleta-desabilitada");
            return;
        }

        final ItemStack item = requireNonNull(click.clickedItem());
        final byte vipId = NBTs.getTagValue(item, ITEMS_TAG, NBTCompound::getByte);
        vipItemsRepository
            .getItems(player.getUniqueId())
            .thenAccept(itemList -> scheduler.runTask(0, false, () -> {
                if (!player.isOnline()) {
                    return;
                }

                final VipItems matchingItems = itemList.stream()
                    .filter(items -> items.vipId() == vipId)
                    .findFirst()
                    .orElse(null);

                if (matchingItems == null) {
                    respond(player, "Vip-itens-inexistentes");
                    Views.get().open(player, VipItemsView.class);
                    return;
                }

                final VipData.VIP configVip = vipConfig.data().getById(vipId);
                final ItemStack[] vipItems = vipItemsConfig.data().getByName(configVip.name()).data();

                if (Players.giveItemsChecking(player, vipItems)) {
                    respond(player, "Vip-itens-inventario-cheio");
                    return;
                }

                final short amountReduced = matchingItems.amount() == 1
                    ? (short) -1
                    : 1;

                respond(player, "Vip-itens-colhidos");
                vipItemsRepository.removeItems(player.getUniqueId(), vipId, amountReduced);
                Views.get().open(player, VipItemsView.class);
            }));
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
