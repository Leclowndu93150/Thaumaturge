package com.leclowndu93150.thaumaturge.mixin.sable.api;

import com.leclowndu93150.thaumaturge.compat.sable.SableAssembly;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SubLevelAssemblyHelper.class)
public class SubLevelAssemblyHelperMixin {
    @Inject(method = "moveBlocks", at = @At("TAIL"), require = 0)
    private static void thaumaturge$carryBlockData(
            ServerLevel level, AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci) {
        SableAssembly.blocksMoved(level, transform, blocks);
    }
}
