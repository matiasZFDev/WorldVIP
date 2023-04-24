package com.worldplugins.vip.database.market;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.util.TemporaryDataSet;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@ExtensionMethod({
    UUIDExtensions.class
})

public class SQLSellingKeyRepository implements SellingKeyRepository {
    private final @NonNull Executor executor;
    private final @NonNull SQLExecutor sqlExecutor;
    private final @NonNull TemporaryDataSet<SellingKey> cache;

    private static final @NonNull String MARKET_TABLE = "worldvip_loja";

    public SQLSellingKeyRepository(
        @NonNull Executor executor,
        @NonNull SQLExecutor sqlExecutor,
        @NonNull SchedulerBuilder scheduler
    ) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        this.cache = new TemporaryDataSet<>(scheduler, 60 * 10, 60 * 5, true);
        createTable();
    }

    private void createTable() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + MARKET_TABLE + "(" +
                "seller_id BINARY(16) NOT NULL, " +
                "price DOUBLE NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL, " +
                "vip_duration INT NOT NULL" +
            ")"
        );
    }

    @Override
    public @NonNull CompletableFuture<Collection<SellingKey>> getSellerKeys(@NonNull UUID sellerId) {
        return getAllKeys().thenApply(keys -> keys.stream()
            .filter(key -> key.getSellerId().equals(sellerId))
            .collect(Collectors.toList()
        ));
    }

    @Override
    public @NonNull CompletableFuture<Collection<SellingKey>> getAllKeys() {
        return cache.expired()
            ? CompletableFuture.supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT * FROM " + MARKET_TABLE,
                statement -> {},
                result -> {
                    final Collection<SellingKey> data = new ArrayList<>();

                    while (result.next()) {
                        data.add(new SellingKey(
                            ((byte[]) result.get("seller_id")).toUUID(),
                            result.get("price"),
                            result.get("vip_id"),
                            VipType.fromId((byte) result.get("vip_type")),
                            result.get("vip_duration")
                        ));
                    }

                    cache.addAll(data);
                    return cache.getAll();
                }
            ), executor)
            : CompletableFuture.completedFuture(cache.getAll());
    }

    @Override
    public void addKey(@NonNull SellingKey key) {
        if (!cache.expired()) {
            cache.add(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + MARKET_TABLE + "(seller_id, price, vip_id, vip_type, vip_duration)" +
                "VALUES(?,?,?,?,?)",
            statement -> {
                statement.set(1, key.getSellerId().getBytes());
                statement.set(2, key.getPrice());
                statement.set(3, key.getVipId());
                statement.set(4, key.getVipType().getId());
                statement.set(5, key.getVipDuration());
            }
        ), executor);
    }

    @Override
    public void removeKey(@NonNull SellingKey key) {
        if (!cache.expired()) {
            cache.remove(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + MARKET_TABLE + " WHERE " +
                "seller_id=? AND price=? AND vip_id=? AND vip_type=? AND vip_duration=?",
            statement -> {
                statement.set(1, key.getSellerId().getBytes());
                statement.set(2, key.getPrice());
                statement.set(3, key.getVipId());
                statement.set(4, key.getVipType().getId());
                statement.set(5, key.getVipDuration());
            }
        ), executor);
    }
}
