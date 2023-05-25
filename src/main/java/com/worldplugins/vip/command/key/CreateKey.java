package com.worldplugins.vip.command.key;

import com.worldplugins.vip.GlobalValues;
import com.worldplugins.vip.config.data.VipData;
import com.worldplugins.vip.database.key.ValidKeyRepository;
import com.worldplugins.vip.database.key.ValidVipKey;
import com.worldplugins.vip.database.player.model.VipType;
import com.worldplugins.vip.key.VipKeyGenerator;
import com.worldplugins.vip.util.CommandParameters;
import com.worldplugins.vip.util.CommandParams;
import com.worldplugins.vip.util.TimeParser;
import me.post.lib.command.CommandModule;
import me.post.lib.command.annotation.Command;
import me.post.lib.config.model.ConfigModel;
import me.post.lib.util.Numbers;
import me.post.lib.util.Scheduler;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

import static com.worldplugins.vip.Response.respond;
import static me.post.lib.util.Pairs.to;

public class CreateKey implements CommandModule {
    private final @NotNull VipKeyGenerator keyGenerator;
    private final @NotNull ConfigModel<VipData> vipConfig;
    private final @NotNull ValidKeyRepository validKeyRepository;
    private final @NotNull Scheduler scheduler;

    public CreateKey(
        @NotNull VipKeyGenerator keyGenerator,
        @NotNull ConfigModel<VipData> vipConfig,
        @NotNull ValidKeyRepository validKeyRepository,
        @NotNull Scheduler scheduler
    ) {
        this.keyGenerator = keyGenerator;
        this.vipConfig = vipConfig;
        this.validKeyRepository = validKeyRepository;
        this.scheduler = scheduler;
    }

    @Command(name = "criarkey")
    @Override
    public void execute(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!sender.hasPermission("worldvip.criarkey")) {
            respond(sender, "Criar-key-permissoes");
            return;
        }

        if (!(args.length > 2 && args.length < 7)) {
            respond(sender, "Criar-key-uso");
            return;
        }

        final String keyCode = !args[0].equals("GERAR")
            ? args[0]
            : keyGenerator.generate();

        if (keyCode.length() > GlobalValues.MAX_KEY_LENGTH) {
            respond(sender, "Key-tamanho-maximo", message -> message.replace(
                to("@maximo", String.valueOf(GlobalValues.MAX_KEY_LENGTH))
            ));
            return;
        }

        final VipData.VIP configVip = vipConfig.data().getByName(args[1]);

        if (configVip == null) {
            respond(sender, "Vip-inexistente", message -> message.replace(
                to("@nome", args[1]),
                to("@vips", vipConfig.data().all().toString())
            ));
            return;
        }

        final VipType vipType = VipType.fromName(args[2].toLowerCase());

        if (vipType == null) {
            final Collection<VipType> types = Arrays.asList(VipType.values());
            respond(sender, "Tipo-vip-inexistente", message -> message.replace(
                to("@nome", args[2]),
                to("@tipos", types.toString())
            ));
            return;
        }

        final ParamsResult params = getParamsResult(sender, vipType, args);

        if (params == null) {
            return;
        }

        validKeyRepository.getKeyByCode(keyCode).thenAccept(key -> {
            if (key != null) {
                scheduler.runTask(0, false, () ->
                    respond(sender, "Key-duplicada", message -> message.replace(
                        to("@key", keyCode)
                    ))
                );
                return;
            }

            scheduler.runTask(0, false, () -> {
                final ValidVipKey validKey = new ValidVipKey(
                    params.generatorName,
                    keyCode,
                    configVip.id(),
                    vipType,
                    params.duration,
                    params.usages
                );
                validKeyRepository.addKey(validKey);
                respond(sender, "Key-criada", message -> message.replace(
                    to("@key", keyCode),
                    to("@vip", configVip.display()),
                    to("@usos", String.valueOf(params.usages)),
                    to("@tipo", vipType.getName().toUpperCase()),
                    to("@tempo", params.durationFormat)
                ));
            });
        });
    }

    private static class ParamsResult {
        private final @NotNull String durationFormat;
        private final int duration;
        private final short usages;
        private final @Nullable String generatorName;

        private ParamsResult(
            @NotNull String durationFormat,
            int duration,
            short usages,
            @Nullable String generatorName
        ) {
            this.durationFormat = durationFormat;
            this.duration = duration;
            this.usages = usages;
            this.generatorName = generatorName;
        }
    }

    private @Nullable ParamsResult getParamsResult(
        @NotNull CommandSender sender,
        @NotNull VipType vipType,
        @NotNull String[] args
    ) {
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
                respond(sender, "Vip-duracao-invalida", message -> message.replace(
                    to("@valor", durationFormat)
                ));
                return null;
            }
        }

        final short usages;

        if (rawUsages == null) {
            usages = 1;
        } else {
            final Short usageCheck = Numbers.toShortOrNull(rawUsages);

            if (usageCheck == null) {
                respond(sender, "Key-usos-invalidos", message -> message.replace(
                    to("@valor", rawUsages)
                ));
                return null;
            }

            usages = usageCheck;
        }

        final String generatorName = rawPlayer == null
            ? sender instanceof Player ? sender.getName() : null
            : rawPlayer;

        return new ParamsResult(durationFormat, duration, usages, generatorName);
    }
}
