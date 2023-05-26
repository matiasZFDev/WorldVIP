package com.worldplugins.vip.config.menu;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.config.model.menu.MenuData;
import com.worldplugins.lib.util.MenuContents;
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
            .modifyItems(MenuContents::colorItems)
            .getData(dataSection -> new HashMap<String, Object>() {{
                put("Formato-vip-ativo", dataSection.getString("Formato-vip-ativo"));
                put("Formato-vip-inativo", dataSection.getString("Formato-vip-inativo"));
            }})
            .build();
    }

    @Override
    public @NotNull MenuData data() {
        return data;
    }
}
