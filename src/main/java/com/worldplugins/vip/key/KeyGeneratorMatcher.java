package com.worldplugins.vip.key;

import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.view.KeysView;
import me.post.lib.util.Scheduler;
import me.post.lib.view.Views;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.worldplugins.vip.Response.respond;

public class KeyGeneratorMatcher {
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull Scheduler scheduler;

    public KeyGeneratorMatcher(@NotNull ValidKeyRepository validKeyRepository, @NotNull Scheduler scheduler) {
        this.validKeyRepository = validKeyRepository;
        this.scheduler = scheduler;
    }

    public void tryMatch(
        @NotNull Player player,
        @NotNull String keyCode,
        int keysViewPage,
        @NotNull Consumer<ValidVipKey> onMatch
    ) {
        validKeyRepository.getKeyByCode(keyCode).thenAccept(key -> scheduler.runTask(0, false, () -> {
            if (!player.isOnline()) {
                return;
            }

            if (key == null || key.generatorName() == null || !player.getName().equals(key.generatorName())) {
                Views.get().open(player, KeysView.class, new KeysView.Context(keysViewPage));
                respond(player, "Gerenciar-key-error");
                return;
            }

            onMatch.accept(key);
        }));
    }
}
