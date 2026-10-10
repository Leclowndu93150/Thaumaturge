package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.infusion.EssentiaSources;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
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
    private static final int SOURCE_RANGE = 16;
    private static final int WORK_INTERVAL = 5;
    private static final int INTAKE_SUCTION = 128;
    private static final int SINGLE_POINT = 1;
    private static final int VISUAL_EXTENSION = 10;
    private static final long SEARCH_BACKOFF_TICKS = 200L;

    private final boolean input;
    private final EssentiaSources sources;
    private int ticks;
    private long resumeAt;
    private boolean searchFailed;

    public BlockEntityEssentiaPort(BlockPos pos, BlockState state) {
        this(pos, state, state.getBlock() instanceof BlockEssentiaPort port && port.isInput());
    }

    public BlockEntityEssentiaPort(BlockPos pos, BlockState state, boolean input) {
        super(TTBlockEntities.ESSENTIA_PORT.get(), pos, state);
        this.input = input;
        this.sources = new EssentiaSources(pos, SOURCE_RANGE).facing(state.getValue(BlockStateProperties.FACING)).drainEffectTarget(Vec3.atCenterOf(pos));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaPort port) {
        if (++port.ticks % WORK_INTERVAL != 0 || !(level instanceof ServerLevel server) || server.getGameTime() < port.resumeAt) {
            return;
        }
        Direction toTube = port.tubeSide();
        Direction tubeFace = toTube.getOpposite();
        IEssentiaTransport tube = EssentiaFlowHandler.transport(server, pos.relative(toTube), tubeFace);
        if (tube == null || !tube.isConnectable(tubeFace)) {
            return;
        }
        if (port.input) {
            port.pushIntoSources(server, tube, tubeFace);
        } else {
            port.pullFromSources(server, pos, tube, tubeFace);
        }
        if (port.searchFailed) {
            port.resumeAt = server.getGameTime() + SEARCH_BACKOFF_TICKS;
        }
        port.searchFailed = false;
    }

    private boolean pushIntoSources(ServerLevel server, IEssentiaTransport tube, Direction tubeFace) {
        if (!tube.canOutputTo(tubeFace) || tube.getEssentiaAmount(tubeFace) < SINGLE_POINT || tube.getSuctionAmount(tubeFace) >= INTAKE_SUCTION || tube.getMinimumSuction() > INTAKE_SUCTION) {
            return false;
        }
        Holder<IAspect> aspect = tube.getEssentiaType(tubeFace);
        if (aspect == null || tube.takeEssentia(aspect, SINGLE_POINT, tubeFace) != SINGLE_POINT) {
            return false;
        }
        if (sources.insert(server, aspect, VISUAL_EXTENSION)) {
            return true;
        }
        searchFailed = true;
        if (tube.addEssentia(aspect, SINGLE_POINT, tubeFace) != SINGLE_POINT) {
            AuraHelper.addFlux(server, worldPosition, SINGLE_POINT);
        }
        return false;
    }

    private boolean pullFromSources(ServerLevel server, BlockPos pos, IEssentiaTransport tube, Direction tubeFace) {
        Holder<IAspect> wanted = tube.getSuctionType(tubeFace);
        if (wanted == null || !tube.canInputFrom(tubeFace) || tube.getSuctionAmount(tubeFace) <= 0 || tube.addEssentia(wanted, SINGLE_POINT, tubeFace, true) < SINGLE_POINT) {
            return false;
        }
        if (!sources.drain(server, wanted, VISUAL_EXTENSION)) {
            searchFailed = true;
            return false;
        }
        if (tube.addEssentia(wanted, SINGLE_POINT, tubeFace) == SINGLE_POINT) {
            return true;
        }
        if (!sources.insert(server, wanted, VISUAL_EXTENSION)) {
            AuraHelper.addFlux(server, pos, SINGLE_POINT);
        }
        return false;
    }

    private Direction tubeSide() {
        Direction mount = getBlockState().getValue(BlockStateProperties.FACING);
        return mount.getOpposite();
    }

    private boolean touchesTube(@Nullable Direction face) {
        return face != null && face == tubeSide();
    }

    @Override
    public boolean isConnectable(Direction face) {
        return touchesTube(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return touchesTube(face) && input;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return touchesTube(face) && !input;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (input && touchesTube(face)) {
            return INTAKE_SUCTION;
        }
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (amount != SINGLE_POINT || !input || !touchesTube(face)) {
            return 0;
        }
        if (level instanceof ServerLevel server && sources.insert(server, aspect, VISUAL_EXTENSION)) {
            return SINGLE_POINT;
        }
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int spaceFor(Holder<IAspect> aspect, Direction face) {
        return input && touchesTube(face) ? SINGLE_POINT : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
