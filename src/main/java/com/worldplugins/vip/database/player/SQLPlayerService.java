package com.worldplugins.vip.database.player;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.vip.database.player.model.*;
import me.post.lib.database.cache.Cache;
import me.post.lib.util.UUIDs;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import static java.util.Objects.requireNonNull;

public class SQLPlayerService implements PlayerService {
    private final @NotNull Executor executor;
    private final @NotNull SQLExecutor sqlExecutor;
    private final @NotNull Cache<UUID, VipPlayer> players;

    private static final @NotNull String SPENT_TABLE = "worldvip_gasto";
    private static final @NotNull String PLAYER_TABLE = "worldvip_jogadores";
    private static final @NotNull String OWNING_TABLE = "worldvip_vips";

    public SQLPlayerService(
        @NotNull Executor executor,
        @NotNull SQLExecutor sqlExecutor,
        @NotNull Cache<UUID, VipPlayer> players
    ) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        this.players = players;
        createTables();
        loadPlayers();
    }

    private void createTables() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + SPENT_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "spent DOUBLE NOT NULL, " +
                "PRIMARY KEY(player_id)" +
            ")"
        );
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + PLAYER_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL, " +
                "vip_duration INT NOT NULL, " +
                "PRIMARY KEY(player_id)" +
            ")"
        );
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + OWNING_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL, " +
                "vip_duration INT NOT NULL" +
            ")"
        );
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + SPENT_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "spent INT NOT NULL, " +
                "PRIMARY KEY(player_id)" +
            ")"
        );
    }

    private void loadPlayers() {
        sqlExecutor.executeQuery(
            "SELECT * FROM " + SPENT_TABLE,
            statement -> {},
            result -> {
                final Map<UUID, VipPlayer> data = new HashMap<>();

                while (result.next()) {
                    final UUID playerId = UUIDs.toUUID(result.get("player_id"));
                    players.set(playerId, new VipPlayer(
                        playerId,
                        result.get("spent"),
                        null,
                        new OwningVIPs(new ArrayList<>(2)
                    )));
                }

                return data;
            }
        );

        sqlExecutor.executeQuery(
            "SELECT * FROM " + PLAYER_TABLE,
            statement -> {},
            result -> {
                while (result.next()) {
                    final UUID playerId = UUIDs.toUUID(result.get("player_id"));
                    players.get(playerId).setActiveVip(
                        new VIP(
                            result.get("vip_id", Byte.class),
                            VipType.fromId(result.get("vip_type", Byte.class)),
                            result.get("vip_duration")
                        )
                    );
                }

                return null;
            }
        );

        sqlExecutor.executeQuery(
            "SELECT * FROM " + OWNING_TABLE,
            statement -> {},
            result -> {
                while (result.next()) {
                    final UUID playerId = UUIDs.toUUID(result.get("player_id"));
                    players.get(playerId).owningVips().add(
                        new VIP(
                            result.get("vip_id", Byte.class),
                            VipType.fromId(result.get("vip_type", Byte.class)),
                            result.get("vip_duration")
                        )
                    );
                }

                return null;
            }
        );
    }

    @Override
    public VipPlayer getById(@NotNull UUID playerId) {
        return players.get(playerId);
    }

    @Override
    public void addSpent(@NotNull UUID playerId, double value) {
        players.get(playerId).incrementSpent(value);

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "UPDATE " + SPENT_TABLE + " SET spent=? WHERE player_id=?",
            statement -> {
                statement.set(1, value);
                statement.set(2, UUIDs.getBytes(playerId));
            }
        ), executor);
    }

    @Override
    public void setVip(@NotNull UUID playerId, @NotNull VIP vip) {
        final VipPlayer playerCache = players.get(playerId);

        if (playerCache != null) {
            playerCache.setActiveVip(vip);
        } else {
            final VipPlayer vipPlayer = new VipPlayer(
                playerId,
                0,
                vip,
                new OwningVIPs(new ArrayList<>(0))
            );
            players.set(playerId, vipPlayer);
            CompletableFuture.runAsync(() -> sqlExecutor.update(
                "INSERT INTO " + SPENT_TABLE + "(player_id, spent) VALUES(?,?)",
                statement -> {
                    statement.set(1, UUIDs.getBytes(playerId));
                    statement.set(2, vipPlayer.spent());
                }
            ), executor);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + PLAYER_TABLE + "(player_id, vip_id, vip_type, vip_duration) VALUES(?,?,?,?)",
            statement -> {
                statement.set(1, UUIDs.getBytes(playerId));
                statement.set(2, vip.id());
                statement.set(3, vip.type());
                statement.set(4, vip.duration());
            }
        ), executor);
    }

    @Override
    public void removeVip(@NotNull UUID playerId) {
        final VipPlayer playerCache = players.get(playerId);
        playerCache.setActiveVip(null);

        final boolean uncache = playerCache.owningVips().vips().isEmpty();

        if (uncache) {
            players.remove(playerId);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + PLAYER_TABLE + " WHERE player_id=?",
            statement -> statement.set(1, UUIDs.getBytes(playerId))
        ));
    }

    @Override
    public void updatePrimaryVip(@NotNull Collection<VipPlayer> players) {
        CompletableFuture.runAsync(() -> sqlExecutor.executeBatch(
            "UPDATE " + PLAYER_TABLE + " SET vip_duration=? WHERE player_id=?",
            statement ->
                players.forEach(vipPlayer -> {
                    statement.set(
                        1,
                        requireNonNull(vipPlayer.activeVip()).duration()
                    );
                    statement.set(2, vipPlayer);
                    statement.addBatch();
                })
        ), executor);
    }

    @Override
    public void addOwningVip(@NotNull UUID playerId, @NotNull VIP vip) {
        final OwningVIP newVip = players.get(playerId).owningVips().add(vip);
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + OWNING_TABLE + "(player_id, vip_id, vip_type, vip_duration) VALUES(?,?,?,?)",
            statement -> {
                statement.set(1, UUIDs.getBytes(playerId));
                statement.set(2, newVip.id());
                statement.set(3, newVip.type());
                statement.set(4, newVip.duration());
            }
        ), executor);
    }

    @Override
    public void removeOwningVip(@NotNull UUID playerId, @NotNull OwningVIP owningVip) {
        players.get(playerId).owningVips().remove(owningVip.id(), owningVip.type());
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + OWNING_TABLE + " WHERE player_id=? AND vip_id=? AND vip_type=?",
            statement -> {
                statement.set(1, playerId);
                statement.set(2, owningVip.id());
                statement.set(3, owningVip.type().id());
            }
        ), executor);
    }

    @Override
    public void updateOwningVips(@NotNull Map<UUID, Collection<OwningVIP>> vips) {
        CompletableFuture.runAsync(() -> sqlExecutor.executeBatch(
            "UPDATE " + OWNING_TABLE +
                " SET vip_duration=? " +
                "WHERE player_id=? AND vip_id=? AND vip_type=?",
            statement ->
                vips.forEach((playerId, owningVips) ->
                    owningVips.forEach(owningVip -> {
                        statement.set(1, owningVip.duration());
                        statement.set(2, UUIDs.getBytes(playerId));
                        statement.set(3, owningVip.id());
                        statement.set(4, owningVip.type().id());
                        statement.addBatch();
                    })
                )
        ), executor);
    }
}
