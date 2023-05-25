package com.worldplugins.vip.config.menu;

import com.worldplugins.lib.config.model.MenuModel;
import com.worldplugins.lib.config.model.menu.MenuData;
import com.worldplugins.lib.util.ConfigSections;
import com.worldplugins.lib.util.MenuContents;
import com.worldplugins.lib.util.MenuModels;
import me.post.lib.config.wrapper.ConfigWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnknownNullability;

import java.util.HashMap;

public class KeysMenuModel implements MenuModel {
    private @UnknownNullability MenuData data;
    private final @NotNull ConfigWrapper configWrapper;

    public KeysMenuModel(@NotNull ConfigWrapper configWrapper) {
        this.configWrapper = configWrapper;
    }

    public void update() {
        data = MenuModels.fetch(configWrapper.unwrap())
            .modifyItems(MenuContents::colorItems)
            .getData(dataSection -> new HashMap<String, Object>() {{
                put("Slots", dataSection.getIntegerList("Slots"));
                put("Display-key", ConfigSections.itemDisplay(dataSection, "Display-key"));
            }})
            .build();
    }

    @Override
    public @NotNull MenuData data() {
        return data;
    }
}
