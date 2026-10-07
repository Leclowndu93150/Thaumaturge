package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockEldritchAltar extends BlockEldritchStructure implements EntityBlock {
    private static final ResourceLocation OCULUS_RESEARCH = TTIds.rl("oculus");

    public static final MapCodec<BlockEldritchAltar> CODEC = simpleCodec(BlockEldritchAltar::new);

    private static final int MAX_EYES = 4;
    private static final int RITUAL_CHARGE = 100;

    private static final VoxelShape SHAPE = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 1.0, 16.0),
            box(0.0, 1.0, 1.0, 16.0, 2.0, 15.0),
            box(1.0, 1.0, 0.0, 15.0, 2.0, 1.0),
            box(1.0, 1.0, 15.0, 15.0, 2.0, 16.0),
            box(1.0, 2.0, 1.0, 15.0, 5.0, 15.0),
            box(1.0, 5.0, 2.0, 15.0, 6.0, 14.0),
            box(2.0, 5.0, 1.0, 14.0, 6.0, 2.0),
            box(2.0, 5.0, 14.0, 14.0, 6.0, 15.0),
            box(2.0, 6.0, 2.0, 14.0, 9.0, 14.0),
            box(2.0, 9.0, 3.0, 14.0, 10.0, 13.0),
            box(3.0, 9.0, 2.0, 13.0, 10.0, 3.0),
            box(3.0, 9.0, 13.0, 13.0, 10.0, 14.0),
            box(3.0, 10.0, 3.0, 13.0, 13.0, 13.0),
            box(3.0, 13.0, 4.0, 13.0, 14.0, 12.0),
            box(4.0, 13.0, 3.0, 12.0, 14.0, 4.0),
            box(4.0, 13.0, 12.0, 12.0, 14.0, 13.0));

    public BlockEldritchAltar(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockEldritchAltar> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityEldritchAltar(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return type == TTBlockEntities.ELDRITCH_ALTAR.get()
                ? (tickLevel, pos, tickState, altar) -> ((BlockEntityEldritchAltar) altar).serverTick(tickLevel, pos)
                : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof BlockEntityEldritchAltar altar)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(TTItems.ELDRITCH_EYE.get())) {
            if (altar.getEyes() >= MAX_EYES) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }
            if (altar.getEyes() >= 2) {
                altar.setSpawner(true);
                altar.setSpawnType(BlockEntityEldritchAltar.SPAWN_GUARDIAN);
            }
            altar.setEyes((byte) (altar.getEyes() + 1));
            altar.checkForMaze();
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            altar.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
            level.playSound(null, pos, TTSounds.CRYSTAL.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }
        if (stack.getItem() instanceof ICaster) {
            if (altar.getEyes() < MAX_EYES) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (level.isClientSide()) {
                return ItemInteractionResult.SUCCESS;
            }
            if (!altar.checkForMaze()) {
                return ItemInteractionResult.SUCCESS;
            }
            if (!KnowledgeAccess.of(player).isResearchComplete(OCULUS_RESEARCH)) {
                TTActionBar.sendPurple(player, "gui.thaumaturge.altar.ritual_unknown");
                return ItemInteractionResult.SUCCESS;
            }
            if (AuraHelper.drainVis(level, pos, RITUAL_CHARGE, true) >= RITUAL_CHARGE) {
                AuraHelper.drainVis(level, pos, RITUAL_CHARGE, false);
                altar.openPortal();
            } else {
                TTActionBar.sendPurple(player, "gui.thaumaturge.altar.not_enough_vis");
            }
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
