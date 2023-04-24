package com.worldplugins.vip.extension;

import com.worldplugins.lib.view.View;
import com.worldplugins.lib.view.ViewContext;
import com.worldplugins.vip.GlobalAccess;
import lombok.NonNull;
import org.bukkit.entity.Player;

public class ViewExtensions {
    public static void openView(
        @NonNull Player player,
        @NonNull Class<? extends View<? extends ViewContext>> viewClass,
        ViewContext context
    ) {
        GlobalAccess.getViewManager().openView(viewClass, context, player);
    }

    public static void openView(
        @NonNull Player player,
        @NonNull Class<? extends View<? extends ViewContext>> viewClass
    ) {
        openView(player, viewClass, null);
    }
}
