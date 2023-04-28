package com.worldplugins.vip.util;

import lombok.NonNull;

import java.util.Arrays;

public class CommandParameters {
    public static @NonNull CommandParams of(@NonNull String[] args, int offset) {
        return new CommandParams(Arrays.copyOfRange(args, offset, args.length));
    }
}
