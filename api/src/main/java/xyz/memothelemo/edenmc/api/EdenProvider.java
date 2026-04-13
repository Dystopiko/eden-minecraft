package xyz.memothelemo.edenmc.api;

import org.jetbrains.annotations.NotNull;

import static org.jetbrains.annotations.ApiStatus.Internal;

/**
 * Static accessor for the {@link Eden} API.
 *
 * <p>Holds the single active {@link Eden} implementation registered by the
 * Eden mod at startup. Other mods should call {@link #get()} to obtain the
 * instance rather than depending on Eden's internals directly.
 *
 * <p>This class is platform-agnostic and works on any mod loader that exposes
 * vanilla Minecraft internals (such as Fabric, Quilt, or Forge). Bukkit-based
 * platforms (Spigot, Paper, etc.) are not supported.
 *
 * @see Eden
 */
@SuppressWarnings("unused")
public final class EdenProvider {
    private static Eden instance = null;

    /**
     * Returns the active {@link Eden} instance.
     *
     * @return the Eden API instance; never null
     * @throws IllegalStateException if called before the Eden mod has finished
     *     initializing. Make sure your mod/plugin depends on {@code Eden} in its
     *     {@code fabric.mod.json} to guarantee load order.
     */
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
