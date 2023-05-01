package com.worldplugins.vip.manager;

import lombok.NonNull;

import java.util.UUID;

public interface PointsManager {
    boolean has(@NonNull UUID playerId, double points);
    void withdraw(@NonNull UUID playerId, double points);
    void deposit(@NonNull UUID playerId, double points);
}
