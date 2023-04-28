package com.worldplugins.vip.manager;

import com.worldplugins.lib.common.Updatable;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.vip.database.player.model.VipPlayer;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class VipTopManager implements Updatable {
    @RequiredArgsConstructor
    @Getter
    public static class TopPlayer {
        private final @NonNull UUID playerId;
        private final double spent;
    }

    private final @NonNull Cache<UUID, VipPlayer> players;
    private Collection<TopPlayer> topPlayers;

    @Override
    public void update() {
        topPlayers = players.getValues().stream()
            .sorted(Comparator.comparingDouble(VipPlayer::getSpent))
            .limit(10)
            .map(vipPlayer -> new TopPlayer(vipPlayer.getId(), vipPlayer.getSpent()))
            .collect(Collectors.toList());
    }

    public @NonNull Collection<TopPlayer> getTop() {
        return topPlayers;
    }
}
