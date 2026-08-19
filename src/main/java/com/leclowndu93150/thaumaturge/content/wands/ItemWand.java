package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.casters.CasterTriggerRegistry;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusUnit;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.casters.IFocusBlockPicker;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.api.wands.IWandRodOnUpdate;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.casters.CasterManager;
import com.leclowndu93150.thaumaturge.content.casters.ItemFocus;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.misc.TCActionBar;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCWandParts;
import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemWand extends Item implements ICaster, IArchitect {
    private static final float REFINE_SPARKLE_SPREAD = 2.0F;
    private static final DecimalFormat VIS_FORMAT = new DecimalFormat("#######.##");
    private static final String STAFF_ROD_SUFFIX = "_staff";
    private static final int NO_AURA_MESSAGE_INTERVAL_TICKS = 20;

    public ItemWand(Properties properties) {
        super(properties);
    }

    public static ItemStack create(Item wandItem, WandCap cap, WandRod rod, boolean sceptre) {
        ItemStack stack = new ItemStack(wandItem);
        stack.set(TCDataComponents.WAND_PARTS.get(), new WandParts(cap, rod, sceptre));
        return stack;
    }

    public WandParts getParts(ItemStack stack) {
        return WandVisHelper.getParts(stack);
    }

    public boolean isStaff(ItemStack stack) {
        return getParts(stack).rod().staff();
    }

    public boolean isSceptre(ItemStack stack) {
        return getParts(stack).sceptre();
    }

    public boolean hasRunes(ItemStack stack) {
        return getParts(stack).rod().runes();
    }

    @Override
    public Component getName(ItemStack stack) {
        WandParts parts = getParts(stack);
        String capName = TCWandParts.caps().getKey(parts.cap()).getPath();
        String rodName = TCWandParts.rods().getKey(parts.rod()).getPath();
        if (rodName.endsWith(STAFF_ROD_SUFFIX)) {
            rodName = rodName.substring(0, rodName.length() - STAFF_ROD_SUFFIX.length());
        }
        String objKey = parts.rod().staff() ? "item.thaumaturge.wand.staff" : parts.sceptre() ? "item.thaumaturge.wand.sceptre" : "item.thaumaturge.wand.named";
        return Component.translatable(objKey, Component.translatable("wand.thaumaturge.cap." + capName), Component.translatable("wand.thaumaturge.rod." + rodName));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack wandStack = player.getItemInHand(hand);
        ItemStack focusStack = getFocusStack(wandStack);
        if (focusStack.getItem() instanceof ItemFocus focus) {
            if (CasterManager.isOnCooldown(player)) {
                return InteractionResult.PASS;
            }
            FocusPackage core = ItemFocus.getPackage(focusStack);
            if (core == null) {
                return InteractionResult.PASS;
            }
            if (player.isShiftKeyDown() && containsElement(core, IFocusBlockPicker.class)) {
                return InteractionResult.PASS;
            }
            if (!consumeVis(wandStack, player, focus.getVisCost(focusStack), false, level.isClientSide())) {
                if (player instanceof ServerPlayer serverPlayer) {
                    sendWandActionBar(serverPlayer, "tc.wand.notenoughvis");
                }
                return InteractionResult.FAIL;
            }
            int cooldown = focus.getActivationTime(focusStack);
            CasterManager.setCooldown(player, cooldown);
            player.getCooldowns().addCooldown(wandStack, cooldown);
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            FocusEngine.cast(player, core);
            player.swing(hand);
            return InteractionResult.SUCCESS;
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    public @Nullable ItemFocus getFocus(ItemStack stack) {
        return getFocusStack(stack).getItem() instanceof ItemFocus focus ? focus : null;
    }

    @Override
    public ItemStack getFocusStack(ItemStack stack) {
        ItemStackTemplate template = stack.get(TCDataComponents.SOCKETED_FOCUS.get());
        return template == null ? ItemStack.EMPTY : template.create();
    }

    @Override
    public void setFocus(ItemStack stack, ItemStack focus) {
        if (focus == null || focus.isEmpty()) {
            stack.remove(TCDataComponents.SOCKETED_FOCUS.get());
        } else {
            stack.set(TCDataComponents.SOCKETED_FOCUS.get(), ItemStackTemplate.fromNonEmptyStack(focus));
        }
    }

    @Override
    public float getConsumptionModifier(ItemStack stack, Player player, boolean crafting) {
        WandParts parts = getParts(stack);
        float modifier = parts.cap().baseCostModifier();
        if (player != null) {
            modifier -= CasterManager.getTotalVisDiscount(player);
        }
        if (parts.sceptre()) {
            modifier -= WandEconomy.SCEPTRE_DISCOUNT;
        }
        return Math.max(modifier, WandEconomy.MIN_CONSUMPTION_MODIFIER);
    }

    @Override
    public boolean consumeVis(ItemStack stack, Player player, float amount, boolean crafting, boolean simulate) {
        if (amount <= 0.0F) {
            return true;
        }
        Map<ResourceKey<IAspect>, Integer> split = WandVisHelper.evenSplit(Math.round(amount * WandEconomy.CENTIVIS_PER_VIS));
        return WandVisHelper.consumeAllVis(stack, player, split, !simulate, crafting);
    }

    @Override
    public @Nullable BlockState getPickedBlock(ItemStack stack) {
        FocusPackage core = ItemFocus.getPackage(getFocusStack(stack));
        if (core != null && containsElement(core, IFocusBlockPicker.class)) {
            return stack.get(TCDataComponents.PICKED_BLOCK.get());
        }
        return null;
    }

    private static boolean containsElement(FocusPackage core, Class<?> marker) {
        for (FocusUnit unit : core.units()) {
            if (marker.isInstance(FocusEngine.element(unit.element()))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        InteractionHand hand = context.getHand();
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof IInteractWithCaster target && target.onCasterRightClick(level, stack, player, pos, side, hand)) {
            return InteractionResult.SUCCESS;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof IInteractWithCaster target && target.onCasterRightClick(level, stack, player, pos, side, hand)) {
            return InteractionResult.SUCCESS;
        }
        if (CasterTriggerRegistry.hasTrigger(state)) {
            return CasterTriggerRegistry.performTrigger(level, stack, player, pos, side, state) ? InteractionResult.SUCCESS : InteractionResult.FAIL;
        }
        ItemStack focusStack = getFocusStack(stack);
        if (!focusStack.isEmpty() && player.isShiftKeyDown() && blockEntity == null) {
            FocusPackage core = ItemFocus.getPackage(focusStack);
            if (core != null && containsElement(core, IFocusBlockPicker.class)) {
                if (!level.isClientSide()) {
                    if (!state.isAir()) {
                        stack.set(TCDataComponents.PICKED_BLOCK.get(), state);
                    }
                    return InteractionResult.SUCCESS;
                }
                player.swing(hand);
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private @Nullable IArchitect architectElement(ItemStack stack) {
        FocusPackage core = ItemFocus.getPackage(getFocusStack(stack));
        if (core == null) {
            return null;
        }
        for (FocusUnit unit : core.units()) {
            if (FocusEngine.element(unit.element()) instanceof IArchitect architect) {
                return architect;
            }
        }
        return null;
    }

    @Override
    public @Nullable HitResult getArchitectMOP(ItemStack stack, Level level, LivingEntity caster) {
        IArchitect architect = architectElement(stack);
        return architect == null ? null : architect.getArchitectMOP(stack, level, caster);
    }

    @Override
    public boolean useBlockHighlight(ItemStack stack) {
        return false;
    }

    @Override
    public List<BlockPos> getArchitectBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        IArchitect architect = architectElement(stack);
        return architect == null ? List.of() : architect.getArchitectBlocks(stack, level, pos, side, player);
    }

    @Override
    public boolean showAxis(ItemStack stack, Level level, Player player, Direction side, EnumAxis axis) {
        IArchitect architect = architectElement(stack);
        return architect != null && architect.showAxis(stack, level, player, side, axis);
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        if (oldStack.getItem() == this && newStack.getItem() == this) {
            return sortingHash(oldStack) != sortingHash(newStack);
        }
        return oldStack.getItem() != newStack.getItem();
    }

    private int sortingHash(ItemStack wandStack) {
        ItemFocus focus = getFocus(wandStack);
        if (focus == null) {
            return 0;
        }
        String sortKey = focus.getSortingHelper(getFocusStack(wandStack));
        return sortKey != null ? sortKey.hashCode() : 0;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int ticksRemaining) {
        if (!(level instanceof ServerLevel) || !(entity instanceof Player player)) {
            return;
        }
        HitResult nodeHit = player.pick(WandEconomy.CRUDE_REFINE_TARGET_RANGE, 0.0F, false);
        if (nodeHit instanceof BlockHitResult nodeBlockHit && level.getBlockEntity(nodeBlockHit.getBlockPos()) instanceof BlockEntityNode node) {
            node.drainToWand((ServerLevel) level, player, stack, ticksRemaining);
            return;
        }
        if (ticksRemaining % WandEconomy.CRUDE_REFINE_INTERVAL_TICKS != 0) {
            return;
        }
        ResourceKey<IAspect> target = refineTarget(player, stack);
        if (target == null) {
            return;
        }
        int room = WandVisHelper.getMaxVis(stack) - WandVisHelper.getVis(stack, target);
        int gain = Math.min(WandEconomy.CRUDE_REFINE_CENTIVIS_PER_OP, room);
        if (gain <= 0) {
            return;
        }
        float rawCost = gain * WandEconomy.RAW_TO_PRIMAL_RATIO / (float) WandEconomy.CENTIVIS_PER_VIS;
        float drained = AuraHelper.drainVis(level, player.blockPosition(), rawCost, false);
        int gained = (int) (drained * WandEconomy.CENTIVIS_PER_VIS / WandEconomy.RAW_TO_PRIMAL_RATIO);
        if (gained > 0) {
            WandVisHelper.addRealVis(stack, target, gained, true);
            sendRefineSparkle((ServerLevel) level, player, target);
        } else if (ticksRemaining % NO_AURA_MESSAGE_INTERVAL_TICKS == 0 && player instanceof ServerPlayer serverPlayer) {
            sendWandActionBar(serverPlayer, "tc.wand.noaura");
        }
    }

    private static void sendWandActionBar(ServerPlayer player, String key) {
        TCActionBar.sendPurple(player, key);
    }

    private static void sendRefineSparkle(ServerLevel level, Player player, ResourceKey<IAspect> aspect) {
        int color = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(aspect).map(holder -> holder.value().color()).orElse(0xFFFFFF);
        Vec3 origin = player.getEyePosition().add((level.getRandom().nextFloat() - level.getRandom().nextFloat()) * REFINE_SPARKLE_SPREAD, level.getRandom().nextFloat() * REFINE_SPARKLE_SPREAD,
                (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * REFINE_SPARKLE_SPREAD);
        Vec3 hand = player.getEyePosition().add(player.getLookAngle().scale(0.5)).add(0.0, -0.3, 0.0);
        EffectDispatch.spawnVisSparkle(level, origin, hand, color);
    }

    private @Nullable ResourceKey<IAspect> refineTarget(Player player, ItemStack stack) {
        HitResult hit = player.pick(WandEconomy.CRUDE_REFINE_TARGET_RANGE, 0.0F, false);
        if (hit instanceof BlockHitResult blockHit && player.level().getBlockState(blockHit.getBlockPos()).getBlock() instanceof BlockCrystal crystal && !crystal.isFlux() && crystal.aspect() != null
                && TCAspects.PRIMALS.contains(crystal.aspect())) {
            return crystal.aspect();
        }
        WandVis vis = WandVisHelper.getAllVis(stack);
        int max = WandVisHelper.getMaxVis(stack);
        ResourceKey<IAspect> lowest = null;
        int lowestAmount = Integer.MAX_VALUE;
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            int amount = vis.amount(primal);
            if (amount < max && amount < lowestAmount) {
                lowestAmount = amount;
                lowest = primal;
            }
        }
        return lowest;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (entity instanceof Player player) {
            IWandRodOnUpdate onUpdate = getParts(stack).rod().onUpdate();
            if (onUpdate != null) {
                onUpdate.onUpdate(stack, player);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.thaumaturge.wand.capacity", WandVisHelper.getMaxVis(stack) / WandEconomy.CENTIVIS_PER_VIS).withStyle(ChatFormatting.GOLD));
        WandVis vis = WandVisHelper.getAllVis(stack);
        HolderLookup.Provider registries = context.registries();
        MutableComponent amounts = null;
        Map<ResourceKey<IAspect>, Integer> pctByPrimal = new LinkedHashMap<>();
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            pctByPrimal.put(primal, Math.round(WandVisHelper.getConsumptionModifier(stack, null, primal, false) * 100.0F));
            int amount = vis.amount(primal);
            if (amount <= 0) {
                continue;
            }
            Component chunk = Component.literal(VIS_FORMAT.format(amount / (float) WandEconomy.CENTIVIS_PER_VIS)).withStyle(WandTooltips.primalColor(registries, primal));
            if (amounts == null) {
                amounts = Component.empty().append(chunk);
            } else {
                amounts.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY)).append(chunk);
            }
        }
        if (amounts != null) {
            builder.accept(amounts);
        }
        builder.accept(WandTooltips.costSummary(registries, pctByPrimal));
        ItemStack focusStack = getFocusStack(stack);
        if (focusStack.getItem() instanceof ItemFocus focus) {
            builder.accept(Component.translatable("tooltip.thaumaturge.caster.vis_cost", ItemFocus.formatVis(focus.getVisCost(focusStack))).withStyle(ChatFormatting.ITALIC, ChatFormatting.AQUA));
            builder.accept(focusStack.getHoverName().copy().withStyle(ChatFormatting.BOLD, ChatFormatting.ITALIC, ChatFormatting.GREEN));
            focus.addFocusInformation(focusStack, builder);
        }
    }
}
