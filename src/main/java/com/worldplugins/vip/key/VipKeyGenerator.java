package com.worldplugins.vip.key;

import lombok.NonNull;

public interface VipKeyGenerator {
    @NonNull String generate();
}
