package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public record ConstructDeployment<T extends EntityOwnedConstruct>(Supplier<EntityType<T>> type, BiConsumer<T, Player> orient) {
    private static final int FOOTPRINT_HEIGHT = 2;
    private static final float SETTLE_VOLUME = 0.75F;
    private static final float SETTLE_PITCH = 0.8F;

    public static <T extends EntityOwnedConstruct> ConstructDeployment<T> upright(Supplier<EntityType<T>> type) {
        return new ConstructDeployment<>(type, ConstructDeployment::keepDefaultFacing);
    }

    public InteractionResult deploy(UseOnContext context) {
        Player player = context.getPlayer();
        Direction face = context.getClickedFace();
        if (player == null || face == Direction.DOWN) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos footing = level.getBlockState(clicked).canBeReplaced() ? clicked : clicked.relative(face);
        if (!player.mayUseItemAt(footing, face, context.getItemInHand()) || !isClear(level, footing)) {
            return InteractionResult.PASS;
        }
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        T construct = type.get().create(server, EntitySpawnReason.SPAWN_ITEM_USE);
        if (construct == null) {
            return InteractionResult.FAIL;
        }
        for (int height = 0; height < FOOTPRINT_HEIGHT; height++) {
            server.removeBlock(footing.above(height), false);
        }
        construct.snapTo(footing.getX() + 0.5, footing.getY(), footing.getZ() + 0.5, 0.0F, 0.0F);
        construct.setValidSpawn();
        construct.setOwner(player);
        orient.accept(construct, player);
        server.addFreshEntity(construct);
        server.playSound(null, construct.getX(), construct.getY(), construct.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, SETTLE_VOLUME, SETTLE_PITCH);
        context.getItemInHand().consume(1, player);
        return InteractionResult.SUCCESS;
    }

    private static boolean isClear(Level level, BlockPos footing) {
        for (int height = 0; height < FOOTPRINT_HEIGHT; height++) {
            if (!level.getBlockState(footing.above(height)).canBeReplaced()) {
                return false;
            }
        }
        return level.getEntities((Entity) null, new AABB(footing).expandTowards(0.0, FOOTPRINT_HEIGHT - 1, 0.0)).isEmpty();
    }

    private static <T extends EntityOwnedConstruct> void keepDefaultFacing(T construct, Player player) {}
}
