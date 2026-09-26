package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityBrainyDrowned;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.BabyDrownedModel;
import net.minecraft.client.model.monster.zombie.DrownedModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class BrainyDrownedRenderer extends AbstractZombieRenderer<EntityBrainyDrowned, ZombieRenderState, DrownedModel> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/brainy_drowned.png");
    private static final float SWIM_TILT_DEGREES = -10.0F;

    public BrainyDrownedRenderer(EntityRendererProvider.Context context) {
        super(context, new DrownedModel(context.bakeLayer(ModelLayers.DROWNED)), new BabyDrownedModel(context.bakeLayer(ModelLayers.DROWNED_BABY)),
                ArmorModelSet.bake(ModelLayers.DROWNED_ARMOR, context.getModelSet(), DrownedModel::new),
                ArmorModelSet.bake(ModelLayers.DROWNED_BABY_ARMOR, context.getModelSet(), BabyDrownedModel::new));
        this.addLayer(new BrainyDrownedOuterLayer(this, context.getModelSet()));
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }

    @Override
    protected void setupRotations(ZombieRenderState state, PoseStack poseStack, float bodyRot, float entityScale) {
        super.setupRotations(state, poseStack, bodyRot, entityScale);
        if (state.swimAmount > 0.0F) {
            float tilt = Mth.lerp(state.swimAmount, 0.0F, SWIM_TILT_DEGREES - state.xRot);
            poseStack.rotateAround(Axis.XP.rotationDegrees(tilt), 0.0F, state.boundingBoxHeight / 2.0F / entityScale, 0.0F);
        }
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(EntityBrainyDrowned mob, HumanoidArm arm) {
        ItemStack held = mob.getItemHeldByArm(arm);
        return mob.getMainArm() == arm && mob.isAggressive() && held.is(Items.TRIDENT) ? HumanoidModel.ArmPose.THROW_TRIDENT : super.getArmPose(mob, arm);
    }
}
