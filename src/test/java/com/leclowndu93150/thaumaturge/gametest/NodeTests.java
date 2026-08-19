package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeGenerator;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;

public final class NodeTests {
    private static final BlockPos NODE_POS = new BlockPos(2, 2, 3);
    private static final int VITIUM_AMOUNT = 30;
    private static final int REFILL_WAIT_TICKS = 120;
    private static final float REFILL_VIS = 300.0F;

    private NodeTests() {}

    public static void register(TCTestRegistrar r) {
        r.add("node/unbreakable_by_hand", 20, helper -> {
            BlockEntityNode node = placeVitiumNode(helper);
            if (node == null) {
                return;
            }
            BlockPos pos = helper.absolutePos(NODE_POS);
            float speed = helper.getLevel().getBlockState(pos).getDestroySpeed(helper.getLevel(), pos);
            if (speed >= 0.0F) {
                helper.fail("Node block is breakable (destroy speed " + speed + "); TC nodes are unbreakable");
                return;
            }
            helper.succeed();
        });

        r.add("node/energize_decomposes_to_primals", 40, helper -> {
            BlockEntityNode node = placeVitiumNode(helper);
            if (node == null) {
                return;
            }
            node.setEnergized(true);
            for (AspectInstance entry : node.getAspects().entries()) {
                if (!entry.aspect().value().isPrimal()) {
                    helper.fail("Energized node still holds compound aspect " + entry.aspect().unwrapKey().orElseThrow().identifier());
                    return;
                }
            }
            AspectList original = node.getAspectsBaseOriginal();
            if (original == null || original.isEmpty()) {
                helper.fail("Energized node did not preserve its original base");
                return;
            }
            if (original.amountOf(vitium(helper)) != VITIUM_AMOUNT) {
                helper.fail("Preserved base lost the vitium amount: " + original);
                return;
            }
            helper.succeed();
        });

        r.add("node/deenergize_restores_original", 40, helper -> {
            BlockEntityNode node = placeVitiumNode(helper);
            if (node == null) {
                return;
            }
            node.setEnergized(true);
            node.setEnergized(false);
            if (node.getAspectsBase().amountOf(vitium(helper)) != VITIUM_AMOUNT) {
                helper.fail("De-energized node did not restore its vitium base: " + node.getAspectsBase());
                return;
            }
            if (node.getAspectsBaseOriginal() != null) {
                helper.fail("Original base snapshot should clear after restore");
                return;
            }
            helper.succeed();
        });

        r.add("node/energized_refills_from_aura", 200, helper -> {
            BlockEntityNode node = placeVitiumNode(helper);
            if (node == null) {
                return;
            }
            node.setEnergized(true);
            for (AspectInstance entry : List.copyOf(node.getAspects().entries())) {
                node.takeFromContainer(entry.aspect(), entry.amount());
            }
            if (node.getAspects().totalAmount() != 0) {
                helper.fail("Failed to deplete the node before the refill check");
                return;
            }
            AuraHelper.addVis(helper.getLevel(), helper.absolutePos(NODE_POS), REFILL_VIS);
            helper.runAfterDelay(REFILL_WAIT_TICKS, () -> {
                if (node.getAspects().totalAmount() <= 0) {
                    helper.fail("Energized node did not refill from the aura within " + REFILL_WAIT_TICKS + " ticks");
                    return;
                }
                helper.succeed();
            });
        });

        r.add("node/tainted_energized_needs_flux", 400, helper -> {
            BlockEntityNode node = placeNode(helper, NodeType.TAINTED);
            if (node == null) {
                return;
            }
            node.setEnergized(true);
            for (AspectInstance entry : List.copyOf(node.getAspects().entries())) {
                node.takeFromContainer(entry.aspect(), entry.amount());
            }
            AuraHelper.addVis(helper.getLevel(), helper.absolutePos(NODE_POS), REFILL_VIS);
            helper.runAfterDelay(REFILL_WAIT_TICKS, () -> {
                if (node.getAspects().totalAmount() > 0) {
                    helper.fail("Tainted energized node refilled from vis; it should require flux");
                    return;
                }
                AuraHelper.addFlux(helper.getLevel(), helper.absolutePos(NODE_POS), REFILL_VIS);
                helper.runAfterDelay(REFILL_WAIT_TICKS, () -> {
                    if (node.getAspects().totalAmount() <= 0) {
                        helper.fail("Tainted energized node did not refill from flux");
                        return;
                    }
                    helper.succeed();
                });
            });
        });
    }

    private static Holder<IAspect> vitium(GameTestHelper helper) {
        return resolve(helper, TCAspects.VITIUM);
    }

    private static Holder<IAspect> resolve(GameTestHelper helper, ResourceKey<IAspect> key) {
        Holder<IAspect> holder = Aspects.resolve(helper.getLevel().registryAccess(), key);
        if (holder == null) {
            throw new IllegalStateException("Aspect " + key + " missing from the registry");
        }
        return holder;
    }

    private static BlockEntityNode placeVitiumNode(GameTestHelper helper) {
        return placeNode(helper, NodeType.NORMAL);
    }

    private static BlockEntityNode placeNode(GameTestHelper helper, NodeType type) {
        AspectList aspects = AspectList.EMPTY.add(vitium(helper), VITIUM_AMOUNT);
        BlockPos pos = helper.absolutePos(NODE_POS);
        if (!NodeGenerator.createNodeAt(helper.getLevel(), pos, type, null, aspects)) {
            helper.fail("NodeGenerator.createNodeAt failed");
            return null;
        }
        if (!(helper.getLevel().getBlockEntity(pos) instanceof BlockEntityNode node)) {
            helper.fail("No node block entity after createNodeAt");
            return null;
        }
        return node;
    }
}
