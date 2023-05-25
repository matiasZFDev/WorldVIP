package com.worldplugins.vip.key;

import com.worldplugins.vip.config.data.MainData;
import me.post.lib.config.model.ConfigModel;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigKeyGenerator implements VipKeyGenerator {
    private enum CharGen {
        NUMBER(
            () -> (char) (48 + random.nextInt(9)),
            MainData.KeyGenOptions::numbers
        ),
        LOWERCASE_LETTERS(
            () -> (char) (97 + random.nextInt(122 - 97)),
            MainData.KeyGenOptions::lowercaseLetters
        ),
        UPPERCASE_LETTERS(
            () -> (char) (65 + random.nextInt(90 - 65)),
            MainData.KeyGenOptions::uppercaseLetters
        );

        private final @NotNull Supplier<Character> charSupplier;
        private final @NotNull Function<MainData.KeyGenOptions, Boolean> option;
        private static final @NotNull List<CharGen> list = Arrays.asList(values());

        CharGen(@NotNull Supplier<Character> charSupplier, @NotNull Function<MainData.KeyGenOptions, Boolean> option) {
            this.charSupplier = charSupplier;
            this.option = option;
        }

        public @NotNull Supplier<Character> charSupplier() {
            return charSupplier;
        }

        public @NotNull Function<MainData.KeyGenOptions, Boolean> option() {
            return option;
        }

        public static @NotNull List<CharGen> list() {
            return list;
        }
    }

    private final @NotNull ConfigModel<MainData> mainConfig;
    private static final @NotNull Random random = new Random();

    public ConfigKeyGenerator(@NotNull ConfigModel<MainData> mainConfig) {
        this.mainConfig = mainConfig;
    }

    @Override
    public @NotNull String generate() {
        final byte keyLength = mainConfig.data().keyGen().length();
        final StringBuilder codeBuilder = new StringBuilder(keyLength);

        for (int i = 0; i < keyLength; i++) {
            codeBuilder.append(rollCharGen().get());
        }

        return codeBuilder.toString();
    }

    private @NotNull Supplier<Character> rollCharGen() {
        final List<Supplier<Character>> filteredGenList = CharGen.list().stream()
            .filter(charGen -> charGen.option().apply(mainConfig.data().keyGen()))
            .map(CharGen::charSupplier)
            .collect(Collectors.toList());

        if (filteredGenList.isEmpty()) {
            return CharGen.list().get(random.nextInt(CharGen.list().size())).charSupplier();
        }

        return filteredGenList.get(random.nextInt(filteredGenList.size()));
    }
}
