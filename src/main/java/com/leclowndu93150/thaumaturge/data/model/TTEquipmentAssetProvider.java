package com.leclowndu93150.thaumaturge.data.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.client.data.models.EquipmentAssetProvider;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.equipment.EquipmentAsset;

public final class TTEquipmentAssetProvider extends EquipmentAssetProvider {
    private static final int ROBE_UNDYED_COLOR = 0x6A3880;
    private static final String CLOTH_SUFFIX = "_cloth";

    public TTEquipmentAssetProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void registerModels(BiConsumer<ResourceKey<EquipmentAsset>, EquipmentClientInfo> output) {
        output.accept(TTMaterials.ASSET_THAUMIUM, armor("thaumium"));
        output.accept(TTMaterials.ASSET_TRAVELLER, armor("traveller"));
        output.accept(TTMaterials.ASSET_VOID, armor("void"));
        output.accept(TTMaterials.ASSET_FORTRESS, armor("fortress"));
        output.accept(TTMaterials.ASSET_CULTIST_PLATE, dyeableCloth("cultist_plate"));
        output.accept(TTMaterials.ASSET_CULTIST_RANGER, dyeableCloth("cultist_ranger"));
        output.accept(TTMaterials.ASSET_CULTIST_CAPTAIN, humanoidLayers(plain("cultist_captain"), dyedCloth("cultist_captain")));
        output.accept(TTMaterials.ASSET_ZOMBIE_PLATE, armor("zombie_plate"));
        output.accept(TTMaterials.ASSET_CULTIST_ROBE, dyeableCloth("cultist_robe"));
        output.accept(TTMaterials.ASSET_CULTIST_LEADER, dyeableCloth("cultist_leader"));
        output.accept(TTMaterials.ASSET_CULTIST_BOOTS, humanoidOnly("cultist_boots"));
        output.accept(TTMaterials.ASSET_THAUMOSTATIC_HARNESS, humanoidOnly("thaumostatic_harness"));
        output.accept(TTItems.GOGGLES_REVEALING_ASSET, humanoidOnly("goggles_revealing"));
        output.accept(TTMaterials.ASSET_ROBES, armorLayers(dyed("robes"), plain("robes_overlay")));
        output.accept(TTMaterials.ASSET_VOID_ROBE, armorLayers(dyed("void_robe_overlay"), plain("void_robe")));
    }

    private static EquipmentClientInfo.Layer plain(String texture) {
        return new EquipmentClientInfo.Layer(TTIds.rl(texture));
    }

    private static EquipmentClientInfo.Layer dyed(String texture) {
        return new EquipmentClientInfo.Layer(TTIds.rl(texture), Optional.of(new EquipmentClientInfo.Dyeable(Optional.of(ROBE_UNDYED_COLOR))), false);
    }

    private static EquipmentClientInfo.Layer dyedCloth(String texture) {
        return new EquipmentClientInfo.Layer(TTIds.rl(texture + CLOTH_SUFFIX), Optional.of(new EquipmentClientInfo.Dyeable(Optional.empty())), false);
    }

    private static EquipmentClientInfo humanoidOnly(String texture) {
        return humanoidLayers(plain(texture));
    }

    private static EquipmentClientInfo humanoidLayers(EquipmentClientInfo.Layer... layers) {
        return EquipmentClientInfo.builder().addLayers(EquipmentClientInfo.LayerType.HUMANOID, layers).build();
    }

    private static EquipmentClientInfo dyeableCloth(String texture) {
        return armorLayers(plain(texture), dyedCloth(texture));
    }

    private static EquipmentClientInfo armor(String texture) {
        return armorLayers(plain(texture));
    }

    private static EquipmentClientInfo armorLayers(EquipmentClientInfo.Layer... layers) {
        return EquipmentClientInfo.builder().addLayers(EquipmentClientInfo.LayerType.HUMANOID, layers).addLayers(EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS, layers).build();
    }
}
