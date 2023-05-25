package com.worldplugins.vip.util;

import org.jetbrains.annotations.NotNull;

public final class CommandParams {
    private final @NotNull String[] options;

    public CommandParams(@NotNull String[] options) {
        this.options = options;
    }

    public String get(int position) {
        if (position > options.length - 1) {
            return null;
        }

        return options[position];
    }
}
