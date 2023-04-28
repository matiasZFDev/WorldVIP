package com.worldplugins.vip.database.key;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.util.ExpiringMap;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ExtensionMethod({
    UUIDExtensions.class
})

public class SQLValidKeyRepository implements ValidKeyRepository {
    private final @NonNull Executor executor;
    private final @NonNull SQLExecutor sqlExecutor;
    private final @NonNull Cache<String, ValidVipKey> globalCache;
    private final @NonNull Cache<String, Collection<ValidVipKey>> generatorCache;

    private static final @NonNull String KEYS_TABLE = "worldvip_keys_validas";

    public SQLValidKeyRepository(
        @NonNull Executor executor,
        @NonNull SQLExecutor sqlExecutor,
        @NonNull SchedulerBuilder scheduler
    ) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        this.globalCache = new ExpiringMap<>(scheduler, 120, 120, true);
        this.generatorCache = new ExpiringMap<>(scheduler, 60 * 5, 60 * 5, true);
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
    public @NonNull CompletableFuture<Collection<ValidVipKey>> getKeys(@NonNull String generatorName) {
        return CompletableFuture
            .supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT code, vip_id, vip_type, duration, usages FROM " + KEYS_TABLE
                    + " WHERE generator_id=?",
                statement -> statement.set(1, generatorName),
                result -> {
                    final Collection<ValidVipKey> keys = new ArrayList<>();

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
                keys.forEach(key -> globalCache.set(key.getCode(), key));
                generatorCache.set(generatorName, keys);
            });
    }

    @Override
    public @NonNull CompletableFuture<ValidVipKey> getKeyByCode(@NonNull String code) {
        return CompletableFuture
            .supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT generator_id, usages FROM " + KEYS_TABLE + " WHERE code=?",
                statement -> statement.set(1, code),
                result -> result.next()
                    ? new ValidVipKey(
                        result.get("generator_anem"), code, result.get("vip_id"),
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
    public void consumeKey(@NonNull ValidVipKey key) {
        key.setUsages((short) (key.getUsages() - 1));

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "UPDATE " + KEYS_TABLE + " SET usages=usages - 1 WHERE code=?",
            statement -> statement.set(1, key.getCode())
        ), executor);
    }

    @Override
    public void addKey(@NonNull ValidVipKey key) {
        globalCache.set(key.getCode(), key);

        if (key.getGeneratorName() != null) {
            if (!generatorCache.containsKey(key.getGeneratorName())) {
                generatorCache.set(key.getGeneratorName(), new ArrayList<>(1));
            }

            generatorCache.get(key.getGeneratorName()).add(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + KEYS_TABLE
                + "(generator_id, code, vip_id, vip_type, duration, usages) VALUES(?,?,?,?,?,?)",
            statement -> {
                statement.set(1, key.getGeneratorName().getBytes());
                statement.set(2, key.getCode());
                statement.set(3, key.getVipId());
                statement.set(4, key.getVipType().getId());
                statement.set(5, key.getVipDuration());
                statement.set(6, key.getUsages());
            }
        ), executor);
    }

    @Override
    public void removeKey(@NonNull ValidVipKey key) {
        globalCache.remove(key.getCode());

        if (key.getGeneratorName() != null && generatorCache.containsKey(key.getGeneratorName())) {
            generatorCache.get(key.getGeneratorName()).remove(key);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + KEYS_TABLE + " WHERE code=?",
            statement -> statement.set(1, key.getCode())
        ), executor);
    }
}
