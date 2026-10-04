package com.leclowndu93150.thaumaturge.mixin.client.model;

import com.leclowndu93150.thaumaturge.client.taint.overlay.ModelRootParts;
import java.util.List;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(HierarchicalModel.class)
public abstract class HierarchicalModelMixin implements ModelRootParts {
    @Shadow
    public abstract ModelPart root();

    @Override
    public Iterable<ModelPart> thaumaturge$rootParts() {
        return List.of(root());
    }
}
