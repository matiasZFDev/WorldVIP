package com.worldplugins.vip.manager;

import lombok.NonNull;

import java.util.UUID;

public interface PointsManager {
    boolean hasPoints(@NonNull UUID playerId, double points);
    void withdrawPoints(@NonNull UUID playerId, double points);
}
