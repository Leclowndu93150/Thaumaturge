package com.leclowndu93150.thaumaturge.client.model.entity;

import com.leclowndu93150.thaumaturge.client.entity.EldritchGuardianRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public final class EldritchGuardianModel extends EntityModel<EldritchGuardianRenderState> {
    private static final int TEX_WIDTH = 128;
    private static final int TEX_HEIGHT = 64;
    private static final float SHOULDERPLATE_TILT_X = -0.3665191F;
    private static final float SHOULDERPLATE_TILT_Y = 0.3141593F;
    private static final float SHOULDERPLATE_TILT_Z = 0.4363323F;
    private static final float ARM_BASE_TILT = -0.9599311F;
    private static final float ARM_TILT_Y = 0.1047198F;
    private static final float ARM_TILT_Z = 0.1919862F;
    private static final float SHOULDER_TILT_Y = 0.122173F;
    private static final float SHOULDER_TILT_Z = 0.0349066F;
    private static final float LEGPANEL_TILT = 0.4363323F;

    private final ModelPart hood;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart legpanelC1;
    private final ModelPart legpanelC2;
    private final ModelPart legpanelC3;
    private final ModelPart cloak1;
    private final ModelPart cloak2;
    private final ModelPart cloak3;
    private final ModelPart sidepanelL2;
    private final ModelPart sidepanelL3;
    private final ModelPart sidepanelL4;
    private final ModelPart sidepanelR2;
    private final ModelPart sidepanelR3;
    private final ModelPart sidepanelR4;

    public EldritchGuardianModel(ModelPart root) {
        super(root);
        this.hood = root.getChild("hood1");
        this.armL = root.getChild("arm_l1");
        this.armR = root.getChild("arm_r1");
        this.legpanelC1 = root.getChild("legpanel_c1");
        this.legpanelC2 = this.legpanelC1.getChild("legpanel_c2");
        this.legpanelC3 = this.legpanelC2.getChild("legpanel_c3");
        this.cloak1 = root.getChild("cloak1");
        this.cloak2 = this.cloak1.getChild("cloak2");
        this.cloak3 = this.cloak2.getChild("cloak3");
        this.sidepanelL2 = root.getChild("sidepanel_l2");
        this.sidepanelL3 = this.sidepanelL2.getChild("sidepanel_l3");
        this.sidepanelL4 = this.sidepanelL3.getChild("sidepanel_l4");
        this.sidepanelR2 = root.getChild("sidepanel_r2");
        this.sidepanelR3 = this.sidepanelR2.getChild("sidepanel_r3");
        this.sidepanelR4 = this.sidepanelR3.getChild("sidepanel_r4");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("belt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1, 3, 6), PartPose.offset(0.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8, 4, 1), PartPose.offset(0.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("mbelt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.offset(0.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("mbelt_r", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1, 3, 6), PartPose.offset(0.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("belt_l", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1, 3, 6), PartPose.offset(0.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8, 7, 2), PartPose.offset(0.0F, -6.0F, 0.0F));
        PartDefinition hood1 = root.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(40, 12).addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8), PartPose.offset(0.0F, -6.0F, 0.0F));
        hood1.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(36, 28).addBox(-3.5F, -8.7F, 2.0F, 7, 7, 3), PartPose.rotation(-0.2268928F, 0.0F, 0.0F));
        hood1.addOrReplaceChild("hood3", CubeListBuilder.create().texOffs(22, 19).addBox(-3.0F, -9.0F, 2.5F, 6, 6, 3), PartPose.rotation(-0.3490659F, 0.0F, 0.0F));
        hood1.addOrReplaceChild("hood4", CubeListBuilder.create().texOffs(40, 4).addBox(-2.5F, -9.7F, 3.5F, 5, 5, 3), PartPose.rotation(-0.5759587F, 0.0F, 0.0F));
        root.addOrReplaceChild("backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8, 11, 2), PartPose.offset(0.0F, -6.0F, 0.0F));
        root.addOrReplaceChild("shoulderplate_top_r", CubeListBuilder.create().texOffs(110, 37).addBox(-5.5F, -2.5F, -3.5F, 2, 1, 7),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, SHOULDERPLATE_TILT_Y, SHOULDERPLATE_TILT_Z));
        root.addOrReplaceChild("shoulderplate_r1", CubeListBuilder.create().texOffs(110, 45).addBox(3.5F, -1.5F, -3.5F, 1, 4, 7),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, -SHOULDERPLATE_TILT_Y, -SHOULDERPLATE_TILT_Z));
        root.addOrReplaceChild("shoulderplate_r2", CubeListBuilder.create().texOffs(94, 45).addBox(-3.5F, 1.5F, -3.5F, 1, 3, 7),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, SHOULDERPLATE_TILT_Y, SHOULDERPLATE_TILT_Z));
        root.addOrReplaceChild("shoulderplate_r3", CubeListBuilder.create().texOffs(94, 45).addBox(-2.5F, 3.5F, -3.5F, 1, 3, 7),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, SHOULDERPLATE_TILT_Y, SHOULDERPLATE_TILT_Z));
        root.addOrReplaceChild("shoulder_r", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5, 5, 5),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, SHOULDER_TILT_Y, SHOULDER_TILT_Z));
        PartDefinition armL1 = root.addOrReplaceChild("arm_l1", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, 2.5F, -1.5F, 4, 10, 5),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, ARM_BASE_TILT, -ARM_TILT_Y, -ARM_TILT_Z));
        armL1.addOrReplaceChild("arm_l2", CubeListBuilder.create().texOffs(76, 28).addBox(-1.0F, 9.5F, 3.5F, 4, 3, 3), PartPose.ZERO);
        armL1.addOrReplaceChild("arm_l3", CubeListBuilder.create().texOffs(76, 23).addBox(-1.0F, 6.5F, 3.5F, 4, 3, 2), PartPose.ZERO);
        PartDefinition armR1 = root.addOrReplaceChild("arm_r1", CubeListBuilder.create().texOffs(72, 8).addBox(-3.0F, 2.5F, -1.5F, 4, 10, 5),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, ARM_BASE_TILT, ARM_TILT_Y, ARM_TILT_Z));
        armR1.addOrReplaceChild("arm_r2", CubeListBuilder.create().texOffs(76, 28).addBox(-3.0F, 9.5F, 3.5F, 4, 3, 3), PartPose.ZERO);
        armR1.addOrReplaceChild("arm_r3", CubeListBuilder.create().texOffs(76, 23).addBox(-3.0F, 6.5F, 3.5F, 4, 3, 2), PartPose.ZERO);
        root.addOrReplaceChild("shoulder_l", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-1.5F, -2.5F, -2.5F, 5, 5, 5),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, -SHOULDER_TILT_Y, -SHOULDER_TILT_Z));
        root.addOrReplaceChild("shoulderplate_top_l", CubeListBuilder.create().texOffs(110, 37).addBox(3.5F, -2.5F, -3.5F, 2, 1, 7),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, -SHOULDERPLATE_TILT_Y, -SHOULDERPLATE_TILT_Z));
        root.addOrReplaceChild("shoulderplate_l1", CubeListBuilder.create().texOffs(110, 45).addBox(-4.5F, -1.5F, -3.5F, 1, 4, 7),
                PartPose.offsetAndRotation(-5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, SHOULDERPLATE_TILT_Y, SHOULDERPLATE_TILT_Z));
        root.addOrReplaceChild("shoulderplate_l2", CubeListBuilder.create().texOffs(94, 45).addBox(2.5F, 1.5F, -3.5F, 1, 3, 7),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, -SHOULDERPLATE_TILT_Y, -SHOULDERPLATE_TILT_Z));
        root.addOrReplaceChild("shoulderplate_l3", CubeListBuilder.create().texOffs(94, 45).addBox(1.5F, 3.5F, -3.5F, 1, 3, 7),
                PartPose.offsetAndRotation(5.0F, -4.0F, 0.0F, SHOULDERPLATE_TILT_X, -SHOULDERPLATE_TILT_Y, -SHOULDERPLATE_TILT_Z));
        root.addOrReplaceChild("legpanel_r4", CubeListBuilder.create().texOffs(0, 43).addBox(-3.0F, 0.5F, -3.5F, 2, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanel_r5", CubeListBuilder.create().texOffs(0, 47).addBox(-3.0F, 2.5F, -2.5F, 2, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanel_r6", CubeListBuilder.create().texOffs(6, 43).addBox(-3.0F, 4.5F, -1.5F, 2, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, -LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanel_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 0.5F, 2.5F, 5, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanel_r2", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 2.5F, 1.5F, 5, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanel_r3", CubeListBuilder.create().texOffs(0, 18).addBox(-3.0F, 4.5F, 0.5F, 5, 3, 1), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanel_l3", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 4.5F, 0.5F, 5, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanel_l4", CubeListBuilder.create().texOffs(0, 43).addBox(1.0F, 0.5F, -3.5F, 2, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanel_l5", CubeListBuilder.create().texOffs(0, 47).addBox(1.0F, 2.5F, -2.5F, 2, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("legpanel_l6", CubeListBuilder.create().texOffs(6, 43).addBox(1.0F, 4.5F, -1.5F, 2, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, -LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanel_l1", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 0.5F, 2.5F, 5, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("backpanel_l2", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 2.5F, 1.5F, 5, 3, 1), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, LEGPANEL_TILT, 0.0F, 0.0F));
        root.addOrReplaceChild("sidepanel_l1", CubeListBuilder.create().texOffs(0, 22).addBox(1.5F, 0.5F, -2.5F, 1, 4, 5), PartPose.offsetAndRotation(2.0F, 6.0F, 0.0F, 0.0F, 0.0F, -LEGPANEL_TILT));
        root.addOrReplaceChild("sidepanel_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.5F, 0.5F, -2.5F, 1, 4, 5), PartPose.offsetAndRotation(-2.0F, 6.0F, 0.0F, 0.0F, 0.0F, LEGPANEL_TILT));
        PartDefinition sidepanelR2 = root.addOrReplaceChild("sidepanel_r2", CubeListBuilder.create().texOffs(0, 54).addBox(0.0F, 0.0F, -0.5F, 1, 5, 5),
                PartPose.offsetAndRotation(-4.5F, 9.5F, -2.0F, 0.0F, 0.0F, 0.122173F));
        PartDefinition sidepanelR3 = sidepanelR2.addOrReplaceChild("sidepanel_r3", CubeListBuilder.create().texOffs(0, 35).addBox(0.0F, 0.0F, -0.5F, 1, 3, 5),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.296706F));
        sidepanelR3.addOrReplaceChild("sidepanel_r4", CubeListBuilder.create().texOffs(24, 35).addBox(0.0F, 0.0F, -0.5F, 1, 3, 5),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, (float) (Math.PI / 6)));
        PartDefinition sidepanelL2 = root.addOrReplaceChild("sidepanel_l2", CubeListBuilder.create().texOffs(0, 54).addBox(0.0F, 0.0F, -0.5F, 1, 5, 5),
                PartPose.offsetAndRotation(4.5F, 9.5F, -2.0F, 0.0F, 0.0F, -0.122173F));
        PartDefinition sidepanelL3 = sidepanelL2.addOrReplaceChild("sidepanel_l3", CubeListBuilder.create().texOffs(0, 35).addBox(0.0F, 0.0F, -0.5F, 1, 3, 5),
                PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, -0.296706F));
        sidepanelL3.addOrReplaceChild("sidepanel_l4", CubeListBuilder.create().texOffs(24, 35).addBox(0.0F, 0.0F, -0.5F, 1, 3, 5),
                PartPose.offsetAndRotation(0.0F, 3.0F, 0.0F, 0.0F, 0.0F, (float) (-Math.PI / 6)));
        PartDefinition legpanelC1 = root.addOrReplaceChild("legpanel_c1", CubeListBuilder.create().texOffs(16, 45).addBox(-3.0F, 0.0F, -0.5F, 6, 8, 1), PartPose.offset(0.0F, 5.5F, -3.0F));
        PartDefinition legpanelC2 = legpanelC1.addOrReplaceChild("legpanel_c2", CubeListBuilder.create().texOffs(16, 54).addBox(-3.0F, 0.0F, -0.5F, 6, 4, 1), PartPose.offset(0.0F, 8.0F, 0.0F));
        legpanelC2.addOrReplaceChild("legpanel_c3", CubeListBuilder.create().texOffs(32, 59).addBox(-3.0F, 0.0F, -0.5F, 6, 4, 1), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition cloak1 = root.addOrReplaceChild("cloak1", CubeListBuilder.create().texOffs(106, 0).addBox(0.0F, 0.0F, -0.5F, 10, 18, 1), PartPose.offset(-5.0F, -6.0F, 4.0F));
        PartDefinition cloak2 = cloak1.addOrReplaceChild("cloak2", CubeListBuilder.create().texOffs(106, 19).addBox(0.0F, 0.0F, -0.5F, 10, 4, 1), PartPose.offset(0.0F, 18.0F, 0.0F));
        cloak2.addOrReplaceChild("cloak3", CubeListBuilder.create().texOffs(106, 24).addBox(0.0F, 0.0F, -0.5F, 10, 4, 1), PartPose.offset(0.0F, 4.0F, 0.0F));
        return LayerDefinition.create(mesh, TEX_WIDTH, TEX_HEIGHT);
    }

    @Override
    public void setupAnim(EldritchGuardianRenderState state) {
        super.setupAnim(state);
        float ticks = state.ageInTicks;
        this.hood.yRot = state.yRot * (float) (Math.PI / 180.0);
        this.hood.xRot = state.xRot * (float) (Math.PI / 180.0);
        this.armL.xRot = -1.0F - state.armLiftL + Mth.sin((ticks + 20.0F) / 10.0F) * 0.08F;
        this.armR.xRot = -1.0F - state.armLiftR + Mth.sin(ticks / 10.0F) * 0.08F;
        this.legpanelC1.xRot = -0.15F + Mth.sin(ticks / 8.0F) * 0.12F;
        this.legpanelC2.xRot = Mth.sin((ticks - 5.0F) / 8.0F) * 0.13F;
        this.legpanelC3.xRot = Mth.sin((ticks - 10.0F) / 8.0F) * 0.14F;
        this.cloak1.xRot = 0.2F + Mth.sin(ticks / 7.0F) * 0.08F;
        this.cloak2.xRot = Mth.sin((ticks - 5.0F) / 7.0F) * 0.1F;
        this.cloak3.xRot = Mth.sin((ticks - 10.0F) / 7.0F) * 0.12F;
        this.sidepanelL2.zRot = -0.2F + Mth.sin((ticks + 10.0F) / 8.0F) * 0.12F;
        this.sidepanelL3.zRot = Mth.sin((ticks + 5.0F) / 8.0F) * 0.13F;
        this.sidepanelL4.zRot = Mth.sin(ticks / 8.0F) * 0.14F;
        this.sidepanelR2.zRot = 0.2F + Mth.sin((ticks - 5.0F) / 8.0F) * 0.12F;
        this.sidepanelR3.zRot = Mth.sin((ticks - 10.0F) / 8.0F) * 0.13F;
        this.sidepanelR4.zRot = Mth.sin((ticks - 15.0F) / 8.0F) * 0.14F;
    }
}
