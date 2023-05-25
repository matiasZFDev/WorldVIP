package com.worldplugins.vip.database.market;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.util.TemporaryDataSet;
import me.post.lib.util.Scheduler;
import me.post.lib.util.UUIDs;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SQLSellingKeyRepository implements SellingKeyRepository {
    private final @NotNull Executor executor;
    private final @NotNull SQLExecutor sqlExecutor;
    private final @NotNull TemporaryDataSet<SellingKey> cache;

    private static final @NotNull String MARKET_TABLE = "worldvip_loja";

    public SQLSellingKeyRepository(
        @NotNull Executor executor,
        @NotNull SQLExecutor sqlExecutor,
        @NotNull Scheduler scheduler
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
    public @NotNull CompletableFuture<List<SellingKey>> getAllKeys() {
        return cache.expired()
            ? CompletableFuture.supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT * FROM " + MARKET_TABLE,
                statement -> {},
                result -> {
                    final Collection<SellingKey> data = new ArrayList<>();

                    while (result.next()) {
                        data.add(new SellingKey(
                            result.get("code"),
                            UUIDs.toUUID(result.get("seller_id")),
                            result.get("price"),
                            result.get("vip_id"),
                            VipType.fromId(result.get("vip_type")),
                            result.get("vip_duration"),
                            result.get("vip_short"),
                            result.get("post_timestamp")
                        ));
                    }

                    cache.clear();
                    cache.addAll(data);
                    return cache.getAll();
                }
            ), executor)
            : CompletableFuture.completedFuture(cache.getAll());
    }

    @Override
    public void addKey(@NotNull SellingKey key) {
        if (!cache.expired()) {
            cache.add(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + MARKET_TABLE +
                "(code, seller_id, price, vip_id, vip_type, vip_duration, vip_usages, post_timestamp)" +
                "VALUES(?,?,?,?,?,?,?,?)",
            statement -> {
                statement.set(1, key.code());
                statement.set(2, UUIDs.getBytes(key.sellerId()));
                statement.set(3, key.price());
                statement.set(4, key.vipId());
                statement.set(5, key.vipType().id());
                statement.set(6, key.vipDuration());
                statement.set(7, key.vipUsages());
                statement.set(8, key.postTimestamp());
            }
        ), executor);
    }

    @Override
    public void removeKey(@NotNull SellingKey key) {
        if (!cache.expired()) {
            cache.remove(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + MARKET_TABLE + " WHERE code=?",
            statement -> statement.set(1, key.code())
        ), executor);
    }
}
