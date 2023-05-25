package com.worldplugins.vip.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class CommandParameters {
    public static @NotNull CommandParams of(@NotNull String[] args, int offset) {
        return new CommandParams(Arrays.copyOfRange(args, offset, args.length));
    }
}
