package com.worldplugins.vip.database.items;

import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.extension.UUIDExtensions;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@ExtensionMethod({
    UUIDExtensions.class
})

public class SQLVipItemsRepository implements VipItemsRepository {
    private final @NonNull Executor executor;
    private final @NonNull SQLExecutor sqlExecutor;

    private static final @NonNull String ITEMS_TABLE = "worldvip_itens";

    public SQLVipItemsRepository(@NonNull Executor executor, @NonNull SQLExecutor sqlExecutor) {
        this.executor = executor;
        this.sqlExecutor = sqlExecutor;
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
    public @NonNull CompletableFuture<Collection<VipItems>> getItems(@NonNull UUID playerId) {
        return CompletableFuture.supplyAsync(() -> sqlExecutor.executeQuery(
            "SELECT vip_id, amount FROM " + ITEMS_TABLE + "WHERE player_id=?",
            statement -> statement.set(1, playerId.getBytes()),
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
        ), executor);
    }

    @Override
    public void addItems(@NonNull UUID playerId, @NonNull VipItems items) {
        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "INSERT INTO " + ITEMS_TABLE + "(player_id, vip_id, amount) VALUES(?,?,?)",
            statement -> {
                statement.set(1, items.getPlayerId().getBytes());
                statement.set(2, items.getVipId());
                statement.set(3, items.getAmount());
            }
        ), executor);
    }

    @Override
    public void removeItems(@NonNull UUID playerId, byte vipId, short amount) {
        if (amount == -1) {
            CompletableFuture.runAsync(() -> sqlExecutor.update(
                "DELETE FROM " + ITEMS_TABLE + " WHERE player_id=? AND vip_id=?",
                statement -> {
                    statement.set(1, playerId.getBytes());
                    statement.set(2, vipId);
                }
            ), executor);
            return;
        }

        CompletableFuture.runAsync(() -> sqlExecutor.update(
            "UPDATE " + ITEMS_TABLE + " SET amount=amount-? WHERE player_id=? AND vip_id=?",
            statement -> {
                statement.set(1, amount);
                statement.set(2, playerId.getBytes());
                statement.set(3, vipId);
            }
        ), executor);
    }
}
