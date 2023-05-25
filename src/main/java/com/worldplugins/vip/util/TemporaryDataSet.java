package com.worldplugins.vip.util;

import me.post.lib.util.Scheduler;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

public class TemporaryDataSet<E> {
    private final @NotNull List<E> data;
    private final int expireAfterWrite;
    private final int expireAfterAccess;

    private int expireIn = -1;

    public TemporaryDataSet(
        @NotNull Scheduler scheduler,
        int expireAfterWrite,
        int expireAfterAccess,
        boolean concurrent
    ) {
        this.data = concurrent ? new Vector<>() : new ArrayList<>();
        this.expireAfterWrite = expireAfterWrite;
        this.expireAfterAccess = expireAfterAccess;
        scheduler.runTimer(20, 20, false, this::run);
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
    public void addAll(@NotNull Collection<E> elements) {
        if (expireAfterWrite != -1  && expireIn < expireAfterWrite) {
            expireIn = expireAfterWrite;
        }

        data.addAll(elements);
    }

    public void clear() {
        data.clear();
    }

    public @NotNull List<E> getAll() {
        if (expireIn < expireAfterAccess) {
            expireIn = expireAfterAccess;
        }

        return data;
    }
}
