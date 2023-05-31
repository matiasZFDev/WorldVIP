package com.worldplugins.vip.view;

import com.worldplugins.lib.config.common.ItemDisplay;
import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.util.Strings;
import com.worldplugins.lib.view.ConfigContextBuilder;
import com.worldplugins.lib.view.PageConfigContextBuilder;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.key.KeyGeneratorMatcher;
import com.worldplugins.vip.util.VipDuration;
import me.post.deps.nbt_api.nbtapi.NBTCompound;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NBTs;
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

import java.util.List;

import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class KeysView implements View {
    public static class Context {
        private final int page;

        public Context(int page) {
            this.page = page;
        }
    }

    private final @NotNull MenuModel menuModel;
    private final @NotNull ViewContext viewContext;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull Scheduler scheduler;
    private final @NotNull KeyGeneratorMatcher keyGeneratorMatcher;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public static final @NotNull String KEY_CODE_TAG = "wvip_view_key_code";

    public KeysView(
        @NotNull MenuModel menuModel,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull Scheduler scheduler,
        @NotNull KeyGeneratorMatcher keyGeneratorMatcher,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.menuModel = menuModel;
        this.viewContext = new MapViewContext();
        this.validKeyRepository = validKeyRepository;
        this.scheduler = scheduler;
        this.keyGeneratorMatcher = keyGeneratorMatcher;
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

        validKeyRepository.getKeys(player.getName()).thenAccept(keys -> scheduler.runTask(0, false, () -> {
            if (viewContext.getViewer(player.getUniqueId()) == null) {
                return;
            }

            openWithKeys(player, data, keys);
        }));
    }

    public void openWithKeys(
        @NotNull Player player,
        @Nullable Object data,
        @NotNull List<ValidVipKey> keys
    ) {
        final Context context = data == null
            ? new Context(0)
            : (Context) data;
        final int page = context.page;
        final List<Integer> slots = menuModel.data().getData("Slots");
        final ItemDisplay keyDisplay = menuModel.data().getData("Display-key");

        PageConfigContextBuilder.of(
                menuModel,
                currentPage -> Views.get().open(player, KeysView.class, new Context(currentPage)),
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
                click -> Views.get().open(click.whoClicked(), VipMenuView.class)
            )
            .previousPageButtonAs("Pagina-anterior")
            .nextPageButtonAs("Pagina-seguinte")
            .withSlots(slots)
            .fill(
                keys,
                key -> {
                    final VipData.VIP configVip = vipConfig.data().getById(key.vipId());
                    return ItemTransformer.of(configVip.item().clone())
                        .display(keyDisplay)
                        .nameFormat(to("@vip", configVip.display()))
                        .loreFormat(
                            to("@tipo", key.vipType().getName().toUpperCase()),
                            to("@tempo", VipDuration.format(key)),
                            to("@usos", String.valueOf(key.usages()))
                        )
                        .colorMeta()
                        .addNBT(nbtItem -> nbtItem.setString(KEY_CODE_TAG, key.code())                        )
                        .transform();
                },
                click -> {
                    final String keyCode = NBTs.getTagValue(
                        requireNonNull(click.clickedItem()),
                        KEY_CODE_TAG,
                        NBTCompound::getString
                    );

                    keyGeneratorMatcher.tryMatch(player, keyCode, page, key ->
                        Views.get().open(
                            player,
                            ManageKeyView.class,
                            new ManageKeyView.Context(key, page)
                        )
                    );
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
