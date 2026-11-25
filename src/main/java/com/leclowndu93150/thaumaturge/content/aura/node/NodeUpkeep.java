package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.wands.EntityAspectOrb;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

final class NodeUpkeep {
    private static final float RAW_PER_POINT = 3.0F;
    private static final float REFILL_SUCCESS_THRESHOLD = 2.99F;
    private static final int DEGRADE_AFTER_FAILURES = 10;
    private static final int TAINT_DRIFT_ODDS = 20;
    private static final float TAINT_DRIFT_FLUX_RATIO = 0.5F;
    private static final int HEAL_ODDS = 50;
    private static final float HEAL_FLUX_RATIO = 0.1F;
    private static final float HEAL_VIS_RATIO = 0.9F;
    private static final int DISCHARGE_RANGE = 4;
    private static final int DISCHARGE_INTERVAL_FAST = 1;
    private static final int DISCHARGE_INTERVAL_PALE = 3;
    private static final int DISCHARGE_INTERVAL_DEFAULT = 2;
    private static final float ELEVATED_DIVISOR = 1.5F;
    private static final int PALE_RECOVERY_ODDS = 100;
    private static final int DONOR_BASE_LOSS_ODDS = 3;
    private static final float ZAP_VOLUME = 0.1F;
    private static final float ZAP_PITCH_BASE = 1.0F;
    private static final float ZAP_PITCH_SPREAD = 0.2F;
    private static final float DISCHARGE_BOLT_WIDTH = 0.3F;
    private static final int DECAY_INTERVAL = 1200;
    private static final int DECAY_REMOVAL_ODDS = 20;
    private static final int DECAY_MODIFIER_ODDS = 5;
    private static final int DECAY_FADE_ODDS = 5;
    private static final int STABILITY_INTERVAL = 100;
    private static final int UNSTABLE_BASIC_ODDS = 10000;
    private static final int UNSTABLE_ADVANCED_ODDS = 5000;
    private static final int FADING_BASIC_ODDS = 12500;
    private static final int FADING_ADVANCED_ODDS = 6250;
    private static final double BLOCK_CENTER = 0.5;

    private NodeUpkeep() {}

    static void refill(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        if (node.refillWait > 0) {
            node.refillWait--;
        }
        int interval = node.refillInterval();
        if (interval <= 0 || node.tickCounter % interval != 0 || node.refillWait > 0) {
            return;
        }
        driftWithChunk(node, level, pos, random);
        List<Holder<IAspect>> eligible = new ArrayList<>();
        for (AspectInstance entry : node.held.entries()) {
            if (entry.amount() < node.aspectsBase.amountOf(entry.aspect())) {
                eligible.add(entry.aspect());
            }
        }
        if (eligible.isEmpty()) {
            node.starvation = 0;
            return;
        }
        Holder<IAspect> chosen = eligible.get(random.nextInt(eligible.size()));
        boolean flux = node.kind() == NodeType.TAINTED;
        float taken = flux ? AuraHelper.drainFlux(level, pos, RAW_PER_POINT, false) : AuraHelper.drainVis(level, pos, RAW_PER_POINT, false);
        if (taken >= REFILL_SUCCESS_THRESHOLD) {
            node.held = node.held.add(chosen, 1);
            node.starvation = 0;
            node.changed();
            return;
        }
        if (taken > 0.0F) {
            if (flux) {
                AuraHelper.addFlux(level, pos, taken);
            } else {
                AuraHelper.addVis(level, pos, taken);
            }
        }
        if (++node.starvation >= DEGRADE_AFTER_FAILURES) {
            node.starvation = 0;
            degrade(node);
        }
    }

    private static void degrade(BlockEntityNode node) {
        if (node.trait() == NodeModifier.FADING) {
            if (node.kind() != NodeType.HUNGRY) {
                node.reclassify(NodeType.HUNGRY);
            }
        } else {
            node.assignTrait(NodeRules.degrade(node.trait()));
        }
        node.invalidateRefill();
    }

    private static void driftWithChunk(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        int auraBase = AuraHelper.getAuraBase(level, pos);
        if (auraBase <= 0) {
            return;
        }
        float flux = AuraHelper.getFlux(level, pos);
        NodeType type = node.kind();
        if (type != NodeType.TAINTED && type != NodeType.PURE && flux > TAINT_DRIFT_FLUX_RATIO * auraBase && random.nextInt(TAINT_DRIFT_ODDS) == 0) {
            node.reclassify(NodeType.TAINTED);
            node.invalidateRefill();
        }
        if (level.getBiome(pos).is(TTBiomes.MAGICAL_FOREST) && flux < HEAL_FLUX_RATIO * auraBase && AuraHelper.getVis(level, pos) >= HEAL_VIS_RATIO * auraBase && random.nextInt(HEAL_ODDS) == 0
                && node.trait() != NodeModifier.BRIGHT) {
            node.assignTrait(NodeRules.improve(node.trait()));
            node.invalidateRefill();
        }
    }

    static void discharge(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        NodeModifier modifier = node.trait();
        if (!node.allowDischarge() || modifier == NodeModifier.FADING || node.lock == BlockEntityNode.LOCK_BASIC) {
            return;
        }
        int interval = DISCHARGE_INTERVAL_DEFAULT;
        if (modifier == NodeModifier.BRIGHT || node.kind() == NodeType.HUNGRY && modifier != null) {
            interval = DISCHARGE_INTERVAL_FAST;
        } else if (modifier == NodeModifier.PALE) {
            interval = DISCHARGE_INTERVAL_PALE;
        }
        if (node.tickCounter % interval != 0 || modifier == NodeModifier.PALE && random.nextBoolean()) {
            return;
        }
        BlockPos partnerPos = pos.offset(dischargeOffset(random), dischargeOffset(random), dischargeOffset(random));
        if (partnerPos.equals(pos) || !level.hasChunkAt(partnerPos) || !(level.getBlockEntity(partnerPos) instanceof BlockEntityNode donor) || !donor.allowDischarge()
                || donor.lock != BlockEntityNode.LOCK_NONE || donor.held.isEmpty() || donor.averageContent() >= node.averageContent()) {
            return;
        }
        transfer(node, donor, random);
        if (donor.held.isEmpty() && !donor.isEnergized()) {
            donor.collapse(level, partnerPos);
        } else {
            donor.refillWait = donor.refillInterval() / 2;
            donor.changed();
        }
        level.playSound(null, partnerPos, TTSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_VOLUME, ZAP_PITCH_BASE + random.nextFloat() * ZAP_PITCH_SPREAD);
        Effects.boltStrike(level, Vec3.atCenterOf(partnerPos)).to(Vec3.atCenterOf(pos)).width(DISCHARGE_BOLT_WIDTH).send();
        node.invalidateRefill();
    }

    private static int dischargeOffset(RandomSource random) {
        return random.nextInt(DISCHARGE_RANGE + 1) - random.nextInt(DISCHARGE_RANGE + 1);
    }

    private static void transfer(BlockEntityNode receiver, BlockEntityNode donor, RandomSource random) {
        List<AspectInstance> offered = donor.held.entries();
        Holder<IAspect> aspect = offered.get(random.nextInt(offered.size())).aspect();
        boolean fits = receiver.held.amountOf(aspect) < receiver.aspectsBase.amountOf(aspect);
        donor.held = donor.held.remove(aspect, 1);
        if (fits) {
            receiver.held = receiver.held.add(aspect, 1);
            return;
        }
        boolean elevated = receiver.kind() == NodeType.HUNGRY || receiver.trait() == NodeModifier.BRIGHT;
        int base = receiver.aspectsBase.amountOf(aspect);
        int divisor = elevated ? 1 + (int) (base / ELEVATED_DIVISOR) : 1 + base;
        if (random.nextInt(divisor) != 0) {
            return;
        }
        receiver.aspectsBase = receiver.aspectsBase.add(aspect, 1);
        if (base == 0) {
            receiver.held = receiver.held.add(aspect, 1);
        }
        if (receiver.trait() == NodeModifier.PALE && random.nextInt(PALE_RECOVERY_ODDS) == 0) {
            receiver.assignTrait(null);
        }
        if (random.nextInt(DONOR_BASE_LOSS_ODDS) == 0) {
            donor.aspectsBase = donor.aspectsBase.remove(aspect, 1);
        }
    }

    static boolean decay(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        if (node.tickCounter % DECAY_INTERVAL != 0) {
            return false;
        }
        for (AspectInstance entry : node.aspectsBase.entries()) {
            if (node.held.amountOf(entry.aspect()) > 0) {
                continue;
            }
            decayAspect(node, entry, random);
            node.invalidateRefill();
            break;
        }
        if (node.aspectsBase.isEmpty()) {
            node.removeDepleted(level, pos);
            return true;
        }
        return false;
    }

    private static void decayAspect(BlockEntityNode node, AspectInstance entry, RandomSource random) {
        AspectList reduced = node.aspectsBase.remove(entry.aspect(), 1);
        if (reduced.amountOf(entry.aspect()) > 0 && random.nextInt(DECAY_REMOVAL_ODDS) != 0) {
            node.aspectsBase = reduced;
            return;
        }
        node.aspectsBase = node.aspectsBase.without(entry.aspect());
        node.held = node.held.without(entry.aspect());
        NodeModifier modifier = node.trait();
        if (random.nextInt(DECAY_MODIFIER_ODDS) == 0) {
            if (modifier == NodeModifier.BRIGHT) {
                modifier = null;
            } else if (modifier == null) {
                modifier = NodeModifier.PALE;
            }
        }
        if (modifier == NodeModifier.PALE && random.nextInt(DECAY_FADE_ODDS) == 0) {
            modifier = NodeModifier.FADING;
        }
        node.assignTrait(modifier);
    }

    static void stability(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        if (node.tickCounter % STABILITY_INTERVAL != 0) {
            return;
        }
        int lock = node.lock;
        if (node.kind() == NodeType.UNSTABLE) {
            if (lock == BlockEntityNode.LOCK_NONE) {
                if (random.nextBoolean()) {
                    releaseOrb(node, level, pos, random);
                }
            } else if (random.nextBoolean() && random.nextInt(lock == BlockEntityNode.LOCK_ADVANCED ? UNSTABLE_ADVANCED_ODDS : UNSTABLE_BASIC_ODDS) == 0) {
                node.reclassify(NodeType.NORMAL);
                node.invalidateRefill();
            }
        }
        if (node.trait() == NodeModifier.FADING && lock != BlockEntityNode.LOCK_NONE && random.nextInt(lock == BlockEntityNode.LOCK_ADVANCED ? FADING_ADVANCED_ODDS : FADING_BASIC_ODDS) == 0) {
            node.assignTrait(NodeModifier.PALE);
            node.invalidateRefill();
        }
    }

    private static void releaseOrb(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        List<Holder<IAspect>> primals = new ArrayList<>();
        for (AspectInstance entry : node.held.entries()) {
            if (entry.aspect().value().isPrimal()) {
                primals.add(entry.aspect());
            }
        }
        if (primals.isEmpty()) {
            return;
        }
        Holder<IAspect> chosen = primals.get(random.nextInt(primals.size()));
        node.held = node.held.remove(chosen, 1);
        level.addFreshEntity(new EntityAspectOrb(level, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, chosen.unwrapKey().orElseThrow(), 1));
        node.invalidateRefill();
    }
}
