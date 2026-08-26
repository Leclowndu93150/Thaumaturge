package com.leclowndu93150.thaumaturge.mixin.client.particle;

import com.leclowndu93150.thaumaturge.client.warding.WardClientHandler;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ParticleEngine.class)
public class ParticleEngineMixin {
    @Inject(method = "crack", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$wardHitEffect(BlockPos pos, Direction direction, CallbackInfo ci) {
        ClientLevel level = Minecraft.getInstance().level;
        if (level != null && ClientWardHolder.isWarded(pos)) {
            WardClientHandler.spawnHitEffect(level, pos, direction, null);
            ci.cancel();
        }
    }
}
