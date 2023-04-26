package com.worldplugins.vip.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.lib.util.BukkitSerializer;
import com.worldplugins.vip.config.data.VipItemsData;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.stream.Collectors;

@ExtensionMethod({
    ConfigurationExtensions.class
})

public class VipItemsConfig implements InjectedConfigCache<VipItemsData> {
    @ConfigSpec(path = "itens_data")
    public @NonNull VipItemsData transform(@NonNull FileConfiguration config) {
        return new VipItemsData(
            config.getKeys(false).stream()
                .map(key -> new VipItemsData.VipItems(key, BukkitSerializer.deserialize(config.getString(key))))
                .collect(Collectors.toList())
        );
    }
}
