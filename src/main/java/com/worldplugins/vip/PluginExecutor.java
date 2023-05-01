package com.worldplugins.vip;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.impl.EffectsConfig;
import com.worldplugins.lib.config.cache.impl.MessagesConfig;
import com.worldplugins.lib.config.cache.impl.SoundsConfig;
import com.worldplugins.lib.registry.CommandRegistry;
import com.worldplugins.lib.registry.ViewRegistry;
import com.worldplugins.lib.util.ConfigUtils;
import com.worldplugins.vip.command.*;
import com.worldplugins.vip.command.key.*;
import com.worldplugins.vip.command.vip.*;
import com.worldplugins.vip.config.MainConfig;
import com.worldplugins.vip.config.ServerConfig;
import com.worldplugins.vip.config.VipConfig;
import com.worldplugins.vip.config.VipItemsConfig;
import com.worldplugins.vip.config.data.ServerData;
import com.worldplugins.vip.controller.KeysController;
import com.worldplugins.vip.controller.OwningVipsController;
import com.worldplugins.vip.controller.VipItemsController;
import com.worldplugins.vip.controller.VipKeyShopController;
import com.worldplugins.vip.database.DatabaseAccessor;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.handler.VipHandler;
import com.worldplugins.vip.handler.OwningVipHandler;
import com.worldplugins.vip.init.ConfigCacheInitializer;
import com.worldplugins.lib.manager.config.ConfigCacheManager;
import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.lib.manager.config.YamlConfigManager;
import com.worldplugins.lib.manager.view.MenuContainerManager;
import com.worldplugins.lib.manager.view.MenuContainerManagerImpl;
import com.worldplugins.lib.manager.view.ViewManager;
import com.worldplugins.lib.manager.view.ViewManagerImpl;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.init.DatabaseInitializer;
import com.worldplugins.vip.init.PermissionManagerInitializer;
import com.worldplugins.vip.init.PointsInitializer;
import com.worldplugins.vip.key.ConfigKeyGenerator;
import com.worldplugins.vip.key.VipKeyGenerator;
import com.worldplugins.vip.manager.PermissionManager;
import com.worldplugins.vip.manager.PointsManager;
import com.worldplugins.vip.manager.VipTopManager;
import com.worldplugins.vip.view.*;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

@RequiredArgsConstructor
public class PluginExecutor {
    private final @NonNull JavaPlugin plugin;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull ConfigManager configManager;
    private final @NonNull ConfigCacheManager configCacheManager;
    private final @NonNull MenuContainerManager menuContainerManager;
    private final @NonNull ViewManager viewManager;

    private final @NonNull DatabaseAccessor databaseAccessor;
    private final VipKeyGenerator keyGenerator;
    private final @NonNull PermissionManager permissionManager;
    private final @NonNull PointsManager pointsManager;
    private final @NonNull VipHandler vipHandler;
    private final @NonNull OwningVipHandler owningVipHandler;
    private final @NonNull VipTopManager topManager;

    public PluginExecutor(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        scheduler = new SchedulerBuilder(plugin);
        configManager = new YamlConfigManager(plugin);
        configCacheManager = new ConfigCacheInitializer(configManager).init();
        menuContainerManager = new MenuContainerManagerImpl();
        viewManager = new ViewManagerImpl();

        databaseAccessor = new DatabaseInitializer(configManager, plugin, scheduler).init();
        keyGenerator = new ConfigKeyGenerator(config(MainConfig.class));
        permissionManager = new PermissionManagerInitializer().init();
        pointsManager = new PointsInitializer(plugin).init();
        owningVipHandler = new OwningVipHandler(
            databaseAccessor.getPlayerService(), permissionManager, config(VipConfig.class),
            config(MainConfig.class)
        );
        vipHandler = new VipHandler(
            databaseAccessor.getPlayerService(), databaseAccessor.getVipItemsRepository(),
            permissionManager, owningVipHandler, config(VipConfig.class),
            config(MainConfig.class), config(VipItemsConfig.class)
        );
        topManager = new VipTopManager(databaseAccessor.getPlayerCache());
    }

    /**
     * @return A runnable executed when disabling
     * */
    public @NonNull Runnable execute() {
        prepareGlobalAccess();
        checkBasicVips();
        registerListeners();
        registerCommands();
        registerViews();
        scheduleTasks();
        return () -> {};
    }

    private void prepareGlobalAccess() {
        GlobalAccess.setMessages(config(MessagesConfig.class));
        GlobalAccess.setSounds(config(SoundsConfig.class));
        GlobalAccess.setEffects(config(EffectsConfig.class));
        GlobalAccess.setViewManager(viewManager);
    }

    private void checkBasicVips() {
        final ConfigCache<ServerData> serverConfig = config(ServerConfig.class);
        final long lastOnlineInstant = serverConfig.data().getLastOnlineInstant();

        if (lastOnlineInstant == 0) {
            return;
        }

        final int elapsedSeconds = (int) ((System.nanoTime() - lastOnlineInstant) / 1000);

        databaseAccessor.getPlayerCache().getValues().forEach(vipPlayer -> {
            vipPlayer.getOwningVips().getVips().forEach(owningVip -> {
                if (owningVip.getType() != VipType.BASIC) {
                    return;
                }

                owningVip.decrementDuration(elapsedSeconds);

                if (owningVip.getDuration() > -1) {
                    return;
                }

                owningVipHandler.remove(vipPlayer, owningVip);
            });

            if (vipPlayer.getActiveVip() != null) {
                if (vipPlayer.getActiveVip().getType() != VipType.BASIC) {
                    return;
                }

                vipPlayer.getActiveVip().decrementDuration(elapsedSeconds);

                if (vipPlayer.getActiveVip().getDuration() > -1) {
                    return;
                }

                vipHandler.remove(vipPlayer);
            }
        });
    }

    private <T> @NonNull T config(@NonNull Class<? extends ConfigCache<?>> clazz) {
        return configCacheManager.get(clazz);
    }

    private void regListeners(@NonNull Listener... listeners) {
        final PluginManager pluginManager = plugin.getServer().getPluginManager();
        for (final Listener listener : listeners) {
            pluginManager.registerEvents(listener, plugin);
        }
    }

    private void registerListeners() {
    }

    private void registerCommands() {
        final CommandRegistry registry = new CommandRegistry(plugin);

        registry.command(
            new Help(),
            new CreateKey(
                keyGenerator, config(VipConfig.class), databaseAccessor.getValidKeyRepository(), scheduler
            ),
            new RemoveKey(databaseAccessor.getValidKeyRepository(), scheduler),
            new SeeKeys(
                scheduler, databaseAccessor.getValidKeyRepository(), config(VipConfig.class),
                config(MainConfig.class)
            ),
            new VipDurationLeft(databaseAccessor.getPlayerService()),
            new UseKey(databaseAccessor.getValidKeyRepository(), scheduler, vipHandler),
            new GiveVip(databaseAccessor.getPendingVipRepository(), vipHandler, config(VipConfig.class)),
            new SetVip(vipHandler, config(VipConfig.class)),
            new RemoveVip(
                config(VipConfig.class), databaseAccessor.getPlayerService(), vipHandler,
                owningVipHandler
            ),
            new SwitchVip(
                databaseAccessor.getPlayerService(), databaseAccessor.getPlayerCache(),
                config(MainConfig.class), config(VipConfig.class), owningVipHandler, vipHandler
            ),
            new RemovePendingVips(databaseAccessor.getPendingVipRepository()),
            new VipTop(),
            new VipMenu()
        );
        registry.autoTabCompleter(
            "criarkey", "removerkey", "verkeys", "usarkey", "tempovip", "darvip", "setarvip",
            "removervip", "trocarvip", "vip"
        );
        registry.registerAll();
    }

    private void registerViews() {
        final ViewRegistry registry = new ViewRegistry(viewManager, menuContainerManager, configManager);
        final KeysController keysController = new KeysController(
            databaseAccessor.getValidKeyRepository(), scheduler, menuContainerManager
        );
        final VipItemsController vipItemsController = new VipItemsController(
            databaseAccessor.getVipItemsRepository(), scheduler
        );
        final OwningVipsController owningVipsController = new OwningVipsController(
            databaseAccessor.getPlayerService(), menuContainerManager
        );
        final VipKeyShopController vipKeyShopController = new VipKeyShopController(
            databaseAccessor.getSellingKeyRepository(), scheduler, menuContainerManager
        );

        registry.register(
            new VipItemsEditView(config(VipItemsConfig.class)),
            new VipTopView(topManager),
            new VipMenuView(
                databaseAccessor.getPlayerService(), keysController, vipItemsController,
                owningVipsController, vipKeyShopController, config(VipConfig.class),
                config(MainConfig.class)
            ),
            new VipItemsView(
                databaseAccessor.getVipItemsRepository(), scheduler, vipItemsController,
                config(MainConfig.class), config(VipConfig.class), config(VipItemsConfig.class)
            ),
            new KeysView(config(VipConfig.class), keysController),
            new OwningVipsView(config(VipConfig.class), owningVipsController),
            new KeyMarketView(
                databaseAccessor.getSellingKeyRepository(), scheduler, vipKeyShopController,
                config(VipConfig.class)
            ),
            new KeyMarketPurchaseView(
                vipKeyShopController, databaseAccessor.getSellingKeyRepository(), scheduler,
                pointsManager, databaseAccessor.getValidKeyRepository(), keyGenerator,
                config(VipConfig.class)
            )
        );
    }

    private void scheduleTasks() {
        scheduler.newTimer(() ->
            ConfigUtils.update(config(ServerConfig.class), config ->
                config.set("Ultimo-instante-online", System.nanoTime())
            )
        ).delay(0L).period(20L).run();
        scheduler.newTimer(topManager::update)
            .delay(0L)
            .period(20 * 60 * 5)
            .run();
    }
}
