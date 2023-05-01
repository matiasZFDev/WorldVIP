package com.worldplugins.vip.util;

import lombok.NonNull;
import org.bukkit.Bukkit;

import java.util.UUID;

public class BukkitUtils {
    public static String getPlayerName(@NonNull UUID id) {
        return Bukkit.getPlayer(id) != null
            ? Bukkit.getPlayer(id).getName()
            : Bukkit.getOfflinePlayer(id).getName();
    }
}
