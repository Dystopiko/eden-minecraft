package com.github.dystopiko.edenmc.api.impl.model;

import com.github.dystopiko.edenmc.api.model.User;
import com.github.dystopiko.edenmc.api.model.organization.Member;
import com.github.dystopiko.edenmc.api.model.organization.MemberRank;
import com.github.dystopiko.edenmc.sessions.MemberSession;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.floodgate.api.FloodgateApi;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public final class MemberImpl implements User, Member {
    private final @NonNull MemberSession session;
    private final boolean isBedrock;

    public MemberImpl(@NonNull UUID uuid, @NonNull MemberSession session) {
        this.isBedrock = FloodgateApi.getInstance().isFloodgatePlayer(uuid);
        this.session = session;
    }

    @Override
    public @NonNull String getDiscordId() {
        return session.getMember().getId();
    }

    @Override
    public @NonNull String getDiscordName() {
        return session.getMember().getName();
    }

    @Override
    public @Nullable OffsetDateTime getLastLogin() {
        return session.getLastLoginAt();
    }

    @Override
    public @NonNull List<String> getPerks() {
        return session.getPerks();
    }

    @Override
    public boolean isBedrock() {
        return this.isBedrock;
    }

    @Override
    public @NonNull MemberRank getMemberRank() {
        String rank = session.getRank();
        return switch (rank) {
            case "admin" -> MemberRank.ADMIN;
            case "staff" -> MemberRank.STAFF;
            case "contributor" -> MemberRank.CONTRIBUTOR;
            case "member" -> MemberRank.MEMBER;
            default -> MemberRank.UNKNOWN;
        };
    }
}
