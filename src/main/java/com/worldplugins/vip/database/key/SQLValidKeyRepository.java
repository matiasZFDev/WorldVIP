package com.worldplugins.vip.database.key;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.extension.UUIDExtensions;
import com.worldplugins.vip.database.player.model.VipType;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@ExtensionMethod({
    UUIDExtensions.class
})

public class SQLValidKeyRepository implements ValidKeyRepository {
    private final @NonNull Executor executor;
    private final @NonNull SQLExecutor sqlExecutor;
    private final @NonNull Cache<String, ValidVipKey> cache;

    private static final @NonNull String KEYS_TABLE = "worldvip_keys_validas";

    public SQLValidKeyRepository(@NonNull Executor executor, @NonNull SQLExecutor sqlExecutor) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        this.cache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES)
            .expireAfterAccess(1, TimeUnit.MINUTES)
            .build();
        createTables();
    }

    private void createTables() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + KEYS_TABLE + "(" +
                "generator_id BINARY(16), " +
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
    public @NonNull CompletableFuture<Collection<ValidVipKey>> getKeys(@NonNull UUID generatorId) {
        return CompletableFuture.supplyAsync(() -> sqlExecutor.executeQuery(
            "SELECT code, vip_id, vip_type, duration, usages FROM " + KEYS_TABLE
                + " WHERE generator_id=?",
            statement -> statement.set(1, generatorId.getBytes()),
            result -> {
                final Collection<ValidVipKey> keys = new ArrayList<>();

                while (result.next()) {
                    keys.add(new ValidVipKey(
                        generatorId, result.get("code"), result.get("vip_id"),
                        VipType.fromId(result.get("vip_type")), result.get("duration"),
                        result.get("usages")
                    ));
                }

                return keys;
            }
        ), executor);
    }

    @Override
    public @NonNull CompletableFuture<ValidVipKey> getKeyByCode(@NonNull String code) {
        return CompletableFuture
            .supplyAsync(() -> sqlExecutor.executeQuery(
                "SELECT generator_id, usages FROM " + KEYS_TABLE + " WHERE code=?",
                statement -> statement.set(1, code),
                result -> result.next()
                    ? new ValidVipKey(
                        ((byte[]) result.get("generator_id")).toUUID(), code, result.get("vip_id"),
                        VipType.fromId(result.get("vip_type")), result.get("duration"), result.get("usages")
                    )
                    : null
            ), executor)
            .whenComplete((key, t) -> {
                if (key == null) {
                    return;
                }

                cache.put(code, key);
            });
    }

    @Override
    public void consumeKey(@NonNull String code) {
        final ValidVipKey key = cache.getIfPresent(code);

        if (key != null) {
            key.setUsages((short) (key.getUsages() - 1));
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "UPDATE " + KEYS_TABLE + " SET usages=usages - 1 WHERE code=?",
            statement -> statement.set(1, code)
        ), executor);
    }

    @Override
    public void addKey(@NonNull ValidVipKey key) {
        cache.put(key.getCode(), key);

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + KEYS_TABLE
                + "(generator_id, code, vip_id, vip_type, duration, usages) VALUES(?,?,?,?,?,?)",
            statement -> {
                statement.set(1, key.getGeneratorId().getBytes());
                statement.set(2, key.getCode());
                statement.set(3, key.getVipId());
                statement.set(4, key.getVipType().getId());
                statement.set(5, key.getVipDuration());
                statement.set(6, key.getUsages());
            }
        ), executor);
    }

    @Override
    public void removeKey(@NonNull String code) {
        if (cache.getIfPresent(code) != null) {
            cache.invalidate(code);
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + KEYS_TABLE + " WHERE code=?",
            statement -> statement.set(1, code)
        ), executor);
    }
}
