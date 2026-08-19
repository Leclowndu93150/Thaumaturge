package com.leclowndu93150.thaumaturge.mixin.world.level.block.piston;

import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonBaseBlock.class)
public class PistonBaseBlockMixin {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private static void thaumaturge$wardResistsPistons(BlockState state, Level level, BlockPos pos, Direction direction, boolean allowDestroyable, Direction connectionDirection, CallbackInfoReturnable<Boolean> cir) {
        if (WardHandler.isWarded(level, pos)) {
            cir.setReturnValue(false);
        }
    }
}
