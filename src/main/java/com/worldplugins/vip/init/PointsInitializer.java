package com.worldplugins.vip.init;

import com.worldplugins.vip.manager.PointsManager;
import com.worldplugins.vip.util.BukkitUtils;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import com.ystoreplugins.ypoints.api.yPointsAPI;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

import static me.post.lib.util.Colors.color;

public class PointsInitializer {
    private enum PointsSupport {
        PLAYER_POINTS("PlayerPoints") {
            @Override
            public @NotNull PointsManager createManager() {
                return new PointsManager() {
                    @Override
                    public boolean has(@NotNull UUID playerId, double points) {
                        return PlayerPoints.getInstance().getAPI().look(playerId) >= points;
                    }

                    @Override
                    public void withdraw(@NotNull UUID playerId, double points) {
                        PlayerPoints.getInstance().getAPI().take(playerId, (int) points);
                    }

                    @Override
                    public void deposit(@NotNull UUID playerId, double points) {
                        PlayerPoints.getInstance().getAPI().give(playerId, (int) points);
                    }
                };
            }
        },
        Y_POINTS("yPoints") {
            @Override
            public @NotNull PointsManager createManager() {
                return new PointsManager() {
                    @Override
                    public boolean has(@NotNull UUID playerId, double points) {
                        return yPointsAPI.has(Bukkit.getPlayer(playerId).getName(), points);
                    }

                    @Override
                    public void withdraw(@NotNull UUID playerId, double points) {
                        yPointsAPI.withdraw(
                            Bukkit.getPlayer(playerId).getName(),
                            points,
                            true
                        );
                    }

                    @Override
                    public void deposit(@NotNull UUID playerId, double points) {
                        yPointsAPI.deposit(BukkitUtils.getPlayerName(playerId), points, true);
                    }
                };
            }
        };

        private final @NotNull String pluginName;

        PointsSupport(@NotNull String pluginName) {
            this.pluginName = pluginName;
        }

        public abstract @NotNull PointsManager createManager();
    }

    private final @NotNull Plugin plugin;

    public PointsInitializer(@NotNull Plugin plugin) {
        this.plugin = plugin;
    }

    public @NotNull PointsManager init() {
        final PointsManager manager = Arrays.stream(PointsSupport.values())
            .filter(support -> plugin.getServer().getPluginManager().isPluginEnabled(support.pluginName))
            .findFirst()
            .map(PointsSupport::createManager)
            .orElse(null);

        if (manager == null) {
            Bukkit.getConsoleSender().sendMessage(color("&b[WorldVIP] Plugin de pontos não encontrado."));
            Bukkit.getConsoleSender().sendMessage(color("&b[WorldVIP] Desligando plugin..."));
            plugin.getServer().getPluginManager().disablePlugin(plugin);
            return null;
        }

        return manager;
    }
}
