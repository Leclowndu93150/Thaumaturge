package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TravellerBootsItem extends ArmorItem {
    private static final int MAX_CHARGE = 240;
    private static final int ENERGY_PER_CHARGE = 60;
    private static final int ENERGY_INTERVAL_TICKS = 20;
    private static final float GROUND_BOOST = 0.05F;
    private static final float JUMP_BOOST = 0.275F;
    private static final float WATER_AIR_BOOST = 0.025F;
    private static final float VANILLA_AIR_CONTROL = 0.02F;
    private static final float BOOSTED_AIR_CONTROL = 0.05F;
    private static final float AIR_CONTROL_BONUS = BOOSTED_AIR_CONTROL - VANILLA_AIR_CONTROL;
    private static final float STEP_HEIGHT_BONUS = 0.4F;
    private static final AttributeModifier STEP_MODIFIER = new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "traveller_step"),
            STEP_HEIGHT_BONUS,
            AttributeModifier.Operation.ADD_VALUE);
    private static final AttributeModifier JUMP_MODIFIER = new AttributeModifier(
            ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "traveller_jump"),
            JUMP_BOOST,
            AttributeModifier.Operation.ADD_VALUE);

    public TravellerBootsItem(Properties properties) {
        super(
                TTMaterials.ARMOR_TRAVELLER,
                ArmorItem.Type.BOOTS,
                properties.component(
                        TTDataComponents.RECHARGEABLE.get(), new ChargeProfile(MAX_CHARGE, ChargeDisplay.ON_CHANGE)));
    }

    @Override
    public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (event.getSlot() != EquipmentSlot.FEET
                || !(event.getEntity() instanceof ServerPlayer player)
                || !(event.getFrom().getItem() instanceof TravellerBootsItem)
                || event.getTo().getItem() instanceof TravellerBootsItem) {
            return;
        }
        AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        AttributeInstance jumpHeight = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (stepHeight != null) stepHeight.removeModifier(STEP_MODIFIER.id());
        if (jumpHeight != null) jumpHeight.removeModifier(JUMP_MODIFIER.id());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide()
                || !(entity instanceof ServerPlayer player)
                || player.getItemBySlot(EquipmentSlot.FEET) != stack) {
            return;
        }
        if (player.tickCount % ENERGY_INTERVAL_TICKS == 0) {
            int energy = stack.getOrDefault(TTDataComponents.ENERGY.get(), 0);
            if (energy > 0) {
                energy--;
            } else if (RechargeAccess.spendVis(stack, 1)) {
                energy = ENERGY_PER_CHARGE;
            }
            stack.set(TTDataComponents.ENERGY.get(), energy);
        }
        boolean active = isActive(player, stack);
        boolean stepActive = isStepActive(player, stack);
        AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        AttributeInstance jumpHeight = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (stepHeight != null) {
            if (stepActive && !stepHeight.hasModifier(STEP_MODIFIER.id())) {
                stepHeight.addTransientModifier(STEP_MODIFIER);
            } else if (!stepActive && stepHeight.hasModifier(STEP_MODIFIER.id())) {
                stepHeight.removeModifier(STEP_MODIFIER.id());
            }
        }
        if (jumpHeight != null) {
            if (active && !jumpHeight.hasModifier(JUMP_MODIFIER.id())) {
                jumpHeight.addTransientModifier(JUMP_MODIFIER);
            } else if (!active && jumpHeight.hasModifier(JUMP_MODIFIER.id())) {
                jumpHeight.removeModifier(JUMP_MODIFIER.id());
            }
        }
    }

    static boolean isActive(Player player, ItemStack boots) {
        return RechargeAccess.storedVis(boots) > 0 && !player.getAbilities().flying && !player.isShiftKeyDown();
    }

    static boolean isStepActive(Player player, ItemStack boots) {
        return isActive(player, boots) && !player.isShiftKeyDown() && player.zza > 0.0F;
    }

    public static void clientMovementTick(Player player, ItemStack stack) {
        if (!isActive(player, stack) || player.isPassenger()) {
            return;
        }
        if (player.onGround()) {
            if (player.zza > 0.0F) {
                float bonus = player.isInWater() ? GROUND_BOOST / 4.0F : GROUND_BOOST;
                player.moveRelative(bonus, FORWARD);
            }
        } else if (player.isInWater()) {
            if (player.zza > 0.0F) {
                player.moveRelative(WATER_AIR_BOOST, FORWARD);
            }
        } else if (player.zza != 0.0F || player.xxa != 0.0F) {
            player.moveRelative(AIR_CONTROL_BONUS, new Vec3(player.xxa, 0.0, player.zza));
        }
    }

    private static final Vec3 FORWARD = new Vec3(0.0, 0.0, 1.0);
}
