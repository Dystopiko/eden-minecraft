package com.github.dystopiko.edenmc.api.impl;

import com.github.dystopiko.edenmc.api.Eden;
import com.github.dystopiko.edenmc.api.impl.model.GuestImpl;
import com.github.dystopiko.edenmc.api.impl.model.MemberImpl;
import com.github.dystopiko.edenmc.api.model.User;
import com.github.dystopiko.edenmc.sessions.MemberSession;
import com.github.dystopiko.edenmc.sessions.Session;
import com.github.dystopiko.edenmc.sessions.SessionManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public final class EdenImpl implements Eden {
    @Override
    public @NonNull User getUser(Player player) {
        UUID uuid = player.getUUID();
        Session session = SessionManager.INSTANCE.getSession(uuid);
        return userFromSession(uuid, session);
    }

    @Override
    public @NonNull User getUser(ServerPlayer player) {
        UUID uuid = player.getUUID();
        Session session = SessionManager.INSTANCE.getSession(uuid);
        return userFromSession(uuid, session);
    }

    @Override
    public @Nullable User getUser(UUID uuid) {
        Session session = SessionManager.INSTANCE.getSessionNullable(uuid);
        return session != null ? userFromSession(uuid, session) : null;
    }

    private @NonNull User userFromSession(UUID uuid, Session session) {
        if (session instanceof MemberSession) {
            return new MemberImpl(uuid, (MemberSession) session);
        }
        return new GuestImpl(uuid);
    }
}
