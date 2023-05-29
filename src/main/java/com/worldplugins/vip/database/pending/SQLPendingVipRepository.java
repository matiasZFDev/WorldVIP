package com.worldplugins.vip.database.pending;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.vip.database.player.model.VipType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SQLPendingVipRepository implements PendingVipRepository {
    private final @NotNull Executor executor;
    private final @NotNull SQLExecutor sqlExecutor;

    private static final @NotNull String PENDINGS_TABLE = "worldvip_pendentes";

    public SQLPendingVipRepository(@NotNull Executor executor, @NotNull SQLExecutor sqlExecutor) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        createTables();
    }

    private void createTables() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + PENDINGS_TABLE + "(" +
                "player_name VARCHAR(16) NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL, " +
                "vip_duration INT NOT NULL" +
            ")"
        );
    }

    @Override
    public @NotNull CompletableFuture<Collection<PendingVIP>> getPendingVips(@NotNull String playerName) {
        return CompletableFuture.supplyAsync(() -> sqlExecutor.executeQuery(
            "SELECT vip_id, vip_type, vip_duration FROM " + PENDINGS_TABLE + " WHERE player_name=?",
            statement -> statement.set(1, playerName),
            result -> {
                final Collection<PendingVIP> pendings = new ArrayList<>(0);

                while (result.next()) {
                    pendings.add(new PendingVIP(
                        playerName,
                        result.get("vip_id", Byte.class),
                        VipType.fromId(result.get("vip_type", Byte.class)),
                        result.get("vip_duration")
                    ));
                }

                return pendings;
            }
        ), executor);
    }

    @Override
    public void addPending(@NotNull PendingVIP vip) {
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + PENDINGS_TABLE + "(player_name, vip_id, vip_type, vip_duration) " +
                "VALUES(?,?,?,?)",
            statement -> {
                statement.set(1, vip.playerName());
                statement.set(2, vip.id());
                statement.set(3, vip.type().id());
                statement.set(4, vip.duration());
            }
        ), executor);
    }

    @Override
    public void removePendings(@NotNull String playerName) {
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + PENDINGS_TABLE + " WHERE player_name=?",
            statement -> statement.set(1, playerName)
        ), executor);
    }
}
