package com.worldplugins.vip.config.menu;

import com.worldplugins.lib.config.cache.annotation.MenuContainerSpec;
import com.worldplugins.lib.config.cache.menu.InjectedMenuContainer;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.extension.bukkit.ConfigurationExtensions;
import com.worldplugins.lib.extension.bukkit.MenuItemsExtension;
import com.worldplugins.lib.util.MenuDataUtils;
import lombok.NonNull;
import lombok.experimental.ExtensionMethod;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

@ExtensionMethod({
    ConfigurationExtensions.class,
    MenuItemsExtension.class
})

@MenuContainerSpec(name = "coletar_itens")
public class VipItemsMenuContainer implements InjectedMenuContainer {
    public @NonNull MenuData createData(@NonNull ConfigurationSection section) {
        return MenuDataUtils.fetch(section)
            .modifyItems(items -> items.colorAll())
            .modifyData(dataSection -> new HashMap<String, Object>() {{
                put("Display-itens", dataSection.itemDisplay("Display-itens"));
            }})
            .build();
    }
}
