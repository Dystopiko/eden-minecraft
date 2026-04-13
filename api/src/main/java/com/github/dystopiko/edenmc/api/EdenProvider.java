package com.github.dystopiko.edenmc.api;

import org.jetbrains.annotations.NotNull;

import static org.jetbrains.annotations.ApiStatus.Internal;

@SuppressWarnings("unused")
public final class EdenProvider {
    private static Eden instance = null;

    public static @NotNull Eden get() {
        Eden instance = EdenProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("EdenMC is not loaded yet!");
        }
        return instance;
    }

    @Internal
    static void register(Eden instance) {
        EdenProvider.instance = instance;
    }

    private EdenProvider() {}
}
