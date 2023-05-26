package com.worldplugins.vip.config.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MainData {


    public static class KeyGenOptions {
        private final byte length;
        private final boolean numbers;
        private final boolean lowercaseLetters;
        private final boolean uppercaseLetters;

        public KeyGenOptions(
            byte length,
            boolean numbers,
            boolean lowercaseLetters,
            boolean uppercaseLetters
        ) {
            this.length = length;
            this.numbers = numbers;
            this.lowercaseLetters = lowercaseLetters;
            this.uppercaseLetters = uppercaseLetters;
        }

        public byte length() {
            return length;
        }

        public boolean numbers() {
            return numbers;
        }


        public boolean lowercaseLetters() {
            return lowercaseLetters;
        }

        public boolean uppercaseLetters() {
            return uppercaseLetters;
        }
    }

    public static class KeyListingOptions {
        private final @NotNull List<String> playerKeysMessage;
        private final @NotNull List<String> ownKeysMessages;
        private final @NotNull String keyFormat;
        private final @NotNull String hoverMessage;

        public KeyListingOptions(
            @NotNull List<String> playerKeysMessage,
            @NotNull List<String> ownKeysMessages,
            @NotNull String keyFormat,
            @NotNull String hoverMessage
        ) {
            this.playerKeysMessage = playerKeysMessage;
            this.ownKeysMessages = ownKeysMessages;
            this.keyFormat = keyFormat;
            this.hoverMessage = hoverMessage;
        }

        public @NotNull List<String> playerKeysMessage() {
            return playerKeysMessage;
        }

        public @NotNull List<String> ownKeysMessages() {
            return ownKeysMessages;
        }

        public @NotNull String keyFormat() {
            return keyFormat;
        }

        public @NotNull String hoverMessage() {
            return hoverMessage;
        }
    }

    private final @NotNull KeyGenOptions keyGen;
    private final @NotNull KeyListingOptions keyListing;
    private final boolean stackVips;
    private final boolean storeItems;
    private final int switchVipDelay;
    private final boolean simultaneousReduction;
    private final int maxSellingKeysPerPlayer;

    public MainData(
        @NotNull KeyGenOptions keyGen,
        @NotNull KeyListingOptions keyListing,
        boolean stackVips,
        boolean storeItems,
        int switchVipDelay,
        boolean simultaneousReduction,
        int maxSellingKeysPerPlayer
    ) {
        this.keyGen = keyGen;
        this.keyListing = keyListing;
        this.stackVips = stackVips;
        this.storeItems = storeItems;
        this.switchVipDelay = switchVipDelay;
        this.simultaneousReduction = simultaneousReduction;
        this.maxSellingKeysPerPlayer = maxSellingKeysPerPlayer;
    }

    public @NotNull KeyGenOptions keyGen() {
        return keyGen;
    }

    public @NotNull KeyListingOptions keyListing() {
        return keyListing;
    }

    public boolean stackVips() {
        return stackVips;
    }

    public boolean storeItems() {
        return storeItems;
    }

    public int switchVipDelay() {
        return switchVipDelay;
    }

    public boolean simultaneousReduction() {
        return simultaneousReduction;
    }

    public int maxSellingKeysPerPlayer() {
        return maxSellingKeysPerPlayer;
    }
}
