package com.worldplugins.vip.database.player;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.database.key.ValidKeyRepository;
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
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull VipItemsRepository vipItemsRepository;

    private static final @NonNull String PLAYER_TABLE = "worldvip_jogadores";
    private static final @NonNull String OWNING_TABLE = "worldvip_vips";

    public SQLPlayerService(
        @NonNull Executor executor,
        @NonNull SQLExecutor sqlExecutor,
        @NonNull Cache<UUID, VipPlayer> players,
        @NonNull ValidKeyRepository validKeyRepository,
        @NonNull VipItemsRepository vipItemsRepository
    ) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        this.players = players;
        this.validKeyRepository = validKeyRepository;
        this.vipItemsRepository = vipItemsRepository;
        createTables();
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

    @Override
    public @NonNull CompletableFuture<VipPlayer> getById(@NonNull UUID playerId) {
        if (players.containsKey(playerId)) {
            return CompletableFuture.completedFuture(players.get(playerId));
        }

        return getActiveVip(playerId).thenCompose(activeVip ->
            getOwningVips(playerId).thenCompose(owningVips ->
                validKeyRepository.getKeys(playerId).thenApply(PlayerKeys::new).thenCompose(playerKeys ->
                    vipItemsRepository.getItems(playerId).thenApply(PlayerItems::new).thenApply(playerItems ->
                        activeVip == null &&
                        owningVips.getVips().isEmpty() &&
                        playerKeys.all().isEmpty() &&
                        playerItems.all().isEmpty()
                            ? null
                            : new VipPlayer(playerId, activeVip, owningVips, playerKeys, playerItems)
                    )
                )
            )
        ).whenComplete((vipPlayer, t) -> players.set(playerId, vipPlayer));
    }

    private @NonNull CompletableFuture<VIP> getActiveVip(@NonNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> sqlExecutor.executeQuery(
            "SELECT vip_id, vip_type, vip_duration FROM " + PLAYER_TABLE + " WHERE player_id=?",
            statement -> statement.set(1, playerId.getBytes()),
            result -> result.next()
                ? new VIP(
                    result.get("vip_id"),
                    VipType.fromId(result.get("vip_type")),
                    result.get("vip_duration")
                )
                : null
        ), executor);
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
                new OwningVIPs(new ArrayList<>(0)),
                new PlayerKeys(new ArrayList<>(0)),
                new PlayerItems(new ArrayList<>(0))
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

        final boolean uncache =
            playerCache.getOwningVips().getVips().isEmpty() &&
            playerCache.getKeys().all().isEmpty() &&
            playerCache.getItems().all().isEmpty();

        if (uncache) {
            players.remove(playerId);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + PLAYER_TABLE + " WHERE player_id=?",
            statement -> statement.set(1, playerId.getBytes())
        ));
    }

    @Override
    public void updatePlayers(@NonNull Collection<VipPlayer> players) {
        CompletableFuture.runAsync(() -> sqlExecutor.executeBatch(
            "UPDATE " + PLAYER_TABLE + " SET vip_duration=? WHERE player_id=?",
            statement -> {
                players.forEach(vipPlayer -> {
                    statement.set(1, vipPlayer.getActiveVip().getDuration());
                    statement.set(2, vipPlayer);
                    statement.addBatch();
                });
            }
        ), executor);
    }

    private @NonNull CompletableFuture<OwningVIPs> getOwningVips(@NonNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> sqlExecutor.executeQuery(
            "SELECT vip_id, vip_type, vip_duration FROM " + OWNING_TABLE + " WHERE player_id=?",
            statement -> statement.set(1, playerId.getBytes()),
            result -> {
                final List<OwningVIP> vips = new ArrayList<>();

                while (result.next()) {
                    vips.add(new OwningVIP(
                        result.get("vip_id"),
                        VipType.fromId(result.get("vip_type")),
                        result.get("vip_duration")
                    ));
                }

                return vips.isEmpty() ? null : new OwningVIPs(vips);
            }
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
            statement -> {
                vips.forEach((playerId, owningVips) -> {
                    owningVips.forEach(owningVip -> {
                        statement.set(1, owningVip.getDuration());
                        statement.set(2, playerId.getBytes());
                        statement.set(3, owningVip.getId());
                        statement.set(4, owningVip.getType().getId());
                        statement.addBatch();
                    });
                });
            }
        ), executor);
    }
}
