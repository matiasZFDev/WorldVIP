package com.worldplugins.vip.view;

import com.worldplugins.lib.config.common.ItemDisplay;
import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.util.Strings;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.lib.view.PageConfigContextBuilder;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.util.BukkitUtils;
import com.worldplugins.vip.util.VipDuration;
import com.worldplugins.vip.view.data.KeyMarketOrder;
import me.post.deps.nbt_api.nbtapi.NBTCompound;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NBTs;
import me.post.lib.util.NumberFormats;
import me.post.lib.util.Scheduler;
import me.post.lib.util.Time;
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

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.worldplugins.vip.Response.respond;
import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class KeyMarketView implements View {
    public static class Context {
        private final int page;
        private final @NotNull KeyMarketOrder order;

        public Context(int page, @NotNull KeyMarketOrder order) {
            this.page = page;
            this.order = order;
        }
    }

    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull Scheduler scheduler;
    private final @NotNull ConfigModel<VipData> vipConfig;

    private static final @NotNull String SELLING_KEY_TAG = "wvip_selling_key_code";

    public KeyMarketView(
        @NotNull MenuModel menuModel,
        @NotNull SellingKeyRepository sellingKeyRepository,
        @NotNull Scheduler scheduler,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.sellingKeyRepository = sellingKeyRepository;
        this.scheduler = scheduler;
        this.vipConfig = vipConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        ConfigContextBuilder.withModel(menuModel)
            .asViewState()
            .editTitle(title ->
                Strings.replace(
                    title,
                    to("@atual", "?"),
                    to("@totais", "?")
                )
            )
            .removeMenuItem("Voltar", "Vazio", "Pagina-seguinte", "Pagina-anterior")
            .build(viewContext, player, null);

        sellingKeyRepository.getAllKeys().thenAccept(keys -> scheduler.runTask(0, false, () -> {
            if (viewContext.getViewer(player.getUniqueId()) == null) {
                return;
            }

            openMarketView(player, data, keys);
        }));
    }

    public void openMarketView(
        @NotNull Player player,
        @Nullable Object data,
        @NotNull List<SellingKey> sellingKeys
    ) {
        final Context context = data == null
            ? new Context(0, KeyMarketOrder.NONE)
            : (Context) data;
        final List<Integer> slots = menuModel.data().getData("Slots");
        final ItemDisplay keyDisplay = menuModel.data().getData("Display-key");

        PageConfigContextBuilder.of(
                menuModel,
                page -> Views.get().open(player, KeyMarketView.class, context),
                context.page
            )
            .asViewState()
            .editTitle((pageInfo, title) ->
                Strings.replace(
                    title,
                    to("@atual",String.valueOf(pageInfo.page() + 1)),
                    to("@totais",String.valueOf(pageInfo.totalPages()))
                )
            )
            .removeMenuItem("Carregando")
            .apply(builder -> {
                if (sellingKeys.isEmpty()) {
                    return;
                }

                builder.removeMenuItem("Vazio");
            })
            .handleMenuItemClick(
                "Minhas-keys",
                click -> Views.get().open(player, ManageSellingKeysView.class)
            )
            .handleMenuItemClick(
                "Voltar",
                click -> Views.get().open(click.whoClicked(), VipMenuView.class)
            )
            .apply(builder -> {
                final Collection<KeyMarketOrder> orders = KeyMarketOrder.orders();

                orders.remove(context.order);
                orders.forEach(order -> builder.removeMenuItem(order.configItemId()));
                builder.handleMenuItemClick(
                    context.order.configItemId(),
                    click -> {
                        if (click.clickType().isRightClick()) {
                            Views.get().open(
                                player.getPlayer(),
                                KeyMarketView.class,
                                new Context(context.page, context.order.next())
                            );
                            return;
                        }

                        if (click.clickType().isLeftClick()) {
                            Views.get().open(
                                player.getPlayer(),
                                KeyMarketView.class,
                                new Context(context.page, context.order.alternate())
                            );
                        }
                    }
                );
            })
            .nextPageButtonAs("Pagina-seguinte")
            .previousPageButtonAs("Pagina-anterior")
            .withSlots(slots)
            .fill(
                sellingKeys.stream()
                    .sorted(context.order.comparator())
                    .collect(Collectors.toList()),
                key -> {
                    final VipData.VIP configVip = vipConfig.data().getById(key.vipId());
                    final int postTimeElapsed = (int) TimeUnit
                        .MILLISECONDS
                        .toSeconds(System.nanoTime() - key.postTimestamp());
                    return ItemTransformer.of(configVip.item().clone())
                        .display(keyDisplay)
                        .nameFormat(to("@vip", configVip.display()))
                        .loreFormat(
                            to("@vendedor", BukkitUtils.getPlayerName(key.sellerId())),
                            to("@preco", NumberFormats.suffixed(key.price())),
                            to("@tipo", key.vipType().getName().toUpperCase()),
                            to("@tempo", VipDuration.format(key)),
                            to("@usos", String.valueOf(key.vipUsages())),
                            to("@data-postagem", Time.toFormat(postTimeElapsed))
                        )
                        .addNBT(SELLING_KEY_TAG, key.code())
                        .transform();
                },
                click -> {
                    final String code = NBTs.getTagValue(
                        requireNonNull(click.clickedItem()),
                        SELLING_KEY_TAG,
                        NBTCompound::getString
                    );

                    sellingKeyRepository
                        .getAllKeys()
                        .thenAccept(keys -> scheduler.runTask(0, false, () -> {
                            final SellingKey matchingKey = keys.stream()
                                .filter(key -> key.code().equals(code))
                                .findFirst()
                                .orElse(null);

                            if (matchingKey == null) {
                                respond(player, "Mercado-key-inexistente");
                                Views.get().open(player, KeyMarketView.class, context);
                                return;
                            }

                            Views.get().open(
                                player,
                                KeyMarketPurchaseView.class,
                                new KeyMarketPurchaseView.Context(matchingKey, context)
                            );
                        }));
                }
            )
            .build(viewContext, player, null);
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
