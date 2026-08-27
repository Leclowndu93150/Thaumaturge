package com.leclowndu93150.thaumaturge.content.entity.construct;

import java.util.List;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class TurretPlacerItem extends Item {
    public interface ConstructFactory extends Function<Level, EntityOwnedConstruct> {}

    private final ConstructFactory factory;

    public TurretPlacerItem(Properties properties, ConstructFactory factory) {
        super(properties);
        this.factory = factory;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return placeConstruct(context, factory);
    }

    public static InteractionResult placeConstruct(UseOnContext context, ConstructFactory factory) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        Direction side = context.getClickedFace();
        if (side == Direction.DOWN || player == null) {
            return InteractionResult.PASS;
        }
        BlockPos clicked = context.getClickedPos();
        boolean replaceable = level.getBlockState(clicked).canBeReplaced();
        BlockPos base = replaceable ? clicked : clicked.relative(side);
        if (!player.mayUseItemAt(base, side, context.getItemInHand())) {
            return InteractionResult.PASS;
        }
        BlockPos above = base.above();
        boolean blocked = !level.isEmptyBlock(base) && !level.getBlockState(base).canBeReplaced();
        blocked |= !level.isEmptyBlock(above) && !level.getBlockState(above).canBeReplaced();
        if (blocked) {
            return InteractionResult.PASS;
        }
        List<Entity> entities = level.getEntities(null, new AABB(base.getX(), base.getY(), base.getZ(), base.getX() + 1.0, base.getY() + 2.0, base.getZ() + 1.0));
        if (!entities.isEmpty()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.PASS;
        }
        level.removeBlock(base, false);
        level.removeBlock(above, false);
        EntityOwnedConstruct construct = factory.apply(level);
        construct.snapTo(base.getX() + 0.5, base.getY(), base.getZ() + 0.5, 0.0F, 0.0F);
        if (construct instanceof EntityArcaneBore bore) {
            bore.setFacing(player.getDirection());
        }
        level.addFreshEntity(construct);
        construct.setOwned(true);
        construct.setValidSpawn();
        construct.setOwner(player);
        level.playSound(null, construct.getX(), construct.getY(), construct.getZ(), SoundEvents.ARMOR_STAND_PLACE, SoundSource.BLOCKS, 0.75F, 0.8F);
        context.getItemInHand().shrink(1);
        return InteractionResult.SUCCESS;
    }
}
