package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.casters.CasterTriggerRegistry;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.casters.IFocusBlockPicker;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.spell.casting.SpellCasting;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItem;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.world.crystal.BlockCrystal;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class ItemWand extends Item implements ICaster, IArchitect, IChanneledItem {
    private static final int USE_DURATION_TICKS = 72000;
    private static final int NO_AURA_MESSAGE_PERIOD = 20;
    private static final double REFINE_SPARKLE_SPREAD = 2.0;
    private static final double HAND_FORWARD_OFFSET = 0.5;
    private static final double HAND_DOWN_OFFSET = 0.3;
    private static final String STAFF_ROD_SUFFIX = "_staff";
    private static final String NAME_PLAIN_KEY = "item.thaumaturge.wand.named";
    private static final String NAME_STAFF_KEY = "item.thaumaturge.wand.staff";
    private static final String NAME_SCEPTRE_KEY = "item.thaumaturge.wand.sceptre";
    private static final String CAP_NAME_PREFIX = "wand.thaumaturge.cap.";
    private static final String ROD_NAME_PREFIX = "wand.thaumaturge.rod.";
    private static final String CAPACITY_KEY = "tooltip.thaumaturge.wand.capacity";
    private static final String NO_AURA_KEY = "message.thaumaturge.wand.no_aura";
    private static final String VIS_AMOUNT_PATTERN = "#######.##";
    private static final String VIS_SEPARATOR = " | ";
    private static final float PERCENT = 100.0F;

    public ItemWand(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack create(Item item, WandCap cap, WandRod rod, boolean sceptre) {
        ItemStack stack = new ItemStack(item);
        stack.set(TTDataComponents.WAND_PARTS.get(), new WandParts(cap, rod, sceptre));
        return stack;
    }

    public WandParts assembly(ItemStack stack) {
        return WandVisHelper.partsOf(stack);
    }

    public boolean usesStaffRod(ItemStack stack) {
        return assembly(stack).rod().staff();
    }

    public boolean hasSceptreCore(ItemStack stack) {
        return assembly(stack).sceptre();
    }

    public boolean rodCarriesRunes(ItemStack stack) {
        return assembly(stack).rod().runes();
    }

    @Override
    public @Nullable HitResult aim(ItemStack stack, Level level, LivingEntity caster) {
        return architectOf(stack, level).map(architect -> architect.aim(stack, level, caster)).orElse(null);
    }

    @Override
    public boolean replacesBlockHighlight(ItemStack stack) {
        return false;
    }

    @Override
    public List<BlockPos> previewBlocks(ItemStack stack, Level level, BlockPos pos, Direction side, Player player) {
        return architectOf(stack, level).map(architect -> architect.previewBlocks(stack, level, pos, side, player)).orElse(Collections.emptyList());
    }

    @Override
    public boolean showsAxis(ItemStack stack, Level level, Player player, Direction side, Direction.Axis axis) {
        return architectOf(stack, level).map(architect -> architect.showsAxis(stack, level, player, side, axis)).orElse(false);
    }

    private Optional<IArchitect> architectOf(ItemStack stack, Level level) {
        return Optional.ofNullable(focusBehavior(stack, level.registryAccess(), IArchitect.class));
    }

    @Override
    public Component getName(ItemStack stack) {
        WandParts parts = assembly(stack);
        String capPath = TTWandParts.caps().getKey(parts.cap()).getPath();
        String rodPath = TTWandParts.rods().getKey(parts.rod()).getPath();
        if (rodPath.endsWith(STAFF_ROD_SUFFIX)) {
            rodPath = rodPath.substring(0, rodPath.length() - STAFF_ROD_SUFFIX.length());
        }
        String template = parts.rod().staff() ? NAME_STAFF_KEY : parts.sceptre() ? NAME_SCEPTRE_KEY : NAME_PLAIN_KEY;
        return Component.translatable(template, Component.translatable(CAP_NAME_PREFIX + capPath), Component.translatable(ROD_NAME_PREFIX + rodPath));
    }

    @Override
    public ItemStack getFocusStack(ItemStack stack) {
        ItemStackTemplate template = stack.get(TTDataComponents.SOCKETED_FOCUS.get());
        return template == null ? ItemStack.EMPTY : template.create();
    }

    @Override
    public void setFocus(ItemStack stack, @Nullable ItemStack focus) {
        if (focus == null || focus.isEmpty()) {
            stack.remove(TTDataComponents.SOCKETED_FOCUS.get());
        } else {
            stack.set(TTDataComponents.SOCKETED_FOCUS.get(), ItemStackTemplate.fromNonEmptyStack(focus));
        }
    }

    @Override
    public @Nullable BlockState getPickedBlock(ItemStack stack) {
        return stack.get(TTDataComponents.PICKED_BLOCK.get());
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION_TICKS;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remaining) {
        ItemStack focus = getFocusStack(stack);
        boolean channelling = !focus.isEmpty() && SpellCasting.channels(focus);
        if (channelling && level instanceof ServerLevel serverLevel && entity instanceof Player player) {
            SpellCasting.release(serverLevel, player, stack, focus, USE_DURATION_TICKS - remaining);
            return true;
        }
        return super.releaseUsing(stack, level, entity, remaining);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (level instanceof ServerLevel serverLevel && user instanceof Player player) {
            tickChannel(serverLevel, player, stack, remaining);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        ItemStack socketed = getFocusStack(held);
        boolean channelOnly = socketed.isEmpty() || traceNode(level, player) != null;
        if (!channelOnly) {
            return castOrYield(level, player, hand, held, socketed);
        }
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    private InteractionResult castOrYield(Level level, Player player, InteractionHand hand, ItemStack wand, ItemStack focus) {
        if (player.isShiftKeyDown() && focusBehavior(wand, level.registryAccess(), IFocusBlockPicker.class) != null) {
            return InteractionResult.PASS;
        }
        return SpellCasting.use(level, player, hand, wand, focus);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player user = context.getPlayer();
        if (user != null && interactWithBlock(stack, context, user)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private boolean interactWithBlock(ItemStack stack, UseOnContext context, Player player) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        BlockEntity blockEntity = level.getBlockEntity(context.getClickedPos());
        if (relaysToCaster(blockEntity, stack, context, player) || relaysToCaster(state.getBlock(), stack, context, player)) {
            return true;
        }
        if (triggersCaster(stack, context, player, state)) {
            return true;
        }
        return blockEntity == null && tryPickBlock(level, stack, player, context.getHand(), state);
    }

    private static boolean relaysToCaster(@Nullable Object target, ItemStack stack, UseOnContext context, Player player) {
        if (!(target instanceof IInteractWithCaster interaction)) {
            return false;
        }
        return interaction.onCasterRightClick(context.getLevel(), stack, player, context.getClickedPos(), context.getClickedFace(), context.getHand());
    }

    private static boolean triggersCaster(ItemStack stack, UseOnContext context, Player player, BlockState state) {
        if (!CasterTriggerRegistry.hasTrigger(state)) {
            return false;
        }
        return CasterTriggerRegistry.performTrigger(context.getLevel(), stack, player, context.getClickedPos(), context.getClickedFace(), state);
    }

    private boolean tryPickBlock(Level level, ItemStack stack, Player player, InteractionHand hand, BlockState state) {
        if (!player.isShiftKeyDown() || getFocusStack(stack).isEmpty()) {
            return false;
        }
        if (focusBehavior(stack, level.registryAccess(), IFocusBlockPicker.class) == null) {
            return false;
        }
        if (level.isClientSide()) {
            player.swing(hand);
        } else if (!state.isAir()) {
            stack.set(TTDataComponents.PICKED_BLOCK.get(), state);
        }
        return true;
    }

    private static @Nullable BlockEntityNode traceNode(Level level, Player player) {
        BlockHitResult hit = trace(level, player, player.blockInteractionRange());
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        return level.getBlockEntity(hit.getBlockPos()) instanceof BlockEntityNode node ? node : null;
    }

    private static BlockHitResult trace(Level level, Player player, double range) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(range));
        return level.clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    private <T> @Nullable T focusBehavior(ItemStack wand, HolderLookup.Provider registries, Class<T> type) {
        return focusParts(wand, registries).map(SpellPart::behavior).filter(type::isInstance).map(type::cast).findFirst().orElse(null);
    }

    private Stream<SpellPart> focusParts(ItemStack wand, HolderLookup.Provider registries) {
        ItemStack focus = getFocusStack(wand);
        Spell spell = focus.isEmpty() ? null : Spells.spellOf(focus);
        if (spell == null) {
            return Stream.empty();
        }
        return spell.nodes().stream().flatMap(node -> Spells.part(registries, node.part()).stream());
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        boolean bothWands = oldStack.getItem() instanceof ItemWand && newStack.getItem() instanceof ItemWand;
        return bothWands ? sortKeyHash(oldStack) != sortKeyHash(newStack) : oldStack.getItem() != newStack.getItem();
    }

    private int sortKeyHash(ItemStack wand) {
        return Optional.of(getFocusStack(wand)).filter(focus -> !focus.isEmpty()).map(FocusItems::sortKey).map(String::hashCode).orElse(0);
    }

    @Override
    public float getConsumptionModifier(ItemStack stack, @Nullable Player player, boolean crafting) {
        float base = assembly(stack).cap().baseCostModifier();
        return WandVisHelper.modifier(assembly(stack), player, base);
    }

    @Override
    public boolean consumeVis(ItemStack stack, Player player, float amount, boolean crafting, boolean simulate) {
        return amount <= 0.0F || payEvenly(stack, player, amount, crafting, !simulate);
    }

    private static boolean payEvenly(ItemStack stack, Player player, float vis, boolean crafting, boolean commit) {
        int total = Math.round(vis * WandEconomy.CENTIVIS_PER_VIS);
        return WandVisHelper.payScalarCosts(stack, player, WandVisHelper.evenSplit(total), commit, crafting);
    }

    private void tickChannel(ServerLevel level, Player player, ItemStack wand, int remaining) {
        BlockEntityNode node = traceNode(level, player);
        if (node != null) {
            node.drainToWand(level, player, wand, remaining);
            return;
        }
        ItemStack socketed = getFocusStack(wand);
        boolean channelled = !socketed.isEmpty() && SpellCasting.channels(socketed);
        if (channelled) {
            SpellCasting.tick(level, player, wand, socketed, USE_DURATION_TICKS - remaining);
        } else {
            refine(level, player, wand, remaining);
        }
    }

    private void refine(ServerLevel level, Player player, ItemStack wand, int remaining) {
        if (remaining % WandEconomy.CRUDE_REFINE_INTERVAL_TICKS != 0) {
            return;
        }
        ResourceKey<IAspect> target = chooseRefineTarget(wand, level, player);
        if (target == null) {
            return;
        }
        int headroom = WandVisHelper.capacityOf(wand) - WandVisHelper.storedIn(wand, target);
        int wanted = Math.min(WandEconomy.CRUDE_REFINE_CENTIVIS_PER_OP, headroom);
        if (wanted <= 0) {
            return;
        }
        int gained = drawRefined(level, player, wanted);
        if (gained <= 0) {
            if (remaining % NO_AURA_MESSAGE_PERIOD == 0) {
                TTActionBar.sendPurple(player, NO_AURA_KEY);
            }
            return;
        }
        WandVisHelper.topUpCentivis(wand, target, gained, true);
        sparkle(level, player, target);
    }

    private static int drawRefined(ServerLevel level, Player player, int wantedCentivis) {
        float rawCost = wantedCentivis * (float) WandEconomy.RAW_TO_PRIMAL_RATIO / WandEconomy.CENTIVIS_PER_VIS;
        double drained = AuraHelper.drainVis(level, player.blockPosition(), rawCost, false);
        return (int) (drained * WandEconomy.CENTIVIS_PER_VIS / WandEconomy.RAW_TO_PRIMAL_RATIO);
    }

    private @Nullable ResourceKey<IAspect> chooseRefineTarget(ItemStack wand, ServerLevel level, Player player) {
        ResourceKey<IAspect> aimedCrystal = crystalPrimalUnderCrosshair(player, level);
        return aimedCrystal != null ? aimedCrystal : emptiestPrimal(wand);
    }

    private static @Nullable ResourceKey<IAspect> emptiestPrimal(ItemStack wand) {
        int capacity = WandVisHelper.capacityOf(wand);
        return TTAspects.PRIMALS.stream().filter(primal -> WandVisHelper.storedIn(wand, primal) < capacity).min(Comparator.comparingInt(primal -> WandVisHelper.storedIn(wand, primal))).orElse(null);
    }

    private static @Nullable ResourceKey<IAspect> crystalPrimalUnderCrosshair(Player player, ServerLevel level) {
        BlockHitResult hit = trace(level, player, WandEconomy.CRUDE_REFINE_TARGET_RANGE);
        return hit.getType() == HitResult.Type.BLOCK ? primalOfCrystal(level.getBlockState(hit.getBlockPos())) : null;
    }

    private static @Nullable ResourceKey<IAspect> primalOfCrystal(BlockState state) {
        if (state.getBlock() instanceof BlockCrystal crystal && !crystal.isFlux() && TTAspects.PRIMALS.contains(crystal.aspect())) {
            return crystal.aspect();
        }
        return null;
    }

    private static void sparkle(ServerLevel level, Player player, ResourceKey<IAspect> aspect) {
        Vec3 eye = player.getEyePosition();
        Vec3 hand = eye.add(player.getLookAngle().scale(HAND_FORWARD_OFFSET)).subtract(0.0, HAND_DOWN_OFFSET, 0.0);
        int color = WandVisHelper.colorOf(level.registryAccess(), aspect);
        RandomSource random = level.getRandom();
        double dx = (random.nextFloat() - random.nextFloat()) * REFINE_SPARKLE_SPREAD;
        double dy = random.nextFloat() * REFINE_SPARKLE_SPREAD;
        double dz = (random.nextFloat() - random.nextFloat()) * REFINE_SPARKLE_SPREAD;
        EffectDispatch.spawnVisSparkle(level, eye.add(dx, dy, dz), hand, color);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if (owner instanceof Player player) {
            Optional.ofNullable(assembly(stack).rod().onUpdate()).ifPresent(callback -> callback.onUpdate(stack, player));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        HolderLookup.Provider registries = context.registries();
        int wholeVisCapacity = WandVisHelper.capacityOf(stack) / WandEconomy.CENTIVIS_PER_VIS;
        builder.accept(Component.translatable(CAPACITY_KEY, wholeVisCapacity).withStyle(ChatFormatting.GOLD));
        Optional.ofNullable(visAmounts(stack, registries)).ifPresent(builder);
        builder.accept(WandTooltips.costSummary(registries, costPercents(stack)));
        ItemStack socketed = getFocusStack(stack);
        if (socketed.isEmpty()) {
            return;
        }
        builder.accept(focusTitle(socketed));
        if (registries != null) {
            FocusItem.describe(socketed, registries, builder);
        }
    }

    private static Map<ResourceKey<IAspect>, Integer> costPercents(ItemStack stack) {
        return TTAspects.PRIMALS.stream()
                .collect(Collectors.toMap(Function.identity(), primal -> Math.round(WandVisHelper.costFactor(stack, null, primal, false) * PERCENT), (first, second) -> first, LinkedHashMap::new));
    }

    private static MutableComponent focusTitle(ItemStack focus) {
        return focus.getHoverName().copy().withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD, ChatFormatting.ITALIC);
    }

    private static @Nullable MutableComponent visAmounts(ItemStack stack, HolderLookup.@Nullable Provider registries) {
        DecimalFormat format = new DecimalFormat(VIS_AMOUNT_PATTERN, DecimalFormatSymbols.getInstance(Locale.ROOT));
        List<MutableComponent> amounts = new ArrayList<>();
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            int stored = WandVisHelper.storedIn(stack, primal);
            if (stored > 0) {
                String text = format.format(stored / (float) WandEconomy.CENTIVIS_PER_VIS);
                amounts.add(Component.literal(text).withStyle(WandTooltips.primalColor(registries, primal)));
            }
        }
        if (amounts.isEmpty()) {
            return null;
        }
        MutableComponent line = amounts.get(0);
        for (MutableComponent amount : amounts.subList(1, amounts.size())) {
            line.append(Component.literal(VIS_SEPARATOR).withStyle(ChatFormatting.DARK_GRAY)).append(amount);
        }
        return line;
    }
}
