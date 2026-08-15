package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.TCResearchEntries;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import com.leclowndu93150.thaumaturge.content.research.scan.ScanRaycastHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

public final class ThaumometerItem extends Item {
    public static final double SCAN_ENTITY_MIN_RANGE = 1.0;
    public static final double SCAN_ENTITY_RANGE = 9.0;
    public static final int USE_DURATION_TICKS = 25;
    public static final int SCAN_COMPLETE_ELAPSED_TICKS = 20;

    private static final int SCAN_RELEASE_TOLERANCE_TICKS = 18;
    private static final int AURA_CHECK_INTERVAL_TICKS = 20;
    private static final int FLUX_WARN_BASE_DIVISOR = 3;

    public ThaumometerItem(Item.Properties properties) {
        super(properties);
    }

    // Runs before the block's own use handler so scanning a container doesn't open its screen instead.
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        return beginScan(context.getLevel(), player, context.getHand());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return new InteractionResultHolder<>(beginScan(level, player, hand), player.getItemInHand(hand));
    }

    private InteractionResult beginScan(Level level, Player player, InteractionHand hand) {
        return beginScanAt(player, hand, resolveTarget(level, player));
    }

    static InteractionResult beginScanAt(Player player, InteractionHand hand, @Nullable Object target) {
        if (!ScanningManager.isThingStillScannable(player, target)) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remaining) {
        if (level.isClientSide() || !(entity instanceof Player)) {
            return;
        }
        if (USE_DURATION_TICKS - remaining >= SCAN_COMPLETE_ELAPSED_TICKS) {
            entity.releaseUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        if (level.isClientSide() || !(entity instanceof Player player)) {
            return;
        }
        if (USE_DURATION_TICKS - remaining < SCAN_RELEASE_TOLERANCE_TICKS) {
            return;
        }
        Object target = resolveTarget(level, player);
        if (!ScanningManager.isThingStillScannable(player, target)) {
            return;
        }
        ScanningManager.scanTheThing(player, target);
        return;
    }

    public static @Nullable Object resolveTarget(Level level, Player player) {
        HitResult hitResult = ScanRaycastHelper.performRaycast(player, ClipContext.Fluid.SOURCE_ONLY);
        return hitResult.getType() == HitResult.Type.BLOCK
                ? ((BlockHitResult) hitResult).getBlockPos()
                : hitResult instanceof EntityHitResult result ? result.getEntity() : null;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level levelIn, Entity owner, int slotId, boolean isSelected) {
        if (!(levelIn instanceof ServerLevel level)) {
            return;
        }
        boolean held = isSelected || (owner instanceof LivingEntity living && living.getOffhandItem() == stack);
        if (held && owner.tickCount % AURA_CHECK_INTERVAL_TICKS == 0 && owner instanceof ServerPlayer player) {
            warnAboutFlux(level, player);
        }
    }

    private static void warnAboutFlux(ServerLevel level, ServerPlayer player) {
        BlockPos pos = player.blockPosition();
        float flux = AuraHelper.getFlux(level, pos);
        boolean dangerous = flux > AuraHelper.getVis(level, pos)
                || flux > AuraHelper.getAuraBase(level, pos) / (float) FLUX_WARN_BASE_DIVISOR;
        if (dangerous && !KnowledgeAccess.of(player).isResearchKnown(TCResearchEntries.FLUX)) {
            ResearchManager.complete(player, TCResearchEntries.FLUX);
            player.displayClientMessage(
                    Component.translatable("research.thaumaturge.flux.warn").withStyle(ChatFormatting.DARK_PURPLE),
                    true);
        }
    }
}
