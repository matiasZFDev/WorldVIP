package com.worldplugins.vip.manager;

import com.worldplugins.vip.database.player.model.VipPlayer;
import me.post.lib.common.Updatable;
import me.post.lib.database.cache.Cache;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

public class VipTopManager implements Updatable {
    public static class TopPlayer {
        private final @NotNull UUID playerId;
        private final double spent;

        public TopPlayer(@NotNull UUID playerId, double spent) {
            this.playerId = playerId;
            this.spent = spent;
        }

        public @NotNull UUID playerId() {
            return playerId;
        }

        public double spent() {
            return spent;
        }
    }

    private final @NotNull Cache<UUID, VipPlayer> players;
    private Collection<TopPlayer> topPlayers;

    public VipTopManager(@NotNull Cache<UUID, VipPlayer> players) {
        this.players = players;
        this.topPlayers = new ArrayList<>();
    }

    @Override
    public void update() {
        topPlayers = players.getValues().stream()
            .sorted(Comparator.comparingDouble(VipPlayer::spent))
            .limit(10)
            .map(vipPlayer -> new TopPlayer(vipPlayer.id(), vipPlayer.spent()))
            .collect(Collectors.toList());
    }

    public @NotNull Collection<TopPlayer> getTop() {
        return topPlayers;
    }
}
