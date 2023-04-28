package com.worldplugins.vip.command.key;

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
import com.worldplugins.vip.key.VipKeyGenerator;
import com.worldplugins.vip.util.CommandParameters;
import com.worldplugins.vip.util.CommandParams;
import com.worldplugins.vip.util.TimeParser;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;

@ExtensionMethod({
    ResponseExtensions.class,
    GenericExtensions.class,
    NumberExtensions.class
})

@RequiredArgsConstructor
public class CreateKey implements CommandModule {
    private final @NonNull VipKeyGenerator keyGenerator;
    private final @NonNull ConfigCache<VipData> vipConfig;
    private final @NonNull ValidKeyRepository validKeyRepository;
    private final @NonNull SchedulerBuilder scheduler;

    @Command(
        name = "criarkey",
        permission = "worldvip.criarkey",
        argsChecks = {
            @ArgsChecker(size = 2, check = ArgsCheck.GREATER_SIZE),
            @ArgsChecker(size = 7, check = ArgsCheck.LOWER_SIZE)
        },
        usage = "&cArgumentos invalidos. Digite /criarkey <GERAR | key> <vip> <tipo> [tempo] [usos] [jogador]"
    )
    @Override
    public void execute(@NonNull CommandSender sender, @NonNull String[] args) {
        final String keyCode = !args[0].equals("GERAR")
            ? args[0]
            : keyGenerator.generate();

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

        final CommandParams params = CommandParameters.of(args, 4);
        final String rawUsages = params.get(0);
        final String rawPlayer = params.get(1);

        if (vipType == VipType.PERMANENT) {
            durationFormat = "∞";
            duration = -1;
        } else {
            durationFormat = args[3];
            duration = TimeParser.parseTime(durationFormat);

            if (duration == null) {
                sender.respond("Vip-duracao-invalida", message -> message.replace(
                    "@valor".to(durationFormat)
                ));
                return;
            }
        }

        final short usages;

        if (rawUsages == null) {
            usages = 1;
        } else {
            final Short usageCheck = rawUsages.toShortOrNull();

            if (usageCheck == null) {
                sender.respond("Key-usos-invalidos", message -> message.replace(
                    "@valor".to(rawUsages)
                ));
                return;
            }

            usages = usageCheck;
        }

        final String generatorName = rawPlayer == null
            ? sender instanceof Player ? sender.getName() : null
            : rawPlayer;

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
                    generatorName, keyCode, configVip.getId(), vipType, duration, usages
                );
                validKeyRepository.addKey(validKey);
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
