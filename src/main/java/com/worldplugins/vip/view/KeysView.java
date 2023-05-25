package com.worldplugins.vip.view;

import com.worldplugins.lib.config.common.ItemDisplay;
import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.util.ItemTransformer;
import com.worldplugins.lib.util.Strings;
import com.worldplugins.lib.view.PageConfigContextBuilder;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.controller.KeysController;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.key.KeyManagement;
import com.worldplugins.vip.util.VipDuration;
import me.post.deps.nbt_api.nbtapi.NBTCompound;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.NBTs;
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

import java.util.List;

import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class KeysView implements View {
    public static class Context {
        private final int page;
        private final @NotNull List<ValidVipKey> keys;

        public Context(int page, @NotNull List<ValidVipKey> keys) {
            this.page = page;
            this.keys = keys;
        }
    }

    private final @NotNull MenuModel menuModel;
    private final @NotNull ViewContext viewContext;
    private final @NotNull KeysController keysController;
    private final @NotNull KeyManagement keyManagement;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public static final @NotNull String KEY_CODE_TAG = "wvip_view_key_code";

    public KeysView(
        @NotNull MenuModel menuModel,
        @NotNull KeysController keysController,
        @NotNull KeyManagement keyManagement,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.menuModel = menuModel;
        this.viewContext = new MapViewContext();
        this.keysController = keysController;
        this.keyManagement = keyManagement;
        this.vipConfig = vipConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final Context context = (Context) requireNonNull(data);
        final int page = context.page;
        final List<Integer> slots = menuModel.data().getData("Slots");
        final ItemDisplay keyDisplay = menuModel.data().getData("Display-key");

        PageConfigContextBuilder.of(
                menuModel,
                currentPage -> keysController.openView(player, currentPage),
                context.page
            )
            .editTitle((pageInfo, title) ->
                Strings.replace(
                    title,
                    to("@atual", String.valueOf(pageInfo.page() + 1)),
                    to("@totais", String.valueOf(pageInfo.totalPages()))
                )
            )
            .handleMenuItemClick("Voltar", click ->
                Views.get().open(click.whoClicked(), VipMenuView.class)
            )
            .previousPageButtonAs("Pagina-anterior")
            .nextPageButtonAs("Pagina-seguinte")
            .withSlots(slots)
            .fill(
                context.keys,
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
                        .addNBT(KEY_CODE_TAG, key.code())
                        .transform();
                },
                click -> {
                    final String keyCode = NBTs.getTagValue(
                        requireNonNull(click.clickedItem()),
                        KEY_CODE_TAG,
                        NBTCompound::getString
                    );
                    keyManagement.manage(
                        player, keyCode, page, key -> {
                            // open key management view
                        }
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
