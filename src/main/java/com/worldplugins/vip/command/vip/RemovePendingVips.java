package com.worldplugins.vip.command.vip;

import com.worldplugins.vip.database.pending.PendingVipRepository;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class RemovePendingVips implements CommandModule {
    private final @NotNull PendingVipRepository pendingVipRepository;

    public RemovePendingVips(@NotNull PendingVipRepository pendingVipRepository) {
        this.pendingVipRepository = pendingVipRepository;
    }

    @Command(name = "vip removerpendentes")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldvip.removerpendentes")) {
            respond(sender, "Remover-pendentes-permissoes");
            return;
        }

        if (args.length != 1) {
            respond(sender, "Remover-pendentes-uso");
            return;
        }

        pendingVipRepository.removePendings(args[0]);
        respond(sender, "Vip-pendentes-removidos", message -> message.replace(
            to("@jogador", args[0])
        ));
    }
}
