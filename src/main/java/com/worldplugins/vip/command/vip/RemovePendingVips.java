package com.worldplugins.vip.command.vip;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.vip.database.pending.PendingVipRepository;
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
public class RemovePendingVips implements CommandModule {
    private final @NonNull PendingVipRepository pendingVipRepository;

    @Command(
        name = "vip removerpendentes",
        permission = "worldvip.removerpendentes",
        argsChecks = {@ArgsChecker(size = 1)},
        usage = "&cArgumentos invalidos. Digite /vip removerpendentes <jogador>"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        pendingVipRepository.removePendings(args[0]);
        sender.respond("Vip-pendentes-removidos", message -> message.replace(
            "@jogador".to(args[0])
        ));
    }
}
