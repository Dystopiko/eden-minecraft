package xyz.memothelemo.edenmc.api;

import xyz.memothelemo.edenmc.api.model.User;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

@SuppressWarnings("unused")
public interface Eden {
    @NonNull
    User getUser(Player player);

    @NonNull
    User getUser(ServerPlayer player);

    @Nullable
    User getUser(UUID playerUuid);
}
