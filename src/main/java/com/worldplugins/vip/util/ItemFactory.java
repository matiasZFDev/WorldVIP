package com.worldplugins.vip.util;

import com.worldplugins.lib.config.cache.menu.MenuItem;
import lombok.NonNull;
import org.bukkit.inventory.ItemStack;

public class ItemFactory {
    public static @NonNull MenuItem dynamicOf(@NonNull String id, int slot, @NonNull ItemStack item) {
        return new MenuItem(id, slot, item, null);
    }
}
