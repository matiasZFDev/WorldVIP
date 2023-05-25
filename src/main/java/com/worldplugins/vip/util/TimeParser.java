package com.worldplugins.vip.util;

import me.post.lib.util.Numbers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TimeParser {
    public static @Nullable Integer parseTime(@NotNull String timeFormat) {
        if (timeFormat.length() <= 1) {
            return null;
        }

        final Integer numericValue = Numbers.toIntOrNull(
            timeFormat.substring(0, timeFormat.length() - 1)
        );

        if (numericValue == null) {
            return null;
        }

        final char suffix = timeFormat.charAt(timeFormat.length() - 1);

        switch (suffix) {
            case 's':
                return numericValue;

            case 'm':
                return numericValue * 60;

            case 'h':
                return numericValue * 60 * 60;

            case 'd':
                return numericValue * 60 * 60 * 24;

            default:
                return null;
        }
    }
}
