package xyz.memothelemo.edenmc.api;

import xyz.memothelemo.edenmc.api.model.User;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
     * Returns a current session from a specified player.
     *
     * @param player Minecraft player handle
     * @return player's current session. it never returns null
     */
    @NonNull
    User getUser(Player player);

    /**
     * Returns a current session from a specified server player.
     *
     * <p>It is equivalent to {@link #getUser(Player)} but it accepts {@link ServerPlayer}
     * and it can be called directly as an argument.
     *
     * @param player Minecraft server player handle
     * @return player's current session. it never returns null
     */
    @NonNull
    User getUser(ServerPlayer player);

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
