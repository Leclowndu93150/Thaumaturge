package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyZombie;
import com.leclowndu93150.thaumaturge.content.entity.EntityGiantBrainyZombie;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.BabyZombieModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public final class BrainyZombieRenderer extends AbstractZombieRenderer<EntityBrainyZombie, BrainyZombieRenderState, ZombieModel<BrainyZombieRenderState>> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/brainy_zombie.png");
    private static final float ANGER_TINT_STRENGTH = 0.5F;

    public BrainyZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), new BabyZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_BABY)),
                ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), ZombieModel::new), ArmorModelSet.bake(ModelLayers.ZOMBIE_BABY_ARMOR, context.getModelSet(), BabyZombieModel::new));
    }

    @Override
    public BrainyZombieRenderState createRenderState() {
        return new BrainyZombieRenderState();
    }

    @Override
    public void extractRenderState(EntityBrainyZombie entity, BrainyZombieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.anger = entity instanceof EntityGiantBrainyZombie giant ? giant.getAnger() : 0.0F;
    }

    @Override
    public Identifier getTextureLocation(BrainyZombieRenderState state) {
        return TEXTURE;
    }

    @Override
    protected int getModelTint(BrainyZombieRenderState state) {
        if (state.anger <= 0.0F) {
            return super.getModelTint(state);
        }
        float fade = 1.0F - Math.min(1.0F, state.anger) * ANGER_TINT_STRENGTH;
        return ARGB.colorFromFloat(1.0F, 1.0F, Mth.clamp(fade, 0.0F, 1.0F), Mth.clamp(fade, 0.0F, 1.0F));
    }
}
