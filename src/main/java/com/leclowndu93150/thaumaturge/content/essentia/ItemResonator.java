package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.AspectCapabilities;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityCondenser;
import com.leclowndu93150.thaumaturge.content.essentia.tube.BlockEntityTubeBuffer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.TubeGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ItemResonator extends Item {
    private static final String CONTENTS_KEY = "message.thaumaturge.resonator.contents";
    private static final String SUCTION_KEY = "message.thaumaturge.resonator.suction";
    private static final String UNTYPED_KEY = "message.thaumaturge.resonator.untyped";
    private static final String CONDENSER_COST_KEY = "message.thaumaturge.resonator.condenser_cost";
    private static final String CONDENSER_TIME_KEY = "message.thaumaturge.resonator.condenser_time";
    private static final int TICKS_PER_SECOND = 20;
    private static final double CENTER_OFFSET = 0.5;
    private static final float SOUND_VOLUME = 0.5F;
    private static final float SOUND_BASE_PITCH = 1.9F;
    private static final float SOUND_PITCH_SPREAD = 0.1F;

    public ItemResonator(Item.Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction face = context.getClickedFace();
        if (player == null) {
            return InteractionResult.FAIL;
        }
        if (level.getBlockEntity(pos) instanceof BlockEntityTubeBuffer) {
            int part = TubeGeometry.BUFFER.subHit(context.getClickLocation().subtract(Vec3.atLowerCornerOf(pos)));
            // An arm's surface normal can point across the arm rather than along its connection.
            if (part != TubeGeometry.CORE_HIT) {
                face = Direction.from3DDataValue(part);
            }
        }
        IEssentiaConduit conduit = EssentiaAccess.conduit(level, pos, face);
        if (conduit == null) {
            return InteractionResult.FAIL;
        }
        if (level.isClientSide()) {
            player.swing(context.getHand());
            return InteractionResult.SUCCESS;
        }
        reportContents(player, level, pos, face, conduit);
        reportSuction(player, conduit, face);
        reportCondenser(player, level.getBlockEntity(pos));
        level.playSound(
                null,
                pos.getX() + CENTER_OFFSET,
                pos.getY() + CENTER_OFFSET,
                pos.getZ() + CENTER_OFFSET,
                SoundEvents.SHIELD_BLOCK,
                SoundSource.BLOCKS,
                SOUND_VOLUME,
                SOUND_BASE_PITCH + level.getRandom().nextFloat() * SOUND_PITCH_SPREAD);
        return InteractionResult.SUCCESS;
    }

    private static void reportContents(
            Player player, Level level, BlockPos pos, Direction face, IEssentiaConduit conduit) {
        IAspectContainer container = level.getBlockEntity(pos) instanceof BlockEntityTubeBuffer
                ? level.getCapability(AspectCapabilities.CONTAINER, pos, face)
                : null;
        if (container != null) {
            for (AspectInstance entry : container.getAspects().sortedByTag()) {
                player.sendSystemMessage(contents(entry.amount(), entry.aspect()));
            }
            return;
        }
        Holder<IAspect> type = conduit.heldAspect(face);
        if (type != null) {
            player.sendSystemMessage(contents(conduit.heldAmount(face), type));
        }
    }

    private static void reportSuction(Player player, IEssentiaConduit conduit, Direction face) {
        EssentiaPull pull = conduit.pull(face);
        Holder<IAspect> type = pull.aspect();
        Component name = type == null ? Component.translatable(UNTYPED_KEY) : AspectComponents.trueName(type);
        player.sendSystemMessage(Component.translatable(SUCTION_KEY, Integer.toString(pull.strength()), name));
    }

    private static void reportCondenser(Player player, @Nullable BlockEntity entity) {
        if (entity instanceof BlockEntityCondenser condenser) {
            player.sendSystemMessage(Component.translatable(CONDENSER_COST_KEY, Integer.toString(condenser.cost())));
            player.sendSystemMessage(Component.translatable(
                    CONDENSER_TIME_KEY,
                    Integer.toString(condenser.interval()),
                    Integer.toString(condenser.interval() / TICKS_PER_SECOND)));
        }
    }

    private static Component contents(int amount, Holder<IAspect> aspect) {
        return Component.translatable(CONTENTS_KEY, Integer.toString(amount), AspectComponents.trueName(aspect));
    }
}
