package com.worldplugins.vip.view;

import com.worldplugins.lib.config.common.ItemDisplay;
import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.util.Strings;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.lib.view.PageConfigContextBuilder;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.market.SellingKey;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.database.player.model.VipType;
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

import java.util.List;
import java.util.stream.Collectors;

import static com.worldplugins.vip.Response.respond;
import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class ManageSellingKeysView implements View {
    public static class Context {
        private final int page;

        public Context(int page) {
            this.page = page;
        }
    }

    private final @NotNull ViewContext viewContext;
    private final @NotNull MenuModel menuModel;
    private final @NotNull SellingKeyRepository sellingKeyRepository;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull Scheduler scheduler;
    private final @NotNull ConfigModel<VipData> vipConfig;

    private static final @NotNull String SELLING_KEY_TAG = "wvip_manage_selling_key_code";

    public ManageSellingKeysView(
        @NotNull MenuModel menuModel,
        @NotNull SellingKeyRepository sellingKeyRepository,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull Scheduler scheduler,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.viewContext = new MapViewContext();
        this.menuModel = menuModel;
        this.sellingKeyRepository = sellingKeyRepository;
        this.validKeyRepository = validKeyRepository;
        this.scheduler = scheduler;
        this.vipConfig = vipConfig;
    }


    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        ConfigContextBuilder.withModel(menuModel)
            .editTitle(title ->
                Strings.replace(
                    title,
                    to("@atual", "?"),
                    to("@totais", "?")
                )
            )
            .removeMenuItem("Voltar", "Vazio", "Pagina-seguinte", "Pagina-anterior")
            .build(viewContext, player, null);

        sellingKeyRepository.getAllKeys().thenAccept(sellingKeys -> scheduler.runTask(0, false, () -> {
            if (!player.isOnline()) {
                return;
            }

            if (viewContext.getViewer(player.getUniqueId()) == null) {
                return;
            }

            openWithKeys(
                player,
                data,
                sellingKeys.stream()
                    .filter(sellingKey -> sellingKey.sellerId().equals(player.getUniqueId()))
                    .collect(Collectors.toList())
            );
        }));
    }

    private void openWithKeys(@NotNull Player player, @Nullable Object data, @NotNull List<SellingKey> keys) {
        final Context context = data == null
            ? new Context(0)
            : (Context) data;
        final List<Integer> slots = menuModel.data().getData("Slots");
        final ItemDisplay keyDisplay = menuModel.data().getData("Display-key");

        PageConfigContextBuilder.of(
                menuModel,
                page -> Views.get().open(player, ManageSellingKeysView.class, context),
                context.page
            )
            .editTitle((pageInfo, title) ->
                Strings.replace(
                    title,
                    to("@atual", String.valueOf(pageInfo.page() + 1)),
                    to("@totais", String.valueOf(pageInfo.totalPages()))
                )
            )
            .removeMenuItem("Carregando")
            .apply(builder -> {
                if (keys.isEmpty()) {
                    return;
                }

                builder.removeMenuItem("Vazio");
            })
            .handleMenuItemClick(
                "Voltar",
                click -> Views.get().open(player, KeyMarketView.class)
            )
            .nextPageButtonAs("Pagina-seguinte")
            .previousPageButtonAs("Pagina-anterior")
            .withSlots(slots)
            .fill(
                keys,
                key -> {
                    final VipData.VIP configVip = vipConfig.data().getById(key.vipId());
                    final String keyDuration = key.vipType() == VipType.PERMANENT
                        ? GlobalValues.PERMANENT_DURATION
                        : Time.toFormat(key.vipDuration());

                    return ItemTransformer.of(configVip.item())
                        .display(keyDisplay)
                        .nameFormat(to("@vip", configVip.display()))
                        .loreFormat(
                            to("@tipo", key.vipType().getName().toUpperCase()),
                            to("@tempo", keyDuration),
                            to("@usos", String.valueOf(key.vipUsages())),
                            to("@preco", NumberFormats.comma(key.price()))
                        )
                        .colorMeta()
                        .addNBT(nbtItem -> nbtItem.setString(SELLING_KEY_TAG, key.code()))
                        .transform();
                },
                click -> {
                    final String sellingKeyCode = NBTs.getTagValue(
                        requireNonNull(click.clickedItem()),
                        SELLING_KEY_TAG,
                        NBTCompound::getString
                    );

                    final SellingKey matchingCodeKey = keys.stream()
                        .filter(key -> key.code().equals(sellingKeyCode))
                        .findFirst()
                        .orElse(null);

                    if (matchingCodeKey == null) {
                        respond(player, "Retirar-key-mercado-inexistente");
                        return;
                    }

                    final ValidVipKey returnedKey = new ValidVipKey(
                        player.getName(),
                        matchingCodeKey.code(),
                        matchingCodeKey.vipId(),
                        matchingCodeKey.vipType(),
                        matchingCodeKey.vipDuration(),
                        matchingCodeKey.vipUsages()
                    );

                    sellingKeyRepository.removeKey(matchingCodeKey);
                    validKeyRepository.addKey(returnedKey);
                    Views.get().open(player, ManageSellingKeysView.class, context);
                    respond(player, "Key-mercado-retirada");
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
