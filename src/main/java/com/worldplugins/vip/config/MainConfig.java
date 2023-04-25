package com.worldplugins.vip.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.vip.config.data.MainData;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.file.FileConfiguration;

@ExtensionMethod({
    ConfigurationExtensions.class
})

public class MainConfig implements InjectedConfigCache<MainData> {
    @ConfigSpec(path = "config")
    public @NonNull MainData transform(@NonNull FileConfiguration config) {
        return new MainData(
            config.getString("Formato-key"),
            new MainData.KeyGenOptions(
                config.getByte("Tamanho"),
                config.getBoolean("Geracao-key.Numeros"),
                config.getBoolean("Geracao-key.Letras-minusculas"),
                config.getBoolean("Geracao-key.Letras-maiusculas")
            )
        );
    }
}
