package com.worldplugins.vip.controller;

import com.worldplugins.lib.config.cache.menu.MenuContainer;
import com.worldplugins.lib.manager.view.MenuContainerManager;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.config.menu.KeysMenuContainer;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.view.KeysView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    ViewExtensions.class
})

@RequiredArgsConstructor
public class KeysController {
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull MenuContainerManager menuContainerManager;

    public void openView(@NonNull Player player, int page) {
        final MenuContainer keysMenuContainer = menuContainerManager.get(KeysMenuContainer.class);
        final List<Integer> slots = keysMenuContainer.getData().getData("Slots");

        validKeyRepository.getKeys(player.getName()).thenAccept(keys ->
            scheduler.newTask(() -> {
                final int totalPages = keys.size() <= slots.size()
                    ? 1
                    : keys.size() / slots.size();
                final Collection<ValidVipKey> pageKeys = keys.stream()
                    .skip((long) page * slots.size())
                    .limit(slots.size())
                    .collect(Collectors.toList());
                player.openView(KeysView.class, new KeysView.Context(page, totalPages, pageKeys));
            }).run()
        );
    }
}
