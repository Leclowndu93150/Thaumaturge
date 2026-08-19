package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNodeTransducer;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public final class TransducerTests {
    private static final BlockPos STABILIZER_POS = new BlockPos(2, 1, 3);
    private static final BlockPos NODE_POS = new BlockPos(2, 2, 3);
    private static final BlockPos TRANSDUCER_POS = new BlockPos(2, 3, 3);
    private static final BlockPos REDSTONE_POS = new BlockPos(3, 3, 3);
    private static final int VITIUM_AMOUNT = 30;
    private static final int CHARGE_CHECK_TICKS = 60;
    private static final int FULL_CHARGE_TICKS = 1300;
    private static final int DECAY_TICKS = 1150;
    private static final int REVERT_POLL_TICKS = 20;

    private TransducerTests() {}

    public static void register(TCTestRegistrar r) {
        r.add("transducer/charges_and_drains_node", 200, helper -> {
            BlockEntityNode node = buildStack(helper);
            if (node == null) {
                return;
            }
            helper.runAfterDelay(CHARGE_CHECK_TICKS, () -> {
                BlockEntityNodeTransducer transducer = helper.getBlockEntity(TRANSDUCER_POS, BlockEntityNodeTransducer.class);
                if (transducer == null) {
                    helper.fail("Transducer block entity missing");
                    return;
                }
                if (transducer.getCount() <= 0) {
                    helper.fail("Transducer charge did not advance while powered with a stabilized node");
                    return;
                }
                if (node.getAspects().totalAmount() >= VITIUM_AMOUNT) {
                    helper.fail("Charging did not drain the node's stored aspects");
                    return;
                }
                helper.succeed();
            });
        });

        r.add("transducer/full_cycle_energize_and_revert", 2700, helper -> {
            BlockEntityNode node = buildStack(helper);
            if (node == null) {
                return;
            }
            helper.runAfterDelay(FULL_CHARGE_TICKS, () -> {
                if (!node.isEnergized()) {
                    BlockEntityNodeTransducer transducer = helper.getBlockEntity(TRANSDUCER_POS, BlockEntityNodeTransducer.class);
                    helper.fail("Node not energized after " + FULL_CHARGE_TICKS + " powered ticks; charge " + (transducer == null ? "?" : transducer.getCount()));
                    return;
                }
                for (var entry : node.getAspectsBase().entries()) {
                    if (!entry.aspect().value().isPrimal()) {
                        helper.fail("Energized base still holds a compound aspect");
                        return;
                    }
                }
                AspectList preserved = node.getAspectsBaseOriginal();
                if (preserved == null || preserved.amountOf(vitium(helper)) <= 0) {
                    helper.fail("Energized node did not preserve a vitium base snapshot");
                    return;
                }
                int preservedVitium = preserved.amountOf(vitium(helper));
                helper.setBlock(REDSTONE_POS, Blocks.AIR.defaultBlockState());
                pollRevert(helper, node, preservedVitium, DECAY_TICKS);
            });
        });
    }

    private static void pollRevert(GameTestHelper helper, BlockEntityNode node, int preservedVitium, int remainingTicks) {
        helper.runAfterDelay(REVERT_POLL_TICKS, () -> {
            if (!node.isEnergized()) {
                if (node.getAspectsBase().amountOf(vitium(helper)) != preservedVitium) {
                    helper.fail("Reverted node base does not match the preserved snapshot of " + preservedVitium + ": " + node.getAspectsBase());
                    return;
                }
                helper.succeed();
                return;
            }
            if (remainingTicks <= 0) {
                helper.fail("Node stayed energized after power loss");
                return;
            }
            pollRevert(helper, node, preservedVitium, remainingTicks - REVERT_POLL_TICKS);
        });
    }

    private static Holder<IAspect> vitium(GameTestHelper helper) {
        Holder<IAspect> holder = Aspects.resolve(helper.getLevel().registryAccess(), TCAspects.VITIUM);
        if (holder == null) {
            throw new IllegalStateException("Vitium missing from the aspect registry");
        }
        return holder;
    }

    private static BlockEntityNode buildStack(GameTestHelper helper) {
        helper.setBlock(STABILIZER_POS, TCBlocks.NODE_STABILIZER.get().defaultBlockState());
        AspectList aspects = AspectList.EMPTY.add(vitium(helper), VITIUM_AMOUNT);
        BlockPos nodePos = helper.absolutePos(NODE_POS);
        if (!NodeGenerator.createNodeAt(helper.getLevel(), nodePos, NodeType.NORMAL, null, aspects)) {
            helper.fail("NodeGenerator.createNodeAt failed");
            return null;
        }
        helper.setBlock(TRANSDUCER_POS, TCBlocks.NODE_TRANSDUCER.get().defaultBlockState());
        helper.setBlock(REDSTONE_POS, Blocks.REDSTONE_BLOCK.defaultBlockState());
        if (!(helper.getLevel().getBlockEntity(nodePos) instanceof BlockEntityNode node)) {
            helper.fail("No node block entity after createNodeAt");
            return null;
        }
        return node;
    }
}
