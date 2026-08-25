package com.leclowndu93150.thaumaturge.content.equipment.runic;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class RunicShielding {
    public static final ResourceLocation MODIFIER_ID = TCIds.rl("runic_shielding");
    private static final int SCAN_INTERVAL = 20;

    private RunicShielding() {}

    public static int worn(ServerPlayer player) {
        int max = 0;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
                max += InfusionRunicAugmentRecipe.charge(player.getItemBySlot(slot));
            }
        }
        if (ModList.get().isLoaded(TCIds.CURIOS)) {
            for (ItemStack stack : ThaumaturgeCuriosCompat.equippedCurios(player)) {
                max += InfusionRunicAugmentRecipe.charge(stack);
            }
        }
        return max;
    }

    public static void tick(ServerPlayer player) {
        RunicShieldState state = player.getData(TCAttachments.RUNIC_SHIELD.get());
        if (player.tickCount % SCAN_INTERVAL == 0) {
            int max = worn(player);
            applyMaxModifier(player, max);
            if (state.maxCharge > max) {
                player.setAbsorptionAmount(Math.max(0.0F, player.getAbsorptionAmount() - (state.maxCharge - max)));
            }
            state.maxCharge = max;
        }
        if (state.maxCharge <= 0) {
            return;
        }
        ServerLevel level = player.serverLevel();
        long time = level.getGameTime();
        int charge = (int) player.getAbsorptionAmount();
        if (charge == 0 && state.lastCharge > 0) {
            state.nextCycle = time + ThaumaturgeCommonConfig.SHIELD_WAIT.get();
            state.lastCharge = 0;
        }
        if (charge < state.maxCharge
                && time >= state.nextCycle
                && !AuraHelper.shouldPreserveAura(level, player, player.blockPosition())) {
            BlockPos pos = player.blockPosition();
            double cost = ThaumaturgeCommonConfig.SHIELD_COST.get();
            if (AuraHelper.getVis(level, pos) >= cost) {
                AuraHelper.drainVis(level, pos, (float) cost, false);
                state.nextCycle = time + ThaumaturgeCommonConfig.SHIELD_RECHARGE.get();
                player.setAbsorptionAmount(charge + 1);
                state.lastCharge = charge + 1;
            }
        }
    }

    private static void applyMaxModifier(ServerPlayer player, int max) {
        AttributeInstance attribute = player.getAttribute(Attributes.MAX_ABSORPTION);
        if (attribute == null) {
            return;
        }
        AttributeModifier existing = attribute.getModifier(MODIFIER_ID);
        if (existing != null && (int) existing.amount() == max) {
            return;
        }
        attribute.removeModifier(MODIFIER_ID);
        if (max > 0) {
            attribute.addTransientModifier(
                    new AttributeModifier(MODIFIER_ID, max, AttributeModifier.Operation.ADD_VALUE));
        }
    }
}
