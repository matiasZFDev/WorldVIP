package com.worldplugins.vip.util;

import com.worldplugins.lib.util.SchedulerBuilder;
import com.worldplugins.lib.util.cache.Cache;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ExpiringMap<K, V> implements Cache<K, V> {
    @AllArgsConstructor
    private static class ExpiringValue<V> {
        @Getter
        private final V value;
        @Setter
        private int timeLeft;
    }

    private final @NonNull Map<K, ExpiringValue<V>> data;
    private final int expireAfterWrite;
    private final int expireAfterRead;

    public ExpiringMap(
        @NonNull SchedulerBuilder scheduler,
        int expireAfterWrite,
        int expireAfterRead,
        boolean concurrent
    ) {
        this.data = concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
        this.expireAfterWrite = expireAfterWrite;
        this.expireAfterRead = expireAfterRead;
        scheduler.newTimer(this::run)
            .delay(20L)
            .period(20L)
            .run();
    }

    private void run() {
        final Iterator<Map.Entry<K, ExpiringValue<V>>> it = data.entrySet().iterator();

        while (it.hasNext()) {
            final Map.Entry<K, ExpiringValue<V>> entry = it.next();

            if (entry.getValue().timeLeft == 0) {
                it.remove();
            }

            entry.getValue().setTimeLeft(entry.getValue().timeLeft - 1);
        }
    }

    @Override
    public void set(K key, V value) {
        data.put(key, new ExpiringValue<>(value, expireAfterWrite));
    }

    @Override
    public void remove(K key) {
        data.remove(key);
    }

    @Override
    public V get(K key) {
        if (!data.containsKey(key)) {
            return null;
        }

        final ExpiringValue<V> expiringValue = data.get(key);

        if (expiringValue.timeLeft < expireAfterRead) {
            expiringValue.setTimeLeft(expireAfterRead);
        }

        return data.get(key).value;
    }

    @Override
    public boolean containsKey(K k) {
        return data.containsKey(k);
    }

    @Override
    public @NonNull Collection<V> getValues() {
        return data.values().stream().map(ExpiringValue::getValue).collect(Collectors.toList());
    }
}
