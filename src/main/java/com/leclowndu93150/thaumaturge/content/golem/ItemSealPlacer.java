package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jspecify.annotations.Nullable;

public final class ItemSealPlacer extends Item implements ISealDisplayer {
    private final Identifier sealKey;

    public ItemSealPlacer(Properties properties) {
        this(null, properties);
    }

    public ItemSealPlacer(@Nullable Identifier sealKey, Properties properties) {
        super(properties);
        this.sealKey = sealKey;
    }

    public @Nullable Identifier sealKey() {
        return sealKey;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (sealKey == null || player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        ISeal seal = GolemHelper.createSeal(sealKey);
        if (seal == null || !seal.canPlaceAt(level, pos, context.getClickedFace())) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (SealHandler.addSealEntity((ServerLevel) level, pos, context.getClickedFace(), seal, player) && !player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}
