package com.leclowndu93150.thaumaturge.content.warp.roll;

import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.api.warp.WarpStrengthEvent;
import com.leclowndu93150.thaumaturge.content.equipment.FortressArmorItem;
import com.leclowndu93150.thaumaturge.content.warp.PlayerWarpState;
import com.leclowndu93150.thaumaturge.content.warp.WarpGear;
import com.leclowndu93150.thaumaturge.content.warp.WarpLedger;
import com.leclowndu93150.thaumaturge.network.ClientboundWarpFXPayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

public final class WarpCheck {
    private static final int DECAY_PER_CHECK = 1;
    private static final int GRINNING_DEVIL_MASK = 0;
    private static final int MASK_MIN_PENALTY = 2;
    private static final int MASK_MAX_PENALTY = 5;
    private static final int NO_PENALTY = 0;

    private WarpCheck() {}

    public static void run(ServerPlayer player) {
        if (!player.level().getServer().isSameThread()) {
            throw new IllegalStateException("Warp checks require the server thread");
        }
        if (!player.isAlive() || player.isRemoved()) {
            return;
        }
        WarpReading reading = decayAndRead(player);
        RandomSource random = player.getRandom();
        if (!fires(reading, random)) {
            return;
        }
        WarpStrengthEvent strength = NeoForge.EVENT_BUS.post(new WarpStrengthEvent(player, strength(player, reading, random)));
        PacketDistributor.sendToPlayer(player, ClientboundWarpFXPayload.heartbeat());
        WarpLedger.state(player).setCounter(reading.counterAfterEvent());
        player.syncData(TTAttachments.WARP);
        unleash(player, reading, random, strength.strength());
        WarpMilestones.reach(player, reading.actual());
    }

    private static WarpReading decayAndRead(ServerPlayer player) {
        WarpLedger.change(player, -DECAY_PER_CHECK, WarpType.TEMPORARY);
        PlayerWarpState state = WarpLedger.state(player);
        return WarpReading.of(state, WarpGear.worn(player));
    }

    private static boolean fires(WarpReading reading, RandomSource random) {
        return reading.primed() && random.nextDouble() < reading.fireChance();
    }

    private static int strength(ServerPlayer player, WarpReading reading, RandomSource random) {
        int penalty = maskPenalty(player, random);
        return random.nextInt(reading.effective()) + reading.gear() - penalty;
    }

    private static int maskPenalty(ServerPlayer player, RandomSource random) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        boolean devil = head.getItem() instanceof FortressArmorItem && FortressArmorItem.mask(head) == GRINNING_DEVIL_MASK;
        return devil ? random.nextIntBetweenInclusive(MASK_MIN_PENALTY, MASK_MAX_PENALTY) : NO_PENALTY;
    }

    private static void unleash(ServerPlayer player, WarpReading reading, RandomSource random, int strength) {
        if (strength <= 0) {
            return;
        }
        WarpRoll roll = new WarpRoll(player, player.level(), random, reading.effective(), reading.normal());
        WarpOutcomeTable.standard().select(strength).ifPresent(outcome -> outcome.apply(roll));
    }
}
