package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import org.jspecify.annotations.Nullable;

public final class ItemSealPlacer extends Item implements ISealDisplayer {
    private final @Nullable Identifier sealKey;

    public ItemSealPlacer(Item.Properties properties) {
        this(null, properties);
    }

    public ItemSealPlacer(@Nullable Identifier sealKey, Item.Properties properties) {
        super(properties);
        this.sealKey = sealKey;
    }

    public @Nullable Identifier sealKey() {
        return sealKey;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown() || sealKey == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        if (!player.mayUseItemAt(pos.relative(face), face, stack)) {
            return InteractionResult.FAIL;
        }
        Optional<SealType> type = GolemHelper.sealType(sealKey);
        if (type.isEmpty() || !type.get().placement().allows(level, pos, face)) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        return stack == context.getItemInHand() && GolemHelper.tryPlaceSeal(context, sealKey) ? InteractionResult.SUCCESS_SERVER : InteractionResult.FAIL;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}
