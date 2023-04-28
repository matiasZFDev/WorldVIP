package com.worldplugins.vip.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.vip.config.data.ServerData;
import lombok.NonNull;
import org.bukkit.configuration.file.FileConfiguration;

public class ServerConfig implements InjectedConfigCache<ServerData> {
    @ConfigSpec(path = "server")
    public @NonNull ServerData transform(@NonNull FileConfiguration config) {
        return new ServerData(config.getLong("Ultimo-instante-online"));
    }
}
