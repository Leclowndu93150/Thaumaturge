package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.content.misc.TCActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerFx;
import com.leclowndu93150.thaumaturge.content.recipe.dust.DustTriggerSwapQueue;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class SalisMundusItem extends Item {
    private static final int SWAP_DELAY_TICKS = 50;

    public SalisMundusItem(Item.Properties properties) {
        super(properties);
    }

    @SubscribeEvent
    public static void allowUseOnCraftingTable(UseItemOnBlockEvent event) {
        if (event.getUsePhase() != UseItemOnBlockEvent.UsePhase.BLOCK)
            return;
        if (!event.getItemStack().is(TCItems.SALIS_MUNDUS))
            return;
        if (event.getPlayer() == null)
            return;
        if (!event.getPlayer().isCrouching())
            return;
        event.cancelWithResult(InteractionResult.PASS);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (!player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        player.swing(context.getHand());
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ServerLevel serverLevel = (ServerLevel) level;
        BlockState clicked = level.getBlockState(pos);
        DustTriggerInput input = new DustTriggerInput(stack, level, pos, clicked);
        Optional<RecipeHolder<DustTrigger>> match = serverLevel.recipeAccess().getRecipeFor(TCRecipeTypes.DUST_TRIGGER.get(), input, level);
        if (match.isEmpty()) {
            return InteractionResult.PASS;
        }
        RecipeHolder<DustTrigger> holder = match.get();
        DustTrigger trigger = holder.value();
        if (!trigger.doesPassGate(player)) {
            Thaumaturge.LOGGER.debug("Salis Mundus trigger {} blocked by research gate {}", holder.id(), trigger.researchGate().orElse(null));
            TCActionBar.sendPurple(player, "tc.dust.noresearch");
            return InteractionResult.PASS;
        }
        ItemStack result = trigger.assemble(input);
        if (result.isEmpty()) {
            return InteractionResult.PASS;
        }
        ItemStack consumed = stack.copyWithCount(1);
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        DustTriggerPlacement placement = null;
        if (trigger.isMultiblock()) {
            placement = trigger.findPlacement(input);
            if (placement == null) {
                return InteractionResult.PASS;
            }
            trigger.execute(input, player, placement, context.getClickedFace());
        } else if (result.getItem() instanceof BlockItem blockItem) {
            DustTriggerSwapQueue.enqueuePlace(serverLevel, pos, clicked, blockItem.getBlock().defaultBlockState(), SWAP_DELAY_TICKS);
        } else {
            DustTriggerSwapQueue.enqueueDrop(serverLevel, pos, clicked, result, SWAP_DELAY_TICKS);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            Vec3 hitWorld = context.getClickLocation();
            DustTriggerFx.emitUseBurst(serverLevel, serverPlayer, context.getHand(), pos);
            DustTriggerFx.emitTriggerSparkles(serverLevel, serverPlayer, pos, trigger, hitWorld, placement);
            serverPlayer.awardStat(Stats.ITEM_CRAFTED.get(result.getItem()), result.getCount());
            ResearchProgressionEvents.recordCrafted(serverPlayer, result);
            CriteriaTriggers.RECIPE_CRAFTED.trigger(serverPlayer, holder.id(), List.of(consumed));
        }
        level.playSound(null, pos, TCSounds.DUST.get(), SoundSource.PLAYERS, 0.33F, 1.0F + (float) level.getRandom().nextGaussian() * 0.05F);
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        return true;
    }
}
