package com.worldplugins.vip.key;

import com.worldplugins.vip.controller.KeysController;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import me.post.lib.util.Scheduler;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.worldplugins.vip.Response.respond;

public class KeyManagement {
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull Scheduler scheduler;
    private final @NotNull KeysController keysController;

    public KeyManagement(
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull Scheduler scheduler,
        @NotNull KeysController keysController
    ) {
        this.validKeyRepository = validKeyRepository;
        this.scheduler = scheduler;
        this.keysController = keysController;
    }

    public void manage(
        @NotNull Player player,
        @NotNull String keyCode,
        int keysViewPage,
        @NotNull Consumer<ValidVipKey> onSuccess
    ) {
        validKeyRepository.getKeyByCode(keyCode).thenAccept(key -> scheduler.runTask(0, false, () -> {
            if (!player.isOnline()) {
                return;
            }

            if (key == null) {
                keysController.openView(player, keysViewPage);
                respond(player, "Gerenciar-key-error");
                return;
            }

            if (key.generatorName() == null) {
                keysController.openView(player, keysViewPage);
                respond(player, "Gerenciar-key-error");
                return;
            }

            if (!player.getName().equals(key.generatorName())) {
                keysController.openView(player, keysViewPage);
                respond(player, "Gerenciar-key-error");
                return;
            }

            onSuccess.accept(key);
        }));
    }
}
