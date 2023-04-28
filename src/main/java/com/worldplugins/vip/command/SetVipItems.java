package com.worldplugins.vip.command;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.CommandTarget;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.extension.ViewExtensions;
import com.worldplugins.vip.view.VipItemsEditView;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.stream.Collectors;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    ViewExtensions.class
})

@RequiredArgsConstructor
public class SetVipItems implements CommandModule {
    private final @NonNull ConfigCache<VipData> vipConfig;

    @Command(
        name = "vip itens",
        permission = "worldvip.setitems",
        target = CommandTarget.PLAYER,
        argsChecks = {@ArgsChecker(size = 1)},
        usage = "&cArgumentos invalidos. Digite /vip itens <vip>"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = (Player) sender;
        final String vipName = args[0];

        if (vipConfig.data().getByName(vipName) == null) {
            final List<String> vipList = vipConfig.data().all().stream()
                    .map(VipData.VIP::getName)
                    .collect(Collectors.toList());
            player.respond("Vip-inexistente", message -> message.replace(
                "@nome".to(vipName),
                "@vips".to(vipList.toString())
            ));
            return;
        }

        player.openView(VipItemsEditView.class, new VipItemsEditView.Context(vipName));
    }
}
