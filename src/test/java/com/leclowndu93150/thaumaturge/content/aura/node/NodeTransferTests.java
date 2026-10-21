package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.nodes.CapturedNode;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehaviorState;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSnapshot;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.api.nodes.INodeCaptureItem;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeResult;
import com.leclowndu93150.thaumaturge.api.nodes.NodeTransfers;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestPlayers;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

@EventBusSubscriber(modid = TTIds.MODID)
public final class NodeTransferTests {
    private NodeTransferTests() {}

    @SubscribeEvent
    public static void registerItems(RegisterEvent event) {
        if (Boolean.getBoolean("thaumaturge.gametest")) {
            event.register(Registries.ITEM, registry -> registry.register(TTIds.rl("test_node_vessel"), new TestVessel()));
        }
    }

    private static final class TestVessel extends Item implements INodeCaptureItem {
        private TestVessel() {
            super(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, TTIds.rl("test_node_vessel"))).stacksTo(1));
        }
    }

    public static void register(TTTestRegistrar registrar) {
        registrar.add("node_transfer/pending_release_requires_persisted_identity", 20, helper -> {
            BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
            helper.getLevel().setBlockAndUpdate(pos, TTBlocks.NODE.get().defaultBlockState());
            BlockEntityNode node = (BlockEntityNode) helper.getLevel().getBlockEntity(pos);
            CapturedNode captured = new CapturedNode(CapturedNode.VERSION, UUID.randomUUID(), node.snapshot());
            GlobalPos target = GlobalPos.of(helper.getLevel().dimension(), pos);
            try {
                NodeTransferJournal.write(helper.getLevel(), new NodeTransferJournal.Entry(captured, UUID.randomUUID(), target, Optional.of(target), false));
                node.restoreSnapshot(captured.snapshot());
                if (NodeTransferOperations.reconcile(node) || NodeTransferJournal.read(helper.getLevel(), captured.snapshot().identity()).orElseThrow().settled()) {
                    helper.fail("A fresh placement settled an uncheckpointed release");
                    return;
                }
                TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, helper.getLevel().registryAccess());
                node.saveAdditional(output);
                node.loadAdditional(TagValueInput.create(ProblemReporter.DISCARDING, helper.getLevel().registryAccess(), output.buildResult()));
                if (!NodeTransferOperations.reconcile(node) || !NodeTransferJournal.read(helper.getLevel(), captured.snapshot().identity()).orElseThrow().settled()) {
                    helper.fail("A persisted target did not settle the recovered release");
                    return;
                }
            } catch (IOException exception) {
                helper.fail("Node transfer journal failed: " + exception.getMessage());
                return;
            }
            helper.succeed();
        });
        registrar.add("node_transfer/capture_release_replay_and_occupied_target", 40, helper -> {
            BlockPos source = helper.absolutePos(new BlockPos(2, 2, 2));
            BlockPos target = helper.absolutePos(new BlockPos(3, 2, 2));
            helper.getLevel().setBlockAndUpdate(source, TTBlocks.NODE.get().defaultBlockState());
            BlockEntityNode node = (BlockEntityNode) helper.getLevel().getBlockEntity(source);
            var player = TTTestPlayers.create(helper);
            player.getAbilities().mayBuild = true;
            ItemStack vessel = new ItemStack(BuiltInRegistries.ITEM.getValue(TTIds.rl("test_node_vessel")));
            player.getInventory().setItem(0, vessel);
            NodeOperationContext context = NodeOperationContext.player(player, source, TTIds.rl("test_transfer"));
            NodeSnapshot expected = node.snapshot();
            player.getAbilities().mayBuild = false;
            if (NodeTransfers.capture(context, expected, 0) == NodeResult.SUCCESS || helper.getLevel().getBlockEntity(source) != node) {
                helper.fail("Denied capture removed the source node");
                return;
            }
            player.getAbilities().mayBuild = true;
            vessel.setCount(2);
            if (NodeTransfers.capture(context, expected, 0) == NodeResult.SUCCESS || vessel.has(TTDataComponents.CAPTURED_NODE)) {
                helper.fail("Invalid inventory slot accepted a captured node");
                return;
            }
            vessel.setCount(1);
            if (NodeTransfers.capture(context, expected, 0) != NodeResult.SUCCESS || !helper.getLevel().getBlockState(source).isAir()) {
                helper.fail("Capture did not move the node into the vessel");
                return;
            }
            ItemStack duplicate = vessel.copy();
            helper.getLevel().setBlockAndUpdate(target, Blocks.STONE.defaultBlockState());
            if (NodeTransfers.release(context, 0, target) == NodeResult.SUCCESS || !vessel.has(TTDataComponents.CAPTURED_NODE)) {
                helper.fail("Occupied target consumed the captured node");
                return;
            }
            helper.getLevel().setBlockAndUpdate(target, Blocks.AIR.defaultBlockState());
            if (NodeTransfers.release(context, 0, target) != NodeResult.SUCCESS || vessel.has(TTDataComponents.CAPTURED_NODE)) {
                helper.fail("Release failed to consume one captured node");
                return;
            }
            BlockEntityNode released = (BlockEntityNode) helper.getLevel().getBlockEntity(target);
            if (!released.snapshot().identity().equals(expected.identity())) {
                helper.fail("Release changed the node identity");
                return;
            }
            player.getInventory().setItem(0, duplicate);
            if (NodeTransfers.release(context, 0, source) == NodeResult.SUCCESS || !helper.getLevel().getBlockState(source).isAir()) {
                helper.fail("A copied carrier released the node twice");
                return;
            }
            helper.succeed();
        });
        registrar.add("node_transfer/codec_preserves_unknown_behavior", 20, helper -> {
            CapturedNode captured = captured();
            var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
            var encoded = CapturedNode.CODEC.encodeStart(ops, captured).getOrThrow();
            CapturedNode decoded = CapturedNode.CODEC.parse(ops, encoded).getOrThrow();
            if (!decoded.equals(captured)) {
                helper.fail("Captured node codec changed identity, revision or unknown behavior state");
                return;
            }
            encoded.getAsJsonObject().addProperty("version", 2);
            if (CapturedNode.CODEC.parse(ops, encoded).result().isPresent()) {
                helper.fail("Captured node codec accepted an unknown version");
                return;
            }
            helper.succeed();
        });
        registrar.add("node_transfer/codec_rejects_oversized_behavior", 20, helper -> {
            var ops = helper.getLevel().registryAccess().createSerializationContext(JsonOps.INSTANCE);
            var encoded = CapturedNode.CODEC.encodeStart(ops, captured()).getOrThrow();
            encoded.getAsJsonObject().getAsJsonObject("node").getAsJsonObject("behavior").addProperty("data", "x".repeat(NodeBehaviorState.MAX_DATA_BYTES + 1));
            if (CapturedNode.CODEC.parse(ops, encoded).result().isPresent()) {
                helper.fail("Captured node codec accepted oversized behavior state");
                return;
            }
            helper.succeed();
        });
        registrar.add("node_transfer/journal_reload_suppresses_source", 20, helper -> {
            BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
            helper.getLevel().setBlockAndUpdate(pos, TTBlocks.NODE.get().defaultBlockState());
            BlockEntityNode node = (BlockEntityNode) helper.getLevel().getBlockEntity(pos);
            CapturedNode captured = new CapturedNode(CapturedNode.VERSION, UUID.randomUUID(), node.snapshot());
            NodeTransferJournal.Entry entry = new NodeTransferJournal.Entry(captured, UUID.randomUUID(), GlobalPos.of(helper.getLevel().dimension(), pos), Optional.empty(), true);
            try {
                NodeTransferJournal.write(helper.getLevel(), entry);
                if (!NodeTransferJournal.read(helper.getLevel(), node.snapshot().identity()).orElseThrow().equals(entry)) {
                    helper.fail("Durable node transfer changed after reloading the journal");
                    return;
                }
                if (NodeTransferOperations.reconcile(node) || !helper.getLevel().getBlockState(pos).isAir()) {
                    helper.fail("Captured node remained active after journal recovery");
                    return;
                }
            } catch (IOException exception) {
                helper.fail("Node transfer journal failed: " + exception.getMessage());
                return;
            }
            helper.succeed();
        });
        registrar.add("node_transfer/settled_reload_preserves_later_changes", 20, helper -> {
            BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
            helper.getLevel().setBlockAndUpdate(pos, TTBlocks.NODE.get().defaultBlockState());
            BlockEntityNode node = (BlockEntityNode) helper.getLevel().getBlockEntity(pos);
            CapturedNode captured = new CapturedNode(CapturedNode.VERSION, UUID.randomUUID(), node.snapshot());
            GlobalPos location = GlobalPos.of(helper.getLevel().dimension(), pos);
            try {
                NodeTransferJournal.write(helper.getLevel(), new NodeTransferJournal.Entry(captured, UUID.randomUUID(), location, Optional.of(location), true));
                node.reclassify(NodeType.PURE);
                if (!NodeTransferOperations.reconcile(node) || node.kind() != NodeType.PURE) {
                    helper.fail("Settled transfer restored an obsolete node snapshot");
                    return;
                }
            } catch (IOException exception) {
                helper.fail("Node transfer journal failed: " + exception.getMessage());
                return;
            }
            helper.succeed();
        });
    }

    private static CapturedNode captured() {
        NodeSnapshot snapshot = new NodeSnapshot(UUID.randomUUID(), 7, AspectList.EMPTY, AspectList.EMPTY, Optional.empty(), NodeType.NORMAL, Optional.empty(), false, false, false,
                Optional.of(new NodeBehaviorState(TTIds.rl("missing_behavior"), 3, "{\"growth\":17}")));
        return new CapturedNode(CapturedNode.VERSION, UUID.randomUUID(), snapshot);
    }
}
