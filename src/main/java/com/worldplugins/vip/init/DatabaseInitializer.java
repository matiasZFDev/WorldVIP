package com.worldplugins.vip.init;

import com.worldplugins.lib.common.Factory;
import com.worldplugins.lib.common.Initializer;
import com.worldplugins.lib.database.sql.SQLDatabase;
import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.factory.SQLDatabaseFactoryProducer;
import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.util.cache.Cache;
import com.worldplugins.lib.util.cache.SimpleCache;
import com.worldplugins.vip.database.DatabaseAccessor;
import com.worldplugins.vip.database.items.SQLVipItemsRepository;
import com.worldplugins.vip.database.items.VipItemsRepository;
import com.worldplugins.vip.database.key.SQLValidKeyRepository;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.market.SQLSellingKeyRepository;
import com.worldplugins.vip.database.market.SellingKeyRepository;
import com.worldplugins.vip.database.pending.SQLPendingVipRepository;
import com.worldplugins.vip.database.player.SQLPlayerService;
import com.worldplugins.vip.database.player.model.VipPlayer;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@RequiredArgsConstructor
public class DatabaseInitializer implements Initializer<DatabaseAccessor> {
    private final @NonNull ConfigManager configManager;
    private final @NonNull Plugin plugin;
    private final @NonNull SchedulerBuilder scheduler;

    @Override
    public @NonNull DatabaseAccessor init() {
        final Executor executor = Executors.newSingleThreadExecutor();
        final SQLExecutor sqlExecutor = new SQLExecutor(databaseFactory().create().connect());
        final Cache<UUID, VipPlayer> playerCache = new SimpleCache<>(new HashMap<>());
        final ValidKeyRepository validKeyRepository = new SQLValidKeyRepository(executor, sqlExecutor);
        final VipItemsRepository vipItemsRepository = new SQLVipItemsRepository(executor, sqlExecutor);
        final SellingKeyRepository sellingKeyRepository = new SQLSellingKeyRepository(
            executor, sqlExecutor, scheduler
        );

        return new DatabaseAccessor(
            new SQLPlayerService(executor, sqlExecutor, playerCache, validKeyRepository, vipItemsRepository),
            new SQLPendingVipRepository(executor, sqlExecutor),
            validKeyRepository,
            vipItemsRepository,
            sellingKeyRepository
        );
    }

    private @NonNull Factory<SQLDatabase> databaseFactory() {
        final ConfigurationSection dataSection = configManager.getContainer("config").config().getConfigurationSection("Database");
        final String databaseType = dataSection.getString("Tipo");
        final Factory<SQLDatabase> sqlFactory = new SQLDatabaseFactoryProducer(databaseType, dataSection, plugin).create();

        if (sqlFactory == null)
            throw new IllegalArgumentException("O tipo de banco de dados '" + databaseType + "' não existe.");

        return sqlFactory;
    }
}
