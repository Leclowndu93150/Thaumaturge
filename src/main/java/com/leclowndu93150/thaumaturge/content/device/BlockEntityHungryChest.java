package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class BlockEntityHungryChest extends ChestBlockEntity {
    private static final double EAT_REACH = 0.1;

    public BlockEntityHungryChest(BlockPos pos, BlockState state) {
        super(TTBlockEntities.HUNGRY_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.thaumaturge.hungry_chest");
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityHungryChest chest) {
        List<ItemEntity> items = level.getEntitiesOfClass(
                ItemEntity.class,
                new AABB(pos).inflate(EAT_REACH, 0.0, EAT_REACH).expandTowards(0.0, EAT_REACH * 3.0, 0.0));
        if (items.isEmpty()) {
            return;
        }
        for (ItemEntity item : items) {
            if (item.isRemoved()) {
                continue;
            }
            ItemStack original = item.getItem();
            ItemStack leftovers = InvHelper.insertStackAt(level, pos, Direction.UP, original.copy(), false);
            if (leftovers.getCount() != original.getCount()) {
                item.playSound(
                        SoundEvents.GENERIC_EAT,
                        0.25F,
                        (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F + 1.0F);
                chest.setChanged();
            }
            if (leftovers.isEmpty()) {
                item.discard();
            } else {
                item.setItem(leftovers);
            }
        }
    }
}
