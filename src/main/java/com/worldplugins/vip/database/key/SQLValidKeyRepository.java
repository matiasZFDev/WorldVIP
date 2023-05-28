package com.worldplugins.vip.database.key;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.vip.database.player.model.VipType;
import me.post.lib.database.cache.Cache;
import me.post.lib.database.cache.ExpiringMap;
import me.post.lib.database.cache.SynchronizedExpiringCache;
import me.post.lib.database.cache.implementor.BukkitExpiringCacheImplementor;
import me.post.lib.util.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SQLValidKeyRepository implements ValidKeyRepository {
    private final @NotNull Executor executor;
    private final @NotNull SQLExecutor sqlExecutor;
    private final @NotNull Cache<String, ValidVipKey> globalCache;
    private final @NotNull Cache<String, List<ValidVipKey>> generatorCache;

    private static final @NotNull String KEYS_TABLE = "worldvip_keys_validas";

    public SQLValidKeyRepository(
        @NotNull Executor executor,
        @NotNull SQLExecutor sqlExecutor,
        @NotNull Scheduler scheduler
    ) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        this.globalCache = new BukkitExpiringCacheImplementor<>(
            new SynchronizedExpiringCache<>(
                new ExpiringMap<>(
                    new HashMap<>(),
                    120,
                    120
                )
            ),
            scheduler,
            true
        );
        this.generatorCache = new BukkitExpiringCacheImplementor<>(
            new SynchronizedExpiringCache<>(
                new ExpiringMap<>(
                    new HashMap<>(),
                    120,
                    120
                )
            ),
            scheduler,
            true
        );
        createTables();
    }

    private void createTables() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + KEYS_TABLE + "(" +
                "generator_name VARCHAR(16), " +
                "code CHAR(20) NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL, " +
                "duration INT NOT NULL, " +
                "usages SMALLINT NOT NULL, " +
                "PRIMARY KEY(code)" +
            ")"
        );
    }

    @Override
    public @NotNull CompletableFuture<List<ValidVipKey>> getKeys(@NotNull String generatorName) {
        if (generatorCache.containsKey(generatorName)) {
            return CompletableFuture.completedFuture(generatorCache.get(generatorName));
        }

        return CompletableFuture
            .supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT code, vip_id, vip_type, duration, usages FROM " + KEYS_TABLE
                    + " WHERE generator_name=?",
                statement -> statement.set(1, generatorName),
                result -> {
                    final List<ValidVipKey> keys = new ArrayList<>();

                    while (result.next()) {
                        keys.add(new ValidVipKey(
                            generatorName, result.get("code"), result.get("vip_id"),
                            VipType.fromId(result.get("vip_type")), result.get("duration"),
                            result.get("usages")
                        ));
                    }

                    return keys;
                }
            ), executor)
            .whenComplete((keys, t) -> {
                keys.forEach(key -> globalCache.set(key.code(), key));
                generatorCache.set(generatorName, keys);
            });
    }

    @Override
    public @NotNull CompletableFuture<ValidVipKey> getKeyByCode(@NotNull String code) {
        if (globalCache.containsKey(code)) {
            return CompletableFuture.completedFuture(globalCache.get(code));
        }

        return CompletableFuture
            .supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT generator_name, vip_id, vip_type, duration, usages FROM "
                    + KEYS_TABLE + " WHERE code=?",
                statement -> statement.set(1, code),
                result -> result.next()
                    ? new ValidVipKey(
                        result.get("generator_name"), code, result.get("vip_id"),
                        VipType.fromId(result.get("vip_type")), result.get("duration"),
                        result.get("usages")
                    )
                    : null
            ), executor)
            .whenComplete((key, t) -> {
                if (key == null) {
                    return;
                }

                globalCache.set(code, key);
            });
    }

    @Override
    public void consumeKey(@NotNull ValidVipKey key) {
        key.setUsages((short) (key.usages() - 1));

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "UPDATE " + KEYS_TABLE + " SET usages=usages - 1 WHERE code=?",
            statement -> statement.set(1, key.code())
        ), executor);
    }

    @Override
    public void addKey(@NotNull ValidVipKey key) {
        globalCache.set(key.code(), key);

        if (key.generatorName() != null) {
            if (!generatorCache.containsKey(key.generatorName())) {
                generatorCache.set(key.generatorName(), new ArrayList<>(1));
            }

            generatorCache.get(key.generatorName()).add(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + KEYS_TABLE
                + "(generator_name, code, vip_id, vip_type, duration, usages) VALUES(?,?,?,?,?,?)",
            statement -> {
                statement.set(1, key.generatorName());
                statement.set(2, key.code());
                statement.set(3, key.vipId());
                statement.set(4, key.vipType().id());
                statement.set(5, key.vipDuration());
                statement.set(6, key.usages());
            }
        ), executor);
    }

    @Override
    public void removeKey(@NotNull ValidVipKey key) {
        globalCache.remove(key.code());

        if (key.generatorName() != null && generatorCache.containsKey(key.generatorName())) {
            generatorCache.get(key.generatorName()).remove(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + KEYS_TABLE + " WHERE code=?",
            statement -> statement.set(1, key.code())
        ), executor);
    }
}
