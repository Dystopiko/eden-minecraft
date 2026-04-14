package xyz.memothelemo.edenmc.api.impl;

import xyz.memothelemo.edenmc.api.Eden;
import xyz.memothelemo.edenmc.api.impl.model.GuestImpl;
import xyz.memothelemo.edenmc.api.impl.model.MemberImpl;
import xyz.memothelemo.edenmc.api.model.User;
import xyz.memothelemo.edenmc.sessions.MemberSession;
import xyz.memothelemo.edenmc.sessions.Session;
import xyz.memothelemo.edenmc.sessions.SessionManager;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public final class EdenImpl implements Eden {
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
