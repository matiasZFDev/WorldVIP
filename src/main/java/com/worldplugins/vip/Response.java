package com.worldplugins.vip;

import com.worldplugins.lib.config.response.message.SubjectMessage;
import com.worldplugins.lib.extension.GenericExtensions;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import java.util.Set;
import java.util.function.Function;

@ExtensionMethod({
    GenericExtensions.class
})

public class Response {
    private static final @NonNull CommandSender fakeSender = new CommandSender() {
        @Override
        public boolean isOp() {
            return false;
        }

        @Override
        public void setOp(boolean b) {

        }

        @Override
        public boolean isPermissionSet(String s) {
            return false;
        }

        @Override
        public boolean isPermissionSet(Permission permission) {
            return false;
        }

        @Override
        public boolean hasPermission(String s) {
            return false;
        }

        @Override
        public boolean hasPermission(Permission permission) {
            return false;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b) {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin) {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, String s, boolean b, int i) {
            return null;
        }

        @Override
        public PermissionAttachment addAttachment(Plugin plugin, int i) {
            return null;
        }

        @Override
        public void removeAttachment(PermissionAttachment permissionAttachment) {

        }

        @Override
        public void recalculatePermissions() {

        }

        @Override
        public Set<PermissionAttachmentInfo> getEffectivePermissions() {
            return null;
        }

        @Override
        public void sendMessage(String s) {

        }

        @Override
        public void sendMessage(String[] strings) {

        }

        @Override
        public Server getServer() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    };

    public static <T extends CommandSender> void send(
        T sender,
        @NonNull String key,
        Function<SubjectMessage, SubjectMessage> transformFunction
    ) {
        if (sender == null)
            return;

        message(sender, key, transformFunction);

        if (sender instanceof Player) {
            GlobalAccess.getSounds().data().get(key).ifNotNull(message -> message.send(sender));
            GlobalAccess.getEffects().data().get(key).ifNotNull(message -> message.send(sender));
        }
    }

    public static <T extends CommandSender> void send(T sender, @NonNull String key) {
        send(sender, key, message -> message);
    }

    public static void send(
        @NonNull String key,
        Function<SubjectMessage, SubjectMessage> transformFunction
    ) {
        send(fakeSender, key, transformFunction);
    }

    public static void send(@NonNull String key) {
        send(fakeSender, key);
    }

    private static void message(
        @NonNull CommandSender sender,
        @NonNull String key,
        Function<SubjectMessage, SubjectMessage> transformFunction
    ) {
        final SubjectMessage message = GlobalAccess.getMessages().data().get(key);

        if (message == null)
            return;

        if (transformFunction == null)
            message.color().send(sender);
        else
            transformFunction.apply(message).color().send(sender);
    }
}
