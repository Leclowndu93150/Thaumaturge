package com.leclowndu93150.thaumaturge.content.taint.entity.ability;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

final class TaintedHarvest {
    private static final int MIN_WOOL = 1;
    private static final int EXTRA_WOOL = 2;
    private static final float WOOL_DROP_HEIGHT = 1.0F;
    private static final float SHEARING_FLUX = 3.0F;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;

    private TaintedHarvest() {}

    static @Nullable InteractionResult interact(Player player, InteractionHand hand, ItemStack held, LivingEntity mob) {
        if (held.is(Tags.Items.TOOLS_SHEAR)
                && mob instanceof Sheep sheep
                && MobTraits.has(sheep, TTMobTraits.TAINT_SHEARING.getKey())) {
            return shear(player, hand, held, sheep);
        }
        if (held.is(Items.BUCKET) && !mob.isBaby() && MobTraits.has(mob, TTMobTraits.FLUX_MILK.getKey())) {
            return milk(player, hand, held);
        }
        return null;
    }

    private static InteractionResult shear(Player player, InteractionHand hand, ItemStack shears, Sheep sheep) {
        if (!(sheep.level() instanceof ServerLevel level) || !sheep.readyForShearing()) {
            return InteractionResult.CONSUME;
        }
        level.playSound(null, sheep, SoundEvents.SHEEP_SHEAR, SoundSource.PLAYERS, SOUND_VOLUME, SOUND_PITCH);
        sheep.setSheared(true);
        sheep.spawnAtLocation(
                new ItemStack(Items.PURPLE_WOOL, MIN_WOOL + level.getRandom().nextInt(EXTRA_WOOL + 1)),
                WOOL_DROP_HEIGHT);
        AuraHelper.polluteAura(level, sheep.blockPosition(), SHEARING_FLUX, true);
        sheep.gameEvent(GameEvent.SHEAR, player);
        shears.hurtAndBreak(1, player, LivingEntity.getSlotForHand(hand));
        return InteractionResult.SUCCESS;
    }

    private static InteractionResult milk(Player player, InteractionHand hand, ItemStack bucket) {
        player.playSound(SoundEvents.COW_MILK, SOUND_VOLUME, SOUND_PITCH);
        player.setItemInHand(
                hand, ItemUtils.createFilledResult(bucket, player, new ItemStack(TTItems.BUCKET_FLUX_GOO.get())));
        return InteractionResult.SUCCESS;
    }
}
