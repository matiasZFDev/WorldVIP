package com.worldplugins.vip.manager;

import lombok.NonNull;
import org.bukkit.entity.Player;

public interface PermissionManager {
    void addGroup(@NonNull Player player, @NonNull String group);
    void removeGroup(@NonNull Player player, @NonNull String group);
}
