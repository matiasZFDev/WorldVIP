package com.worldplugins.vip.database.player.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class VipPlayer {
    private final @NotNull UUID id;
    private double spent;
    private @Nullable VIP activeVip;
    private final @NotNull OwningVIPs owningVips;

    public VipPlayer(@NotNull UUID id, double spent, @Nullable VIP activeVip, @NotNull OwningVIPs owningVips) {
        this.id = id;
        this.spent = spent;
        this.activeVip = activeVip;
        this.owningVips = owningVips;
    }

    public UUID id() {
        return id;
    }

    public double spent() {
        return spent;
    }

    public @Nullable VIP activeVip() {
        return activeVip;
    }

    public void setActiveVip(@Nullable VIP activeVip) {
        this.activeVip = activeVip;
    }

    public void incrementSpent(double amount) {
        spent += amount;
    }

    public @NotNull OwningVIPs owningVips() {
        return owningVips;
    }
}
