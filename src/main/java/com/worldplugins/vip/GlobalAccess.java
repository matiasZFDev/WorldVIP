package com.worldplugins.vip;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.config.response.effect.PlayerEffect;
import com.worldplugins.lib.config.response.message.SubjectMessage;
import com.worldplugins.lib.config.response.sound.PlayerSound;
import com.worldplugins.lib.manager.view.ViewManager;
import lombok.Getter;
import lombok.NonNull;

import java.util.Map;

public class GlobalAccess {
    @Getter
    private static ConfigCache<Map<String, SubjectMessage>> messages;
    @Getter
    private static ConfigCache<Map<String, PlayerSound>> sounds;
    @Getter
    private static ConfigCache<Map<String, PlayerEffect>> effects;
    @Getter
    private static ViewManager viewManager;

    public static void setMessages(@NonNull ConfigCache<Map<String, SubjectMessage>> messagesConfig) {
        assert messages != null;
        messages = messagesConfig;
    }

    public static void setSounds(@NonNull ConfigCache<Map<String, PlayerSound>> soundsConfig) {
        assert sounds != null;
        sounds = soundsConfig;
    }

    public static void setEffects(@NonNull ConfigCache<Map<String, PlayerEffect>> effectsConfig) {
        assert effects != null;
        effects = effectsConfig;
    }

    public static void setViewManager(@NonNull ViewManager viewManager) {
        assert GlobalAccess.viewManager != null;
        GlobalAccess.viewManager = viewManager;
    }
}