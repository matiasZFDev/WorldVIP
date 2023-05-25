package com.worldplugins.vip.controller;

import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.view.KeysView;
import me.post.lib.util.Scheduler;
import me.post.lib.view.Views;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class KeysController {
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull Scheduler scheduler;

    public KeysController(@NotNull ValidKeyRepository validKeyRepository, @NotNull Scheduler scheduler) {
        this.validKeyRepository = validKeyRepository;
        this.scheduler = scheduler;
    }

    public void openView(@NotNull Player player, int page) {
        validKeyRepository.getKeys(player.getName()).thenAccept(keys ->
            scheduler.runTask(0, false, () -> {
                if (!player.isOnline()) {
                    return;
                }

                Views.get().open(player, KeysView.class, new KeysView.Context(page, keys));
            })
        );
    }
}
