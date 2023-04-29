package com.worldplugins.vip.controller;

import com.worldplugins.lib.config.cache.menu.MenuContainer;
import com.worldplugins.lib.manager.view.MenuContainerManager;
import com.worldplugins.vip.config.menu.OwningVipsMenuContainer;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.OwningVIP;
import com.worldplugins.vip.database.player.model.VipPlayer;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.view.OwningVipsView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    ViewExtensions.class
})

@RequiredArgsConstructor
public class OwningVipsController {
    private final @NonNull PlayerService playerService;
    private final @NonNull MenuContainerManager menuContainerManager;

    public void openView(@NonNull Player player, int page) {
        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());
        final Collection<OwningVIP> owningVips = vipPlayer == null
            ? Collections.emptyList()
            : vipPlayer.getOwningVips().getVips();
        final MenuContainer keysMenuContainer = menuContainerManager.get(OwningVipsMenuContainer.class);
        final List<Integer> slots = keysMenuContainer.getData().getData("Slots");

        final int totalPages = owningVips.size() <= slots.size()
            ? 1
            : owningVips.size() / slots.size();
        final Collection<OwningVIP> pageVips = owningVips.stream()
            .skip((long) page * slots.size())
            .limit(slots.size())
            .collect(Collectors.toList());
        player.openView(OwningVipsView.class, new OwningVipsView.Context(page, totalPages, pageVips));
    }
}
