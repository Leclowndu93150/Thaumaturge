package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.misc.TCActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerFx;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

public final class NodeJarRitual {
    public static final Identifier RESEARCH_NODE_JAR = TCIds.rl("node_jar");

    private static final int JAR_VIS_COST_PER_PRIMAL = 70;
    private static final float MODIFIER_DEGRADE_CHANCE = 0.75F;
    private static final int SHELL_BREAK_MARGIN_TICKS = 20;
    private static final int JAR_SETTLE_DELAY_TICKS = 15;

    private NodeJarRitual() {}

    public static boolean tryJarNode(ServerLevel level, BlockPos nodePos, Player player) {
        if (!(level.getBlockEntity(nodePos) instanceof BlockEntityNode node) || node instanceof BlockEntityJarNode) {
            return false;
        }
        if (node.isJarring()) {
            return true;
        }
        if (!KnowledgeAccess.of(player).isResearchKnown(RESEARCH_NODE_JAR)) {
            TCActionBar.sendPurple(player, "tc.jar.noresearch");
            return true;
        }
        if (!fitsStructure(level, nodePos)) {
            TCActionBar.sendPurple(player, "tc.jar.structure");
            return true;
        }
        Map<ResourceKey<IAspect>, Integer> cost = new LinkedHashMap<>();
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            cost.put(primal, JAR_VIS_COST_PER_PRIMAL * WandEconomy.CENTIVIS_PER_VIS);
        }
        if (!WandVisHelper.consumeSpecificFromHotbar(player, cost, true)) {
            TCActionBar.sendPurple(player, "tc.jar.vis", JAR_VIS_COST_PER_PRIMAL);
            return true;
        }
        level.playSound(null, nodePos, TCSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        List<BlockPos> structure = structurePositions(nodePos);
        Vec3 hand = player instanceof ServerPlayer serverPlayer ? DustTriggerFx.posToHand(serverPlayer, InteractionHand.MAIN_HAND) : Vec3.atCenterOf(nodePos);
        if (player instanceof ServerPlayer) {
            for (BlockPos structurePos : structure) {
                DustTriggerFx.emitBlockSparkles(level, structurePos, hand);
            }
        }
        int lastBreak = 0;
        for (BlockPos structurePos : structure) {
            double dist = Math.sqrt(hand.distanceToSqr(Vec3.atCenterOf(structurePos)));
            int delay = (int) (dist * DustTriggerFx.SPARKLE_TICKS_PER_BLOCK) + SHELL_BREAK_MARGIN_TICKS;
            DustTriggerSwapQueue.enqueueClear(level, structurePos, level.getBlockState(structurePos), delay);
            lastBreak = Math.max(lastBreak, delay);
        }
        node.beginJarring(lastBreak + JAR_SETTLE_DELAY_TICKS);
        return true;
    }

    public static void completeJar(ServerLevel level, BlockPos nodePos, BlockEntityNode node) {
        RandomSource random = level.getRandom();
        NodeModifier modifier = node.getNodeModifier();
        if (random.nextFloat() < MODIFIER_DEGRADE_CHANCE) {
            if (modifier == null) {
                modifier = NodeModifier.PALE;
            } else if (modifier == NodeModifier.BRIGHT) {
                modifier = null;
            } else if (modifier == NodeModifier.PALE) {
                modifier = NodeModifier.FADING;
            }
        }
        NodeData data = new NodeData(node.getNodeType(), Optional.ofNullable(modifier), node.getAspects(), node.getAspectsBase());
        level.removeBlockEntity(nodePos);
        level.setBlock(nodePos, TCBlocks.JAR_NODE.get().defaultBlockState(), Block.UPDATE_ALL);
        if (level.getBlockEntity(nodePos) instanceof BlockEntityJarNode jar) {
            jar.applyNodeData(data);
            jar.setChanged();
            level.sendBlockUpdated(nodePos, jar.getBlockState(), jar.getBlockState(), Block.UPDATE_ALL);
        }
        Effects.bamf(level, nodePos).withSound().fancy().send();
    }

    public static boolean fitsStructure(Level level, BlockPos nodePos) {
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                if (!level.getBlockState(nodePos.offset(xx, 2, zz)).is(BlockTags.WOODEN_SLABS)) {
                    return false;
                }
                for (int yy = -1; yy <= 1; yy++) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    BlockState state = level.getBlockState(nodePos.offset(xx, yy, zz));
                    if (!state.is(Tags.Blocks.GLASS_BLOCKS)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static List<BlockPos> structurePositions(BlockPos nodePos) {
        List<BlockPos> positions = new ArrayList<>();
        for (int xx = -1; xx <= 1; xx++) {
            for (int zz = -1; zz <= 1; zz++) {
                positions.add(nodePos.offset(xx, 2, zz));
                for (int yy = -1; yy <= 1; yy++) {
                    if (xx == 0 && yy == 0 && zz == 0) {
                        continue;
                    }
                    positions.add(nodePos.offset(xx, yy, zz));
                }
            }
        }
        return positions;
    }
}
