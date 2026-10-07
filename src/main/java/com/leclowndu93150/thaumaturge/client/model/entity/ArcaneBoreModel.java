package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.render.BoreDrillFx;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public final class ArcaneBoreModel extends HierarchicalModel<EntityArcaneBore> {
    protected final ModelPart root;

    @Override
    public ModelPart root() {
        return root;
    }

    private static final int TEXTURE_WIDTH = 128;
    private static final int TEXTURE_HEIGHT = 128;
    private static final float CORE_BOB = 0.25F;
    private static final float CORE_PERIOD_TICKS = 40.0F;
    private static final float TIP_RECOIL = 0.25F;
    private static final float TIP_RECOIL_RATE = 1.2F;

    private final ModelPart base;
    private final ModelPart tip;
    private final ModelPart core;
    private final float tipRestZ;
    private final float coreRestY;

    public ArcaneBoreModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        base = root.getChild("base");
        tip = base.getChild("tip");
        core = base.getChild("core");
        tipRestZ = tip.z;
        coreRestY = core.y;
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "tripod",
                CubeListBuilder.create().texOffs(110, 1).addBox(-1.75F, -0.375F, -1.75F, 3.5F, 2.5F, 3.5F),
                PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition base = root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                        .texOffs(1, 1)
                        .addBox(-3.0F, -6.25F, -3.5F, 6.0F, 6.5F, 6.5F)
                        .texOffs(27, 1)
                        .addBox(-3.375F, -4.5F, -1.5F, 0.5F, 3.0F, 3.0F)
                        .texOffs(35, 1)
                        .addBox(2.875F, -4.5F, -1.5F, 0.5F, 3.0F, 3.0F)
                        .texOffs(43, 1)
                        .addBox(-2.0F, -5.0F, -7.0F, 4.0F, 4.0F, 3.5F)
                        .texOffs(59, 1)
                        .addBox(-2.5F, -5.5F, -8.0F, 5.0F, 5.0F, 1.0F)
                        .texOffs(80, 1)
                        .addBox(-2.0F, -6.375F, 2.875F, 4.0F, 6.5F, 0.5F)
                        .texOffs(90, 1)
                        .addBox(-1.5F, -5.75F, 3.25F, 3.0F, 5.5F, 3.0F),
                PartPose.offset(0.0F, 13.0F, 0.0F));
        base.addOrReplaceChild(
                "tip",
                CubeListBuilder.create().texOffs(72, 1).addBox(-1.0F, -1.0F, -0.625F, 2.0F, 2.0F, 1.5F),
                PartPose.offset(0.0F, -3.0F, -8.5F));
        base.addOrReplaceChild(
                "core",
                CubeListBuilder.create().texOffs(103, 1).addBox(-0.75F, -0.75F, -0.75F, 1.5F, 1.5F, 1.5F),
                PartPose.offset(0.0F, -3.0F, 4.75F));
        root.addOrReplaceChild(
                "leg0",
                CubeListBuilder.create()
                        .texOffs(1, 15)
                        .addBox(-1.0F, 2.5F, -1.0F, 2.0F, 11.0F, 2.0F)
                        .texOffs(10, 15)
                        .addBox(-1.25F, 12.5F, -1.25F, 2.5F, 1.5F, 2.5F)
                        .texOffs(21, 15)
                        .addBox(-1.25F, 6.5F, -1.25F, 2.5F, 1.0F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5235988F, 0.7853982F, 0.0F));
        root.addOrReplaceChild(
                "leg1",
                CubeListBuilder.create()
                        .texOffs(32, 15)
                        .addBox(-1.0F, 2.5F, -1.0F, 2.0F, 11.0F, 2.0F)
                        .texOffs(41, 15)
                        .addBox(-1.25F, 12.5F, -1.25F, 2.5F, 1.5F, 2.5F)
                        .texOffs(52, 15)
                        .addBox(-1.25F, 6.5F, -1.25F, 2.5F, 1.0F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5235988F, 2.3561945F, 0.0F));
        root.addOrReplaceChild(
                "leg2",
                CubeListBuilder.create()
                        .texOffs(63, 15)
                        .addBox(-1.0F, 2.5F, -1.0F, 2.0F, 11.0F, 2.0F)
                        .texOffs(72, 15)
                        .addBox(-1.25F, 12.5F, -1.25F, 2.5F, 1.5F, 2.5F)
                        .texOffs(83, 15)
                        .addBox(-1.25F, 6.5F, -1.25F, 2.5F, 1.0F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5235988F, 3.9269908F, 0.0F));
        root.addOrReplaceChild(
                "leg3",
                CubeListBuilder.create()
                        .texOffs(94, 15)
                        .addBox(-1.0F, 2.5F, -1.0F, 2.0F, 11.0F, 2.0F)
                        .texOffs(103, 15)
                        .addBox(-1.25F, 12.5F, -1.25F, 2.5F, 1.5F, 2.5F)
                        .texOffs(114, 15)
                        .addBox(-1.25F, 6.5F, -1.25F, 2.5F, 1.0F, 2.5F),
                PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.5235988F, 5.4977871F, 0.0F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public void setAim(float yawDegrees, float pitchDegrees) {
        base.yRot = yawDegrees * Mth.DEG_TO_RAD;
        base.xRot = pitchDegrees * Mth.DEG_TO_RAD;
    }

    public void animate(float ageInTicks, boolean digging, float beamSpinDegrees) {
        float phase = ageInTicks / CORE_PERIOD_TICKS * Mth.TWO_PI;
        core.y = coreRestY - Mth.sin(phase) * CORE_BOB;
        core.yRot = phase * 0.5F;
        tip.zRot = digging ? beamSpinDegrees * Mth.DEG_TO_RAD : 0.0F;
        tip.z = digging ? tipRestZ + Mth.sin(ageInTicks * TIP_RECOIL_RATE) * TIP_RECOIL : tipRestZ;
    }

    @Override
    public void setupAnim(
            EntityArcaneBore entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float partialTicks = ageInTicks - entity.tickCount;
        setAim(netHeadYaw, headPitch);
        animate(
                ageInTicks,
                (entity.clientDiggingSmoothed() && entity.isActive() && entity.validInventory()),
                BoreDrillFx.beamSpin(entity.level().getGameTime(), partialTicks));
    }
}
