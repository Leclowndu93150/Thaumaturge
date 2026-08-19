package com.leclowndu93150.thaumaturge.client.effect.manager;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Collection;
import java.util.Iterator;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;

public abstract class AbstractFXManager<I extends IFXInstance> {
    protected abstract Collection<I> activeInstances();

    public void tickAll(ClientLevel level) {
        Iterator<I> it = activeInstances().iterator();
        while (it.hasNext()) {
            I inst = it.next();
            inst.tick();
            if (inst.isExpired())
                it.remove();
        }
    }

    public abstract void renderAll(PoseStack poseStack, Camera camera, float partialTick);
}
