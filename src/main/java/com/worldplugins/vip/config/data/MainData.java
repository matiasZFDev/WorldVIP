package com.worldplugins.vip.config.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
@Getter
public class MainData {
    @RequiredArgsConstructor
    @Getter
    @Accessors(fluent = true)
    public static class KeyGenOptions {
        private final byte length;
        private final boolean numbers;
        private final boolean lowercaseLetters;
        private final boolean upercaseLetters;
    }

    private final @NonNull String keyFormat;
    private final @NonNull KeyGenOptions keyGen;
}
