package com.worldplugins.vip.database.items;

import com.worldplugins.lib.database.sql.SQLExecutor;
import me.post.lib.database.cache.Cache;
import me.post.lib.database.cache.SynchronizedExpiringMap;
import me.post.lib.util.Scheduler;
import me.post.lib.util.UUIDs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

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
        this.cache = new SynchronizedExpiringMap<>(
            new HashMap<>(),
            120,
            120,
            checker -> scheduler.runTimer(20L, 20L, true, checker)
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
                "SELECT vip_id, amount FROM " + ITEMS_TABLE + " WHERE player_id=?",
                statement -> statement.set(1, UUIDs.getBytes(playerId)),
                result -> {
                    final Collection<VipItems> items = new ArrayList<>();

                    while (result.next()) {
                        items.add(new VipItems(
                            playerId,
                            result.get("vip_id", Byte.class),
                            result.get("amount", Short.class)
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
        final Collection<VipItems> cachedItems = cache.get(items.playerId());

        if (cachedItems == null) {
            getItems(items.playerId()).thenAccept(playerItems -> {
                final VipItems matchingItems = playerItems.stream()
                    .filter(it -> it.vipId() == items.vipId())
                    .findFirst()
                    .orElse(null);

                if (matchingItems == null) {
                    insertItems(items);
                    return;
                }

                final Collection<VipItems> addedItems = playerItems.stream()
                    .map(it ->
                        it.vipId() != items.vipId()
                            ? it
                            : new VipItems(it.playerId(), it.vipId(), (short) (it.amount() + items.amount()))
                    )
                    .collect(Collectors.toList());
                cache.set(items.playerId(), addedItems);
                updateItems(items);
            });
            return;
        }

        final VipItems matchingItems = cachedItems.stream()
            .filter(it -> it.vipId() == items.vipId())
            .findFirst()
            .orElse(null);

        if (matchingItems == null) {
            cache.get(items.playerId()).add(items);
            insertItems(items);
            return;
        }

        matchingItems.setAmount((short) (matchingItems.amount() + items.amount()));
        updateItems(items);
    }

    private void insertItems(@NotNull VipItems items) {
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + ITEMS_TABLE + "(player_id, vip_id, amount) VALUES(?, ?, ?)",
            statement -> {
                statement.set(1, UUIDs.getBytes(items.playerId()));
                statement.set(2, items.vipId());
                statement.set(3, items.amount());
            }
        ), executor);
    }

    private void updateItems(@NotNull VipItems items) {
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "UPDATE " + ITEMS_TABLE + " SET amount = ? WHERE player_id = ? AND vip_id = ?",
            statement -> {
                statement.set(1, items.amount());
                statement.set(2, UUIDs.getBytes(items.playerId()));
                statement.set(3, items.vipId());
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
