package com.worldplugins.vip.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BukkitUtils {
    public static String getPlayerName(@NotNull UUID id) {
        return Bukkit.getPlayer(id) != null
            ? Bukkit.getPlayer(id).getName()
            : Bukkit.getOfflinePlayer(id).getName();
    }
}
