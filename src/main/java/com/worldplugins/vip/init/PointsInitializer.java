package com.worldplugins.vip.init;

import com.worldplugins.lib.common.Initializer;
import com.worldplugins.vip.manager.PointsManager;
import com.worldplugins.vip.util.BukkitUtils;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import com.ystoreplugins.ypoints.api.yPointsAPI;

import java.util.Arrays;
import java.util.UUID;

@RequiredArgsConstructor
public class PointsInitializer implements Initializer<PointsManager> {
    @RequiredArgsConstructor
    private enum PointsSupport {
        PLAYER_POINTS("PlayerPoints") {
            @Override
            public @NonNull PointsManager createManager() {
                return new PointsManager() {
                    @Override
                    public boolean has(@NonNull UUID playerId, double points) {
                        return PlayerPoints.getInstance().getAPI().look(playerId) >= points;
                    }

                    @Override
                    public void withdraw(@NonNull UUID playerId, double points) {
                        PlayerPoints.getInstance().getAPI().take(playerId, (int) points);
                    }

                    @Override
                    public void deposit(@NonNull UUID playerId, double points) {
                        PlayerPoints.getInstance().getAPI().give(playerId, (int) points);
                    }
                };
            }
        },
        Y_POINTS("yPoints") {
            @Override
            public @NonNull PointsManager createManager() {
                return new PointsManager() {
                    @Override
                    public boolean has(@NonNull UUID playerId, double points) {
                        return yPointsAPI.has(Bukkit.getPlayer(playerId).getName(), points);
                    }

                    @Override
                    public void withdraw(@NonNull UUID playerId, double points) {
                        yPointsAPI.withdraw(
                            Bukkit.getPlayer(playerId).getName(),
                            points,
                            true
                        );
                    }

                    @Override
                    public void deposit(@NonNull UUID playerId, double points) {
                        yPointsAPI.deposit(BukkitUtils.getPlayerName(playerId), points, true);
                    }
                };
            }
        };

        private final @NonNull String pluginName;

        public abstract @NonNull PointsManager createManager();
    }

    private final @NonNull Plugin plugin;

    @Override
    public @NonNull PointsManager init() {
        final PointsManager manager = Arrays.stream(PointsSupport.values())
            .filter(support -> plugin.getServer().getPluginManager().isPluginEnabled(support.pluginName))
            .findFirst()
            .map(PointsSupport::createManager)
            .orElse(null);

        if (manager == null) {
            Bukkit.getConsoleSender().sendMessage("[WorldVIP] Plugin de cash não encontrado.");
            Bukkit.getConsoleSender().sendMessage("[WorldVIP] Desligando plugin...");
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return null;
        }

        return manager;
    }
}
