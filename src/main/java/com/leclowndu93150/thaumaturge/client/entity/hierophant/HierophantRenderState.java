package com.leclowndu93150.thaumaturge.client.entity.hierophant;

import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.EntityEldritchHierophant;
import com.leclowndu93150.thaumaturge.content.entity.boss.hierophant.HierophantAction;
import net.minecraft.util.Mth;

public final class HierophantRenderState {
    public HierophantAction action = HierophantAction.IDLE;
    public float actionTicks;
    public float movement;
    public float recoil;
    public boolean awakened;
    public float ageInTicks;
    public boolean isInvisible;

    public static HierophantRenderState of(EntityEldritchHierophant entity, float partialTick) {
        HierophantRenderState state = new HierophantRenderState();
        state.ageInTicks = entity.tickCount + partialTick;
        state.isInvisible = entity.isInvisible();
        state.action = entity.action();
        state.actionTicks = entity.actionAge(partialTick);
        state.awakened = entity.awakened();
        state.movement = Mth.clamp((float) entity.getDeltaMovement().horizontalDistance() * 8, 0, 1);
        state.recoil = Math.max(0, entity.hurtTime - partialTick);
        return state;
    }
}
