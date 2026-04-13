package xyz.memothelemo.edenmc.api.impl.model;

import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.api.model.organization.MemberRank;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.geysermc.floodgate.api.FloodgateApi;

import java.util.UUID;

public final class GuestImpl implements User {
    private final boolean isBedrock;

    public GuestImpl(@NonNull UUID uuid) {
        this.isBedrock = FloodgateApi.getInstance().isFloodgatePlayer(uuid);
    }

    @Override
    public boolean isBedrock() {
        return this.isBedrock;
    }

    @Override
    public @NonNull MemberRank getMemberRank() {
        return MemberRank.GUEST;
    }
}
