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
import net.minecraft.resources.ResourceKey;
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
    private static final int PULL_REACH = 4;
    private static final int PALE_PULL_EVERY = 3;
    private static final int STEADY_PULL_EVERY = 2;
    private static final float GREEDY_GROWTH_DIVISOR = 1.5F;
    private static final int PALE_CLEARS_ODDS = 100;
    private static final int DONOR_SHRINK_ODDS = 3;
    private static final float ZAP_LOUDNESS = 0.12F;
    private static final float ZAP_LOW_PITCH = 1.0F;
    private static final float ZAP_PITCH_RISE = 0.25F;
    private static final float PULL_BOLT_WIDTH = 0.2F;
    private static final int DECAY_EVERY = 1200;
    private static final int DECAY_DROP_ODDS = 20;
    private static final int DECAY_WORSEN_ODDS = 5;
    private static final int DECAY_FADE_ODDS = 5;
    private static final int STABILITY_EVERY = 100;
    private static final int UNSTABLE_SETTLE_BASIC_ODDS = 10000;
    private static final int UNSTABLE_SETTLE_ADVANCED_ODDS = 5000;
    private static final int FADING_RECOVER_BASIC_ODDS = 12500;
    private static final int FADING_RECOVER_ADVANCED_ODDS = 6250;
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
        if (!node.allowDischarge() || node.lock == BlockEntityNode.LOCK_BASIC || !pullDue(node, random)) {
            return;
        }
        BlockPos spot = NodeRules.scatter(pos, PULL_REACH, random);
        if (spot.equals(pos) || !level.hasChunkAt(spot) || !(level.getBlockEntity(spot) instanceof BlockEntityNode donor) || !canDonate(donor, node)) {
            return;
        }
        List<AspectInstance> offered = donor.held.entries();
        Holder<IAspect> aspect = offered.get(random.nextInt(offered.size())).aspect();
        donor.held = donor.held.reduce(aspect, 1);
        absorb(node, donor, aspect, random);
        donor.refillWait = donor.refillInterval() / 2;
        donor.changed();
        if (donor.held.isEmpty() && !donor.isEnergized()) {
            donor.collapse(level, spot);
        }
        level.playSound(null, spot, TTSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_LOUDNESS, ZAP_LOW_PITCH + random.nextFloat() * ZAP_PITCH_RISE);
        Effects.boltStrike(level, Vec3.atCenterOf(spot)).to(Vec3.atCenterOf(pos)).width(PULL_BOLT_WIDTH).send();
        node.invalidateRefill();
    }

    private static boolean pullDue(BlockEntityNode node, RandomSource random) {
        NodeModifier modifier = node.trait();
        if (modifier == NodeModifier.FADING) {
            return false;
        }
        if (modifier == NodeModifier.PALE) {
            return node.tickCounter % PALE_PULL_EVERY == 0 && random.nextBoolean();
        }
        if (modifier == NodeModifier.BRIGHT || node.kind() == NodeType.HUNGRY) {
            return true;
        }
        return node.tickCounter % STEADY_PULL_EVERY == 0;
    }

    private static boolean canDonate(BlockEntityNode donor, BlockEntityNode receiver) {
        return donor.allowDischarge() && donor.lock == BlockEntityNode.LOCK_NONE && !donor.held.isEmpty() && donor.averageContent() < receiver.averageContent();
    }

    private static void absorb(BlockEntityNode node, BlockEntityNode donor, Holder<IAspect> aspect, RandomSource random) {
        int capacity = node.aspectsBase.amountOf(aspect);
        if (node.held.amountOf(aspect) < capacity) {
            node.held = node.held.add(aspect, 1);
            return;
        }
        boolean greedy = node.kind() == NodeType.HUNGRY || node.trait() == NodeModifier.BRIGHT;
        int odds = 1 + (greedy ? (int) (capacity / GREEDY_GROWTH_DIVISOR) : capacity);
        if (random.nextInt(odds) != 0) {
            return;
        }
        node.aspectsBase = node.aspectsBase.add(aspect, 1);
        if (node.trait() == NodeModifier.PALE && random.nextInt(PALE_CLEARS_ODDS) == 0) {
            node.assignTrait(null);
        }
        if (random.nextInt(DONOR_SHRINK_ODDS) == 0) {
            shrinkCapacity(donor, aspect);
        }
    }

    private static void shrinkCapacity(BlockEntityNode node, Holder<IAspect> aspect) {
        node.aspectsBase = node.aspectsBase.remove(aspect, 1);
        int capacity = node.aspectsBase.amountOf(aspect);
        int stored = node.held.amountOf(aspect);
        if (stored > capacity) {
            node.held = capacity == 0 ? node.held.without(aspect) : node.held.remove(aspect, stored - capacity);
        }
        node.invalidateRefill();
    }

    static boolean decay(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        if (node.tickCounter % DECAY_EVERY != 0) {
            return false;
        }
        boolean touched = false;
        for (AspectInstance slot : node.aspectsBase.entries()) {
            Holder<IAspect> aspect = slot.aspect();
            if (node.held.amountOf(aspect) > 0) {
                continue;
            }
            touched = true;
            int remaining = slot.amount() - 1;
            if (remaining > 0 && random.nextInt(DECAY_DROP_ODDS) != 0) {
                node.aspectsBase = node.aspectsBase.remove(aspect, 1);
                continue;
            }
            node.aspectsBase = node.aspectsBase.without(aspect);
            node.held = node.held.without(aspect);
            worsenAfterLoss(node, random);
            break;
        }
        if (!touched) {
            return false;
        }
        node.invalidateRefill();
        if (node.aspectsBase.isEmpty()) {
            node.removeDepleted(level, pos);
            return true;
        }
        return false;
    }

    private static void worsenAfterLoss(BlockEntityNode node, RandomSource random) {
        if (random.nextInt(DECAY_WORSEN_ODDS) != 0) {
            return;
        }
        NodeModifier modifier = node.trait();
        if (modifier == NodeModifier.BRIGHT) {
            node.assignTrait(null);
        } else if (modifier == null) {
            node.assignTrait(NodeModifier.PALE);
        }
        if (node.trait() == NodeModifier.PALE && random.nextInt(DECAY_FADE_ODDS) == 0) {
            node.assignTrait(NodeModifier.FADING);
        }
    }

    static void stability(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        if (node.tickCounter % STABILITY_EVERY != 0) {
            return;
        }
        boolean advanced = node.lock == BlockEntityNode.LOCK_ADVANCED;
        boolean locked = node.lock != BlockEntityNode.LOCK_NONE;
        if (node.kind() == NodeType.UNSTABLE) {
            if (!random.nextBoolean()) {
                return;
            }
            if (!locked) {
                shedPrimal(node, level, pos, random);
            } else if (random.nextInt(advanced ? UNSTABLE_SETTLE_ADVANCED_ODDS : UNSTABLE_SETTLE_BASIC_ODDS) == 0) {
                node.reclassify(NodeType.NORMAL);
                node.invalidateRefill();
            }
            return;
        }
        if (locked && node.trait() == NodeModifier.FADING && random.nextInt(advanced ? FADING_RECOVER_ADVANCED_ODDS : FADING_RECOVER_BASIC_ODDS) == 0) {
            node.assignTrait(NodeModifier.PALE);
        }
    }

    private static void shedPrimal(BlockEntityNode node, ServerLevel level, BlockPos pos, RandomSource random) {
        List<Holder<IAspect>> primals = new ArrayList<>();
        for (AspectInstance entry : node.held.entries()) {
            if (entry.aspect().value().isPrimal()) {
                primals.add(entry.aspect());
            }
        }
        if (primals.isEmpty()) {
            return;
        }
        Holder<IAspect> shed = primals.get(random.nextInt(primals.size()));
        ResourceKey<IAspect> key = shed.unwrapKey().orElse(null);
        if (key == null) {
            return;
        }
        node.held = node.held.reduce(shed, 1);
        node.changed();
        level.addFreshEntity(new EntityAspectOrb(level, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, key, 1));
    }
}
