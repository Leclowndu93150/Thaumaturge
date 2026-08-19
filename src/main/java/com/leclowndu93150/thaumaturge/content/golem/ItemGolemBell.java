package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.golems.ISealDisplayer;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class ItemGolemBell extends Item implements ISealDisplayer {
    private static final double AIM_RANGE = 5.0;
    private static final double AIM_STEP = 0.1;
    private static final int LOGISTICS_RESEARCH_STAGE = 1;

    public ItemGolemBell(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.swing(hand, true);
        if (level.isClientSide()) {
            player.playSound(
                    SoundEvents.NOTE_BLOCK_BELL.value(),
                    0.6F,
                    1.0F + level.getRandom().nextFloat() * 0.1F);
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        ISealEntity seal = getAimedSeal(player);
        if (seal != null) {
            if (player.isShiftKeyDown()) {
                SealHandler.removeSealEntity((ServerLevel) level, seal.getSealPos(), false);
                level.playSound(null, seal.getSealPos().pos(), TCSounds.ZAP.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            } else {
                SealGuiOpener.open(player, seal);
            }
            return InteractionResultHolder.fail(player.getItemInHand(hand));
        }
        if (player.isShiftKeyDown() && player instanceof ServerPlayer serverPlayer) {
            openLogistics(serverPlayer, null, null);
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return interactWithSeal(context);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return interactWithSeal(context);
    }

    private InteractionResult interactWithSeal(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        ISealEntity seal =
                SealHandler.getSealEntity(level, new SealPos(context.getClickedPos(), context.getClickedFace()));
        if (seal != null) {
            player.swing(context.getHand(), true);
            if (level.isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (player.isShiftKeyDown()) {
                SealHandler.removeSealEntity((ServerLevel) level, seal.getSealPos(), false);
                level.playSound(null, context.getClickedPos(), TCSounds.ZAP.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            } else {
                SealGuiOpener.open(player, seal);
            }
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                Direction face = context.getClickedFace();
                openLogistics(serverPlayer, context.getClickedPos(), face);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private static void openLogistics(ServerPlayer player, @Nullable BlockPos destination, @Nullable Direction side) {
        if (!KnowledgeAccess.of(player).isResearchKnown(TCIds.rl("golem_logistics"), LOGISTICS_RESEARCH_STAGE)) {
            return;
        }
        player.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) ->
                                MenuGolemLogistics.server(containerId, inventory, destination, side),
                        Component.translatable("gui.thaumaturge.golem_logistics")),
                buffer -> {});
    }

    public static @Nullable ISealEntity getAimedSeal(Player player) {
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle().scale(AIM_RANGE);
        Vec3 step = look.scale(AIM_STEP);
        Vec3 probe = start.add(step);
        BlockPos previous = BlockPos.containing(start);
        for (int i = 0; i < (int) (look.length() * 10.0); i++) {
            BlockPos cell = BlockPos.containing(probe);
            if (!cell.equals(previous)) {
                BlockPos delta = cell.subtract(previous);
                ISealEntity seal = sealOnEntryFace(player, cell, delta);
                if (seal != null) {
                    return seal;
                }
                previous = cell;
            }
            probe = probe.add(step);
        }
        return null;
    }

    private static @Nullable ISealEntity sealOnEntryFace(Player player, BlockPos cell, BlockPos delta) {
        if (delta.getX() != 0) {
            ISealEntity seal = SealHandler.getSealEntity(
                    player.level(), new SealPos(cell, delta.getX() > 0 ? Direction.WEST : Direction.EAST));
            if (seal != null) {
                return seal;
            }
        }
        if (delta.getY() != 0) {
            ISealEntity seal = SealHandler.getSealEntity(
                    player.level(), new SealPos(cell, delta.getY() > 0 ? Direction.DOWN : Direction.UP));
            if (seal != null) {
                return seal;
            }
        }
        if (delta.getZ() != 0) {
            return SealHandler.getSealEntity(
                    player.level(), new SealPos(cell, delta.getZ() > 0 ? Direction.NORTH : Direction.SOUTH));
        }
        return null;
    }
}
