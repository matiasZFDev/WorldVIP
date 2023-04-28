package com.worldplugins.vip.config.menu;

import com.worldplugins.lib.config.cache.annotation.MenuContainerSpec;
import com.worldplugins.lib.config.cache.menu.InjectedMenuContainer;
import com.worldplugins.lib.config.cache.menu.MenuData;
import com.worldplugins.lib.util.MenuDataUtils;
import lombok.NonNull;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;

@MenuContainerSpec(name = "vip")
public class VipMenuContainer implements InjectedMenuContainer {
    public @NonNull MenuData createData(@NonNull ConfigurationSection section) {
        return MenuDataUtils.fetch(section)
            .modifyData(dataSection -> new HashMap<String, Object>() {{
                put("Format-vip-ativo", dataSection.getString("Format-vip-ativo"));
                put("Format-vip-inativo", dataSection.getString("Format-vip-inativo"));
            }})
            .build();
    }
}
