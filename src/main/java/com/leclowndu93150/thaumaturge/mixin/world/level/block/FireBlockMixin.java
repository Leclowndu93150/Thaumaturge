package com.leclowndu93150.thaumaturge.mixin.world.level.block;

import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FireBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FireBlock.class)
public class FireBlockMixin {
    @Inject(method = "checkBurnOut", at = @At("HEAD"), cancellable = true)
    private void thaumaturge$wardResistsFire(Level level, BlockPos pos, int chance, RandomSource random, int age, Direction face, CallbackInfo ci) {
        if (WardHandler.isWarded(level, pos)) {
            ci.cancel();
        }
    }
}
