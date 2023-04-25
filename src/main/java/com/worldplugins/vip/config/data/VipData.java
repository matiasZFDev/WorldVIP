package com.worldplugins.vip.config.data;

import com.worldplugins.lib.extension.CollectionExtensions;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.ExtensionMethod;

import java.util.Collection;
import java.util.function.Predicate;

@ExtensionMethod({
    CollectionExtensions.class
})

@RequiredArgsConstructor
public class VipData {
    @RequiredArgsConstructor
    @Getter
    public static class VIP {
        private final byte id;
        private final @NonNull String name;
        private final @NonNull String display;
        private final @NonNull String group;
    }

    private final @NonNull Collection<VIP> vips;

    public @NonNull Collection<VIP> all() {
        return vips.immutable();
    }

    public VIP getById(byte id) {
        return getMatching(vip -> vip.id == id);
    }

    public VIP getByName(@NonNull String name) {
        return getMatching(vip -> vip.name.equals(name));
    }

    private VIP getMatching(@NonNull Predicate<VIP> predicate) {
        return vips.stream().filter(predicate).findFirst().orElse(null);
    }
}
