package com.worldplugins.vip;

import com.worldplugins.lib.config.Updatables;
import com.worldplugins.vip.command.*;
import com.worldplugins.vip.command.key.*;
import com.worldplugins.vip.command.vip.*;
import com.worldplugins.vip.config.MainConfig;
import com.worldplugins.vip.config.ServerConfig;
import com.worldplugins.vip.config.VipConfig;
import com.worldplugins.vip.config.VipItemsConfig;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.ServerData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.config.data.VipItemsData;
import com.worldplugins.vip.config.menu.*;
import com.worldplugins.vip.database.DatabaseAccessor;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.handler.VipHandler;
import com.worldplugins.vip.handler.OwningVipHandler;
import com.worldplugins.vip.init.DatabaseInitializer;
import com.worldplugins.vip.init.PermissionManagerInitializer;
import com.worldplugins.vip.init.PointsInitializer;
import com.worldplugins.vip.key.ConfigKeyGenerator;
import com.worldplugins.vip.key.KeyGeneratorMatcher;
import com.worldplugins.vip.key.VipKeyGenerator;
import com.worldplugins.vip.listener.ActivePendingVipsListener;
import com.worldplugins.vip.listener.PlayerVipTagChatListener;
import com.worldplugins.vip.manager.PermissionManager;
import com.worldplugins.vip.manager.PointsManager;
import com.worldplugins.vip.manager.VipTopManager;
import com.worldplugins.vip.task.VipDatabaseUpdate;
import com.worldplugins.vip.task.VipTimeConsumeTask;
import com.worldplugins.vip.view.*;
import me.post.lib.command.process.CommandRegistry;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigManager;
import me.post.lib.config.wrapper.YamlConfigManager;
import me.post.lib.util.Configurations;
import me.post.lib.util.ConversationProvider;
import me.post.lib.util.Scheduler;
import me.post.lib.view.Views;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class PluginExecutor {
    private final @NotNull JavaPlugin plugin;
    private final @NotNull Scheduler scheduler;
    private final @NotNull ConfigManager configManager;

    private final @NotNull Updatables updatables;
    private final @NotNull ConfigModel<MainData> mainConfig;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<ServerData> serverConfig;
    private final @NotNull ConfigModel<VipItemsData> vipItemsConfig;

    private final @NotNull DatabaseAccessor databaseAccessor;
    private final VipKeyGenerator keyGenerator;
    private final @NotNull PermissionManager permissionManager;
    private final @NotNull PointsManager pointsManager;
    private final @NotNull VipHandler vipHandler;
    private final @NotNull OwningVipHandler owningVipHandler;
    private final @NotNull VipTopManager topManager;

    private final @NotNull VipTimeConsumeTask vipTimeConsumeTask;
    private final @NotNull VipDatabaseUpdate vipDatabaseUpdate;

    public PluginExecutor(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        scheduler = new Scheduler(plugin);
        configManager = new YamlConfigManager(plugin);
        loadConfiguration();

        updatables = new Updatables();
        mainConfig = updatables.include(new MainConfig(configManager.getWrapper("config")));
        serverConfig = updatables.include(new ServerConfig(configManager.getWrapper("server")));
        vipConfig = updatables.include(new VipConfig(configManager.getWrapper("vip")));
        vipItemsConfig = updatables.include(new VipItemsConfig(configManager.getWrapper("itens_data")));

        databaseAccessor = new DatabaseInitializer(configManager.getWrapper("config"), plugin, scheduler).init();
        keyGenerator = new ConfigKeyGenerator(mainConfig);
        permissionManager = new PermissionManagerInitializer().init();
        pointsManager = new PointsInitializer(plugin).init();
        owningVipHandler = new OwningVipHandler(
            databaseAccessor.playerService(),
            permissionManager,
            vipConfig,
            mainConfig
        );
        vipHandler = new VipHandler(
            databaseAccessor.playerService(),
            databaseAccessor.vipItemsRepository(),
            permissionManager,
            owningVipHandler,
            vipConfig,
            mainConfig,
            vipItemsConfig
        );
        topManager = new VipTopManager(databaseAccessor.playerCache());

        vipTimeConsumeTask = new VipTimeConsumeTask(
            databaseAccessor.playerCache(),
            vipHandler,
            owningVipHandler,
            mainConfig
        );
        vipDatabaseUpdate = new VipDatabaseUpdate(
            databaseAccessor.playerCache(),
            databaseAccessor.playerService(),
            mainConfig
        );
    }

    private void loadConfiguration() {
        Arrays
            .asList(
                "config", "itens_data", "vip", "server",
                "menu/coletar_itens", "menu/confirmar_ativacao_key",
                "menu/gerenciar_key", "menu/keys", "menu/mercado_comprar_key",
                "menu/mercado_keys", "menu/top", "menu/vip", "menu/vips_secondarios",
                "menu/mercado_gerenciar_keys",
                "resposta/mensagens", "resposta/sons", "resposta/efeitos"
            )
            .forEach(configManager::load);
    }

    /**
     * @return A runnable executed when disabling.
     * */
    public @NotNull Runnable execute() {
        Response.setup(updatables, configManager);

        checkBasicVips();
        registerCommands();
        registerListeners();
        registerViews();
        registerPlaceholders();
        scheduleTasks();

        updatables.update();
        return () -> {
            vipDatabaseUpdate.run();
        };
    }

    private void checkBasicVips() {
        mainConfig.update();
        serverConfig.update();

        final long lastOnlineInstant = serverConfig.data().lastOnlineInstant();

        if (lastOnlineInstant == 0) {
            return;
        }

        final int elapsedSeconds = (int) TimeUnit
            .NANOSECONDS
            .toSeconds(System.nanoTime() - lastOnlineInstant);

        databaseAccessor.playerCache().getValues().forEach(vipPlayer -> {
            if (mainConfig.data().simultaneousReduction()) {
                vipPlayer.owningVips().vips().forEach(owningVip -> {
                    if (owningVip.type() != VipType.BASIC) {
                        return;
                    }

                    owningVip.decrementDuration(elapsedSeconds);
                });
            }

            final VIP activeVip = vipPlayer.activeVip();

            if (activeVip != null) {
                if (activeVip.type() != VipType.BASIC) {
                    return;
                }

                activeVip.decrementDuration(elapsedSeconds);
            }
        });

        vipTimeConsumeTask.run();
    }

    private void registerCommands() {
        final CommandRegistry registry = CommandRegistry.on(plugin);

        registry.addModules(
            new Help(),
            new CreateKey(
                keyGenerator,
                vipConfig,
                scheduler,
                databaseAccessor.validKeyRepository(),
                databaseAccessor.sellingKeyRepository()
            ),
            new RemoveKey(databaseAccessor.validKeyRepository(), scheduler),
            new SeeKeys(scheduler, databaseAccessor.validKeyRepository(), vipConfig, mainConfig),
            new VipDurationLeft(databaseAccessor.playerService()),
            new UseKey(databaseAccessor.validKeyRepository(), scheduler, vipHandler),
            new GiveVip(databaseAccessor.pendingVipRepository(), vipHandler, vipConfig),
            new SetVip(vipHandler, vipConfig),
            new RemoveVip(vipConfig, databaseAccessor.playerService(), vipHandler, owningVipHandler),
            new SwitchVip(
                databaseAccessor.playerService(),
                databaseAccessor.playerCache(),
                mainConfig,
                vipConfig,
                owningVipHandler,
                vipHandler
            ),
            new RemovePendingVips(databaseAccessor.pendingVipRepository()),
            new VipTop(),
            new VipMenu(),
            new SetVipItems(vipConfig),
            new Reload(configManager, updatables)
        );
        registry.registerAll();
    }

    private void registerListeners() {
        Arrays
            .asList(
                new ActivePendingVipsListener(
                    databaseAccessor.pendingVipRepository(),
                    scheduler,
                    vipHandler
                ),
                new PlayerVipTagChatListener(databaseAccessor.playerService(), vipConfig)
            )
            .forEach(listener -> plugin.getServer().getPluginManager().registerEvents(listener, plugin));
    }

    private void registerViews() {
        final KeyGeneratorMatcher keyGeneratorMatcher = new KeyGeneratorMatcher(
            databaseAccessor.validKeyRepository(), scheduler
        );
        final ConversationProvider conversationProvider = new ConversationProvider(plugin);

        Views.get().register(
            new VipItemsEditView(vipItemsConfig),
            new VipTopView(
                updatables.include(new VipTopMenuModel(configManager.getWrapper("menu/top"))),
                topManager
            ),
            new VipMenuView(
                updatables.include(new VipMenuModel(configManager.getWrapper("menu/vip"))),
                databaseAccessor.playerService(),
                vipConfig,
                mainConfig
            ),
            new VipItemsView(
                updatables.include(new VipItemsMenuModel(configManager.getWrapper("menu/coletar_itens"))),
                databaseAccessor.vipItemsRepository(),
                scheduler,
                mainConfig,
                vipConfig,
                vipItemsConfig
            ),
            new KeysView(
                updatables.include(new KeysMenuModel(configManager.getWrapper("menu/keys"))),
                databaseAccessor.validKeyRepository(),
                scheduler,
                keyGeneratorMatcher,
                vipConfig
            ),
            new OwningVipsView(
                updatables.include(new OwningVipsMenuModel(configManager.getWrapper("menu/vips_secondarios"))),
                databaseAccessor.playerService(),
                vipConfig
            ),
            new KeyMarketView(
                updatables.include(new KeyMarketMenuModel(configManager.getWrapper("menu/mercado_keys"))),
                databaseAccessor.sellingKeyRepository(),
                scheduler,
                vipConfig
            ),
            new KeyMarketPurchaseView(
                updatables.include(new KeyMarketPurchaseMenuModel(configManager.getWrapper("menu/mercado_comprar_key"))),
                databaseAccessor.sellingKeyRepository(),
                scheduler,
                pointsManager,
                databaseAccessor.validKeyRepository(),
                keyGenerator,
                vipConfig
            ),
            new ManageKeyView(
                updatables.include(new ManageKeyMenuModel(configManager.getWrapper("menu/gerenciar_key"))),
                keyGeneratorMatcher,
                conversationProvider,
                scheduler,
                databaseAccessor.sellingKeyRepository(),
                databaseAccessor.validKeyRepository(),
                vipConfig,
                mainConfig
            ),
            new ConfirmKeyActivationView(
                updatables.include(new ConfirmKeyActivationMenuModel(
                    configManager.getWrapper("menu/confirmar_ativacao_key")
                )),
                keyGeneratorMatcher,
                vipHandler,
                databaseAccessor.validKeyRepository(),
                vipConfig
            ),
            new ManageSellingKeysView(
                updatables.include(new ManageSellingKeysMenuModel(configManager.getWrapper("menu/mercado_gerenciar_keys"))),
                databaseAccessor.sellingKeyRepository(),
                databaseAccessor.validKeyRepository(),
                scheduler,
                vipConfig
            )
        );
    }

    private void registerPlaceholders() {
        new VipPlayerholder(databaseAccessor.playerService(), mainConfig, vipConfig).register();
    }

    private void scheduleTasks() {
        scheduler.runTimer(20, 20, false, () ->
            Configurations.update(serverConfig, config ->
                config.set("Ultimo-instante-online", System.nanoTime())
            )
        );
        scheduler.runTimer(20, 20 * 60 * 5, false, topManager::update);
        scheduler.runTimer(20, 20, false, vipTimeConsumeTask);
        scheduler.runTimer(20 * 10, 20 * 20, false, vipDatabaseUpdate);
    }
}
