package com.worldplugins.vip.command.key;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class
})

@RequiredArgsConstructor
public class RemoveKey implements CommandModule {
    private final @NonNull ValidKeyRepository validKeyRepository;
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

        validKeyRepository.getKeyByCode(code).thenAccept(key ->
            scheduler.newTask(() -> {
                if (key == null) {
                    sender.respond("Remover-key-inexistente", message -> message.replace(
                        "@key".to(code)
                    ));
                    return;
                }

                validKeyRepository.removeKey(key);
                sender.respond("Key-removida", message -> message.replace(
                    "@key".to(code)
                ));
            }).run()
        );
    }
}
