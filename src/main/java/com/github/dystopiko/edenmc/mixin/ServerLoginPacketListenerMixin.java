package com.github.dystopiko.edenmc.mixin;

import com.github.dystopiko.edenmc.EdenMod;
import com.github.dystopiko.edenmc.exceptions.GatewayException;
import com.github.dystopiko.edenmc.exceptions.InternalException;
import com.github.dystopiko.edenmc.gateway.GatewayClient;
import com.github.dystopiko.edenmc.gateway.members.EncodedMember;
import com.github.dystopiko.edenmc.gateway.sessions.SessionGranted;
import com.github.dystopiko.edenmc.gateway.state.RequestSessionState;
import com.github.dystopiko.edenmc.sessions.GuestSession;
import com.github.dystopiko.edenmc.sessions.MemberSession;
import com.github.dystopiko.edenmc.sessions.Session;
import com.github.dystopiko.edenmc.sessions.SessionManager;
import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dystopiko.edenmc.utility.ExtensionsKt.resolveIpAddress;

@Mixin(ServerLoginPacketListenerImpl.class)
public abstract class ServerLoginPacketListenerMixin {
    @Unique
    private static final AtomicInteger eden$UNIQUE_THREAD_ID = new AtomicInteger(0);

    @Unique
    private volatile RequestSessionState eden$state = RequestSessionState.INITIAL;

    @Unique
    private UUID eden$playerUUID = null;

    @Unique
    private Instant eden$sinceLastRequest = null;

    @Unique
    private volatile SessionGranted eden$grantedResponse = null;

    @Shadow @Final
    Connection connection;

    @Shadow
    public abstract void disconnect(Component component);

    @Unique
    private void eden$logSessionGranted(
        @NotNull Instant sinceLastRequest,
        @NotNull Session anySession
    ) {
        Duration elapsed = Duration.between(sinceLastRequest, Instant.now());
        if (anySession instanceof MemberSession session) {
            EncodedMember member = session.getMember();
            EdenMod.logger.debug("Session granted for {} (rank={}, elapsed={}ms)",
                member.getName(),
                member.getRank(),
                elapsed.toMillis()
            );
        } else {
            EdenMod.logger.debug("Session granted as guest (elapsed={}ms)", elapsed.toMillis());
        }
    }

    @Unique
    private void eden$onSessionGranted(
        @NotNull UUID uuid,
        @NotNull Instant sinceLastRequest,
        @NotNull SessionGranted response
    ) {
        Session session;
        if (response.getMember() instanceof EncodedMember) {
            session = new MemberSession(
                uuid,
                response.getLastLoginAt(),
                response.getMember(),
                response.getPerks()
            );
        } else {
            session = new GuestSession(uuid);
        }

        this.eden$logSessionGranted(sinceLastRequest, session);

        try {
            SessionManager.INSTANCE.register(uuid, session);
        } catch (InternalException $) {
            EdenMod.logger.warn("{} maybe left the server too quickly whilst requesting session from gateway", uuid);
        }
    }

    @Unique
    private void eden$handleThrowable(@NotNull Throwable throwable) {
        if (throwable instanceof GatewayException error) {
            // Known gateway rejection, surface the human-readable message.
            EdenMod.logger.debug("Gateway rejected session request: {}", error.getErrorMessage());
            this.disconnect(Component.literal(error.getErrorMessage()));
        } else if (throwable instanceof IOException) {
            // Network/IO failure, let the player go through, but they're in temporary session
            // based on the cached data from the SessionManager.
            EdenMod.logger.warn("I/O error occurred while requesting session from gateway", throwable);
            this.disconnect(Component.literal("Eden is offline. Please stay tuned for updates and try again later."));
        } else {
            EdenMod.logger.error("Unexpected error during session request", throwable);
            this.disconnect(Component.literal("An internal error occurred from Eden. Please try again later."));
        }
    }

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void eden$interceptTickMethod(CallbackInfo ci) {
        if (this.eden$grantedResponse != null) {
            this.eden$onSessionGranted(
                this.eden$playerUUID,
                this.eden$sinceLastRequest,
                this.eden$grantedResponse
            );

            // Consume the `eden$requestSessionInProgress` field because
            // this will run multiple times
            this.eden$sinceLastRequest = null;
            this.eden$grantedResponse = null;
            this.eden$state = RequestSessionState.GRANTED;
        }

        if (this.eden$state == RequestSessionState.REQUESTING) {
            ci.cancel();
        }
    }

    @Unique
    private void eden$handleExistingSession(Session session) {
        EdenMod.logger.warn("{} has already granted a new session (maybe got disconnected?)", session.getUuid());
        this.eden$logSessionGranted(this.eden$sinceLastRequest, session);
        this.eden$sinceLastRequest = null;
        this.eden$state = RequestSessionState.GRANTED;
    }

    @Inject(method = "finishLoginAndWaitForClient", at = @At("HEAD"), cancellable = true)
    private void eden$requestSession(GameProfile profile, CallbackInfo ci) {
        GatewayClient gateway = EdenMod.gateway;
        if (gateway == null) return;

        // Do not proceed if it is already authenticated, in other words, go through its inner code
        if (this.eden$state != RequestSessionState.INITIAL) return;

        this.eden$playerUUID = profile.id();
        this.eden$state = RequestSessionState.REQUESTING;
        this.eden$sinceLastRequest = Instant.now();

        // Verify whether the player has already received a new session but hasn’t acknowledged it
        // yet, possibly because they left the server after Eden gave a new session.
        if (SessionManager.INSTANCE.isSessionPresentForPlayer(profile.id())) {
            this.eden$handleExistingSession(SessionManager.INSTANCE.getSession(profile.id()));
            return;
        }

        String ipAddress = resolveIpAddress(this.connection);
        boolean isJava = !FloodgateApi.getInstance().isFloodgateId(profile.id());

        EdenMod.logger.debug(
            "Requesting session from gateway server (uuid={}, ip={}, java={})",
            profile.id(),
            ipAddress,
            isJava
        );

        Thread thread = new Thread("Eden-SessionRequest-#" + eden$UNIQUE_THREAD_ID.incrementAndGet()) {
            @Override
            public void run() {
                try {
                    ServerLoginPacketListenerMixin.this.eden$grantedResponse = gateway.requestSession(
                        ServerLoginPacketListenerMixin.this.eden$playerUUID,
                        ipAddress,
                        isJava
                    );
                } catch (Throwable t) {
                    ServerLoginPacketListenerMixin.this.eden$handleThrowable(t);
                }
            }
        };

        // The try/catch in run() already covers everything; this handler is a
        // last-resort safety net for JVM-level throwables (e.g. OutOfMemoryError)
        // that might escape before our catch block is reached.
        thread.setUncaughtExceptionHandler((t, e) ->
            EdenMod.logger.error("Unhandled throwable on session-request thread:", e));

        thread.start();
        ci.cancel();
    }
}
