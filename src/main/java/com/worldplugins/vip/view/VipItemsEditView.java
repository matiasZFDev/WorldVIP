package com.worldplugins.vip.view;

import com.worldplugins.vip.config.data.VipItemsData;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.BukkitSerializer;
import me.post.lib.util.Configurations;
import me.post.lib.view.View;
import me.post.lib.view.action.ViewClick;
import me.post.lib.view.action.ViewClose;
import me.post.lib.view.context.ViewContext;
import me.post.lib.view.context.builder.ContextBuilder;
import me.post.lib.view.context.impl.MapViewContext;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.worldplugins.vip.Response.respond;
import static java.util.Objects.requireNonNull;
import static me.post.lib.util.Pairs.to;

public class VipItemsEditView implements View {
    public static class Context {
        private final @NotNull String vipName;

        public Context(@NotNull String vipName) {
            this.vipName = vipName;
        }
    }

    private final @NotNull ViewContext viewContext;
    private final @NotNull ConfigModel<VipItemsData> vipItemsConfig;

    public VipItemsEditView(@NotNull ConfigModel<VipItemsData> vipItemsConfig) {
        this.viewContext = new MapViewContext();
        this.vipItemsConfig = vipItemsConfig;
    }

    @Override
    public void open(@NotNull Player player, @Nullable Object data) {
        final Context context = (Context) requireNonNull(data);
        final ItemStack[] vipItems = vipItemsConfig.data().getByName(context.vipName).data();

        ContextBuilder.of(6, "Editar itens")
            .apply(builder -> {
                for (int slot = 0; slot < vipItems.length; slot++) {
                    if (vipItems[slot] == null) {
                        continue;
                    }

                    builder.item(slot, vipItems[slot].clone());
                }
            })
            .build(viewContext, player, data);
    }

    @Override
    public void onClick(@NotNull ViewClick click) {

    }

    @Override
    public void onClose(@NotNull ViewClose close) {
        final Player player = close.whoCloses();
        final Context context = (Context) requireNonNull(
            viewContext.getViewer(player.getUniqueId()).data()
        );
        final ItemStack[] vipItems = close.closedView().getTopInventory().getContents();

        Configurations.update(vipItemsConfig, config ->
            config.set(context.vipName, BukkitSerializer.serialize(vipItems))
        );
        respond(player, "Itens-vip-salvos", message -> message.replace(
            to("@vip", context.vipName)
        ));
    }
}
