package com.worldplugins.vip.util;

import com.worldplugins.lib.util.SchedulerBuilder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

public class TemporaryDataSet<E> {
    private final @NonNull Collection<E> data;
    private final int expireAfterWrite;
    private final int expireAfterAccess;

    private int expireIn = -1;

    public TemporaryDataSet(
        @NonNull SchedulerBuilder scheduler,
        int expireAfterWrite,
        int expireAfterAccess,
        boolean concurrent
    ) {
        this.data = concurrent ? new Vector<>() : new ArrayList<>();
        this.expireAfterWrite = expireAfterWrite;
        this.expireAfterAccess = expireAfterAccess;
        scheduler.newTimer(this::run)
            .delay(20L)
            .period(20L)
            .run();
    }

    private void run() {
        if (expireIn == -1) {
            return;
        }

        expireIn--;

        if (expireIn > -1) {
            return;
        }

        data.clear();
    }

    public boolean expired() {
        if (expireIn == -1) {
            return true;
        }

        if (expireIn < expireAfterAccess) {
            expireIn = expireAfterAccess;
        }

        return false;
    }

    public void add(E element) {
        if (expireAfterWrite != -1 && expireIn < expireAfterWrite) {
            expireIn = expireAfterWrite;
        }

        data.add(element);
    }

    public void remove(E element) {
        if (expireAfterWrite != -1 && expireIn < expireAfterWrite) {
            expireIn = expireAfterWrite;
        }

        data.remove(element);
    }
    public void addAll(@NonNull Collection<E> elements) {
        if (expireAfterWrite != -1  && expireIn < expireAfterWrite) {
            expireIn = expireAfterWrite;
        }

        data.addAll(elements);
    }

    public @NonNull Collection<E> getAll() {
        return data;
    }
}
