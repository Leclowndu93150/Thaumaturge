package com.leclowndu93150.thaumaturge.mixin.client.multiplayer;

import com.leclowndu93150.thaumaturge.client.warding.WardClientHandler;
import com.leclowndu93150.thaumaturge.content.warding.ClientWardHolder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public class ClientLevelMixin {
    @Inject(method = "addBreakingBlockEffect(Lnet/minecraft/core/BlockPos;Lnet/minecraft/core/Direction;Lnet/minecraft/world/phys/HitResult;)V", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$wardHitEffect(BlockPos pos, Direction direction, HitResult hitResult, CallbackInfo ci) {
        if (ClientWardHolder.isWarded(pos)) {
            WardClientHandler.spawnHitEffect((ClientLevel) (Object) this, pos, direction, hitResult);
            ci.cancel();
        }
    }
}
