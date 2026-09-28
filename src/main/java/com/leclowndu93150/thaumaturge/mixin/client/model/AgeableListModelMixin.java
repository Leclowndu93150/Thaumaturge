package com.leclowndu93150.thaumaturge.mixin.client.model;

import com.google.common.collect.Iterables;
import com.leclowndu93150.thaumaturge.client.taint.overlay.ModelRootParts;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AgeableListModel.class)
public abstract class AgeableListModelMixin implements ModelRootParts {
    @Shadow
    protected abstract Iterable<ModelPart> headParts();

    @Shadow
    protected abstract Iterable<ModelPart> bodyParts();

    @Override
    public Iterable<ModelPart> thaumaturge$rootParts() {
        return Iterables.concat(headParts(), bodyParts());
    }
}
