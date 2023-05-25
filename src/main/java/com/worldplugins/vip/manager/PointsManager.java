package com.worldplugins.vip.manager;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface PointsManager {
    boolean has(@NotNull UUID playerId, double points);
    void withdraw(@NotNull UUID playerId, double points);
    void deposit(@NotNull UUID playerId, double points);
}
