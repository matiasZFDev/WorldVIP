package com.worldplugins.vip.util;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class CommandParams {
    private final @NonNull String[] options;

    public String get(int position) {
        if (position > options.length - 1) {
            return null;
        }

        return options[position];
    }
}
