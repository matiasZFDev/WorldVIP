package com.worldplugins.vip.database.player.model;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public enum VipType {
    BASIC((byte) 0, "basico", (byte) 1),
    ONLINE((byte) 1, "online", (byte) 2),
    SERVER((byte) 2, "server", (byte) 3),
    PERMANENT((byte) 3, "permanente", (byte) 4);

    private final byte id;
    private final @NotNull String name;
    private final byte priority;

    private static final @NotNull Map<Byte, VipType> idMap = Arrays.stream(values()).collect(Collectors.toMap(
        VipType::id, Function.identity()
    ));

    VipType(byte id, @NotNull String name, byte priority) {
        this.id = id;
        this.name = name;
        this.priority = priority;
    }

    public byte id() {
        return id;
    }

    public @NotNull String getName() {
        return name;
    }

    public byte priority() {
        return priority;
    }

    public static VipType fromId(byte id) {
        return idMap.get(id);
    }

    public static VipType fromName(@NotNull String name) {
        return Arrays.stream(values())
            .filter(type -> type.getName().equals(name))
            .findFirst()
            .orElse(null);
    }
}
