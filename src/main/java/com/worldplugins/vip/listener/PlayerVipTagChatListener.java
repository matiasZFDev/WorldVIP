package com.worldplugins.vip.listener;

import br.com.devpaulo.legendchat.api.events.ChatMessageEvent;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.VipPlayer;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class PlayerVipTagChatListener implements Listener {
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public PlayerVipTagChatListener(
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.playerService = playerService;
        this.vipConfig = vipConfig;
    }

    @EventHandler
    public void onChat(ChatMessageEvent event) {
        final VipPlayer vipPlayer = playerService.getById(event.getSender().getUniqueId());

        if (vipPlayer == null || vipPlayer.activeVip() == null) {
            return;
        }

        event.setTagValue("vip", vipConfig.data().getById(vipPlayer.activeVip().id()).display());
    }
}
