package com.worldplugins.vip.init;

import com.worldplugins.lib.database.sql.SQLDatabase;
import com.worldplugins.lib.database.sql.SQLExecutor;
import com.worldplugins.lib.factory.SQLDatabaseFactoryProducer;
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
import me.post.lib.common.Factory;
import me.post.lib.config.wrapper.ConfigManager;
import me.post.lib.database.cache.Cache;
import me.post.lib.database.cache.SimpleCache;
import me.post.lib.util.Scheduler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DatabaseInitializer {
    private final @NotNull ConfigManager configManager;
    private final @NotNull Plugin plugin;
    private final @NotNull Scheduler scheduler;

    public DatabaseInitializer(@NotNull ConfigManager configManager, @NotNull Plugin plugin, @NotNull Scheduler scheduler) {
        this.configManager = configManager;
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    public @NotNull DatabaseAccessor init() {
        final Executor executor = Executors.newSingleThreadExecutor();
        final SQLExecutor sqlExecutor = new SQLExecutor(databaseFactory().create().connect());
        final Cache<UUID, VipPlayer> playerCache = new SimpleCache<>(new HashMap<>());
        final ValidKeyRepository validKeyRepository = new SQLValidKeyRepository(executor, sqlExecutor, scheduler);
        final VipItemsRepository vipItemsRepository = new SQLVipItemsRepository(executor, sqlExecutor, scheduler);
        final SellingKeyRepository sellingKeyRepository = new SQLSellingKeyRepository(
            executor, sqlExecutor, scheduler
        );

        return new DatabaseAccessor(
            new SQLPlayerService(executor, sqlExecutor, playerCache),
            new SQLPendingVipRepository(executor, sqlExecutor),
            validKeyRepository,
            vipItemsRepository,
            sellingKeyRepository,
            playerCache
        );
    }

    private @NotNull Factory<SQLDatabase> databaseFactory() {
        final ConfigurationSection dataSection = configManager.getWrapper("config").unwrap().getConfigurationSection("Database");
        final String databaseType = dataSection.getString("Tipo");
        final Factory<SQLDatabase> sqlFactory = new SQLDatabaseFactoryProducer(databaseType, dataSection, plugin).create();

        if (sqlFactory == null)
            throw new IllegalArgumentException("O tipo de banco de dados '" + databaseType + "' não existe.");

        return sqlFactory;
    }
}
