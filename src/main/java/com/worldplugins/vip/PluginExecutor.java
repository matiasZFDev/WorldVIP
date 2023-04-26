package com.worldplugins.vip;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.cache.impl.EffectsConfig;
import com.worldplugins.lib.config.cache.impl.MessagesConfig;
import com.worldplugins.lib.config.cache.impl.SoundsConfig;
import com.worldplugins.lib.registry.CommandRegistry;
import com.worldplugins.lib.registry.ViewRegistry;
import com.worldplugins.vip.command.*;
import com.worldplugins.vip.command.key.*;
import com.worldplugins.vip.command.vip.GiveVip;
import com.worldplugins.vip.command.vip.RemoveVip;
import com.worldplugins.vip.command.vip.SetVip;
import com.worldplugins.vip.command.vip.SwitchVip;
import com.worldplugins.vip.config.MainConfig;
import com.worldplugins.vip.config.VipConfig;
import com.worldplugins.vip.config.VipItemsConfig;
import com.worldplugins.vip.database.DatabaseAccessor;
import com.worldplugins.vip.handler.VipActivationHandler;
import com.worldplugins.vip.handler.VipRemovalHandler;
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
import com.worldplugins.vip.key.ConfigKeyGenerator;
import com.worldplugins.vip.key.KeyStorageManager;
import com.worldplugins.vip.key.VipKeyGenerator;
import com.worldplugins.vip.manager.PermissionManager;
import com.worldplugins.vip.view.VipItemsEditView;
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
    private final @NonNull PermissionManager permissionManager;
    private final @NonNull VipActivationHandler vipActivationHandler;
    private final @NonNull VipRemovalHandler vipRemovalHandler;

    public PluginExecutor(@NonNull JavaPlugin plugin) {
        this.plugin = plugin;
        scheduler = new SchedulerBuilder(plugin);
        configManager = new YamlConfigManager(plugin);
        configCacheManager = new ConfigCacheInitializer(configManager).init();
        menuContainerManager = new MenuContainerManagerImpl();
        viewManager = new ViewManagerImpl();

        databaseAccessor = new DatabaseInitializer(configManager, plugin, scheduler).init();
        permissionManager = new PermissionManagerInitializer().init();
        vipActivationHandler = new VipActivationHandler(
            databaseAccessor.getPlayerService(), databaseAccessor.getVipItemsRepository(),
            scheduler, permissionManager, config(VipConfig.class), config(MainConfig.class),
            config(VipItemsConfig.class)
        );
        vipRemovalHandler = new VipRemovalHandler(
            databaseAccessor.getPlayerService(), vipActivationHandler, permissionManager,
            config(VipConfig.class), config(MainConfig.class)
        );
    }

    /**
     * @return A runnable executed when disabling
     * */
    public @NonNull Runnable execute() {
        prepareGlobalAccess();
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
        final KeyStorageManager keyStorageManager = new KeyStorageManager(
            databaseAccessor.getPlayerService(), databaseAccessor.getValidKeyRepository(), scheduler
        );
        final VipKeyGenerator keyGenerator = new ConfigKeyGenerator(config(MainConfig.class));

        registry.command(
            new Help(),
            new GenerateKey(
                config(VipConfig.class), databaseAccessor.getValidKeyRepository(), keyGenerator,
                scheduler, keyStorageManager
            ),
            new CreateKey(
                config(VipConfig.class), databaseAccessor.getValidKeyRepository(), scheduler,
                keyStorageManager
            ),
            new RemoveKey(keyStorageManager, scheduler),
            new SeeKeys(
                databaseAccessor.getPlayerService(), scheduler, config(VipConfig.class),
                config(MainConfig.class)
            ),
            new VipDurationLeft(databaseAccessor.getPlayerService(), scheduler),
            new UseKey(databaseAccessor.getValidKeyRepository(), scheduler, vipActivationHandler),
            new GiveVip(databaseAccessor.getPendingVipRepository(), vipActivationHandler, config(VipConfig.class)),
            new SetVip(vipActivationHandler, config(VipConfig.class)),
            new RemoveVip(
                config(VipConfig.class), databaseAccessor.getPlayerService(), scheduler, vipRemovalHandler
            ),
            new SwitchVip(
                databaseAccessor.getPlayerService(), databaseAccessor.getPlayerCache(),
                config(MainConfig.class), config(VipConfig.class), vipRemovalHandler, vipActivationHandler
            )
        );
        registry.autoTabCompleter(
            "gerarkey", "removerkey", "verkeys", "usarkey", "tempovip", "darvip", "setarvip",
            "removervip", "trocarvip"
        );
        registry.registerAll();
    }

    private void registerViews() {
        final ViewRegistry registry = new ViewRegistry(viewManager, menuContainerManager, configManager);
        registry.register(
            new VipItemsEditView(config(VipItemsConfig.class))
        );
    }

    private void scheduleTasks() {

    }
}
