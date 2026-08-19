package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityStabilizer;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.research.PlayerKnowledge;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class InstabilityEvents {
    private static final double EVENT_RANGE = 10.0;
    private static final int EJECT_RETRIES = 25;
    private static final int ARC_COLOR = 0x4C004C;

    private InstabilityEvents() {}

    private static final Identifier INSTABILITY_RESEARCH = TCIds.rl("instability");

    private static void grantInstabilityResearch(ServerLevel level, BlockPos matrixPos) {
        List<ServerPlayer> targets = level.getEntitiesOfClass(ServerPlayer.class, new AABB(matrixPos).inflate(EVENT_RANGE));
        for (ServerPlayer player : targets) {
            PlayerKnowledge knowledge = (PlayerKnowledge) KnowledgeAccess.of(player);
            if (!knowledge.isResearchKnown(INSTABILITY_RESEARCH)) {
                knowledge.addResearch(INSTABILITY_RESEARCH);
                knowledge.markComplete(INSTABILITY_RESEARCH);
                knowledge.sync(player);
                player.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("got.instability").withStyle(ChatFormatting.DARK_PURPLE)));
            }
        }
    }

    public static void trigger(ServerLevel level, BlockPos matrixPos, List<BlockPos> pedestals) {
        grantInstabilityResearch(level, matrixPos);
        RandomSource rand = level.getRandom();
        switch (rand.nextInt(24)) {
            case 0, 1, 2, 3 -> ejectItem(level, matrixPos, pedestals, EjectKind.DROP);
            case 4, 5, 6 -> warp(level, matrixPos);
            case 7, 8, 9 -> zap(level, matrixPos, false);
            case 10, 11 -> zap(level, matrixPos, true);
            case 12, 13 -> ejectItem(level, matrixPos, pedestals, EjectKind.DROP_GOO);
            case 14, 15 -> ejectItem(level, matrixPos, pedestals, EjectKind.DROP_POLLUTE);
            case 16 -> ejectItem(level, matrixPos, pedestals, EjectKind.DELETE_GOO);
            case 17 -> ejectItem(level, matrixPos, pedestals, EjectKind.DELETE_POLLUTE);
            case 18, 19 -> harm(level, matrixPos, false);
            case 20, 21 -> ejectItem(level, matrixPos, pedestals, EjectKind.DROP_EXPLODE);
            case 22 -> harm(level, matrixPos, true);
            case 23 -> level.explode(null, matrixPos.getX() + 0.5, matrixPos.getY() + 0.5, matrixPos.getZ() + 0.5, 1.5F + rand.nextFloat(), Level.ExplosionInteraction.NONE);
        }
    }

    private enum EjectKind {
        DROP, DROP_GOO, DROP_POLLUTE, DELETE_GOO, DELETE_POLLUTE, DROP_EXPLODE;

        boolean deletes() {
            return this == DELETE_GOO || this == DELETE_POLLUTE;
        }
    }

    private static void ejectItem(ServerLevel level, BlockPos matrixPos, List<BlockPos> pedestals, EjectKind kind) {
        RandomSource rand = level.getRandom();
        for (int retries = 0; retries < EJECT_RETRIES && !pedestals.isEmpty(); retries++) {
            BlockPos pedestalPos = pedestals.get(rand.nextInt(pedestals.size()));
            if (!(level.getBlockEntity(pedestalPos) instanceof BlockEntityPedestal pedestal) || pedestal.getItem().isEmpty()) {
                continue;
            }
            BlockPos mitigatorPos = pedestal.findInstabilityMitigator();
            if (mitigatorPos != null && level.getBlockEntity(mitigatorPos) instanceof BlockEntityStabilizer stabilizer && stabilizer.mitigate(Mth.nextInt(rand, 5, 10))) {
                return;
            }
            if (kind.deletes()) {
                pedestal.setItem(ItemStack.EMPTY);
            } else {
                Containers.dropItemStack(level, pedestalPos.getX() + 0.5, pedestalPos.getY() + 1.0, pedestalPos.getZ() + 0.5, pedestal.getItem());
                pedestal.setItem(ItemStack.EMPTY);
            }
            switch (kind) {
                case DROP_GOO, DELETE_GOO -> {
                    level.setBlockAndUpdate(pedestalPos.above(), TCBlocks.FLUX_GOO.get().defaultBlockState());
                    level.playSound(null, pedestalPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 0.3F, 1.0F);
                }
                case DROP_POLLUTE, DELETE_POLLUTE -> AuraHelper.polluteAura(level, pedestalPos, 5 + rand.nextInt(5), true);
                case DROP_EXPLODE -> level.explode(null, pedestalPos.getX() + 0.5, pedestalPos.getY() + 0.5, pedestalPos.getZ() + 0.5, 1.0F, Level.ExplosionInteraction.NONE);
                default -> {
                }
            }
            EffectDispatch.spawnArc(level, Vec3.atCenterOf(matrixPos), Vec3.atCenterOf(pedestalPos.above()), ARC_COLOR, 0.0F);
            return;
        }
    }

    private static void zap(ServerLevel level, BlockPos matrixPos, boolean all) {
        RandomSource rand = level.getRandom();
        for (LivingEntity target : nearbyLiving(level, matrixPos)) {
            EffectDispatch.spawnArc(level, Vec3.atCenterOf(matrixPos), target.position().add(0.0, target.getBbHeight() / 2.0, 0.0), ARC_COLOR, 0.0F);
            target.hurt(level.damageSources().magic(), 4 + rand.nextInt(4));
            if (!all) {
                return;
            }
        }
    }

    private static void harm(ServerLevel level, BlockPos matrixPos, boolean all) {
        RandomSource rand = level.getRandom();
        for (LivingEntity target : nearbyLiving(level, matrixPos)) {
            if (rand.nextBoolean()) {
                target.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, 120, 0, false, true));
            } else {
                target.addEffect(new MobEffectInstance(TCMobEffects.VIS_EXHAUST, 2400, 0, true, true));
            }
            if (!all) {
                return;
            }
        }
    }

    private static void warp(ServerLevel level, BlockPos matrixPos) {
        List<Player> players = level.getEntitiesOfClass(Player.class, range(matrixPos));
        if (players.isEmpty()) {
            return;
        }
        Player target = players.get(level.getRandom().nextInt(players.size()));
        if (!(target instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (level.getRandom().nextFloat() < 0.25F) {
            WarpHelper.addWarp(serverPlayer, 1, WarpType.NORMAL);
        } else {
            WarpHelper.addWarp(serverPlayer, 2 + level.getRandom().nextInt(4), WarpType.TEMPORARY);
        }
    }

    private static List<LivingEntity> nearbyLiving(ServerLevel level, BlockPos matrixPos) {
        return level.getEntitiesOfClass(LivingEntity.class, range(matrixPos));
    }

    private static AABB range(BlockPos matrixPos) {
        return new AABB(matrixPos).inflate(EVENT_RANGE);
    }
}
