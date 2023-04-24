package com.worldplugins.vip.database.player.model;

import com.worldplugins.vip.database.key.ValidVipKey;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Collection;

@RequiredArgsConstructor
public class PlayerKeys {
    private final @NonNull Collection<ValidVipKey> keys;

    public @NonNull Collection<ValidVipKey> all() {
        return keys;
    }

    public void add(@NonNull ValidVipKey key) {
        keys.add(key);
    }

    public void remove(@NonNull String code) {
        keys.removeIf(key -> key.getCode().equals(code));
    }
}
