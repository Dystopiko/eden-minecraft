package xyz.memothelemo.edenmc.api.model.organization;

import xyz.memothelemo.edenmc.api.model.User;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.OffsetDateTime;
import java.util.List;

@SuppressWarnings("unused")
public interface Member extends User {
    @NonNull
    String getDiscordId();

    @NonNull
    String getDiscordName();

    @Nullable
    OffsetDateTime getLastLogin();

    @NonNull
    List<String> getPerks();
}
