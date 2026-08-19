package com.leclowndu93150.thaumaturge.content.misc.nitor;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public final class BlockNitor extends BaseEntityBlock {
    public static final MapCodec<BlockNitor> CODEC = RecordCodecBuilder
            .mapCodec(inst -> inst.group(DyeColor.CODEC.fieldOf("dye").forGetter(BlockNitor::dye), propertiesCodec()).apply(inst, BlockNitor::new));

    private static final VoxelShape SHAPE = box(5.28, 5.28, 5.28, 10.56, 10.56, 10.56);

    private final DyeColor dye;

    public BlockNitor(DyeColor dye, BlockBehaviour.Properties properties) {
        super(properties);
        this.dye = dye;
    }

    public DyeColor dye() {
        return this.dye;
    }

    public int dyeColor() {
        return this.dye.getMapColor().col;
    }

    @Override
    protected MapCodec<BlockNitor> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state) {
        return Shapes.empty();
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BlockEntityNitor(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, TCBlockEntities.NITOR.get(), BlockEntityNitor::clientTick);
    }
}
