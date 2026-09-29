package com.leclowndu93150.thaumaturge.mixin.client.lighting;

import com.leclowndu93150.thaumaturge.client.lighting.NitorDynamicLights;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.BlockLightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BlockLightEngine.class)
public abstract class BlockLightEngineMixin {
    @ModifyVariable(method = "getEmission", at = @At("STORE"), ordinal = 0)
    private int thaumaturge$nitorEmission(int emission, long blockNode, BlockState state) {
        return Math.max(emission, NitorDynamicLights.emission(this, blockNode));
    }
}
