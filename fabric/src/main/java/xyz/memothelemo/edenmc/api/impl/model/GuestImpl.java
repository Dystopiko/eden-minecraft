package xyz.memothelemo.edenmc.api.impl.model;

import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.api.model.organization.MemberRank;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public record GuestImpl(boolean isBedrock) implements User {
    public GuestImpl(@NonNull UUID isBedrock) {
        this(FloodgateApi.getInstance().isFloodgatePlayer(isBedrock));
    }

    @Override
    public @NonNull MemberRank getMemberRank() {
        return MemberRank.GUEST;
    }
}
