package com.leclowndu93150.thaumaturge.mixin.sable.plot;

import com.leclowndu93150.thaumaturge.compat.sable.SableAssembly;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.plot.heat.SubLevelHeatMapManager;
import net.minecraft.core.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SubLevelHeatMapManager.class)
abstract class RunicHeatMapMixin {
    private static final String DIRECTIONS = "Ldev/ryanhcode/sable/sublevel/plot/heat/SubLevelHeatMapManager;"
            + "DIRECTION_OFFSETS:[Lnet/minecraft/core/BlockPos;";

    @Shadow
    @Final
    private ServerSubLevel subLevel;

    @ModifyExpressionValue(
            method = "step",
            at = {
                @At(value = "FIELD", target = DIRECTIONS, opcode = Opcodes.GETSTATIC, ordinal = 0),
                @At(value = "FIELD", target = DIRECTIONS, opcode = Opcodes.GETSTATIC, ordinal = 1)
            })
    private BlockPos[] thaumaturge$followRunicLink(BlockPos[] offsets, @Local(index = 1) BlockPos current) {
        return SableAssembly.runicConnections(subLevel.getLevel(), current, offsets, false);
    }

    @ModifyExpressionValue(
            method = "step",
            at = @At(value = "FIELD", target = DIRECTIONS, opcode = Opcodes.GETSTATIC, ordinal = 2))
    private BlockPos[] thaumaturge$checkRemovedRunicLink(BlockPos[] offsets, @Local(index = 3) BlockPos current) {
        return SableAssembly.runicConnections(subLevel.getLevel(), current, offsets, true);
    }

    @ModifyExpressionValue(
            method = "step",
            at = @At(value = "FIELD", target = DIRECTIONS, opcode = Opcodes.GETSTATIC, ordinal = 3))
    private BlockPos[] thaumaturge$findAlternateRunicLink(BlockPos[] offsets, @Local(index = 9) BlockPos neighbor) {
        return SableAssembly.runicConnections(subLevel.getLevel(), neighbor, offsets, false);
    }

    @ModifyExpressionValue(
            method = "onSolidAdded",
            at = @At(value = "FIELD", target = DIRECTIONS, opcode = Opcodes.GETSTATIC))
    private BlockPos[] thaumaturge$connectAddedRunicBlock(BlockPos[] offsets, @Local(argsOnly = true) BlockPos pos) {
        return SableAssembly.runicConnections(subLevel.getLevel(), pos, offsets, false);
    }
}
