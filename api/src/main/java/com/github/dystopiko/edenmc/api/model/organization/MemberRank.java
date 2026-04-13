package com.github.dystopiko.edenmc.api.model.organization;

import org.checkerframework.checker.nullness.qual.NonNull;

@SuppressWarnings("unused")
public enum MemberRank {
    ADMIN("admin"),
    STAFF("staff"),
    CONTRIBUTOR("contributor"),
    MEMBER("member"),
    UNKNOWN("unknown"),
    GUEST("guest");

    private final @NonNull String value;

    MemberRank(@NonNull String value) {
        this.value = value;
    }

    public boolean hasRole() {
        return this != MemberRank.GUEST;
    }

    public boolean isAdmin() {
        return this == MemberRank.ADMIN;
    }

    public boolean isStaff() {
        return this == MemberRank.STAFF;
    }

    public boolean isContributor() {
        return this == MemberRank.CONTRIBUTOR;
    }

    public boolean isGuest() {
        return this == MemberRank.GUEST;
    }

    public @NonNull String toString() {
        return this.value;
    }
}
