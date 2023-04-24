package com.worldplugins.vip.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.vip.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;

@ExtensionMethod({
    ResponseExtensions.class
})

public class Help implements CommandModule {
    @Command(
        name = "vip ajuda",
        usage = "&cArgumentos invalidos. Digite /vip ajuda."
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        if (sender.hasPermission("worldplugin.ajudastaff"))
            sender.respond("Ajuda-staff");
        else
            sender.respond("Ajuda");
    }
}
