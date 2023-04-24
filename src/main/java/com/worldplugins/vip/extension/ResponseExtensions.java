package com.worldplugins.vip.extension;

import com.worldplugins.lib.config.response.message.SubjectMessage;
import com.worldplugins.lib.extension.GenericExtensions;
import com.worldplugins.vip.Response;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.command.CommandSender;

import java.util.function.Function;

@ExtensionMethod({
    GenericExtensions.class
})

public class ResponseExtensions {
    public static <T extends CommandSender> void respond(
        T sender,
        @NonNull String key,
        Function<SubjectMessage, SubjectMessage> transformFunction
    ) {
        Response.send(sender, key, transformFunction);
    }

    public static <T extends CommandSender> void respond(T sender, @NonNull String key) {
        respond(sender, key, null);
    }
}
