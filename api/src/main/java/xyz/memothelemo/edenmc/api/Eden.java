package xyz.memothelemo.edenmc.api;

import org.checkerframework.checker.nullness.qual.Nullable;
import xyz.memothelemo.edenmc.api.model.User;

import java.util.UUID;

/**
 * Public API for the EdenMC mod.
 *
 * <p>This API exposes per-player session data resolved during the Minecraft login
 * handshake, allowing other mods to read player's membership status, and integrate
 * it without depending on internals.
 *
 * <p>In order to get access to the {@link Eden} interface, you need to obtain the
 * instance of this interface by using {@link EdenProvider#get()} accessor from
 * {@link EdenProvider}.
 *
 * @see EdenProvider
 * @see User
 */
@SuppressWarnings("unused")
public interface Eden {
    /**
     * Returns the session for a player by UUID.
     *
     * <p>A session is absent if the player is not currently online, or if
     * they disconnected before the login handshake completed.
     *
     * @param playerUuid the player's Minecraft UUID
     * @return the player's session, or {@code null} if not found
     */
    @Nullable
    User getUser(UUID playerUuid);
}
