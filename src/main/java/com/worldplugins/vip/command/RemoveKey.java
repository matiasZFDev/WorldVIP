package com.worldplugins.vip.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.key.KeyStorageManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class RemoveKey implements CommandModule {
    private final @NonNull KeyStorageManager keyStorageManager;
    private final @NonNull SchedulerBuilder scheduler;

    @Command(
        name = "removerkey",
        permission = "worldvip.removerkey",
        argsChecks = {@ArgsChecker(size = 1)},
        usage = "&cArgumentos invalidos. Digite /removerkey <key>"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final String code = args[0];

        if (code.length() > GlobalValues.MAX_KEY_LENGTH) {
            sender.respond("Remover-key-inexistente", message -> message.replace(
                "@key".to(code)
            ));
            return;
        }

        final UUID requesterId = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        keyStorageManager.get(requesterId, code).thenAccept(key -> {
            scheduler.newTask(() -> {
                if (key == null) {
                    sender.respond("Remover-key-inexistente", message -> message.replace(
                        "@key".to(code)
                    ));
                    return;
                }

                keyStorageManager.remove(key);
                sender.respond("Key-removida", message -> message.replace(
                    "@key".to(code)
                ));
            }).run();
        });
    }
}
