package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class ItemGolemPlacer extends Item implements ISealDisplayer {
    public ItemGolemPlacer(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        GolemProperties props = stack.get(TTDataComponents.GOLEM_PROPERTIES.get());
        if (props == null) {
            return;
        }
        if (props.hasTrait(TTGolemTraits.SMART.get())) {
            if (props.rank() >= EntityThaumaturgeGolem.MAX_RANK) {
                tooltip.accept(Component.translatable("golem.rank").append(" " + props.rank()).withStyle(ChatFormatting.GOLD));
            } else {
                int xp = stack.getOrDefault(TTDataComponents.GOLEM_XP.get(), 0);
                int needed = (props.rank() + 1) * (props.rank() + 1) * EntityThaumaturgeGolem.XP_PER_RANK_UNIT;
                tooltip.accept(Component.translatable("golem.rank").append(" " + props.rank()).withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(" (" + xp + "/" + needed + ")").withStyle(ChatFormatting.DARK_GREEN)));
            }
        }
        Identifier materialKey = TTGolemParts.materials().getKey(props.material());
        if (materialKey != null) {
            tooltip.accept(Component.translatable(GolemMaterial.nameKey(materialKey)).withStyle(ChatFormatting.GREEN));
        }
        for (GolemTrait trait : props.traits()) {
            tooltip.accept(Component.literal("-").append(Component.translatable(GolemTrait.nameKey(TTGolemTraits.registry().getKey(trait)))).withStyle(ChatFormatting.BLUE));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        if (level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty()) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = clicked.relative(context.getClickedFace());
        Player player = context.getPlayer();
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        EntityThaumaturgeGolem golem = TTEntities.THAUMATURGE_GOLEM.get().create(serverLevel, EntitySpawnReason.MOB_SUMMONED);
        if (golem == null) {
            return InteractionResult.FAIL;
        }
        golem.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        if (!serverLevel.addFreshEntity(golem)) {
            return InteractionResult.FAIL;
        }
        golem.setValidSpawn();
        golem.setOwner(player);
        ItemStack held = context.getItemInHand();
        GolemProperties props = held.get(TTDataComponents.GOLEM_PROPERTIES.get());
        if (props != null) {
            golem.setProperties(props);
        }
        golem.setRankXp(held.getOrDefault(TTDataComponents.GOLEM_XP.get(), 0));
        golem.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(pos), EntitySpawnReason.MOB_SUMMONED, null);
        if (!player.hasInfiniteMaterials()) {
            held.shrink(1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
