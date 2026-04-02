package com.github.dystopiko.edenmc.mixin;

import com.github.dystopiko.edenmc.interfaces.ServerPlayerAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.github.dystopiko.edenmc.utility.ChatRankPrefixKt.displayNameForChat;

@Mixin(ServerPlayer.class)
public class ServerPlayerMixin implements ServerPlayerAccessor {
    @Shadow @Final
    private MinecraftServer server;

    @Override @Unique
    public @NotNull MinecraftServer eden$getServer() {
        return this.server;
    }

    @Inject(method = "getTabListDisplayName", at = @At("HEAD"), cancellable = true)
    public void addMemberRankPrefixForTabList(CallbackInfoReturnable<Component> cir) {
        cir.setReturnValue(displayNameForChat((ServerPlayer) (Object) this));
    }
}
