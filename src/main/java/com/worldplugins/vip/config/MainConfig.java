package com.worldplugins.vip.config;

import com.worldplugins.vip.config.data.MainData;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import static me.post.lib.util.Colors.color;

public class MainConfig implements ConfigModel<MainData> {
    private @UnknownNullability MainData data;
    private final @NotNull ConfigWrapper configWrapper;

    public MainConfig(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        final FileConfiguration config = configWrapper.unwrap();
        data = new MainData(
            new MainData.KeyGenOptions(
                (byte) config.getInt("Geracao-key.Tamanho"),
                config.getBoolean("Geracao-key.Numeros"),
                config.getBoolean("Geracao-key.Letras-minusculas"),
                config.getBoolean("Geracao-key.Letras-maiusculas")
            ),
            new MainData.KeyListingOptions(
                config.getStringList("Ver-keys.Mensagem-jogador"),
                config.getStringList("Ver-keys.Mensagem-pessoal"),
                config.getString("Formato-key"),
                color(config.getString("Mensagem-hover"))
            ),
            config.getBoolean("Stackar-vips"),
            config.getBoolean("Coleta-de-itens"),
            config.getInt("Delay-trocar-vip"),
            config.getBoolean("Reducao-simultanea"),
            config.getInt("Limite-de-keys-na-venda-por-jogador")
        );
    }

    @Override
    public @NotNull MainData data() {
        return data;
    }

    @Override
    public @NotNull ConfigWrapper wrapper() {
        return configWrapper;
    }
}
