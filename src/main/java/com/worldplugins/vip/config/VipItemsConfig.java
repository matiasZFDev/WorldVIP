package com.worldplugins.vip.config;

import com.worldplugins.vip.config.data.VipItemsData;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import me.post.lib.util.BukkitSerializer;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.stream.Collectors;

public class VipItemsConfig implements ConfigModel<VipItemsData> {
    private @UnknownNullability VipItemsData data;
    private final @NotNull ConfigWrapper configWrapper;

    public VipItemsConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new VipItemsData(
            config.getKeys(false).stream()
                .map(key -> new VipItemsData.VipItems(key, BukkitSerializer.deserialize(config.getString(key))))
                .collect(Collectors.toList())
        );
    }

    @Override
    public @NotNull VipItemsData data() {
        return data;
    }

    @Override
    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }
}
