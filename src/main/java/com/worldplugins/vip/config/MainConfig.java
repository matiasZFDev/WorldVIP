package com.worldplugins.vip.config;

import com.worldplugins.lib.config.cache.InjectedConfigCache;
import com.worldplugins.lib.config.cache.annotation.ConfigSpec;
import com.worldplugins.lib.extension.bukkit.ColorExtensions;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.vip.config.data.MainData;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

@ExtensionMethod({
    ConfigurationExtensions.class,
    ColorExtensions.class
})

public class MainConfig implements InjectedConfigCache<MainData> {
    @ConfigSpec(path = "config")
    public @NonNull MainData transform(@NonNull FileConfiguration config) {
        return new MainData(
            new MainData.KeyGenOptions(
                config.getByte("Geracao-key.Tamanho"),
                config.getBoolean("Geracao-key.Numeros"),
                config.getBoolean("Geracao-key.Letras-minusculas"),
                config.getBoolean("Geracao-key.Letras-maiusculas")
            ),
            new MainData.KeyListingOptions(
                config.getStringList("Ver-keys.Mensagem-jogador"),
                config.getStringList("Ver-keys.Mensagem-pessoal"),
                config.getString("Formato-key"),
                config.getString("Mensagem-hover").color()
            ),
            config.getOrDefault("Stackar-vips", ConfigurationSection::getBoolean, false)
        );
    }
}
