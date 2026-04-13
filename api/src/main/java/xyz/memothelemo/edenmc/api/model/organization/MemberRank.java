package xyz.memothelemo.edenmc.api.model.organization;

import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * Represents a player's rank within the organization, as resolved by the
 * Eden gateway during login.
 *
 * <p>Use {@link #hasRole()} to distinguish members from guests, and the
 * specific {@code is*()} methods to branch on a particular rank.
 */
@SuppressWarnings("unused")
public enum MemberRank {
    /** Administrator rank */
    ADMIN("admin"),

    /** Staff rank */
    STAFF("staff"),

    /** Contributor rank */
    CONTRIBUTOR("contributor"),

    /** Member rank */
    MEMBER("member"),

    /**
     * A rank returned by the gateway that this version of EdenMC does not
     * recognize. Treat as a non-guest member with no specific permissions.
     */
    UNKNOWN("unknown"),

    /** The player is not a member of the organization. */
    GUEST("guest");

    private final @NonNull String value;

    MemberRank(@NonNull String value) {
        this.value = value;
    }

    /**
     * Returns {@code true} if this rank represents any recognized role within
     * the organization (i.e. not {@link #GUEST}).
     *
     * @return {@code true} if the player holds any organizational role
     */
    public boolean hasRole() {
        return this != MemberRank.GUEST;
    }

    /**
     * Returns {@code true} if this rank is {@link #ADMIN}.
     *
     * @return {@code true} if the player is an administrator
     */
    public boolean isAdmin() {
        return this == MemberRank.ADMIN;
    }

    /**
     * Returns {@code true} if this rank is {@link #STAFF}.
     *
     * @return {@code true} if the player is a staff member
     */
    public boolean isStaff() {
        return this == MemberRank.STAFF;
    }

    /**
     * Returns {@code true} if this rank is {@link #CONTRIBUTOR}.
     *
     * @return {@code true} if the player is a contributor
     */
    public boolean isContributor() {
        return this == MemberRank.CONTRIBUTOR;
    }

    /**
     * Returns {@code true} if this rank is {@link #GUEST}.
     *
     * @return {@code true} if the player is not a member of the organization
     */
    public boolean isGuest() {
        return this == MemberRank.GUEST;
    }

    /**
     * Returns the lowercase string value of this rank as sent by the gateway,
     * e.g. {@code "admin"}, {@code "member"}, {@code "guest"}.
     *
     * @return the gateway string value; never {@code null}
     */
    @Override
    public @NonNull String toString() {
        return this.value;
    }
}
