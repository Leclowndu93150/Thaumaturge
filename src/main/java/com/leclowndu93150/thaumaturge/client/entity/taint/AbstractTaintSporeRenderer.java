package com.leclowndu93150.thaumaturge.client.entity.taint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.taint.entity.AbstractTaintSpore;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

public abstract class AbstractTaintSporeRenderer<T extends AbstractTaintSpore, M extends EntityModel<TaintSporeRenderState>> extends MobRenderer<T, TaintSporeRenderState, M> {
    public static final Identifier TEXTURE = TTIds.rl("textures/entity/taint_spore.png");
    private static final float SHADOW = 0.25F;
    private static final float BURST_TICKS = 20.0F;

    protected AbstractTaintSporeRenderer(EntityRendererProvider.Context context, M model) {
        super(context, model, SHADOW);
    }

    @Override
    public Identifier getTextureLocation(TaintSporeRenderState state) {
        return TEXTURE;
    }

    @Override
    public TaintSporeRenderState createRenderState() {
        return new TaintSporeRenderState();
    }

    @Override
    public void extractRenderState(T entity, TaintSporeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.displaySize = entity.getDisplaySize(partialTicks);
        state.growth = Mth.clamp(entity.getSporeSize() - state.displaySize, 0.0F, 1.0F);
        state.burstProgress = Mth.clamp(state.deathTime / BURST_TICKS, 0.0F, 1.0F);
        state.releaseTime = Math.max(0.0F, entity.releaseTicks() - partialTicks);
        state.hurt = Math.max(0.0F, entity.hurtTime - partialTicks);
    }
}
