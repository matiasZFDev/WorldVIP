package com.worldplugins.vip.controller;

import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.view.VipItemsView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ViewExtensions.class
})

@RequiredArgsConstructor
public class VipItemsController {
    private final @NonNull VipItemsRepository vipItemsRepository;
    private final @NonNull SchedulerBuilder scheduler;

    public void openView(@NonNull Player player) {
        vipItemsRepository.getItems(player.getUniqueId()).thenAccept(itemList ->
            scheduler.newTask(() ->
                player.openView(VipItemsView.class, new VipItemsView.Context(itemList))
            ).run()
        );
    }
}
