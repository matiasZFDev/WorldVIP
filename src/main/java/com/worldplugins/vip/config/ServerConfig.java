package com.worldplugins.vip.config;

import com.worldplugins.vip.config.data.ServerData;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

public class ServerConfig implements ConfigModel<ServerData> {
    private @UnknownNullability ServerData data;
    private final @NotNull ConfigWrapper configWrapper;

    public ServerConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new ServerData(config.getLong("Ultimo-instante-online"));
    }

    @Override
    public @NotNull ServerData data() {
        return data;
    }

    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }
}
