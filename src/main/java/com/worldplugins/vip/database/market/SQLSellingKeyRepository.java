package com.worldplugins.vip.database.market;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.GlobalValues;
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
                "code VARCHAR(" + GlobalValues.MAX_KEY_LENGTH + "), " +
                "seller_id BINARY(16) NOT NULL, " +
                "price DOUBLE NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL, " +
                "vip_duration INT NOT NULL, " +
                "PRIMARY KEY(code)" +
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
                            result.get("code"),
                            ((byte[]) result.get("seller_id")).toUUID(),
                            result.get("price"),
                            result.get("vip_id"),
                            VipType.fromId(result.get("vip_type")),
                            result.get("vip_duration"),
                            result.get("vip_short"),
                            result.get("post_timestamp")
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
            "INSERT INTO " + MARKET_TABLE +
                "(code, seller_id, price, vip_id, vip_type, vip_duration, vip_usages, post_timestamp)" +
                "VALUES(?,?,?,?,?,?,?,?)",
            statement -> {
                statement.set(1, key.getCode());
                statement.set(2, key.getSellerId().getBytes());
                statement.set(3, key.getPrice());
                statement.set(4, key.getVipId());
                statement.set(5, key.getVipType().getId());
                statement.set(6, key.getVipDuration());
                statement.set(7, key.getVipUsages());
                statement.set(8, key.getPostTimestamp());
            }
        ), executor);
    }

    @Override
    public void removeKey(@NonNull SellingKey key) {
        if (!cache.expired()) {
            cache.remove(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + MARKET_TABLE + " WHERE code=?",
            statement -> statement.set(1, key.getCode())
        ), executor);
    }
}
