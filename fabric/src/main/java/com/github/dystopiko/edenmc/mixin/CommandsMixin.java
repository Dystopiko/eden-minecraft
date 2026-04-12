package com.github.dystopiko.edenmc.mixin;

import com.github.dystopiko.edenmc.callbacks.CommandExecutionCallback;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.ContextChain;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class CommandsMixin {
    @Inject(
        method = "performCommand",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/commands/Commands;executeCommandInContext(Lnet/minecraft/commands/CommandSourceStack;Ljava/util/function/Consumer;)V"
        )
    )
    private void eden$invokeEvent(CallbackInfo ci, @Local ContextChain<CommandSourceStack> chainVar) {
        ContextChain<CommandSourceStack> chain = chainVar;

        // Go through every stage until it reaches EXECUTE, so it skips
        // all the modifiers which we do not mind about.
        while (chain.getStage() == ContextChain.Stage.MODIFY) {
            chain = chain.nextStage();
        }

        CommandContext<CommandSourceStack> source = chain.getTopContext();
        CommandExecutionCallback.EVENT.invoker().onCommandExecution(source);
    }
}
