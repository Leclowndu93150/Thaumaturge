package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import com.leclowndu93150.thaumaturge.content.misc.TCActionBar;
import com.leclowndu93150.thaumaturge.mixin.server.network.ServerGamePacketListenerImplAccessor;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ElementalSwordItem extends Item implements IChanneledItem {
    private static final int USE_DURATION = 72000;
    private static final double PULL_RANGE = 2.5;
    private static final int SMOKE_COLOR = 14540253;
    private static final int PULSE_INTERVAL_TICKS = 20;

    public ElementalSwordItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("tooltip.thaumaturge.elemental_sword.toggle").withStyle(ChatFormatting.DARK_GRAY));
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.NONE;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            boolean disabled = !stack.getOrDefault(TCDataComponents.WHIRLWIND_DISABLED.get(), false);
            stack.set(TCDataComponents.WHIRLWIND_DISABLED.get(), disabled);
            if (!level.isClientSide()) {
                TCActionBar.sendPurple(player, disabled ? "tc.elemental_sword.whirlwind_off" : "tc.elemental_sword.whirlwind_on");
                level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.KEY.get(), SoundSource.PLAYERS, 0.5F, disabled ? 0.8F : 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        if (stack.getOrDefault(TCDataComponents.WHIRLWIND_DISABLED.get(), false)) {
            return InteractionResult.PASS;
        }
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int ticksRemaining) {
        super.onUseTick(level, player, stack, ticksRemaining);
        int ticks = USE_DURATION - ticksRemaining;
        Vec3 movement = player.getDeltaMovement();
        double dy = movement.y;
        if (dy < 0.0) {
            dy /= 1.2F;
            player.fallDistance /= 1.2F;
        }
        dy += 0.08F;
        if (dy > 0.5) {
            dy = 0.2F;
        }
        player.setDeltaMovement(movement.x, dy, movement.z);
        if (player instanceof ServerPlayer serverPlayer) {
            ((ServerGamePacketListenerImplAccessor) serverPlayer.connection).setAboveGroundTickCount(0);
        }

        List<Entity> targets = level.getEntities(player, player.getBoundingBox().inflate(PULL_RANGE));
        for (Entity entity : targets) {
            if (entity instanceof Player || !(entity instanceof LivingEntity) || !entity.isAlive() || player.getVehicle() == entity) {
                continue;
            }
            Vec3 p = player.position();
            Vec3 t = entity.position();
            double distance = p.distanceTo(t) + 0.1;
            Vec3 r = t.subtract(p);
            entity.push(r.x / PULL_RANGE / distance, r.y / PULL_RANGE / distance, r.z / PULL_RANGE / distance);
        }

        if (level.isClientSide()) {
            int miny = (int) (player.getBoundingBox().minY - 2.0);
            if (player.onGround()) {
                miny = Mth.floor(player.getBoundingBox().minY);
            }
            for (int a = 0; a < 5; a++) {
                ClientEffects.smokeSpiral(level, player.getX(), player.getBoundingBox().minY + player.getBbHeight() / 2.0F, player.getZ(), 1.5F, level.getRandom().nextInt(360), miny, SMOKE_COLOR);
            }
            if (player.onGround()) {
                float r1 = level.getRandom().nextFloat() * 360.0F;
                float mx = -Mth.sin(r1 / 180.0F * (float) Math.PI) / 5.0F;
                float mz = Mth.cos(r1 / 180.0F * (float) Math.PI) / 5.0F;
                level.addParticle(ParticleTypes.SMOKE, player.getX(), player.getBoundingBox().minY + 0.1F, player.getZ(), mx, 0.0, mz);
            }
        } else if (ticks == 0 || ticks % PULSE_INTERVAL_TICKS == 0) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.WIND.get(), SoundSource.PLAYERS, 0.5F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }

        if (ticks % PULSE_INTERVAL_TICKS == 0) {
            stack.hurtAndBreak(1, player, player.getUsedItemHand() == InteractionHand.OFF_HAND ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND);
        }
    }
}
