package com.leclowndu93150.thaumaturge.content.equipment.hover;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
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
    private static final int HUM_INTERVAL_TICKS = 24;
    private static final float TOGGLE_VOLUME = 0.33F;
    private static final float HUM_VOLUME = 0.05F;
    private static final float HUM_PITCH_SPREAD = 0.05F;
    private static final double IDLE_FALL_DAMPING = 0.75;
    private static final double FLIGHT_GRANT = 1.0;
    private static final AttributeModifier FLIGHT_MODIFIER = new AttributeModifier(TTIds.rl("hover_flight"), FLIGHT_GRANT, AttributeModifier.Operation.ADD_VALUE);

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

    public static void toggle(ServerPlayer player) {
        ItemStack worn = wornHoverGear(player);
        if (!(worn.getItem() instanceof IHoverGear gear)) {
            return;
        }
        boolean hovering = !isHovering(player);
        if (hovering && gear.getHoverFuel(worn) <= 0) {
            return;
        }
        setHovering(player, hovering);
        playToggleSound(player, hovering);
    }

    public static void tick(ServerPlayer player, ItemStack stack, IHoverGear gear) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        boolean hovering = isHovering(player);
        if (hovering && expendCharge(player, stack, gear)) {
            keepAloft(player);
            player.resetFallDistance();
            if (player.tickCount % HUM_INTERVAL_TICKS == 0) {
                play(player, TTSounds.JACOBS.get(), HUM_VOLUME, 1.0F + player.getRandom().nextFloat() * HUM_PITCH_SPREAD);
            }
            return;
        }
        if (hovering) {
            setHovering(player, false);
            playToggleSound(player, false);
        }
        player.fallDistance *= IDLE_FALL_DAMPING;
    }

    public static void setHovering(ServerPlayer player, boolean hovering) {
        player.setData(TTAttachments.HOVERING, hovering);
        if (hovering) {
            keepAloft(player);
        } else {
            land(player);
        }
    }

    private static boolean expendCharge(ServerPlayer player, ItemStack stack, IHoverGear gear) {
        int fuel = gear.getHoverFuel(stack);
        if (fuel <= 0) {
            return false;
        }
        int charge = player.getData(TTAttachments.HOVER_CHARGE);
        if (charge < FUEL_INTERVAL_TICKS) {
            player.setData(TTAttachments.HOVER_CHARGE, charge + 1);
            return true;
        }
        player.setData(TTAttachments.HOVER_CHARGE, 0);
        gear.consumeHoverFuel(stack);
        return fuel > 1;
    }

    private static void keepAloft(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight != null && !flight.hasModifier(FLIGHT_MODIFIER.id())) {
            flight.addTransientModifier(FLIGHT_MODIFIER);
        }
        Abilities abilities = player.getAbilities();
        if (!abilities.flying && !player.onGround()) {
            abilities.flying = true;
            player.onUpdateAbilities();
        }
    }

    private static void land(ServerPlayer player) {
        AttributeInstance flight = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (flight != null) {
            flight.removeModifier(FLIGHT_MODIFIER.id());
        }
        Abilities abilities = player.getAbilities();
        if (abilities.flying && !player.mayFly()) {
            abilities.flying = false;
            player.onUpdateAbilities();
        }
    }

    private static void playToggleSound(ServerPlayer player, boolean hovering) {
        play(player, hovering ? TTSounds.HHON.get() : TTSounds.HHOFF.get(), TOGGLE_VOLUME, 1.0F);
    }

    private static void play(ServerPlayer player, SoundEvent sound, float volume, float pitch) {
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), sound, SoundSource.PLAYERS, volume, pitch);
    }
}
