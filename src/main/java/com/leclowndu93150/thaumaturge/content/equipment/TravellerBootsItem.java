package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class TravellerBootsItem extends Item implements IRechargable {
    private static final int MAX_CHARGE = 240;
    private static final int ENERGY_PER_CHARGE = 60;
    private static final int ENERGY_INTERVAL_TICKS = 20;
    private static final float GROUND_BOOST = 0.05F;
    private static final float JUMP_BOOST = 0.275F;
    private static final float WATER_AIR_BOOST = 0.025F;
    private static final float STEP_HEIGHT_BONUS = 0.4F;
    private static final AttributeModifier STEP_MODIFIER = new AttributeModifier(Identifier.fromNamespaceAndPath(TCIds.MODID, "traveller_step"), STEP_HEIGHT_BONUS,
            AttributeModifier.Operation.ADD_VALUE);

    private static final AttributeModifier JUMP_MODIFIER = new AttributeModifier(Identifier.fromNamespaceAndPath(TCIds.MODID, "traveller_jump"), JUMP_BOOST, AttributeModifier.Operation.ADD_VALUE);

    public TravellerBootsItem(Properties properties) {
        super(properties);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, entity, slot);
        if (slot != EquipmentSlot.FEET || !(entity instanceof ServerPlayer player)) {
            return;
        }
        if (player.tickCount % ENERGY_INTERVAL_TICKS == 0) {
            int energy = stack.getOrDefault(TCDataComponents.ENERGY.get(), 0);
            if (energy > 0) {
                energy--;
            } else if (RechargeAccess.consumeCharge(stack, player, 1)) {
                energy = ENERGY_PER_CHARGE;
            }
            stack.set(TCDataComponents.ENERGY.get(), energy);
        }
        boolean active = RechargeAccess.getCharge(stack) > 0 && !player.getAbilities().flying
                && (player.getLastClientInput().forward() || player.getLastClientInput().backward() || player.getLastClientInput().left() || player.getLastClientInput().right())
                && !player.isShiftKeyDown();
        AttributeInstance stepHeight = player.getAttribute(Attributes.STEP_HEIGHT);
        AttributeInstance jumpHeight = player.getAttribute(Attributes.JUMP_STRENGTH);
        if (stepHeight != null) {
            if (active && !stepHeight.hasModifier(STEP_MODIFIER.id())) {
                stepHeight.addTransientModifier(STEP_MODIFIER);
            } else if (!active && stepHeight.hasModifier(STEP_MODIFIER.id())) {
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

    public static void clientMovementTick(Player player, ItemStack stack) {
        if (RechargeAccess.getCharge(stack) <= 0 || player.getAbilities().flying || player.zza <= 0.0F) {
            return;
        }
        if (player.onGround()) {
            float bonus = GROUND_BOOST;
            if (player.isInWater()) {
                bonus /= 4.0F;
            }
            player.moveRelative(bonus, FORWARD);
        } else if (player.isInWater()) {
            player.moveRelative(WATER_AIR_BOOST, FORWARD);
        }
    }

    private static final Vec3 FORWARD = new Vec3(0.0, 0.0, 1.0);

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return MAX_CHARGE;
    }

    @Override
    public ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) {
        return ChargeDisplay.PERIODIC;
    }
}
