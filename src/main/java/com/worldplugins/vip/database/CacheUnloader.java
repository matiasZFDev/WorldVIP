package com.worldplugins.vip.database;

public interface CacheUnloader<K> {
    void prepareUnload(K key);
    void cancel(K key);
}
