package com.leclowndu93150.thaumaturge.mixin.sable.api;

import com.leclowndu93150.thaumaturge.compat.sable.SableAssembly;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper;
import dev.ryanhcode.sable.api.SubLevelAssemblyHelper.AssemblyTransform;
import dev.ryanhcode.sable.companion.math.BoundingBox3i;
import dev.ryanhcode.sable.companion.math.BoundingBox3ic;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SubLevelAssemblyHelper.class)
public class SubLevelAssemblyHelperMixin {
    @WrapMethod(method = "assembleBlocks")
    private static ServerSubLevel thaumaturge$includeRunicMatrices(
            ServerLevel level,
            BlockPos anchor,
            Iterable<BlockPos> blocks,
            BoundingBox3ic bounds,
            Operation<ServerSubLevel> original) {
        var selected = SableAssembly.withRunicMatrices(level, blocks);
        return original.call(level, anchor, selected, new BoundingBox3i(bounds).expandTo(BoundingBox3i.from(selected)));
    }

    @Inject(method = "moveBlocks", at = @At("TAIL"), require = 0)
    private static void thaumaturge$carryBlockData(
            ServerLevel level, AssemblyTransform transform, Iterable<BlockPos> blocks, CallbackInfo ci) {
        SableAssembly.blocksMoved(level, transform, blocks);
    }

    @Inject(method = "assembleBlocks", at = @At("TAIL"), require = 0)
    private static void thaumaturge$carryAssemblyData(
            ServerLevel level,
            BlockPos anchor,
            Iterable<BlockPos> blocks,
            BoundingBox3ic bounds,
            CallbackInfoReturnable<ServerSubLevel> cir,
            @Local AssemblyTransform transform) {
        SableAssembly.wardsMoved(level, transform, blocks);
    }
}
