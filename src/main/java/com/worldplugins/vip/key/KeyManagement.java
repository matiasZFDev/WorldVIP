package com.worldplugins.vip.key;

import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.controller.KeysController;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.extension.ViewExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

@ExtensionMethod({
    ViewExtensions.class,
    ResponseExtensions.class
})

@RequiredArgsConstructor
public class KeyManagement {
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull KeysController keysController;

    public void manage(
        @NonNull Player player,
        @NonNull String keyCode,
        int keysViewPage,
        @NonNull Consumer<ValidVipKey> onSuccess
    ) {
        validKeyRepository.getKeyByCode(keyCode).thenAccept(key -> scheduler.newTask(() -> {
            if (!player.isOnline()) {
                return;
            }

            if (key == null) {
                player.respond("Gerenciar-key-error");
                keysController.openView(player, keysViewPage);
                return;
            }

            if (key.getGeneratorName() == null) {
                player.respond("Gerenciar-key-error");
                keysController.openView(player, keysViewPage);
                return;
            }

            if (!player.getName().equals(key.getGeneratorName())) {
                player.respond("Gerenciar-key-error");
                keysController.openView(player, keysViewPage);
                return;
            }

            onSuccess.accept(key);
        }).run());
    }
}
