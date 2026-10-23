package com.leclowndu93150.thaumaturge.client.model.gear;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;

public final class RangerArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;
    private static final String HELMET = "helmet";
    private static final String MASK = "mask";
    private static final String HIP = "hip";
    private static final String TABARD_FRONT = "tabard_front";
    private static final String TABARD_FRONT_HEM = "tabard_front_hem";
    private static final String TABARD_BACK = "tabard_back";
    private static final String TABARD_BACK_HEM = "tabard_back_hem";
    private static final PartPose MASK_POSE = PartPose.offsetAndRotation(-0.5F, 2.0F, 0.5F, 0.0873F, 0.0F, 0.0F);

    private final ModelPart frontTabard;
    private final ModelPart frontTabardHem;
    private final ModelPart backTabard;
    private final ModelPart backTabardHem;

    public RangerArmorModel(ModelPart root) {
        super(root);
        ModelPart hip = body.getChild(HIP);
        frontTabard = hip.getChild(TABARD_FRONT);
        frontTabardHem = frontTabard.getChild(TABARD_FRONT_HEM);
        backTabard = hip.getChild(TABARD_BACK);
        backTabardHem = backTabard.getChild(TABARD_BACK_HEM);
    }

    @Override
    public void setupAnim(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        if (entity instanceof ArmorStand) {
            return;
        }
        ArmorCloth.Motion motion = ArmorCloth.sample(entity, limbSwing, limbSwingAmount, ageInTicks);
        float forwardLeg = Math.min(ArmorCloth.forwardSwing(rightLeg, body), ArmorCloth.forwardSwing(leftLeg, body));
        float backwardLeg = Math.max(ArmorCloth.backwardSwing(rightLeg, body), ArmorCloth.backwardSwing(leftLeg, body));
        ArmorCloth.swayFront(frontTabard, frontTabardHem, body, forwardLeg, motion.flutter(), motion);
        ArmorCloth.swayBack(backTabard, backTabardHem, backwardLeg, ArmorCloth.FLIPPED, motion);
    }

    public static LayerDefinition createHead(boolean captain) {
        MeshDefinition mesh = createMesh();
        PartDefinition head = mesh.getRoot().getChild("head");
        PartDefinition helmet = head.addOrReplaceChild(
                HELMET,
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-5.0F, -9.0F, -5.0F, 10.0F, 6.0F, 10.0F, new CubeDeformation(-0.5F))
                        .texOffs(6, 32)
                        .addBox(-5.5F, -5.25F, -6.5F, 11.0F, 2.0F, 3.0F, new CubeDeformation(-0.5F)),
                PartPose.offset(0.0F, -1.0F, 0.0F));
        helmet.addOrReplaceChild(
                "right_guard",
                CubeListBuilder.create()
                        .texOffs(8, 16)
                        .mirror()
                        .addBox(-0.5F, -2.5F, -1.0F, 2.0F, 6.0F, 10.0F, new CubeDeformation(-0.5F))
                        .mirror(false),
                PartPose.offsetAndRotation(4.5F, -1.5F, -4.0F, 0.0F, 0.0F, -0.2618F));
        helmet.addOrReplaceChild(
                "neck_guard",
                CubeListBuilder.create()
                        .texOffs(8, 16)
                        .addBox(-1.5F, -2.5F, -1.0F, 2.0F, 6.0F, 10.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(4.0F, -1.75F, 5.0F, 1.5708F, -1.309F, -1.5708F));
        helmet.addOrReplaceChild(
                "left_guard",
                CubeListBuilder.create()
                        .texOffs(8, 16)
                        .addBox(-1.5F, -2.5F, -1.0F, 2.0F, 6.0F, 10.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-4.5F, -1.5F, -4.0F, 0.0F, 0.0F, 0.2618F));
        if (captain) {
            addPlume(helmet);
        } else {
            addEars(helmet);
        }
        PartDefinition mask =
                helmet.addOrReplaceChild(MASK, CubeListBuilder.create(), PartPose.offset(4.5F, -4.0F, -5.5F));
        mask.addOrReplaceChild(
                "visor",
                CubeListBuilder.create()
                        .texOffs(101, 28)
                        .addBox(-9.0F, -3.0F, 0.0F, 10.0F, 7.0F, 2.0F, new CubeDeformation(-0.51F)),
                MASK_POSE);
        if (captain) {
            mask.addOrReplaceChild(
                    "goggles",
                    CubeListBuilder.create()
                            .texOffs(176, 0)
                            .addBox(-8.0F, -1.5F, -0.5F, 4.0F, 4.0F, 1.0F)
                            .texOffs(176, 0)
                            .addBox(-4.0F, -1.5F, -0.5F, 4.0F, 4.0F, 1.0F),
                    MASK_POSE);
        }
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createChest(boolean captain) {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition chestplate = root.getChild("body")
                .addOrReplaceChild(
                        "chestplate",
                        CubeListBuilder.create()
                                .texOffs(0, 67)
                                .addBox(-4.0F, -8.0F, -8.0F, 8.0F, 8.0F, 2.0F, new CubeDeformation(-0.25F))
                                .texOffs(0, 83)
                                .addBox(-4.0F, -8.0F, -2.0F, 8.0F, 10.0F, 2.0F, new CubeDeformation(-0.25F))
                                .texOffs(0, 77)
                                .addBox(-4.0F, -1.5F, -8.25F, 8.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F))
                                .texOffs(22, 79)
                                .addBox(-5.0F, -3.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F))
                                .texOffs(22, 79)
                                .addBox(-5.0F, -8.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F)),
                        PartPose.offset(0.0F, 9.0F, 4.0F));
        CubeListBuilder bedroll = CubeListBuilder.create()
                .texOffs(78, 8)
                .addBox(-13.0F, 1.0F, -1.0F, 16.0F, 7.0F, 7.0F, new CubeDeformation(-0.25F));
        if (captain) {
            chestplate.addOrReplaceChild(
                    "bedroll", bedroll, PartPose.offsetAndRotation(5.0F, 1.0F, 0.0F, 0.0F, 0.1309F, 1.7017F));
            chestplate.addOrReplaceChild(
                    "banner",
                    CubeListBuilder.create()
                            .texOffs(129, 2)
                            .addBox(-1.0F, -25.0F, -1.0F, 2.0F, 33.0F, 2.0F, new CubeDeformation(-0.25F))
                            .texOffs(123, 60)
                            .addBox(-1.5F, -24.0F, -0.75F, 3.0F, 27.0F, 14.0F, new CubeDeformation(-1.0F))
                            .texOffs(117, 43)
                            .addBox(-1.0F, -24.0F, -2.0F, 2.0F, 2.0F, 15.0F, new CubeDeformation(-0.24F)),
                    PartPose.offsetAndRotation(-5.0F, -7.0F, 0.0F, -0.0873F, 0.0F, 0.0F));
        } else {
            chestplate.addOrReplaceChild(
                    "bedroll", bedroll, PartPose.offsetAndRotation(5.0F, -10.0F, -1.0F, 0.1309F, 0.0F, 0.0F));
        }
        PartDefinition rightShoulder = root.getChild("right_arm")
                .addOrReplaceChild(
                        "right_shoulder",
                        CubeListBuilder.create()
                                .texOffs(50, 62)
                                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)),
                        PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition rightGuard = rightShoulder.addOrReplaceChild(
                "right_shoulder_guard",
                CubeListBuilder.create()
                        .texOffs(41, 16)
                        .addBox(-3.25F, 0.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                        .texOffs(41, 16)
                        .addBox(-2.75F, 2.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.51F)),
                PartPose.offsetAndRotation(0.5F, -0.25F, 0.0F, 0.0F, 0.0F, 0.1745F));
        rightGuard.addOrReplaceChild(
                "right_pauldron",
                CubeListBuilder.create()
                        .texOffs(61, 81)
                        .addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-2.0F, -0.5F, 0.0F, 0.0F, 0.0F, -0.3491F));
        PartDefinition leftShoulder = root.getChild("left_arm")
                .addOrReplaceChild(
                        "left_shoulder",
                        CubeListBuilder.create()
                                .texOffs(50, 62)
                                .mirror()
                                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F))
                                .mirror(false),
                        PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition leftGuard = leftShoulder.addOrReplaceChild(
                "left_shoulder_guard",
                CubeListBuilder.create()
                        .texOffs(41, 16)
                        .mirror()
                        .addBox(1.25F, 0.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                        .mirror(false)
                        .texOffs(41, 16)
                        .mirror()
                        .addBox(0.75F, 2.25F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.51F))
                        .mirror(false),
                PartPose.offsetAndRotation(-0.5F, -0.25F, 0.0F, 0.0F, 0.0F, -0.1745F));
        leftGuard.addOrReplaceChild(
                "left_pauldron",
                CubeListBuilder.create()
                        .texOffs(61, 81)
                        .mirror()
                        .addBox(-3.0F, -2.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                        .mirror(false),
                PartPose.offsetAndRotation(2.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.3491F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition hip = root.getChild("body")
                .addOrReplaceChild(
                        HIP,
                        CubeListBuilder.create()
                                .texOffs(88, 80)
                                .addBox(-5.0F, 9.5F, -3.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F)),
                        PartPose.ZERO);
        hip.addOrReplaceChild(
                "scabbard",
                CubeListBuilder.create()
                        .texOffs(57, 29)
                        .addBox(-2.0F, -6.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(-1.2F)),
                PartPose.offsetAndRotation(5.0F, 11.0F, 0.0F, -1.4835F, -0.2618F, 1.5708F));
        addTabard(
                hip,
                TABARD_FRONT,
                TABARD_FRONT_HEM,
                PartPose.offsetAndRotation(0.0F, 11.25F, -3.0F, -0.0873F, 0.0F, 0.0F));
        addTabard(
                hip,
                TABARD_BACK,
                TABARD_BACK_HEM,
                PartPose.offsetAndRotation(0.0F, 10.75F, 3.0F, -0.0873F, 3.1416F, 0.0F));
        PartDefinition rightPant =
                root.getChild("right_leg").addOrReplaceChild("right_pant", CubeListBuilder.create(), PartPose.ZERO);
        rightPant.addOrReplaceChild(
                "right_cuff",
                CubeListBuilder.create()
                        .texOffs(60, 5)
                        .addBox(-7.0F, -6.0F, -1.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(2.3065F, 6.4992F, -2.0F, 0.0F, 0.0F, 0.1745F));
        rightPant.addOrReplaceChild(
                "right_shin",
                CubeListBuilder.create()
                        .texOffs(60, 15)
                        .addBox(-3.1F, 2.75F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(-0.51F)),
                PartPose.rotation(0.0F, 0.0F, 0.1745F));
        PartDefinition pouch = rightPant.addOrReplaceChild(
                "pouch",
                CubeListBuilder.create()
                        .texOffs(76, 46)
                        .addBox(-3.0F, -4.25F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-4.0F, 4.0F, -1.75F, 0.0F, 0.0F, 0.1745F));
        pouch.addOrReplaceChild(
                "pouch_knot",
                CubeListBuilder.create()
                        .texOffs(78, 32)
                        .addBox(-1.5F, -2.0F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.6F)),
                PartPose.offsetAndRotation(-1.0F, -4.0F, 1.0F, 0.0F, -0.7854F, 0.0F));
        pouch.addOrReplaceChild(
                "pouch_flap",
                CubeListBuilder.create()
                        .texOffs(76, 39)
                        .addBox(-2.0F, -1.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-1.15F, -5.25F, 1.0F, 0.0F, 0.0F, -0.1745F));
        PartDefinition leftPant =
                root.getChild("left_leg").addOrReplaceChild("left_pant", CubeListBuilder.create(), PartPose.ZERO);
        leftPant.addOrReplaceChild(
                "left_cuff",
                CubeListBuilder.create()
                        .texOffs(60, 5)
                        .mirror()
                        .addBox(5.0F, -6.0F, -1.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                        .mirror(false),
                PartPose.offsetAndRotation(-2.3065F, 6.4992F, -2.0F, 0.0F, 0.0F, -0.1745F));
        leftPant.addOrReplaceChild(
                "left_shin",
                CubeListBuilder.create()
                        .texOffs(60, 15)
                        .mirror()
                        .addBox(1.1F, 2.75F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(-0.51F))
                        .mirror(false),
                PartPose.rotation(0.0F, 0.0F, -0.1745F));
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static void addEars(PartDefinition helmet) {
        PartDefinition ears =
                helmet.addOrReplaceChild("ears", CubeListBuilder.create(), PartPose.offset(4.0F, -3.0F, -4.0F));
        ears.addOrReplaceChild(
                "left_ear",
                CubeListBuilder.create()
                        .texOffs(22, 21)
                        .addBox(-2.5F, -2.5F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-9.0F, 1.5F, -0.5F, 0.0F, -0.6981F, 0.2618F));
        ears.addOrReplaceChild(
                "right_ear",
                CubeListBuilder.create()
                        .texOffs(22, 21)
                        .mirror()
                        .addBox(-0.5F, -2.5F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F))
                        .mirror(false),
                PartPose.offsetAndRotation(1.0F, 1.5F, -0.5F, 0.0F, 0.6981F, -0.2618F));
    }

    private static void addPlume(PartDefinition helmet) {
        PartDefinition plume = helmet.addOrReplaceChild(
                "plume",
                CubeListBuilder.create()
                        .texOffs(44, 31)
                        .addBox(-1.0F, -6.0F, -1.0F, 2.0F, 4.0F, 3.0F, CubeDeformation.NONE),
                PartPose.offset(0.0F, -8.0F, 0.0F));
        plume.addOrReplaceChild(
                "plume_tip",
                CubeListBuilder.create()
                        .texOffs(44, 31)
                        .addBox(-1.0F, -4.0F, -1.0F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.01F)),
                PartPose.offsetAndRotation(0.5F, -5.5F, 1.0F, -0.3491F, 0.0F, 0.0F));
        plume.addOrReplaceChild(
                "plume_feather",
                CubeListBuilder.create()
                        .texOffs(43, 42)
                        .addBox(1.0F, -10.0F, -1.0F, 0.0F, 10.0F, 5.0F, CubeDeformation.NONE),
                PartPose.offsetAndRotation(-1.0F, -2.0F, 0.0F, -0.48F, 0.0F, 0.0F));
        plume.addOrReplaceChild(
                "plume_socket",
                CubeListBuilder.create()
                        .texOffs(54, 4)
                        .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, CubeDeformation.NONE),
                PartPose.rotation(0.0F, -0.7418F, 0.0F));
    }

    private static void addTabard(PartDefinition hip, String upper, String hem, PartPose pose) {
        PartDefinition tabard = hip.addOrReplaceChild(
                upper,
                CubeListBuilder.create()
                        .texOffs(42, 30)
                        .addBox(-3.0F, -0.25F, -0.5F, 6.0F, 7.0F, 1.0F, new CubeDeformation(-0.2F)),
                pose);
        tabard.addOrReplaceChild(
                hem,
                CubeListBuilder.create()
                        .texOffs(42, 38)
                        .addBox(-3.0F, -0.25F, -0.5F, 6.0F, 3.0F, 1.0F, new CubeDeformation(-0.2F))
                        .texOffs(43, 42)
                        .addBox(-2.5F, 2.25F, -0.5F, 5.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, -0.0873F, 0.0F, 0.0F));
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition hip = KnightArmorModel.emptyChild(mesh.getRoot().getChild("body"), HIP);
        KnightArmorModel.emptyChild(KnightArmorModel.emptyChild(hip, TABARD_FRONT), TABARD_FRONT_HEM);
        KnightArmorModel.emptyChild(KnightArmorModel.emptyChild(hip, TABARD_BACK), TABARD_BACK_HEM);
        return mesh;
    }
}
