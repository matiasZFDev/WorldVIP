package com.worldplugins.vip;

import com.worldplugins.lib.config.Updatables;
import com.worldplugins.lib.config.model.impl.EffectsModel;
import com.worldplugins.lib.config.model.impl.MessagesModel;
import com.worldplugins.lib.config.model.impl.SoundsModel;
import com.worldplugins.lib.config.model.impl.data.EffectsData;
import com.worldplugins.lib.config.model.impl.data.MessagesData;
import com.worldplugins.lib.config.model.impl.data.SoundsData;
import com.worldplugins.lib.config.model.impl.response.effect.PlayerEffect;
import com.worldplugins.lib.config.model.impl.response.message.SubjectMessage;
import com.worldplugins.lib.config.model.impl.response.sound.PlayerSound;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.config.wrapper.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class Response {
    private static ConfigModel<MessagesData> messages;
    private static ConfigModel<SoundsData> sounds;
    private static ConfigModel<EffectsData> effects;

    public static void setup(@NotNull Updatables updatables, @NotNull ConfigManager configManager) {
        messages = updatables.include(new MessagesModel(configManager.getWrapper("resposta/mensagens")));
        sounds = updatables.include(new SoundsModel(configManager.getWrapper("resposta/sons")));
        effects = updatables.include(new EffectsModel(configManager.getWrapper("resposta/efeitos")));
    }

    public static <T extends CommandSender> void respond(
        @NotNull T sender,
        @NotNull String key,
        @Nullable Function<SubjectMessage, SubjectMessage> transformFunction
    ) {
        message(sender, key, transformFunction);

        if (!(sender instanceof Player)) {
            return;
        }

        final PlayerEffect effect = effects.data().getEffect(key);
        final PlayerSound sound = sounds.data().getSound(key);

        if (effect != null) {
            effect.send(sender);
        }

        if (sound != null) {
            sound.send(sender);
        }
    }

    public static <T extends CommandSender> void respond(T sender, @NotNull String key) {
        respond(sender, key, null);
    }

    private static void message(
        @NotNull CommandSender sender,
        @NotNull String key,
        Function<SubjectMessage, SubjectMessage> transformFunction
    ) {
        final SubjectMessage message = messages.data().getMessage(key);

        if (message == null) {
            return;
        }

        if (transformFunction == null) {
            message.color().send(sender);
        } else {
            transformFunction.apply(message).color().send(sender);
        }
    }
}
