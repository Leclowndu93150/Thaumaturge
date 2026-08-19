package com.leclowndu93150.thaumaturge.client.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.entity.EntityInhabitedZombie;
import com.leclowndu93150.thaumaturge.content.equipment.TCMaterials;
import java.util.Optional;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.zombie.BabyZombieModel;
import net.minecraft.client.model.monster.zombie.ZombieModel;
import net.minecraft.client.renderer.entity.AbstractZombieRenderer;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ZombieRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class InhabitedZombieRenderer extends AbstractZombieRenderer<EntityInhabitedZombie, ZombieRenderState, ZombieModel<ZombieRenderState>> {
    private static final Identifier TEXTURE = TCIds.rl("textures/entity/czombie.png");

    public InhabitedZombieRenderer(EntityRendererProvider.Context context) {
        super(context, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE)), new BabyZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_BABY)),
                ArmorModelSet.bake(ModelLayers.ZOMBIE_ARMOR, context.getModelSet(), ZombieModel::new), ArmorModelSet.bake(ModelLayers.ZOMBIE_BABY_ARMOR, context.getModelSet(), BabyZombieModel::new));
    }

    @Override
    public ZombieRenderState createRenderState() {
        return new ZombieRenderState();
    }

    @Override
    public void extractRenderState(EntityInhabitedZombie entity, ZombieRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.headEquipment = zombiePlate(state.headEquipment);
        state.chestEquipment = zombiePlate(state.chestEquipment);
        state.legsEquipment = zombiePlate(state.legsEquipment);
    }

    private static ItemStack zombiePlate(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || !equippable.assetId().map(TCMaterials.ASSET_CULTIST_PLATE::equals).orElse(false)) {
            return stack;
        }
        ItemStack copy = stack.copy();
        copy.set(DataComponents.EQUIPPABLE,
                new Equippable(equippable.slot(), equippable.equipSound(), Optional.of(TCMaterials.ASSET_ZOMBIE_PLATE), equippable.cameraOverlay(), equippable.allowedEntities(),
                        equippable.dispensable(), equippable.swappable(), equippable.damageOnHurt(), equippable.equipOnInteract(), equippable.canBeSheared(), equippable.shearingSound()));
        return copy;
    }

    @Override
    public Identifier getTextureLocation(ZombieRenderState state) {
        return TEXTURE;
    }
}
