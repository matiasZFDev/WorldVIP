package com.worldplugins.vip.database.pending;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.vip.database.player.model.VipType;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class SQLPendingVipRepository implements PendingVipRepository {
    private final @NonNull Executor executor;
    private final @NonNull SQLExecutor sqlExecutor;

    private static final @NonNull String PENDINGS_TABLE = "worldvip_pendentes";

    public SQLPendingVipRepository(@NonNull Executor executor, @NonNull SQLExecutor sqlExecutor) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
        createTables();
    }

    private void createTables() {
        sqlExecutor.query(
            "CREATE TABLE IF NOT EXISTS " + PENDINGS_TABLE + "(" +
                "player_name VARCHAR(16) NOT NULL, " +
                "vip_id TINYINT NOT NULL, " +
                "vip_type TINYINT NOT NULL" +
            ")"
        );
    }

    @Override
    public @NonNull CompletableFuture<Collection<PendingVIP>> getPendingVips(@NonNull String playerName) {
        return CompletableFuture.supplyAsync(() -> sqlExecutor.executeQuery(
            "SELECT * FROM " + PENDINGS_TABLE + " WHERE player_name=?",
            statement -> statement.set(1, playerName),
            result -> {
                final Collection<PendingVIP> pendings = new ArrayList<>();

                while (result.next()) {
                    pendings.add(new PendingVIP(
                        playerName,
                        result.get("vip_id"),
                        VipType.fromId(result.get("vip_type"))
                    ));
                }

                return pendings;
            }
        ), executor);
    }

    @Override
    public void addPending(@NonNull PendingVIP vip) {
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + PENDINGS_TABLE + "(player_name, vip_id, vip_type) VALUES(?,?,?)",
            statement -> {
                statement.set(1, vip.getPlayerName());
                statement.set(2, vip.getId());
                statement.set(3, vip.getType());
            }
        ), executor);
    }

    @Override
    public void removePendings(@NonNull String playerName) {
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "DELETE FROM " + PENDINGS_TABLE + " WHERE player_name=?",
            statement -> statement.set(1, playerName)
        ), executor);
    }
}
