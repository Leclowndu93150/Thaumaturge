package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.nodes.NodeChange;
import com.leclowndu93150.thaumaturge.api.nodes.NodeBehavior;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationContext;
import com.leclowndu93150.thaumaturge.api.nodes.NodeOperationEvent;
import com.leclowndu93150.thaumaturge.api.nodes.NodeResult;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSnapshot;
import com.leclowndu93150.thaumaturge.api.nodes.NodeSpec;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.Nullable;

public final class NodeOperations {
    private static final int MAX_ASPECTS = 256;
    private static final int MAX_AMOUNT = 1_000_000;

    private NodeOperations() {}

    public static Optional<NodeSnapshot> inspect(ServerLevel level, BlockPos pos) {
        BlockEntityNode node = find(level, pos);
        return node == null ? Optional.empty() : Optional.of(node.snapshot());
    }

    public static @Nullable BlockEntityNode find(ServerLevel level, BlockPos pos) {
        if (!level.getServer().isSameThread() || !level.hasChunkAt(pos) || !level.isInWorldBounds(pos)) {
            return null;
        }
        return level.getBlockEntity(pos) instanceof BlockEntityNode node && !node.isRemoved() && node.availableForApi() ? node : null;
    }

    public static NodeResult guard(NodeOperationContext context) {
        ServerLevel level = context.level();
        BlockPos pos = context.pos();
        if (!level.getServer().isSameThread() || !level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) {
            return NodeResult.UNAVAILABLE;
        }
        if (!level.getWorldBorder().isWithinBounds(pos)) {
            return NodeResult.DENIED;
        }
        if (context.player().isPresent()) {
            ServerPlayer player = context.player().get();
            if (player.level() != level || !player.isAlive() || player.isSpectator() || !player.mayBuild() || !level.mayInteract(player, pos)) {
                return NodeResult.DENIED;
            }
            if (CommonHooks.fireBlockBreak(level, player.gameMode.getGameModeForPlayer(), player, pos, level.getBlockState(pos)).isCanceled()) {
                return NodeResult.DENIED;
            }
        }
        return NeoForge.EVENT_BUS.post(new NodeOperationEvent(context)).isCanceled() ? NodeResult.DENIED : NodeResult.SUCCESS;
    }

    public static boolean valid(NodeChange change) {
        if (change == null || change.type() == null || change.modifier() == null || change.behavior() == null || !validAspects(change.current()) || !validAspects(change.base())) {
            return false;
        }
        for (AspectInstance entry : change.current().entries()) {
            if (entry.amount() > change.base().amountOf(entry.aspect())) {
                return false;
            }
        }
        return true;
    }

    public static NodeResult guardPlacement(NodeOperationContext context) {
        NodeResult gate = guard(context);
        if (gate != NodeResult.SUCCESS) {
            return gate;
        }
        if (context.player().isPresent() && EventHooks.onBlockPlace(context.player().get(), BlockSnapshot.create(context.level().dimension(), context.level(), context.pos()), Direction.UP)) {
            return NodeResult.DENIED;
        }
        return NodeResult.SUCCESS;
    }

    private static boolean validAspects(AspectList aspects) {
        if (aspects == null || aspects.size() > MAX_ASPECTS) {
            return false;
        }
        long total = 0;
        for (AspectInstance entry : aspects.entries()) {
            if (!entry.aspect().isBound() || entry.aspect().unwrapKey().isEmpty() || entry.amount() < 1 || entry.amount() > MAX_AMOUNT) {
                return false;
            }
            total += entry.amount();
        }
        return total <= MAX_AMOUNT;
    }

    public static NodeResult update(NodeOperationContext context, UUID identity, long revision, NodeChange change, TransactionContext transaction) {
        NodeResult gate = guard(context);
        if (gate != NodeResult.SUCCESS) {
            return gate;
        }
        if (!valid(change)) {
            return NodeResult.INVALID;
        }
        BlockEntityNode node = find(context.level(), context.pos());
        if (node == null) {
            return NodeResult.UNAVAILABLE;
        }
        NodeSnapshot snapshot = node.snapshot();
        if (!snapshot.identity().equals(identity) || snapshot.revision() != revision) {
            return NodeResult.STALE;
        }
        if (snapshot.busy() || snapshot.locked()) {
            return NodeResult.BUSY;
        }
        if (snapshot.energized()) {
            return NodeResult.UNSUPPORTED;
        }
        node.update(change, transaction);
        return NodeResult.SUCCESS;
    }

    public static NodeResult create(NodeOperationContext context, NodeSpec spec) {
        NodeResult gate = guardPlacement(context);
        if (gate != NodeResult.SUCCESS) {
            return gate;
        }
        if (context.player().isPresent() && !context.player().get().hasInfiniteMaterials()) {
            return NodeResult.DENIED;
        }
        if (spec == null || !valid(new NodeChange(spec.current(), spec.base(), spec.type(), spec.modifier(), spec.behavior()))) {
            return NodeResult.INVALID;
        }
        ServerLevel level = context.level();
        BlockPos pos = context.pos();
        BlockState previous = level.getBlockState(pos);
        if (!previous.canBeReplaced() || level.getBlockEntity(pos) != null) {
            return NodeResult.UNAVAILABLE;
        }
        if (!level.setBlock(pos, TTBlocks.NODE.get().defaultBlockState(), Block.UPDATE_CLIENTS)) {
            return NodeResult.UNAVAILABLE;
        }
        BlockEntityNode node = find(level, pos);
        if (node == null) {
            level.setBlock(pos, previous, Block.UPDATE_ALL);
            return NodeResult.UNAVAILABLE;
        }
        node.restoreSnapshot(new NodeSnapshot(UUID.randomUUID(), 0, spec.current(), spec.base(), Optional.empty(), spec.type(), spec.modifier(), false, false, false, spec.behavior()));
        node.behaviorEvent(NodeBehavior.Event.ATTACH);
        level.updateNeighborsAt(pos, TTBlocks.NODE.get());
        return NodeResult.SUCCESS;
    }
}
