package com.worldplugins.vip.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.CommandTarget;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.view.VipMenuView;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@ExtensionMethod({
    ViewExtensions.class
})

public class VipMenu implements CommandModule {
    @Command(
        name = "vip",
        target = CommandTarget.PLAYER,
        usage = "&cArgumentos invalidos. Digite /vip."
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = (Player) sender;
        player.openView(VipMenuView.class);
    }
}
