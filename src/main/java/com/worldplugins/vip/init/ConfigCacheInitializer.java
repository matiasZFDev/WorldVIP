package com.worldplugins.vip.init;

import com.worldplugins.lib.common.Initializer;
import com.worldplugins.lib.config.cache.impl.EffectsConfig;
import com.worldplugins.lib.config.cache.impl.MessagesConfig;
import com.worldplugins.lib.config.cache.impl.SoundsConfig;
import com.worldplugins.lib.manager.config.ConfigCacheManager;
import com.worldplugins.lib.manager.config.ConfigCacheManagerImpl;
import com.worldplugins.lib.manager.config.ConfigManager;
import com.worldplugins.lib.registry.ConfigCacheRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ConfigCacheInitializer implements Initializer<ConfigCacheManager> {
    private final @NonNull ConfigManager configManager;

    @Override
    public ConfigCacheManager init() {
        final ConfigCacheManager cacheManager = new ConfigCacheManagerImpl();
        final ConfigCacheRegistry registry = new ConfigCacheRegistry(cacheManager, configManager);
        registry.register(
            MessagesConfig.class,
            SoundsConfig.class,
            EffectsConfig.class
        );
        cacheManager.update();
        return cacheManager;
    }
}
