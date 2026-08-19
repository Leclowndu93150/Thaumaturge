package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.content.aura.relay.BlockEntityVisRelay;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;

public final class RelayTests {
    private static final BlockPos NODE_POS = new BlockPos(1, 2, 1);
    private static final BlockPos RELAY_POS = new BlockPos(1, 2, 4);
    private static final BlockPos RELAY_CHAIN_POS = new BlockPos(3, 2, 6);
    private static final BlockPos CONSUMER_POS = new BlockPos(4, 2, 6);
    private static final int IGNIS_AMOUNT = 30;
    private static final int ACCRUE_WAIT_TICKS = 80;

    private RelayTests() {}

    public static void register(TCTestRegistrar r) {
        r.add("relay/links_and_drains", 300, helper -> {
            BlockEntityNode node = placeEnergizedNode(helper);
            BlockEntityVisRelay relay = placeRelay(helper, RELAY_POS);
            if (node == null || relay == null) {
                return;
            }
            relay.refreshLink(helper.getLevel());
            if (!relay.isLinked() || relay.depth() != 1) {
                helper.fail("Relay did not link to the energized node (depth " + relay.depth() + ")");
                return;
            }
            if (!helper.absolutePos(NODE_POS).equals(relay.parentPos())) {
                helper.fail("Relay linked to " + relay.parentPos() + " instead of the node");
                return;
            }
            helper.runAfterDelay(ACCRUE_WAIT_TICKS, () -> {
                int before = node.getAspects().totalAmount();
                int drained = VisRelayHelper.drainCentivis(helper.getLevel(), helper.absolutePos(CONSUMER_POS), TCAspects.IGNIS, 100, false);
                if (drained <= 0) {
                    helper.fail("Nothing drained through the relay after " + ACCRUE_WAIT_TICKS + " ticks of accrual");
                    return;
                }
                if (node.getAspects().totalAmount() >= before) {
                    helper.fail("Drain did not consume the source node's stored aspects");
                    return;
                }
                helper.succeed();
            });
        });

        r.add("relay/chains_through_parent_relay", 60, helper -> {
            BlockEntityNode node = placeEnergizedNode(helper);
            BlockEntityVisRelay first = placeRelay(helper, RELAY_POS);
            BlockEntityVisRelay second = placeRelay(helper, RELAY_CHAIN_POS);
            if (node == null || first == null || second == null) {
                return;
            }
            first.refreshLink(helper.getLevel());
            if (!first.isLinked()) {
                helper.fail("First relay failed to link to the node");
                return;
            }
            helper.getLevel().removeBlock(helper.absolutePos(NODE_POS), false);
            second.refreshLink(helper.getLevel());
            if (second.depth() != 2 || !helper.absolutePos(RELAY_POS).equals(second.parentPos())) {
                helper.fail("Second relay did not chain through the first (depth " + second.depth() + ", parent " + second.parentPos() + ")");
                return;
            }
            helper.succeed();
        });

        r.add("relay/unlinks_when_deenergized", 60, helper -> {
            BlockEntityNode node = placeEnergizedNode(helper);
            BlockEntityVisRelay relay = placeRelay(helper, RELAY_POS);
            if (node == null || relay == null) {
                return;
            }
            relay.refreshLink(helper.getLevel());
            if (!relay.isLinked()) {
                helper.fail("Relay failed to link before the de-energize check");
                return;
            }
            node.setEnergized(false);
            relay.refreshLink(helper.getLevel());
            if (relay.isLinked()) {
                helper.fail("Relay stayed linked to a de-energized node");
                return;
            }
            helper.succeed();
        });
    }

    private static Holder<IAspect> ignis(GameTestHelper helper) {
        Holder<IAspect> holder = Aspects.resolve(helper.getLevel().registryAccess(), TCAspects.IGNIS);
        if (holder == null) {
            throw new IllegalStateException("Ignis missing from the aspect registry");
        }
        return holder;
    }

    private static BlockEntityNode placeEnergizedNode(GameTestHelper helper) {
        AspectList aspects = AspectList.EMPTY.add(ignis(helper), IGNIS_AMOUNT);
        BlockPos pos = helper.absolutePos(NODE_POS);
        if (!NodeGenerator.createNodeAt(helper.getLevel(), pos, NodeType.NORMAL, null, aspects)) {
            helper.fail("NodeGenerator.createNodeAt failed");
            return null;
        }
        if (!(helper.getLevel().getBlockEntity(pos) instanceof BlockEntityNode node)) {
            helper.fail("No node block entity after createNodeAt");
            return null;
        }
        node.setEnergized(true);
        return node;
    }

    private static BlockEntityVisRelay placeRelay(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, TCBlocks.VIS_RELAY.get().defaultBlockState());
        BlockEntityVisRelay relay = helper.getBlockEntity(pos, BlockEntityVisRelay.class);
        if (relay == null) {
            helper.fail("Vis relay block entity missing after placement");
        }
        return relay;
    }
}
