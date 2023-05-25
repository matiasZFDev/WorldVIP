package com.worldplugins.vip.command;

import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import static com.worldplugins.vip.Response.respond;

public class Help implements CommandModule {
    @Command(name = "vip ajuda")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        respond(sender, sender.hasPermission("worldplugin.ajudastaff") ? "Ajuda-staff" : "Ajuda");
    }
}
