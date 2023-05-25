package com.worldplugins.vip.init;

import com.worldplugins.vip.manager.PermissionManager;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PermissionManagerInitializer {
    public PermissionManager init() {
        return luckPerms();
    }

    private @NotNull PermissionManager luckPerms() {
        return new PermissionManager() {
            @Override
            public void addGroup(@NotNull UUID playerId, @NotNull String group) {
                LuckPermsProvider.get().getUserManager().modifyUser(playerId, user ->
                    user.data().add(Node.builder("group." + group).build())
                );
            }

            @Override
            public void removeGroup(@NotNull UUID playerId, @NotNull String group) {
                LuckPermsProvider.get().getUserManager().modifyUser(playerId, user ->
                    user.data().remove(Node.builder("group." + group).build())
                );
            }
        };
    }
}
