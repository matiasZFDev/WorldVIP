package com.worldplugins.vip.controller;

import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.view.VipItemsView;
import me.post.lib.util.Scheduler;
import me.post.lib.view.Views;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VipItemsController {
    private final @NotNull VipItemsRepository vipItemsRepository;
    private final @NotNull Scheduler scheduler;

    public VipItemsController(@NotNull VipItemsRepository vipItemsRepository, @NotNull Scheduler scheduler) {
        this.vipItemsRepository = vipItemsRepository;
        this.scheduler = scheduler;
    }

    public void openView(@NotNull Player player) {
        vipItemsRepository.getItems(player.getUniqueId()).thenAccept(itemList ->
            scheduler.runTask(0, false, () -> {
                if (!player.isOnline()) {
                    return;
                }

                Views.get().open(player, VipItemsView.class, new VipItemsView.Context(itemList));
            })
        );
    }
}
