package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.mixin.server.network.ServerGamePacketListenerImplAccessor;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ElementalSwordItem extends SwordItem implements IChanneledItem {
    private static final int USE_DURATION = 72000;
    private static final double FIELD_REACH = 2.5;
    private static final double DISTANCE_SOFTENING = 0.1;
    private static final double LIFT = 0.08;
    private static final double FALL_DIVISOR = 1.2;
    private static final double CEILING = 0.5;
    private static final double CEILING_RESET = 0.2;
    private static final int SMOKE_COLOR = 14540253;
    private static final int PULSE_INTERVAL_TICKS = 20;

    public ElementalSwordItem(Properties properties) {
        super(TTMaterials.TOOL_ELEMENTAL, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.thaumaturge.elemental_sword.toggle")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            boolean disabled = !stack.getOrDefault(TTDataComponents.WHIRLWIND_DISABLED.get(), false);
            stack.set(TTDataComponents.WHIRLWIND_DISABLED.get(), disabled);
            if (!level.isClientSide()) {
                TTActionBar.sendPurple(
                        player,
                        disabled
                                ? "message.thaumaturge.elemental_sword.whirlwind_off"
                                : "message.thaumaturge.elemental_sword.whirlwind_on");
                level.playSound(
                        null,
                        player.getX(),
                        player.getY(),
                        player.getZ(),
                        TTSounds.KEY.get(),
                        SoundSource.PLAYERS,
                        0.5F,
                        disabled ? 0.8F : 1.2F);
            }
            return InteractionResultHolder.success(stack);
        }
        if (stack.getOrDefault(TTDataComponents.WHIRLWIND_DISABLED.get(), false)) {
            return InteractionResultHolder.pass(stack);
        }
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int ticksRemaining) {
        super.onUseTick(level, player, stack, ticksRemaining);
        int ticks = USE_DURATION - ticksRemaining;
        Vec3 movement = player.getDeltaMovement();
        double dy = movement.y;
        if (dy < 0.0) {
            dy = dy / FALL_DIVISOR + LIFT;
            player.fallDistance = (float) (player.fallDistance / FALL_DIVISOR);
        } else {
            dy += LIFT;
        }
        if (dy > CEILING) {
            dy = CEILING_RESET;
        }
        player.setDeltaMovement(movement.x, dy, movement.z);
        if (player instanceof ServerPlayer serverPlayer) {
            ((ServerGamePacketListenerImplAccessor) serverPlayer.connection).setAboveGroundTickCount(0);
        }

        repelNearby(level, player);

        if (level.isClientSide()) {
            int minY = Mth.floor(player.getY()) - (player.onGround() ? 0 : 2);
            double centerY = player.getY() + player.getBbHeight() * 0.5;
            for (int a = 0; a < 5; a++) {
                ClientEffects.smokeSpiral(
                        level,
                        player.getX(),
                        centerY,
                        player.getZ(),
                        1.5F,
                        level.getRandom().nextInt(360),
                        minY,
                        SMOKE_COLOR);
            }
            if (player.onGround()) {
                double heading = level.getRandom().nextDouble() * Mth.TWO_PI;
                level.addParticle(
                        ParticleTypes.SMOKE,
                        player.getX(),
                        player.getY() + 0.1,
                        player.getZ(),
                        Math.cos(heading) * 0.2,
                        0.0,
                        Math.sin(heading) * 0.2);
            }
        } else if (ticks == 0 || ticks % PULSE_INTERVAL_TICKS == 0) {
            level.playSound(
                    null,
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    TTSounds.WIND.get(),
                    SoundSource.PLAYERS,
                    0.5F,
                    0.9F + level.getRandom().nextFloat() * 0.2F);
            player.gameEvent(GameEvent.ELYTRA_GLIDE);
        }

        if (!level.isClientSide() && ticks % PULSE_INTERVAL_TICKS == 0) {
            stack.hurtAndBreak(
                    1,
                    player,
                    player.getUsedItemHand() == InteractionHand.OFF_HAND
                            ? EquipmentSlot.OFFHAND
                            : EquipmentSlot.MAINHAND);
        }
    }

    private static void repelNearby(Level level, LivingEntity user) {
        if (level.isClientSide()) {
            return;
        }
        AABB field = user.getBoundingBox().inflate(FIELD_REACH);
        Vec3 origin = user.position();
        Entity mount = user.getVehicle();
        for (LivingEntity target : level.getEntitiesOfClass(
                LivingEntity.class,
                field,
                candidate -> candidate != user
                        && candidate != mount
                        && !(candidate instanceof Player)
                        && candidate.isAlive())) {
            Vec3 away = target.position().subtract(origin);
            double distance = away.length();
            target.push(away.scale(1.0 / (FIELD_REACH * (distance + DISTANCE_SOFTENING))));
        }
    }
}
