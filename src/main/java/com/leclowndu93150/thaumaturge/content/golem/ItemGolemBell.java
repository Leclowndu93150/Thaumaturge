package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemHelper;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.golem.logistics.LogisticsGuiOpener;
import com.leclowndu93150.thaumaturge.content.golem.logistics.LogisticsTarget;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealAccess;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Arrays;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ItemGolemBell extends Item implements ISealDisplayer {
    private static final double AIM_LENGTH = 5.0D;
    private static final double AIM_STEP = 0.05D;
    private static final float CHIME_VOLUME = 0.5F;
    private static final float CHIME_PITCH_SPREAD = 0.2F;
    private static final float ZAP_VOLUME = 0.4F;
    private static final int AIM_SAMPLES = (int) Math.round(AIM_LENGTH / AIM_STEP);

    public ItemGolemBell(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.swing(hand);
        if (level.isClientSide()) {
            chime(level, player);
            return InteractionResult.SUCCESS;
        }
        ISealEntity aimed = getAimedSeal(player);
        if (aimed == null && !LogisticsGuiOpener.canOpen(player)) {
            return super.use(level, player, hand);
        }
        if (aimed == null) {
            LogisticsGuiOpener.open(player, null);
        } else {
            act((ServerLevel) level, player, aimed);
        }
        return InteractionResult.FAIL;
    }

    private static void chime(Level level, Player player) {
        float pitch = 1.0F + level.getRandom().nextFloat() * CHIME_PITCH_SPREAD;
        level.playSound(player, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, CHIME_VOLUME, pitch);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        ISealEntity seal = GolemHelper.getSealEntity(level, new SealPos(pos, face));
        if (seal == null && !LogisticsGuiOpener.canOpen(player)) {
            return InteractionResult.PASS;
        }
        player.swing(context.getHand());
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (seal == null) {
            LogisticsGuiOpener.open(player, new LogisticsTarget(pos, face));
        } else {
            act((ServerLevel) level, player, seal);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    public static @Nullable ISealEntity getAimedSeal(Player player) {
        Level level = player.level();
        Vec3 origin = player.getEyePosition();
        Vec3 direction = player.getLookAngle();
        BlockPos behind = BlockPos.containing(origin);
        int step = 0;
        while (++step <= AIM_SAMPLES) {
            BlockPos ahead = BlockPos.containing(origin.add(direction.scale(step * AIM_STEP)));
            if (ahead.equals(behind)) {
                continue;
            }
            ISealEntity found = sealOnEnteredFace(level, behind, ahead);
            if (found != null) {
                return found;
            }
            if (!level.getBlockState(ahead).getCollisionShape(level, ahead).isEmpty()) {
                break;
            }
            behind = ahead;
        }
        return null;
    }

    private static @Nullable ISealEntity sealOnEnteredFace(Level level, BlockPos from, BlockPos cell) {
        return Arrays.stream(Direction.values()).filter(face -> crossedOnAxis(from, cell, face)).map(face -> GolemHelper.getSealEntity(level, new SealPos(cell, face))).filter(Objects::nonNull)
                .findFirst().orElse(null);
    }

    private static boolean crossedOnAxis(BlockPos from, BlockPos cell, Direction face) {
        int delta = cell.get(face.getAxis()) - from.get(face.getAxis());
        return delta != 0 && Integer.signum(delta) == -face.getAxisDirection().getStep();
    }

    private static void act(ServerLevel level, Player player, ISealEntity seal) {
        if (!player.isShiftKeyDown()) {
            SealGuiOpener.open(player, seal);
            return;
        }
        if (SealAccess.mayEdit(player, seal)) {
            SealHandler.withdraw(level, seal.pos(), false);
            level.playSound(null, seal.pos().pos(), TTSounds.ZAP.get(), SoundSource.BLOCKS, ZAP_VOLUME, 1.0F);
        }
    }
}
