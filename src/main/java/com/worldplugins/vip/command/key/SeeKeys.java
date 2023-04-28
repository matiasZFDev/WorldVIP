package com.worldplugins.vip.command.key;

import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.CommandTarget;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.ReplaceExtensions;
import com.worldplugins.lib.extension.TimeExtensions;
import com.worldplugins.lib.extension.bukkit.ColorExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.MainData;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.extension.ResponseExtensions;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    ReplaceExtensions.class,
    TimeExtensions.class,
    ColorExtensions.class
})

@RequiredArgsConstructor
public class SeeKeys implements CommandModule {
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull ConfigCache<MainData> mainConfig;

    @Command(
        name = "verkeys",
        target = CommandTarget.PLAYER,
        usage = "&cArgumentos invalidos. Digite /verkeys [jogador]"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final Player player = (Player) sender;

        if (args.length == 1) {
            if (!player.hasPermission("worldvip.verkeys")) {
                player.respond("Comando-sem-permissoes");
                return;
            }

            final Player keysPlayer = Bukkit.getPlayer(args[0]);

            if (keysPlayer == null) {
                player.respond("Jogador-offline");
                return;
            }

            validKeyRepository.getKeys(keysPlayer.getName()).thenAccept(keys -> scheduler.newTask(() -> {
                if (keys.isEmpty()) {
                    player.respond("Jogador-sem-keys", message -> message.replace(
                        "@jogador".to(player.getName())
                    ));
                    return;
                }

                messageKeyList(keysPlayer, keys, true);
            }).run());
            return;
        }

        validKeyRepository.getKeys(player.getName()).thenAccept(keys -> scheduler.newTask(() -> {
            if (keys.isEmpty()) {
                sender.respond("Ver-keys-vazio");
                return;
            }

            messageKeyList(player, keys, false);
        }).run());
    }

    private void messageKeyList(
        @NonNull Player player,
        @NonNull Collection<ValidVipKey> keys,
        boolean playerKeys
    ) {
        final List<String> messageList = playerKeys
            ? mainConfig.data().getKeyListing().getPlayerKeysMessage()
            : mainConfig.data().getKeyListing().getOwnKeysMessages();

        final BaseComponent[] keyList = keys.stream()
            .map(key -> {
                final VipData.VIP configVip = vipConfig.data().getById(key.getVipId());
                final String durationFormat = key.getVipDuration() == -1
                    ? ((Integer) key.getVipDuration()).toTime()
                    : GlobalValues.PERMANENT_DURATION;
                final String line = mainConfig.data().getKeyListing().getKeyFormat()
                    .formatReplace(
                        "@key".to(key.getCode()),
                        "@vip".to(configVip.getDisplay()),
                        "@tipo".to(key.getVipType().getName().toUpperCase()),
                        "@duracao".to(durationFormat),
                        "@usos".to(String.valueOf(key.getUsages()))
                    )
                    .color();
                return new ComponentBuilder(line)
                    .event(new HoverEvent(
                        HoverEvent.Action.SHOW_TEXT,
                        new BaseComponent[] {
                            new TextComponent(mainConfig.data().getKeyListing().getHoverMessage())
                        }
                    ))
                    .event(new ClickEvent(
                        ClickEvent.Action.SUGGEST_COMMAND,
                        "/usarkey " + key.getCode()
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

            message[j] = new TextComponent(messageList.get(i).color());
        }

        player.spigot().sendMessage(message);
    }
}
