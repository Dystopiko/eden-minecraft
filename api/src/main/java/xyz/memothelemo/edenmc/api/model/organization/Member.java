package xyz.memothelemo.edenmc.api.model.organization;

import xyz.memothelemo.edenmc.api.model.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Extended identity for a player who is a verified member of the organization.
 *
 * <p>Accessible by casting a {@link User} instance after an {@code instanceof}
 * check. It provides their Discord identity, perks, and login history.
 *
 * @see User
 */
@SuppressWarnings("unused")
public interface Member extends User {
    /**
     * Returns the member's Discord user snowflake ID as a string.
     *
     * @return the Discord snowflake ID; never {@code null}
     */
    @NonNull
    String getDiscordId();

    /**
     * Returns the member's Discord username.
     *
     * @return the Discord username; never {@code null}
     */
    @NonNull
    String getDiscordName();

    /**
     * Returns the timestamp of the member's most recent login, or
     * {@code null} if they have never logged in before.
     *
     * @return the last login timestamp, or {@code null} for first-time players
     */
    @Nullable
    OffsetDateTime getLastLogin();


    /**
     * Returns a list of perks granted to this member by the gateway.
     *
     * @return a list of perks in permission identifiers; empty if no perks are assigned,
     *         never returns {@code null}
     */
    @NonNull
    List<String> getPerks();
}
