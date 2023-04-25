package com.worldplugins.vip.util;

import com.worldplugins.lib.extension.NumberExtensions;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;

@ExtensionMethod({
    NumberExtensions.class
})

public class TimeParser {
    public static Integer parseTime(@NonNull String timeFormat) {
        if (timeFormat.length() <= 1) {
            return null;
        }

        final Integer numericValue = timeFormat
            .substring(0, timeFormat.length() - 1)
            .toIntOrNull();

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
