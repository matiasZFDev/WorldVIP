package com.worldplugins.vip.key;

import com.worldplugins.lib.config.cache.ConfigCache;
import com.worldplugins.vip.config.data.MainData;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigKeyGenerator implements VipKeyGenerator {
    private enum CharGen {
        NUMBER {
            @Override
            public @NonNull Supplier<Character> charSupplier() {
                return () -> (char) (48 + random.nextInt(9));
            }

            @Override
            public @NonNull Function<MainData.KeyGenOptions, Boolean> option() {
                return MainData.KeyGenOptions::numbers;
            }
        },
        LOWERCASE_LETTERS {
            @Override
            public @NonNull Supplier<Character> charSupplier() {
                return () -> (char) (97 + random.nextInt(122 - 97));
            }

            @Override
            public @NonNull Function<MainData.KeyGenOptions, Boolean> option() {
                return MainData.KeyGenOptions::lowercaseLetters;
            }
        },
        UPPERCASE_LETTERS {
            @Override
            public @NonNull Supplier<Character> charSupplier() {
                return () -> (char) (65 + random.nextInt(90 - 65));
            }

            @Override
            public @NonNull Function<MainData.KeyGenOptions, Boolean> option() {
                return MainData.KeyGenOptions::upercaseLetters;
            }
        };

        public abstract @NonNull Supplier<Character> charSupplier();
        public abstract @NonNull Function<MainData.KeyGenOptions, Boolean> option();

        private static final @NonNull List<CharGen> list = Arrays.asList(values());

        public static @NonNull List<CharGen> list() {
            return list;
        }
    }

    private final @NonNull ConfigCache<MainData> mainConfig;
    private static final @NonNull Random random = new Random();

    @Override
    public @NonNull String generate() {
        final byte keyLength = mainConfig.data().getKeyGen().length();
        final StringBuilder codeBuilder = new StringBuilder(keyLength);

        for (int i = 0; i < keyLength; i++) {
            codeBuilder.append(rollCharGen().get());
        }

        return codeBuilder.toString();
    }

    private @NonNull Supplier<Character> rollCharGen() {
        final List<Supplier<Character>> filteredGenList = CharGen.list().stream()
            .filter(charGen -> charGen.option().apply(mainConfig.data().getKeyGen()))
            .map(CharGen::charSupplier)
            .collect(Collectors.toList());

        if (filteredGenList.isEmpty()) {
            return CharGen.list().get(random.nextInt(CharGen.list().size())).charSupplier();
        }

        return filteredGenList.get(random.nextInt(filteredGenList.size()));
    }
}
