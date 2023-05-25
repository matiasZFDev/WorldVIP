package com.worldplugins.vip.database.items;

import com.worldplugins.lib.database.sql.SQLExecutor;
import me.post.lib.database.cache.Cache;
import me.post.lib.database.cache.SynchronizedExpiringCache;
import me.post.lib.database.cache.implementor.BukkitExpiringCacheImplementor;
import me.post.lib.util.Scheduler;
import me.post.lib.util.UUIDs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SQLVipItemsRepository implements VipItemsRepository {
    private final @NotNull Executor executor;
    private final @NotNull SQLExecutor sqlExecutor;
    private final @NotNull Cache<UUID, Collection<VipItems>> cache;

    private static final @NotNull String ITEMS_TABLE = "worldvip_itens";

    public SQLVipItemsRepository(
        @NotNull Executor executor,
        @NotNull SQLExecutor sqlExecutor,
        @NotNull Scheduler scheduler
    ) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        this.cache = new BukkitExpiringCacheImplementor<>(
            new SynchronizedExpiringCache<>(
                new me.post.lib.database.cache.ExpiringMap<>(
                    new HashMap<>(),
                    120,
                    120
                )
            ),
            scheduler,
            true
        );
        createTable();
    }

    private void createTable() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + ITEMS_TABLE + "(" +
                "player_id BINARY(16) NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "amount SMALLINT NOT NULL" +
            ")"
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<VipItems>> getItems(@NotNull UUID playerId) {
        if (cache.containsKey(playerId)) {
            return CompletableFuture.completedFuture(cache.get(playerId));
        }

        return CompletableFuture
            .supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT vip_id, amount FROM " + ITEMS_TABLE + "WHERE player_id=?",
                statement -> statement.set(1, UUIDs.getBytes(playerId)),
                result -> {
                    final Collection<VipItems> items = new ArrayList<>();

                    while (result.next()) {
                        items.add(new VipItems(
                            playerId,
                            result.get("vip_id"),
                            result.get("amount")
                        ));
                    }

                    return items;
                }
            ), executor)
            .whenComplete((itemList, t) ->
                cache.set(playerId, itemList)
            );
    }

    @Override
    public void addItems(@NotNull VipItems items) {
        if (!cache.containsKey(items.playerId())) {
            cache.set(items.playerId(), new ArrayList<>(1));
        }

        cache.get(items.playerId()).add(items);

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + ITEMS_TABLE + "(player_id, vip_id, amount) VALUES(?,?,?)",
            statement -> {
                statement.set(1, UUIDs.getBytes(items.playerId()));
                statement.set(2, items.vipId());
                statement.set(3, items.amount());
            }
        ), executor);
    }

    @Override
    public void removeItems(@NotNull UUID playerId, byte vipId, short amount) {
        if (cache.containsKey(playerId)) {
            cache.get(playerId).removeIf(items -> {
                if (items.vipId() != vipId) {
                    return false;
                }

                if (amount == -1) {
                    return true;
                }

                items.setAmount((short) (items.amount() - amount));
                return false;
            });
        }

        if (amount == -1) {
            CompletableFuture.runAsync(() -> sqlExecutor.update(
                "DELETE FROM " + ITEMS_TABLE + " WHERE player_id=? AND vip_id=?",
                statement -> {
                    statement.set(1, UUIDs.getBytes(playerId));
                    statement.set(2, vipId);
                }
            ), executor);
            return;
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "UPDATE " + ITEMS_TABLE + " SET amount=amount-? WHERE player_id=? AND vip_id=?",
            statement -> {
                statement.set(1, amount);
                statement.set(2, UUIDs.getBytes(playerId));
                statement.set(3, vipId);
            }
        ), executor);
    }
}
