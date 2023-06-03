package com.worldplugins.vip.config;

import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.vip.config.data.VipData;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.stream.Collectors;

public class VipConfig implements ConfigModel<VipData> {
    private @UnknownNullability VipData data;
    private final @NotNull ConfigWrapper configWrapper;

    public VipConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new VipData(ConfigSections.map(config, section -> new VipData.VIP(
            (byte) section.getInt("Id"),
            section.getString("Nome"),
            section.getString("Display"),
            section.getString("Grupo"),
            section.getStringList("Comandos-ativacao"),
            new VipData.VIP.Pricing(
                section.getConfigurationSection("Precos").getKeys(false).stream()
                    .map(key ->
                        new VipData.VIP.Pricing.PricePair(
                            key.equalsIgnoreCase("Permanente")
                                ? -1
                                : Integer.parseInt(key.split("-")[0]),
                            section.getDouble("Precos." + key)
                        )
                    )
                    .collect(Collectors.toList())
            ),
            ConfigSections.getItem(section.getConfigurationSection("Iten"))
        )));
    }

    @Override
    public @NotNull VipData data() {
        return data;
    }

    @Override
    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }
}
