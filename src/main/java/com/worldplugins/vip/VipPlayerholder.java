package com.worldplugins.vip;

import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.player.PlayerService;
import com.worldplugins.vip.database.player.model.VipPlayer;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.post.lib.config.model.ConfigModel;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VipPlayerholder extends PlaceholderExpansion {
    private final @NotNull PlayerService playerService;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<VipData> vipConfig;

    public VipPlayerholder(
        @NotNull PlayerService playerService,
        @NotNull ConfigModel<MainData> mainConfig,
        @NotNull ConfigModel<VipData> vipConfig
    ) {
        this.playerService = playerService;
        this.mainConfig = mainConfig;
        this.vipConfig = vipConfig;
    }

    @Override
    public String getIdentifier() {
        return "wvip";
    }

    @Override
    public String getAuthor() {
        return "worldplugins";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, String params) {
        final VipPlayer vipPlayer = playerService.getById(player.getUniqueId());

        if (vipPlayer == null || vipPlayer.activeVip() == null) {
            return mainConfig.data().noPrimaryVipReplacement();
        }

        return vipConfig.data().getById(vipPlayer.activeVip().id()).display();
    }
}
