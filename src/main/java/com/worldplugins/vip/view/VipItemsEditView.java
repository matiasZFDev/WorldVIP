package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.util.BukkitSerializer;
import com.worldplugins.lib.util.ConfigUtils;
import com.worldplugins.lib.view.CloseableView;
import com.worldplugins.lib.view.ContextView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.vip.config.data.VipItemsData;
import com.worldplugins.vip.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class VipItemsEditView
    extends ContextView<VipItemsEditView.Context>
    implements CloseableView {
    @RequiredArgsConstructor
    public static class Context implements ViewContext {
        private final @NonNull String vipName;
    }

    private final @NonNull ConfigCache<VipItemsData> vipItemsConfig;
    @Override
    public void openView(@NonNull Player player, Context context) {
        final Inventory inventory = Bukkit.createInventory(
            this, 54, "Editar itens - VIP " + context.vipName
        );
        player.openInventory(inventory);
    }

    @Override
    public void onClick(@NonNull InventoryClickEvent event) { }

    @Override
    public void onClose(@NonNull Player player) {
        final String vipName = getContext(player).vipName;
        final ItemStack[] vipItems = player.getOpenInventory().getTopInventory().getContents();

        ConfigUtils.update(vipItemsConfig, config ->
            config.set(vipName, BukkitSerializer.serialize(vipItems))
        );
        player.respond("Itens-vip-salvos", message -> message.replace(
            "@vip".to(vipName)
        ));
    }
}
