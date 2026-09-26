package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.infusion.EssentiaSources;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaPort extends BlockEntity implements IEssentiaTransport {
    private static final int WORK_INTERVAL = 5;
    private static final int SEARCH_RANGE = 16;
    private static final int SUCTION = 128;

    private final boolean input;
    private final EssentiaSources sources;
    private int count;

    public BlockEntityEssentiaPort(BlockPos pos, BlockState state) {
        this(pos, state, state.getBlock() instanceof BlockEssentiaPort port && port.isInput());
    }

    public BlockEntityEssentiaPort(BlockPos pos, BlockState state, boolean input) {
        super(TCBlockEntities.ESSENTIA_PORT.get(), pos, state);
        this.input = input;
        this.sources = new EssentiaSources(pos, SEARCH_RANGE).facing(state.getValue(BlockStateProperties.FACING)).drainEffectTarget(Vec3.atCenterOf(pos));
    }

    private Direction facing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private Direction tubeSide() {
        return facing().getOpposite();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaPort port) {
        if (++port.count % WORK_INTERVAL != 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        if (port.input) {
            port.pullFromTube(server);
        } else {
            port.pushToTube(server);
        }
    }

    private void pullFromTube(ServerLevel server) {
        Direction tubeSide = tubeSide();
        IEssentiaTransport ic = EssentiaFlowHandler.transport(server, getBlockPos().relative(tubeSide), facing());
        if (ic == null || !ic.canOutputTo(facing())) {
            return;
        }
        if (ic.getEssentiaAmount(facing()) > 0 && ic.getSuctionAmount(facing()) < getSuctionAmount(tubeSide) && getSuctionAmount(tubeSide) >= ic.getMinimumSuction()) {
            Holder<IAspect> aspect = ic.getEssentiaType(facing());
            if (aspect != null && sources.insert(server, aspect, WORK_INTERVAL)) {
                ic.takeEssentia(aspect, 1, facing());
            }
        }
    }

    private void pushToTube(ServerLevel server) {
        Direction tubeSide = tubeSide();
        IEssentiaTransport ic = EssentiaFlowHandler.transport(server, getBlockPos().relative(tubeSide), facing());
        if (ic == null || !ic.canInputFrom(facing())) {
            return;
        }
        if (ic.getSuctionAmount(facing()) > 0) {
            Holder<IAspect> aspect = ic.getSuctionType(facing());
            if (aspect != null && sources.drain(server, aspect, WORK_INTERVAL) && ic.addEssentia(aspect, 1, facing()) == 0) {
                sources.insert(server, aspect, WORK_INTERVAL);
            }
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == tubeSide();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return input && face == tubeSide();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return !input && face == tubeSide();
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return input ? SUCTION : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (input && level instanceof ServerLevel server && amount == 1 && sources.insert(server, aspect, WORK_INTERVAL)) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
