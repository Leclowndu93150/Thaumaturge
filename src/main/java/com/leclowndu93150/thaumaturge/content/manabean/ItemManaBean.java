package com.leclowndu93150.thaumaturge.content.manabean;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEffectTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class ItemManaBean extends Item {
    private static final int INSTANT_EFFECT_AMPLIFIER = 0;
    private static final double INSTANT_EFFECT_SCALE = 1.0;
    private static final int EFFECT_MIN_DURATION_TICKS = 160;
    private static final int EFFECT_DURATION_SPREAD_TICKS = 80;
    private static final int EFFECT_AMPLIFIER = 0;
    private static final float ASPECT_GRANT_CHANCE = 0.25F;
    private static final int ASPECT_GRANT_AMOUNT = 1;
    private static final int BEAN_ASPECT_AMOUNT = 1;

    public ItemManaBean(Properties properties) {
        super(properties);
    }

    public static @Nullable Holder<IAspect> aspectOf(ItemStack stack) {
        AspectInstance instance = stack.get(TTDataComponents.CRYSTAL_ASPECT.get());
        return instance == null ? null : instance.aspect();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Holder<IAspect> aspect = aspectOf(stack);
        ItemStack result = super.finishUsingItem(stack, level, entity);
        if (level instanceof ServerLevel serverLevel && entity instanceof ServerPlayer player) {
            RandomSource random = serverLevel.getRandom();
            serverLevel.registryAccess().lookupOrThrow(Registries.MOB_EFFECT).getRandomElementOf(TTEffectTags.MANA_BEAN_EFFECTS, random)
                    .ifPresent(effect -> applyEffect(serverLevel, player, effect, random));
            if (aspect != null && random.nextFloat() < ASPECT_GRANT_CHANCE) {
                AspectPools.grant(player, aspect, ASPECT_GRANT_AMOUNT);
            }
        }
        return result;
    }

    private static void applyEffect(ServerLevel level, ServerPlayer player, Holder<MobEffect> effect, RandomSource random) {
        if (effect.value().isInstantenous()) {
            effect.value().applyInstantenousEffect(level, null, null, player, INSTANT_EFFECT_AMPLIFIER, INSTANT_EFFECT_SCALE);
        } else {
            player.addEffect(new MobEffectInstance(effect, EFFECT_MIN_DURATION_TICKS + random.nextInt(EFFECT_DURATION_SPREAD_TICKS), EFFECT_AMPLIFIER));
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (stack.has(TTDataComponents.CRYSTAL_ASPECT.get())) {
            return;
        }
        List<Holder<IAspect>> aspects = new ArrayList<>();
        level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).listElements().forEach(aspects::add);
        if (!aspects.isEmpty()) {
            stack.set(TTDataComponents.CRYSTAL_ASPECT.get(), new AspectInstance(aspects.get(level.getRandom().nextInt(aspects.size())), BEAN_ASPECT_AMOUNT));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos clicked = context.getClickedPos();
        BlockPos below = clicked.below();
        if (player == null || context.getClickedFace() != Direction.DOWN || !level.getBlockState(clicked).is(BlockTags.LOGS) || !level.getBlockState(below).isAir()
                || !BlockManaPod.canGrowAt(level, below)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        Holder<IAspect> aspect = aspectOf(stack);
        level.setBlock(below, TTBlocks.MANA_POD.get().defaultBlockState(), Block.UPDATE_ALL);
        if (aspect != null && level.getBlockEntity(below) instanceof BlockEntityManaPod pod) {
            pod.setAspect(aspect.unwrapKey().orElse(null));
        }
        if (!player.hasInfiniteMaterials()) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }
}
