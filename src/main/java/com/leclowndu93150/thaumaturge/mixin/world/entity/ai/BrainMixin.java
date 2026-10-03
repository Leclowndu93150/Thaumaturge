package com.leclowndu93150.thaumaturge.mixin.world.entity.ai;

import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitEngine;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Brain.class)
public abstract class BrainMixin<E extends LivingEntity> {
    @Inject(
            method = "tick(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;)V",
            at = @At("HEAD"),
            cancellable = true)
    private void thaumaturge$skipWhileTraitControlled(ServerLevel level, E body, CallbackInfo ci) {
        if (MobTraitEngine.suppressesNativeAi(body)) {
            ci.cancel();
        }
    }
}
