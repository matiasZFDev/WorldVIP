package com.worldplugins.vip.command;

import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.view.Views;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import com.worldplugins.vip.view.VipMenuView;

import static com.worldplugins.vip.Response.respond;

public class VipMenu implements CommandModule {
    @Command(name = "vip")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            respond(sender, "Comando-jogador");
            return;
        }

        final Player player = (Player) sender;
        Views.get().open(player, VipMenuView.class);
    }
}
