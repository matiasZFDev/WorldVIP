package com.worldplugins.vip.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.vip.config.data.VipData;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.stream.Collectors;
import java.util.stream.Stream;

@ExtensionMethod(value = {
    ConfigurationExtensions.class
})

public class VipConfig implements InjectedConfigCache<VipData> {
    @ConfigSpec(path = "vip")
    public @NonNull VipData transform(@NonNull FileConfiguration config) {
        return new VipData(config.map(section -> new VipData.VIP(
            section.getByte("Id"),
            section.getString("Nome"),
            section.getString("Display"),
            section.getString("Grupo"),
            section.getStringList("Comandos-ativacao"),
            new VipData.VIP.Pricing(
                ((Stream<String>) section.section("Precos").getKeys(false).stream())
                    .map(key ->
                        new VipData.VIP.Pricing.PricePair(
                            key.equals("Permanente")
                                ? -1
                                : Integer.parseInt(key.split("-")[0]),
                            section.getDouble("Precos." + key)
                        )
                    )
                    .collect(Collectors.toList())
            )
        )));
    }
}
