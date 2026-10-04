package com.leclowndu93150.thaumaturge.mixin.client.model;

import com.leclowndu93150.thaumaturge.client.taint.overlay.ModelRootParts;
import net.minecraft.client.model.ListModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ListModel.class)
public abstract class ListModelMixin implements ModelRootParts {
    @Shadow
    public abstract Iterable<ModelPart> parts();

    @Override
    public Iterable<ModelPart> thaumaturge$rootParts() {
        return parts();
    }
}
