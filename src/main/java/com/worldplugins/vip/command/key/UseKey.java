package com.worldplugins.vip.command.key;

import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.player.model.VIP;
import com.worldplugins.vip.handler.VipHandler;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.util.Scheduler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class UseKey implements CommandModule {
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull Scheduler scheduler;
    private final @NotNull VipHandler activationHandler;

    public UseKey(
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull Scheduler scheduler,
        @NotNull VipHandler activationHandler
    ) {
        this.validKeyRepository = validKeyRepository;
        this.scheduler = scheduler;
        this.activationHandler = activationHandler;
    }

    @Command(name = "usarkey")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            respond(sender, "Comando-jogador");
            return;
        }

        if (args.length != 1) {
            respond(sender, "Usar-key-uso");
            return;
        }

        final Player player = (Player) sender;
        final String code = args[0];

        validKeyRepository.getKeyByCode(code).thenAccept(key ->
            scheduler.runTask(0, false, () -> {
                if (!player.isOnline()) {
                    return;
                }

                if (key == null) {
                    respond(sender, "Key-inexistente", message -> message.replace(
                        to("@key", code)
                    ));
                    return;
                }

                final VIP vip = new VIP(key.vipId(), key.vipType(), key.vipDuration());
                activationHandler.activate(player.getUniqueId(), vip, true);

                if (key.usages() > 1) {
                    validKeyRepository.consumeKey(key);
                } else {
                    validKeyRepository.removeKey(key);
                }
            })
        );
    }
}
