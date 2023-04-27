package com.worldplugins.vip.util;

import com.worldplugins.lib.util.SchedulerBuilder;
import lombok.NonNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class ExpiringCollection<E> {
    private final @NonNull Map<E, Integer> data;
    private final int expirationTime;
    private final @NonNull Consumer<E> removalListener;

    public ExpiringCollection(
        @NonNull SchedulerBuilder scheduler,
        int expirationTime,
        boolean concurrent,
        @NonNull Consumer<E> removalListener
    ) {
        this.data = concurrent ? new ConcurrentHashMap<>() : new HashMap<>();
        this.expirationTime = expirationTime;
        this.removalListener = removalListener;
        scheduler.newTimer(this::run)
            .delay(20L)
            .period(20L)
            .run();
    }

    private void run() {
        final Iterator<Map.Entry<E, Integer>> it = data.entrySet().iterator();

        while (it.hasNext()) {
            final Map.Entry<E, Integer> entry = it.next();

            if (entry.getValue() == 0) {
                removalListener.accept(entry.getKey());
                it.remove();
            }

            entry.setValue(entry.getValue() - 1);
        }
    }

    public void add(E element) {
        data.put(element, expirationTime);
    }

    public void remove(E element) {
        data.remove(element);
    }
    public void addAll(@NonNull Collection<E> elements) {
        elements.forEach(element -> data.put(element, expirationTime));
    }

    public @NonNull Collection<E> getAll() {
        return data.keySet();
    }
}
