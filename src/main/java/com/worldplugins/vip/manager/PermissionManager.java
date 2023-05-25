package com.worldplugins.vip.manager;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PermissionManager {
    void addGroup(@NotNull UUID playerId, @NotNull String group);
    void removeGroup(@NotNull UUID playerId, @NotNull String group);
}
