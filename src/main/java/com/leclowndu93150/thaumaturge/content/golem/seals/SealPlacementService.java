package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPlacementEvent;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.neoforged.neoforge.common.NeoForge;

public final class SealPlacementService {
    private SealPlacementService() {}

    public static boolean place(UseOnContext context, Identifier typeId) {
        if (!(context.getLevel() instanceof ServerLevel level) || !level.getServer().isSameThread()) {
            throw new IllegalStateException("Seal placement requires the server thread");
        }
        if (!(context.getPlayer() instanceof ServerPlayer player)) {
            return false;
        }
        SealType type = GolemHelper.sealType(typeId).orElse(null);
        if (type == null) {
            return false;
        }
        SealPos position = new SealPos(context.getClickedPos().immutable(), context.getClickedFace());
        ItemStack actual = player.getItemInHand(context.getHand());
        if (actual != context.getItemInHand()) {
            return false;
        }
        ItemStack expected = actual.copy();
        if (!allowed(level, player, position, type, actual)) {
            return false;
        }
        SealPlacementEvent event = new SealPlacementEvent(player, position, typeId, actual);
        NeoForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            return false;
        }
        SealEntity seal = SealEntity.place(position, typeId, type, player.getUUID());
        if (!allowed(level, player, position, type, actual) || player.getItemInHand(context.getHand()) != actual || !ItemStack.matches(actual, expected)) {
            return false;
        }
        if (!SealHandler.register(level, seal)) {
            return false;
        }
        if (!player.hasInfiniteMaterials()) {
            actual.shrink(1);
        }
        return true;
    }

    private static boolean allowed(ServerLevel level, ServerPlayer player, SealPos position, SealType type, ItemStack stack) {
        BlockPos block = position.pos();
        BlockPos adjacent = block.relative(position.face());
        return player.level() == level && player.isAlive() && !player.isSpectator() && player.isWithinBlockInteractionRange(block, 0) && level.hasChunkAt(block) && level.hasChunkAt(adjacent)
                && level.isInWorldBounds(block) && level.isInWorldBounds(adjacent) && level.getWorldBorder().isWithinBounds(block) && level.getWorldBorder().isWithinBounds(adjacent)
                && level.mayInteract(player, block) && level.mayInteract(player, adjacent) && player.mayUseItemAt(adjacent, position.face(), stack) && SealHandler.lookup(level, position) == null
                && type.acceptsPlacer(stack) && type.placement().allows(level, block, position.face()) && type.research().stream().allMatch(key -> KnowledgeAccess.of(player).isResearchComplete(key));
    }
}
