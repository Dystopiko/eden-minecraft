package com.github.dystopiko.edenmc.interfaces;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

public interface ServerPlayerAccessor {
    @NotNull MinecraftServer eden$getServer();
}
