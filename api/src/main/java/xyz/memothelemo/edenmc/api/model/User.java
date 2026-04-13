package xyz.memothelemo.edenmc.api.model;

import xyz.memothelemo.edenmc.api.model.organization.MemberRank;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents an identity for a player, regardless of whether they are a
 * member of the organization. It is obtainable for every player completed
 * the login handshake and successfully joined the server.
 *
 * <p>To access member-specific data such as their Discord identity and perks,
 * check whether this instance is a {@link xyz.memothelemo.edenmc.api.model.organization.Member Member}
 * before casting.
 *
 * @see xyz.memothelemo.edenmc.api.model.organization.Member Member
 */
@SuppressWarnings("unused")
public interface User {
    /**
     * Returns a boolean value of whether the specified player is connected
     * via Bedrock Edition through a Floodgate.
     *
     * @return {@code true} if the specified player is connected via Bedrock
     * Edition through a Floodgate.
     */
    boolean isBedrock();

    /**
     * Returns the player's rank within the organization.
     *
     * <p>Returns {@link MemberRank#GUEST} if the player is not a member.
     *
     * @return the player's rank; never {@code null}
     */
    @NonNull
    MemberRank getMemberRank();
}
