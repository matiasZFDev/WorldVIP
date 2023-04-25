package com.worldplugins.vip.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.vip.config.data.VipData;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;

@ExtensionMethod({
    ConfigurationExtensions.class
})

public class VipConfig implements InjectedConfigCache<VipData> {
    @ConfigSpec(path = "vip")
    public @NonNull VipData transform(@NonNull FileConfiguration config) {
        return new VipData(config.map(section -> new VipData.VIP(
            section.getByte("Id"),
            section.getString("Nome"),
            section.getString("Display"),
            section.getString("Grupo")
        )));
    }
}
