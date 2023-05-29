package com.worldplugins.vip.listener;

import com.worldplugins.vip.database.pending.PendingVipRepository;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.handler.OwningVipHandler;
import com.worldplugins.vip.handler.VipHandler;
import me.post.lib.util.Scheduler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class ActivePendingVipsListener implements Listener {
    private final @NotNull PendingVipRepository pendingVipRepository;
    private final @NotNull Scheduler scheduler;
    private final @NotNull VipHandler vipHandler;

    public ActivePendingVipsListener(
        @NotNull PendingVipRepository pendingVipRepository,
        @NotNull Scheduler scheduler,
        @NotNull VipHandler vipHandler
    ) {
        this.pendingVipRepository = pendingVipRepository;
        this.scheduler = scheduler;
        this.vipHandler = vipHandler;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();

        pendingVipRepository.getPendingVips(player.getName()).thenAccept(pendingVips -> {
           if (pendingVips.isEmpty()) {
               return;
           }

           scheduler.runTask(0, false, () -> {
               pendingVips.forEach(pendingVip ->
                   vipHandler.activate(
                       player.getUniqueId(),
                       new VIP(pendingVip.id(), pendingVip.type(), pendingVip.duration()),
                       true
                   )
               );

               pendingVipRepository.removePendings(player.getName());
           });
        });
    }
}
