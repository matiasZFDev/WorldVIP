package com.worldplugins.vip.command.key;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.CommandTarget;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.handler.VipActivationHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class UseKey implements CommandModule {
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull VipActivationHandler activationHandler;

    @Command(
        name = "usarkey",
        target = CommandTarget.PLAYER,
        usage = "&cArgumentos invalidos. Digite /usarkey <key>",
        argsChecks = {@ArgsChecker(size = 1)}
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = (Player) sender;
        final String code = args[0];

        validKeyRepository.getKeyByCode(code).thenAccept(key -> {
            scheduler.newTask(() -> {
                if (key == null) {
                    sender.respond("Key-inexistente", message -> message.replace(
                        "@key".to(code)
                    ));
                    return;
                }

                final VIP vip = new VIP(key.getVipId(), key.getVipType(), key.getVipDuration());
                activationHandler.activate(player, vip, true);

                if (key.getUsages() > 1) {
                    validKeyRepository.consumeKey(code);
                } else {
                    validKeyRepository.removeKey(code);
                }
            }).run();
        });
    }
}
