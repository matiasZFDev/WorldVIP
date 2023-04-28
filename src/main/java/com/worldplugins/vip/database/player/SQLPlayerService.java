package com.worldplugins.vip.database.player;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.vip.database.player.model.*;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ExtensionMethod({
    UUIDExtensions.class
})

public class SQLPlayerService implements PlayerService {
    private final @NonNull Executor executor;
    private final @NonNull SQLExecutor sqlExecutor;
    private final @NonNull Cache<UUID, VipPlayer> players;

    private static final @NonNull String PLAYER_TABLE = "worldvip_jogadores";
    private static final @NonNull String OWNING_TABLE = "worldvip_vips";

    public SQLPlayerService(
        @NonNull Executor executor,
        @NonNull SQLExecutor sqlExecutor,
        @NonNull Cache<UUID, VipPlayer> players
    ) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        this.players = players;
        createTables();
        loadPlayers();
    }

    private void createTables() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXSITS " + PLAYER_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL, " +
                "vip_duration INT NOT NULL, " +
                "PRIMARY KEY(player_id)" +
            ")"
        );
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXSITS " + OWNING_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL, " +
                "vip_duration INT NOT NULL" +
            ")"
        );
    }

    private void loadPlayers() {
        final Map<UUID, VipPlayer> players = sqlExecutor.executeQuery(
            "SELECT * FROM " + PLAYER_TABLE,
            statement -> {},
            result -> {
                final Map<UUID, VipPlayer> data = new HashMap<>();

                while (result.next()) {
                    final UUID playerId = ((byte[]) result.get("player_id")).toUUID();
                    final VipPlayer vipPlayer = new VipPlayer(
                        playerId,
                        new VIP(
                            result.get("vip_id"), VipType.fromId(result.get("vip_type")),
                            result.get("vip_duration")
                        ),
                        new OwningVIPs(new ArrayList<>(3))
                    );

                    data.put(playerId, vipPlayer);
                }

                return data;
            }
        );

        sqlExecutor.executeQuery(
            "SELECT * FROM " + OWNING_TABLE,
            statement -> {},
            result -> {
                while (result.next()) {
                    final UUID playerId = ((byte[]) result.get("player_id")).toUUID();

                    players.computeIfAbsent(playerId, id -> new VipPlayer(
                        id, null, new OwningVIPs(new ArrayList<>(3))
                    ));

                    players.get(playerId).getOwningVips().add(
                        new VIP(
                            result.get("vip_id"), VipType.fromId(result.get("vip_type")),
                            result.get("vip_duration")
                        )
                    );
                }

                return null;
            }
        );

        players.forEach(this.players::set);
    }

    @Override
    public VipPlayer getById(@NonNull UUID playerId) {
        return players.get(playerId);
    }

    @Override
    public void setVip(@NonNull UUID playerId, @NonNull VIP vip) {
        final VipPlayer playerCache = players.get(playerId);

        if (playerCache != null) {
            playerCache.setActiveVip(vip);
        } else {
            final VipPlayer vipPlayer = new VipPlayer(
                playerId,
                vip,
                new OwningVIPs(new ArrayList<>(0))
            );
            players.set(playerId, vipPlayer);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + PLAYER_TABLE + "(player_id, vip_id, vip_type, vip_duration) VALUES(?,?,?,?)",
            statement -> {
                statement.set(1, playerId.getBytes());
                statement.set(2, vip.getId());
                statement.set(3, vip.getType());
                statement.set(4, vip.getDuration());
            }
        ), executor);
    }

    @Override
    public void removeVip(@NonNull UUID playerId) {
        final VipPlayer playerCache = players.get(playerId);
        playerCache.setActiveVip(null);

        final boolean uncache = playerCache.getOwningVips().getVips().isEmpty();

        if (uncache) {
            players.remove(playerId);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + PLAYER_TABLE + " WHERE player_id=?",
            statement -> statement.set(1, playerId.getBytes())
        ));
    }

    @Override
    public void updatePrimaryVip(@NonNull Collection<VipPlayer> players) {
        CompletableFuture.runAsync(() -> sqlExecutor.executeBatch(
            "UPDATE " + PLAYER_TABLE + " SET vip_duration=? WHERE player_id=?",
            statement ->
                players.forEach(vipPlayer -> {
                    statement.set(1, vipPlayer.getActiveVip().getDuration());
                    statement.set(2, vipPlayer);
                    statement.addBatch();
                })
        ), executor);
    }

    @Override
    public void addOwningVip(@NonNull UUID playerId, @NonNull VIP vip) {
        final OwningVIP newVip = players.get(playerId).getOwningVips().add(vip);
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + OWNING_TABLE + "(player_id, vip_id, vip_type, vip_duration) VALUES(?,?,?,?)",
            statement -> {
                statement.set(1, playerId.getBytes());
                statement.set(2, newVip.getId());
                statement.set(3, newVip.getType());
                statement.set(4, newVip.getDuration());
            }
        ), executor);
    }

    @Override
    public void removeOwningVip(@NonNull UUID playerId, @NonNull OwningVIP owningVip) {
        players.get(playerId).getOwningVips().remove(owningVip.getId(), owningVip.getType());
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + OWNING_TABLE + " WHERE player_id=? AND vip_id=? AND vip_type=?",
            statement -> {
                statement.set(1, playerId);
                statement.set(2, owningVip.getId());
                statement.set(3, owningVip.getType().getId());
            }
        ), executor);
    }

    @Override
    public void updateOwningVips(@NonNull Map<UUID, Collection<OwningVIP>> vips) {
        CompletableFuture.runAsync(() -> sqlExecutor.executeBatch(
            "UPDATE " + OWNING_TABLE +
                " SET vip_duration=? " +
                "WHERE player_id=? AND vip_id=? AND vip_type=?",
            statement ->
                vips.forEach((playerId, owningVips) -> {
                    owningVips.forEach(owningVip -> {
                        statement.set(1, owningVip.getDuration());
                        statement.set(2, playerId.getBytes());
                        statement.set(3, owningVip.getId());
                        statement.set(4, owningVip.getType().getId());
                        statement.addBatch();
                    });
                })
        ), executor);
    }
}
