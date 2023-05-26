package com.worldplugins.vip.command.key;

import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.util.Scheduler;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class RemoveKey implements CommandModule {
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull Scheduler scheduler;

    public RemoveKey(@NotNull ValidKeyRepository validKeyRepository, @NotNull Scheduler scheduler) {
        this.validKeyRepository = validKeyRepository;
        this.scheduler = scheduler;
    }

    @Command(name = "removerkey")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldvip.removerkey")) {
            respond(sender, "Remover-key-permissoes");
            return;
        }

        if (args.length != 1) {
            respond(sender, "Remover-key-uso");
            return;
        }

        final String code = args[0];

        if (code.length() > GlobalValues.MAX_KEY_LENGTH) {
            respond(sender, "Remover-key-inexistente", message -> message.replace(
                to("@key", code)
            ));
            return;
        }

        validKeyRepository.getKeyByCode(code).thenAccept(key ->
            scheduler.runTask(0, false, () -> {
                if (key == null) {
                    respond(sender, "Remover-key-inexistente", message -> message.replace(
                        to("@key", code)
                    ));
                    return;
                }

                validKeyRepository.removeKey(key);
                respond(sender, "Key-removida", message -> message.replace(
                    to("@key", code)
                ));
            })
        );
    }
}
