package com.leclowndu93150.thaumaturge.content.equipment.hover;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.mixin.server.network.ServerGamePacketListenerImplAccessor;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForgeMod;

public final class HoverManager {
    public static final int FUEL_INTERVAL_TICKS = 360;

    private static final Identifier FLIGHT_MODIFIER_ID = TTIds.rl("hover_flight");
    private static final double FLIGHT_MODIFIER_VALUE = 1.0;
    private static final float TOGGLE_VOLUME = 0.33F;
    private static final float TOGGLE_PITCH = 1.0F;
    private static final int HUM_INTERVAL_TICKS = 24;
    private static final float HUM_VOLUME = 0.04F;
    private static final float HUM_PITCH_BASE = 1.0F;
    private static final float HUM_PITCH_SPREAD = 0.15F;
    private static final double FALL_DECAY = 0.75;
    private static final int NO_COUNT = 0;

    private HoverManager() {}

    public static boolean isHovering(Player player) {
        return player.getData(TTAttachments.HOVERING);
    }

    public static ItemStack wornHoverGear(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (chest.getItem() instanceof IHoverGear) {
            return chest;
        }
        if (ModList.get().isLoaded(TTIds.CURIOS)) {
            return ThaumaturgeCuriosCompat.findCurio(player, stack -> stack.getItem() instanceof IHoverGear);
        }
        return ItemStack.EMPTY;
    }

    public static boolean isWearingHoverGear(Player player) {
        return !wornHoverGear(player).isEmpty();
    }

    public static void tick(ServerPlayer player, ItemStack stack, IHoverGear gear) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (!isHovering(player)) {
            player.fallDistance *= FALL_DECAY;
            return;
        }
        player.resetFallDistance();
        ((ServerGamePacketListenerImplAccessor) player.connection).setAboveGroundTickCount(0);
        restoreFlightAttribute(player);
        burnFuel(player, stack, gear);
        if (gear.getHoverFuel(stack) <= 0) {
            setHovering(player, false);
            playToggleSound(player, TTSounds.HHOFF.get());
            return;
        }
        if (player.tickCount % HUM_INTERVAL_TICKS == 0) {
            float pitch = HUM_PITCH_BASE + (player.getRandom().nextFloat() - player.getRandom().nextFloat()) * HUM_PITCH_SPREAD;
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), TTSounds.JACOBS.get(), SoundSource.PLAYERS, HUM_VOLUME, pitch);
        }
    }

    private static void burnFuel(ServerPlayer player, ItemStack stack, IHoverGear gear) {
        int elapsed = player.getData(TTAttachments.HOVER_CHARGE) + 1;
        if (elapsed >= FUEL_INTERVAL_TICKS) {
            gear.consumeHoverFuel(stack);
            elapsed = NO_COUNT;
        }
        player.setData(TTAttachments.HOVER_CHARGE, elapsed);
    }

    private static void restoreFlightAttribute(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight != null && !flight.hasModifier(FLIGHT_MODIFIER_ID)) {
            applyFlight(player);
        }
    }

    private static void playToggleSound(ServerPlayer player, SoundEvent sound) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, TOGGLE_VOLUME, TOGGLE_PITCH);
    }

    public static void toggle(ServerPlayer player) {
        ItemStack gearStack = wornHoverGear(player);
        if (!(gearStack.getItem() instanceof IHoverGear gear)) {
            return;
        }
        boolean enable = !isHovering(player);
        if (enable && gear.getHoverFuel(gearStack) <= 0) {
            return;
        }
        setHovering(player, enable);
        playToggleSound(player, enable ? TTSounds.HHON.get() : TTSounds.HHOFF.get());
    }

    public static void setHovering(ServerPlayer player, boolean hovering) {
        player.setData(TTAttachments.HOVERING, hovering);
        if (hovering) {
            applyFlight(player);
        } else {
            releaseFlight(player);
        }
    }

    private static void applyFlight(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight != null && !flight.hasModifier(FLIGHT_MODIFIER_ID)) {
            flight.addTransientModifier(new AttributeModifier(FLIGHT_MODIFIER_ID, FLIGHT_MODIFIER_VALUE, AttributeModifier.Operation.ADD_VALUE));
        }
        Abilities abilities = player.getAbilities();
        if (!abilities.flying && !player.onGround()) {
            abilities.flying = true;
            player.onUpdateAbilities();
        }
    }

    private static void releaseFlight(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight != null) {
            flight.removeModifier(FLIGHT_MODIFIER_ID);
        }
        Abilities abilities = player.getAbilities();
        if (abilities.flying && !player.mayFly()) {
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }
}
