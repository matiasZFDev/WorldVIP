package com.worldplugins.vip.command;

import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.view.VipItemsEditView;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.view.Views;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class SetVipItems implements CommandModule {
    private final @NotNull ConfigModel<VipData> vipConfig;

    public SetVipItems(@NotNull ConfigModel<VipData> vipConfig) {
        this.vipConfig = vipConfig;
    }

    @Command(name = "vip itens")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldvip.setitems")) {
            respond(sender, "Itens-vip-permissoes");
            return;
        }

        if (!(sender instanceof Player)) {
            respond(sender, "Comando-jogador");
            return;
        }

        if (args.length != 1) {
            respond(sender, "Itens-vip-uso");
            return;
        }

        final Player player = (Player) sender;
        final String vipName = args[0];

        if (vipConfig.data().getByName(vipName) == null) {
            final List<String> vipList = vipConfig.data().all().stream()
                    .map(VipData.VIP::name)
                    .collect(Collectors.toList());
            respond(player, "Vip-inexistente", message -> message.replace(
                to("@nome", vipName),
                to("@vips", vipList.toString())
            ));
            return;
        }

        Views.get().open(player, VipItemsEditView.class, new VipItemsEditView.Context(vipName));
    }
}
