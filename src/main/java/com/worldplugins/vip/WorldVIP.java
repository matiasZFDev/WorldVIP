package com.worldplugins.vip;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

public class WorldVIP extends JavaPlugin {
    private @Nullable Runnable onDisable;

    @Override
    public void onEnable() {
        onDisable = new PluginExecutor(this).execute();
    }

    @Override
    public void onDisable() {
        if (onDisable != null)
            onDisable.run();
    }
}
