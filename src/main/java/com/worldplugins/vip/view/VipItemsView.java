package com.worldplugins.vip.view;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.menu.ItemProcessResult;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.config.cache.menu.MenuItem;
import com.worldplugins.lib.config.data.ItemDisplay;
import com.worldplugins.lib.extension.CollectionExtensions;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.bukkit.ItemExtensions;
import com.worldplugins.lib.extension.bukkit.NBTExtensions;
import com.worldplugins.lib.extension.bukkit.PlayerExtensions;
import com.worldplugins.lib.util.MenuItemsUtils;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.view.MenuDataView;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.lib.view.annotation.ViewSpec;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.data.VipItemsData;
import com.worldplugins.vip.config.menu.VipItemsMenuContainer;
import com.worldplugins.vip.controller.VipItemsController;
import com.worldplugins.vip.database.items.VipItems;
import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.util.ItemFactory;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.minecraft.server.v1_8_R3.NBTTagByte;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    CollectionExtensions.class,
    ItemExtensions.class,
    GenericExtensions.class,
    ViewExtensions.class,
    NBTExtensions.class,
    ResponseExtensions.class,
    PlayerExtensions.class
})

@RequiredArgsConstructor
@ViewSpec(menuContainer = VipItemsMenuContainer.class)
public class VipItemsView extends MenuDataView<VipItemsView.Context> {
    @RequiredArgsConstructor
    public static class Context implements ViewContext {
        private final @NonNull Collection<VipItems> itemsList;
    }

    private final @NonNull VipItemsRepository vipItemsRepository;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull VipItemsController vipItemsController;

    private final @NonNull ConfigCache<MainData> mainConfig;
    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull ConfigCache<VipItemsData> vipItemsConfig;

    @Override
    public @NonNull ItemProcessResult processItems(
        @NonNull Player player,
        Context context,
        @NonNull MenuData menuData
    ) {
        final List<Integer> slots = menuData.getData("Slots");
        final ItemDisplay itemsDisplay = menuData.getData("Display-itens");
        return MenuItemsUtils.newSession(menuData.getItems(), session ->
            session.addDynamics(() ->
                context.itemsList.zip(slots).stream()
                    .map(itemsPair -> {
                        final VipData.VIP configVip = vipConfig.data().getById(itemsPair.first().getVipId());
                        final ItemStack item = configVip.getItem()
                            .display(itemsDisplay)
                            .loreFormat("@quantia".to(String.valueOf(itemsPair.first().getAmount())))
                            .addReferenceValue("vip_id", new NBTTagByte(configVip.getId()));
                        return ItemFactory.dynamicOf("Itens", itemsPair.second(), item);
                    })
                    .collect(Collectors.toList())
            )
        ).build();
    }

    @Override
    public void onClick(@NonNull Player player, @NonNull MenuItem item, @NonNull InventoryClickEvent event) {
        if (item.getId().equals("Voltar")) {
            player.openView(VipMenuView.class);
            return;
        }

        if (item.getId().equals("Itens")) {
            if (!mainConfig.data().storeItems()) {
                player.respond("Coleta-desabilitada");
                return;
            }

            final byte vipId = item.getItem().getReferenceValue("vip_id", NBTTagCompound::getByte);
            vipItemsRepository.getItems(player.getUniqueId()).thenAccept(itemList -> scheduler.newTask(() -> {
                if (!player.isOnline()) {
                    return;
                }

                final VipItems matchingItems = itemList.stream()
                    .filter(items -> items.getVipId() == vipId)
                    .findFirst()
                    .orElse(null);

                if (matchingItems == null) {
                    player.respond("Vip-itens-inexistentes");
                    vipItemsController.openView(player);
                    return;
                }

                final VipData.VIP configVip = vipConfig.data().getById(vipId);
                final ItemStack[] vipItems = vipItemsConfig.data().getByName(configVip.getName()).getData();

                if (player.giveItemsChecking(vipItems)) {
                    player.respond("Vip-itens-inventario-cheio");
                    return;
                }

                final short amountReduced = matchingItems.getAmount() == 1
                    ? (short) -1
                    : 1;
                vipItemsRepository.removeItems(player.getUniqueId(), vipId, amountReduced);
                player.respond("Vip-itens-colhidos");
                vipItemsController.openView(player);
            }).run());
        }
    }
}
