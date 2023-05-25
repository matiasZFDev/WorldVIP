package com.worldplugins.vip.config.menu;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.config.model.menu.MenuData;
import com.worldplugins.lib.util.MenuModels;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;

public class VipMenuModel implements MenuModel {
    private @UnknownNullability MenuData data;
    private final @NotNull ConfigWrapper configWrapper;

    public VipMenuModel(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        data = MenuModels.fetch(configWrapper.unwrap())
            .getData(dataSection -> new HashMap<String, Object>() {{
                put("Format-vip-ativo", dataSection.getString("Format-vip-ativo"));
                put("Format-vip-inativo", dataSection.getString("Format-vip-inativo"));
            }})
            .build();
    }

    @Override
    public @NotNull MenuData data() {
        return data;
    }
}
