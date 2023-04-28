package com.worldplugins.vip.manager;

import lombok.NonNull;

import java.util.UUID;

public interface PermissionManager {
    void addGroup(@NonNull UUID playerId, @NonNull String group);
    void removeGroup(@NonNull UUID playerId, @NonNull String group);
}
