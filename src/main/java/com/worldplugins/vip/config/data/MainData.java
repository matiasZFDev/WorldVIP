package com.worldplugins.vip.config.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

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

    @RequiredArgsConstructor
    @Getter
    public static class KeyListingOptions {
        private final @NonNull List<String> playerKeysMessage;
        private final @NonNull List<String> ownKeysMessages;
        private final @NonNull String keyFormat;
        private final @NonNull String hoverMessage;
    }

    private final @NonNull KeyGenOptions keyGen;
    private final @NonNull KeyListingOptions keyListing;
    @Accessors(fluent = true)
    private final boolean stackVips;
    @Accessors(fluent = true)
    private final boolean storeItems;
    private final int switchVipDelay;
}
