package com.leclowndu93150.thaumaturge.client.model.gear;

import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class FortressArmorModel extends AbstractTTArmorModel {
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 128;
    private static final int MASK_U = 42;
    private static final int[] MASK_V = {20, 11, 2};
    private static final String PART_MASK_PREFIX = "mask_";
    private static final String BROW = "brow";
    private static final String CREST = "crest";
    private static final String CHESTPLATE = "chestplate";
    private static final String HIP = "hip";
    private static final String SKIRT = "skirt";

    private final ModelPart[] masks = new ModelPart[MASK_V.length];
    private final ModelPart goggles;

    public FortressArmorModel(ModelPart root) {
        super(root);
        ModelPart mask = head.getChild(PlateHelmet.HELMET).getChild(PlateHelmet.MASK);
        for (int i = 0; i < masks.length; i++) {
            masks[i] = mask.getChild(maskName(i));
            masks[i].visible = false;
        }
        goggles = mask.getChild(PlateHelmet.GOGGLES);
        goggles.visible = false;
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
        ItemStack helmet = entity.getItemBySlot(EquipmentSlot.HEAD);
        int selected = FortressArmorItem.mask(helmet);
        for (int i = 0; i < masks.length; i++) {
            masks[i].visible = selected == i;
        }
        goggles.visible = FortressArmorItem.hasGoggles(helmet);
    }

    public static LayerDefinition createHead() {
        MeshDefinition mesh = createMesh();
        PartDefinition helmet = PlateHelmet.shell(mesh.getRoot().getChild("head"));
        PartDefinition brow = PlateHelmet.flaps(
                helmet,
                BROW,
                CubeListBuilder.create()
                        .texOffs(70, 9)
                        .addBox(-7.0F, -5.5F, -1.5F, 6.0F, 5.0F, 2.0F, new CubeDeformation(-0.4F)));
        PartDefinition crest = brow.addOrReplaceChild(
                CREST,
                CubeListBuilder.create()
                        .texOffs(69, 24)
                        .addBox(-4.0F, -1.5F, -1.25F, 8.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F))
                        .texOffs(73, 24)
                        .addBox(2.0F, -2.25F, -1.25F, 4.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
                        .texOffs(73, 24)
                        .addBox(-6.0F, -2.25F, -1.25F, 4.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)),
                PartPose.offset(-4.0F, -3.0F, 0.0F));
        crest.addOrReplaceChild(
                "crest_gem",
                CubeListBuilder.create()
                        .texOffs(71, 28)
                        .addBox(-1.0F, -1.0F, -1.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(0.0F, -1.0F, -1.5F, 0.0F, 0.0F, 0.7854F));
        crest.addOrReplaceChild(
                "crest_setting",
                CubeListBuilder.create()
                        .texOffs(69, 18)
                        .addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(0.0F, -0.25F, -1.0F, 0.0F, 0.0F, 0.7854F));
        PartDefinition mask = PlateHelmet.mask(helmet);
        for (int i = 0; i < MASK_V.length; i++) {
            PlateHelmet.visor(mask, maskName(i), MASK_U, MASK_V[i]);
        }
        PlateHelmet.goggles(mask);
        return finish(mesh);
    }

    public static LayerDefinition createChest() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition chestplate = root.getChild("body")
                .addOrReplaceChild(
                        CHESTPLATE,
                        CubeListBuilder.create()
                                .texOffs(113, 5)
                                .addBox(-4.0F, -8.0F, -8.5F, 8.0F, 8.0F, 2.0F, new CubeDeformation(-0.25F))
                                .texOffs(89, 2)
                                .addBox(-4.0F, -8.0F, -10.0F, 8.0F, 9.0F, 4.0F, new CubeDeformation(-1.3F))
                                .texOffs(113, 21)
                                .addBox(-4.0F, -8.0F, -2.0F, 8.0F, 10.0F, 2.0F, new CubeDeformation(-0.25F))
                                .texOffs(106, 38)
                                .addBox(-5.0F, -3.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F))
                                .texOffs(106, 38)
                                .addBox(-5.0F, -8.0F, -7.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.5F)),
                        PartPose.offset(0.0F, 9.0F, 4.0F));
        chestplate.addOrReplaceChild(
                "collar",
                CubeListBuilder.create()
                        .texOffs(113, 15)
                        .addBox(-5.0F, 0.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(1.0F, -0.5F, -7.25F, -0.2618F, 0.0F, 0.0F));
        chestplate.addOrReplaceChild(
                "satchel",
                CubeListBuilder.create()
                        .texOffs(48, 44)
                        .addBox(-3.0F, -4.0F, -1.5F, 6.0F, 8.0F, 3.0F, new CubeDeformation(-0.5F)),
                PartPose.offsetAndRotation(-0.25F, -3.75F, 0.75F, 0.0F, 0.0F, 0.6109F));
        PartDefinition rightShoulder = root.getChild("right_arm")
                .addOrReplaceChild(
                        "right_shoulder",
                        CubeListBuilder.create()
                                .texOffs(43, 61)
                                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F)),
                        PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition rightSleeve = rightShoulder.addOrReplaceChild(
                "right_sleeve",
                CubeListBuilder.create()
                        .texOffs(77, 61)
                        .addBox(-4.0F, 2.25F, -4.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(-1.5F))
                        .texOffs(88, 46)
                        .addBox(0.0F, 5.75F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.2F)),
                PartPose.offset(0.0F, -0.25F, 0.0F));
        rightSleeve.addOrReplaceChild(
                "right_pauldron",
                CubeListBuilder.create()
                        .texOffs(55, 32)
                        .addBox(-4.0F, -4.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.25F))
                        .texOffs(39, 32)
                        .addBox(-3.5F, -1.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.26F))
                        .texOffs(39, 32)
                        .addBox(-3.0F, 1.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.27F)),
                PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0013F, 0.0227F, 0.4343F));
        PartDefinition leftShoulder = root.getChild("left_arm")
                .addOrReplaceChild(
                        "left_shoulder",
                        CubeListBuilder.create()
                                .texOffs(43, 61)
                                .mirror()
                                .addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-1.7F))
                                .mirror(false),
                        PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition leftSleeve = leftShoulder.addOrReplaceChild(
                "left_sleeve",
                CubeListBuilder.create()
                        .texOffs(77, 61)
                        .mirror()
                        .addBox(-2.0F, 2.0F, -4.0F, 6.0F, 9.0F, 8.0F, new CubeDeformation(-1.5F))
                        .mirror(false)
                        .texOffs(88, 46)
                        .mirror()
                        .addBox(-2.0F, 5.5F, -2.5F, 2.0F, 3.0F, 5.0F, new CubeDeformation(-0.2F))
                        .mirror(false),
                PartPose.ZERO);
        leftSleeve.addOrReplaceChild(
                "left_pauldron",
                CubeListBuilder.create()
                        .texOffs(55, 32)
                        .mirror()
                        .addBox(2.0F, -4.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.25F))
                        .mirror(false)
                        .texOffs(39, 32)
                        .mirror()
                        .addBox(1.5F, -1.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.26F))
                        .mirror(false)
                        .texOffs(39, 32)
                        .mirror()
                        .addBox(1.0F, 1.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.27F))
                        .mirror(false),
                PartPose.offsetAndRotation(0.0F, -0.25F, 0.0F, -0.0013F, -0.0227F, -0.4343F));
        return finish(mesh);
    }

    public static LayerDefinition createLegs() {
        MeshDefinition mesh = createMesh();
        PartDefinition root = mesh.getRoot();
        PartDefinition hip = root.getChild("body")
                .addOrReplaceChild(
                        HIP,
                        CubeListBuilder.create()
                                .texOffs(106, 51)
                                .addBox(-5.0F, 9.5F, -3.5F, 10.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F)),
                        PartPose.ZERO);
        hip.addOrReplaceChild(
                "back_tasset",
                CubeListBuilder.create()
                        .texOffs(91, 15)
                        .addBox(-5.0F, 0.0F, -1.0F, 7.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(-1.5F, 11.75F, 3.25F, -0.2618F, 3.1416F, 0.0F));
        hip.addOrReplaceChild(
                "front_tasset",
                CubeListBuilder.create()
                        .texOffs(91, 15)
                        .addBox(-5.0F, 0.0F, -1.0F, 7.0F, 4.0F, 2.0F, new CubeDeformation(-0.2F)),
                PartPose.offsetAndRotation(1.5F, 11.75F, -2.75F, -0.2618F, 0.0F, 0.0F));
        PartDefinition skirt =
                hip.addOrReplaceChild(SKIRT, CubeListBuilder.create(), PartPose.offset(1.0F, 10.25F, -3.25F));
        skirt.addOrReplaceChild(
                "skirt_rim",
                CubeListBuilder.create()
                        .texOffs(113, 15)
                        .addBox(-5.0F, 0.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
                PartPose.rotation(-0.2618F, 0.0F, 0.0F));
        skirt.addOrReplaceChild(
                "back_skirt",
                CubeListBuilder.create()
                        .texOffs(91, 15)
                        .addBox(-5.0F, 0.0F, -1.0F, 7.0F, 4.0F, 2.0F, new CubeDeformation(-0.21F)),
                PartPose.offsetAndRotation(-2.5F, 4.5F, 6.5F, -0.2618F, 3.1416F, 0.0F));
        skirt.addOrReplaceChild(
                "front_skirt",
                CubeListBuilder.create()
                        .texOffs(91, 15)
                        .addBox(-5.0F, 0.0F, -1.0F, 7.0F, 4.0F, 2.0F, new CubeDeformation(-0.21F)),
                PartPose.offsetAndRotation(0.5F, 4.5F, 0.5F, -0.2618F, 0.0F, 0.0F));
        skirt.addOrReplaceChild(
                "scroll",
                CubeListBuilder.create()
                        .texOffs(67, 43)
                        .addBox(-2.0F, -6.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(-1.2F)),
                PartPose.offsetAndRotation(0.0F, 0.75F, 7.25F, 0.0F, 0.0F, 1.3526F));
        PartDefinition rightTasset = root.getChild("right_leg")
                .addOrReplaceChild(
                        "right_tasset",
                        CubeListBuilder.create()
                                .texOffs(93, 21)
                                .addBox(-3.6F, 0.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)),
                        PartPose.rotation(0.0F, 0.0F, 0.1745F));
        rightTasset.addOrReplaceChild(
                "right_greave",
                CubeListBuilder.create()
                        .texOffs(93, 31)
                        .addBox(-3.1F, 2.75F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(-0.51F)),
                PartPose.ZERO);
        PartDefinition leftTasset = root.getChild("left_leg")
                .addOrReplaceChild(
                        "left_tasset",
                        CubeListBuilder.create()
                                .texOffs(93, 21)
                                .mirror()
                                .addBox(1.6F, 0.0F, -3.0F, 2.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
                                .mirror(false),
                        PartPose.rotation(0.0F, 0.0F, -0.1745F));
        leftTasset.addOrReplaceChild(
                "left_greave",
                CubeListBuilder.create()
                        .texOffs(93, 31)
                        .mirror()
                        .addBox(1.1F, 2.75F, -3.0F, 2.0F, 5.0F, 6.0F, new CubeDeformation(-0.51F))
                        .mirror(false),
                PartPose.ZERO);
        return finish(mesh);
    }

    private static String maskName(int index) {
        return PART_MASK_PREFIX + index;
    }

    private static LayerDefinition finish(MeshDefinition mesh) {
        return LayerDefinition.create(mesh, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    private static MeshDefinition createMesh() {
        MeshDefinition mesh = KnightArmorModel.emptyMesh();
        PartDefinition mask = KnightArmorModel.emptyChild(
                KnightArmorModel.emptyChild(mesh.getRoot().getChild("head"), PlateHelmet.HELMET), PlateHelmet.MASK);
        for (int i = 0; i < MASK_V.length; i++) {
            mask.addOrReplaceChild(maskName(i), CubeListBuilder.create(), PartPose.ZERO);
        }
        mask.addOrReplaceChild(PlateHelmet.GOGGLES, CubeListBuilder.create(), PartPose.ZERO);
        return mesh;
    }
}
