package com.worldplugins.vip.config.data;

public class ServerData {
    private final long lastOnlineInstant;

    public ServerData(long lastOnlineInstant) {
        this.lastOnlineInstant = lastOnlineInstant;
    }

    public long lastOnlineInstant() {
        return lastOnlineInstant;
    }
}
