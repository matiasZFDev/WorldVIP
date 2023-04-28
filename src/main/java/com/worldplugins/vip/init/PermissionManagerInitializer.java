package com.worldplugins.vip.init;

import com.worldplugins.lib.common.Initializer;
import com.worldplugins.vip.manager.PermissionManager;
import lombok.NonNull;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.node.Node;

import java.util.UUID;

public class PermissionManagerInitializer implements Initializer<PermissionManager> {
    @Override
    public PermissionManager init() {
        return luckPerms();
    }

    private @NonNull PermissionManager luckPerms() {
        return new PermissionManager() {
            @Override
            public void addGroup(@NonNull UUID playerId, @NonNull String group) {
                LuckPermsProvider.get().getUserManager().modifyUser(playerId, user ->
                    user.data().add(Node.builder("group." + group).build())
                );
            }

            @Override
            public void removeGroup(@NonNull UUID playerId, @NonNull String group) {
                LuckPermsProvider.get().getUserManager().modifyUser(playerId, user ->
                    user.data().remove(Node.builder("group." + group).build())
                );
            }
        };
    }
}
