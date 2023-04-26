package com.worldplugins.vip.command;

import com.worldplugins.lib.command.ArgsCheck;
import com.worldplugins.lib.command.CommandModule;
import com.worldplugins.lib.command.annotation.ArgsChecker;
import com.worldplugins.lib.command.annotation.Command;
import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.lib.extension.NumberExtensions;
import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.extension.ResponseExtensions;
import com.worldplugins.vip.key.KeyStorageManager;
import com.worldplugins.vip.util.TimeParser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberExtensions.class
})

@RequiredArgsConstructor
public class CreateKey implements CommandModule {
    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;
    private final @NonNull KeyStorageManager keyStorageManager;

    @Command(
        name = "criarkey",
        permission = "worldvip.criarkey",
        argsChecks = {
            @ArgsChecker(size = 3, check = ArgsCheck.GREATER_SIZE),
            @ArgsChecker(size = 6, check = ArgsCheck.LOWER_SIZE)
        },
        usage = "&cArgumentos invalidos. Digite /criarkey <codigo> <vip> <tipo> [tempo] [usos]"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final String keyCode = args[0];

        if (keyCode.length() > GlobalValues.MAX_KEY_LENGTH) {
            sender.respond("Key-tamanho-maximo", message -> message.replace(
                "@maximo".to(String.valueOf(GlobalValues.MAX_KEY_LENGTH))
            ));
            return;
        }

        final VipData.VIP configVip = vipConfig.data().getByName(args[1]);

        if (configVip == null) {
            sender.respond("Vip-inexistente", message -> message.replace(
                "@nome".to(args[1]),
                "@vips".to(vipConfig.data().all().toString())
            ));
            return;
        }

        final VipType vipType = VipType.fromName(args[2].toLowerCase());

        if (vipType == null) {
            final Collection<VipType> types = Arrays.asList(VipType.values());
            sender.respond("Tipo-vip-inexistente", message -> message.replace(
                "@nome".to(args[2]),
                "@tipos".to(types.toString())
            ));
            return;
        }

        final String durationFormat;
        final Integer duration;
        final int argsLength;

        if (vipType != VipType.PERMANENT) {
            durationFormat = args[3];
            duration = TimeParser.parseTime(durationFormat);
            argsLength = 3;

            if (duration == null) {
                sender.respond("Vip-duracao-invalida", message -> message.replace(
                    "@valor".to(durationFormat)
                ));
                return;
            }
        } else {
            durationFormat = "∞";
            duration = -1;
            argsLength = 4;
        }

        final short usages;

        if (args.length == argsLength) {
            usages = 1;
        } else {
            final Short usageCheck = args[argsLength].toShortOrNull();

            if (usageCheck == null) {
                sender.respond("Key-usos-invalidos", message -> message.replace(
                    "@valor".to(args[argsLength])
                ));
                return;
            }

            usages = usageCheck;
        }

        final UUID generatorId = sender instanceof Player ? ((Player) sender).getUniqueId() : null;

        validKeyRepository.getKeyByCode(keyCode).thenAccept(key -> {
            if (key != null) {
                scheduler.newTask(() ->
                    sender.respond("Key-duplicada", message -> message.replace(
                        "@key".to(keyCode)
                    ))
                ).run();
                return;
            }

            scheduler.newTask(() -> {
                final ValidVipKey validKey = new ValidVipKey(
                    generatorId, keyCode, configVip.getId(), vipType, duration, usages
                );
                keyStorageManager.store(validKey);
                sender.respond("Key-criada", message -> message.replace(
                    "@key".to(keyCode),
                    "@vip".to(configVip.getDisplay()),
                    "@usos".to(String.valueOf(usages)),
                    "@tipo".to(vipType.getName().toUpperCase()),
                    "@tempo".to(durationFormat)
                ));
            }).run();
        });
    }
}
