package com.worldplugins.vip.database.player.model;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Getter
public enum VipType {
    BASIC((byte) 0, "basico"),
    ONLINE((byte) 1, "online"),
    SERVER((byte) 2, "server"),
    PERMANENT((byte) 3, "permanente");

    private final byte id;
    private final @NonNull String name;

    private static final @NonNull Map<Byte, VipType> idMap = Arrays.stream(values()).collect(Collectors.toMap(
        VipType::getId, Function.identity()
    ));

    public static VipType fromId(byte id) {
        return idMap.get(id);
    }

    public static VipType fromName(@NonNull String name) {
        return Arrays.stream(values())
            .filter(type -> type.name.equals(name))
            .findFirst()
            .orElse(null);
    }
}
