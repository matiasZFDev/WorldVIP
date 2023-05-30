package com.worldplugins.vip.command.key;

import com.worldplugins.lib.util.Strings;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.Colors;
import me.post.lib.util.Scheduler;
import me.post.lib.util.Time;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class SeeKeys implements CommandModule {
    private final @NotNull Scheduler scheduler;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ConfigModel<MainData> mainConfig;

    public SeeKeys(
        @NotNull Scheduler scheduler,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ConfigModel<MainData> mainConfig
    ) {
        this.scheduler = scheduler;
        this.validKeyRepository = validKeyRepository;
        this.vipConfig = vipConfig;
        this.mainConfig = mainConfig;
    }

    @Command(name = "verkeys")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player)) {
            respond(sender, "Comando-jogador");
            return;
        }

        final Player player = (Player) sender;

        if (args.length > 1) {
            if (!player.hasPermission("worldvip.verkeys")) {
                respond(player, "Comando-sem-permissoes");
                return;
            }

            final Player keysPlayer = Bukkit.getPlayer(args[0]);

            if (keysPlayer == null) {
                respond(player, "Jogador-offline");
                return;
            }

            validKeyRepository
                .getKeys(keysPlayer.getName())
                .thenAccept(keys -> scheduler.runTask(0, false, () -> {
                    if (keys.isEmpty()) {
                        respond(player, "Jogador-sem-keys", message -> message.replace(
                            to("@jogador", player.getName())
                        ));
                        return;
                    }

                    messageKeyList(keysPlayer, keys, true);
                }));
            return;
        }

        validKeyRepository.getKeys(player.getName()).thenAccept(keys -> scheduler.runTask(0, false, () -> {
            if (keys.isEmpty()) {
                respond(sender, "Ver-keys-vazio");
                return;
            }

            messageKeyList(player, keys, false);
        }));
    }

    private void messageKeyList(
        @NotNull Player player,
        @NotNull Collection<ValidVipKey> keys,
        boolean playerKeys
    ) {
        final List<String> messageList = playerKeys
            ? mainConfig.data().keyListing().playerKeysMessage()
            : mainConfig.data().keyListing().ownKeysMessages();
        final BaseComponent[] keyList = keys.stream()
            .map(key -> {
                final VipData.VIP configVip = vipConfig.data().getById(key.vipId());
                final String durationFormat = key.vipDuration() == -1
                    ? GlobalValues.PERMANENT_DURATION
                    : Time.toFormat(key.vipDuration());
                final String line = Strings.replace(
                    mainConfig.data().keyListing().keyFormat(),
                    to("@key", key.code()),
                    to("@vip", configVip.display()),
                    to("@tipo", key.vipType().getName().toUpperCase()),
                    to("@duracao", durationFormat),
                    to("@usos", String.valueOf(key.usages()))
                );
                return new ComponentBuilder(Colors.color(line))
                    .event(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[] {
                            new TextComponent(mainConfig.data().keyListing().hoverMessage())
                        }
                    ))
                    .event(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/usarkey " + key.code()
                    ))
                    .create();
            })
            .flatMap(Stream::of)
            .toArray(BaseComponent[]::new);

        final int messageSize = (messageList.size() - 1) + keyList.length;
        final BaseComponent[] message = new BaseComponent[messageSize];
        boolean replaced = false;

        for (int i = 0, j = 0; j < messageSize; i++, j++) {
            if (!replaced && messageList.get(i).contains("@@keys")) {
                for (int k = 0; j < i + keyList.length; j++, k++) {
                    message[j] = keyList[k];
                }

                replaced = true;
                j--;
                continue;
            }

            message[j] = new TextComponent(Colors.color(messageList.get(i)));
        }

        player.spigot().sendMessage(message);
    }
}
