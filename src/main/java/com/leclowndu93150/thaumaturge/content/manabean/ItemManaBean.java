package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCEffectTags;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ItemManaBean extends Item implements IEssentiaContainerItem {
    private static final int EFFECT_AMPLIFIER = 2;
    private static final double INSTANT_HEALTH_FACTOR = 3.0;
    private static final int EFFECT_BASE_DURATION = 160;
    private static final int EFFECT_EXTRA_DURATION = 80;
    private static final float GRANT_CHANCE = 0.25F;

    public ItemManaBean(Properties properties) {
        super(properties);
    }

    public static @Nullable Holder<IAspect> aspectOf(ItemStack stack) {
        AspectInstance stored = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
        return stored == null ? null : stored.aspect();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            RandomSource random = serverLevel.getRandom();
            Optional<Holder<MobEffect>> rolled = serverLevel.registryAccess().lookupOrThrow(TCEffectTags.MANA_BEAN_EFFECTS.registry()).get(TCEffectTags.MANA_BEAN_EFFECTS)
                    .flatMap(set -> set.getRandomElement(random));
            rolled.ifPresent(effect -> {
                if (effect.value().isInstantenous()) {
                    effect.value().applyInstantenousEffect(serverLevel, player, player, player, EFFECT_AMPLIFIER, INSTANT_HEALTH_FACTOR);
                } else {
                    player.addEffect(new MobEffectInstance(effect, EFFECT_BASE_DURATION + random.nextInt(EFFECT_EXTRA_DURATION), 0));
                }
            });
            Holder<IAspect> aspect = aspectOf(stack);
            if (aspect != null && random.nextFloat() < GRANT_CHANCE) {
                AspectPools.grant(player, aspect, 1);
            }
        }
        return super.finishUsingItem(stack, level, entity);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!stack.has(TCDataComponents.CRYSTAL_ASPECT.get())) {
            assignRandomAspect(stack, level);
        }
    }

    private static void assignRandomAspect(ItemStack stack, ServerLevel level) {
        List<Holder.Reference<IAspect>> aspects = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().toList();
        if (!aspects.isEmpty()) {
            Holder<IAspect> aspect = aspects.get(level.getRandom().nextInt(aspects.size()));
            stack.set(TCDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspect, 1));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || context.getClickedFace() != Direction.DOWN) {
            return InteractionResult.PASS;
        }
        BlockPos logPos = context.getClickedPos();
        BlockPos podPos = logPos.below();
        if (!level.getBlockState(logPos).is(BlockTags.LOGS) || !level.getBlockState(podPos).isAir() || !BlockManaPod.canGrowAt(level, podPos)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        level.setBlock(podPos, TCBlocks.MANA_POD.get().defaultBlockState(), 3);
        if (level.getBlockEntity(podPos) instanceof BlockEntityManaPod pod) {
            Holder<IAspect> aspect = aspectOf(context.getItemInHand());
            if (aspect != null) {
                pod.setAspect(aspect.unwrapKey().orElse(null));
            }
        }
        if (!player.hasInfiniteMaterials()) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public AspectList getAspects(ItemStack stack) {
        AspectInstance stored = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
        return stored == null ? AspectList.EMPTY : AspectList.of(stored);
    }

    @Override
    public void setAspects(ItemStack stack, AspectList aspects) {
        if (aspects == null || aspects.isEmpty()) {
            stack.remove(TCDataComponents.CRYSTAL_ASPECT.get());
            return;
        }
        stack.set(TCDataComponents.CRYSTAL_ASPECT.get(), aspects.entries().getFirst().withAmount(1));
    }

    @Override
    public boolean ignoreContainedAspects() {
        return false;
    }
}
