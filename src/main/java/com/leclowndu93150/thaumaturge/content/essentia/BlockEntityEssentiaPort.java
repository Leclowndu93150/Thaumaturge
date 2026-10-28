package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
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

public final class BlockEntityEssentiaPort extends BlockEntity implements IEssentiaConduit {
    private static final int SOURCE_RANGE = 16;
    private static final int WORK_INTERVAL = 5;
    private static final int INTAKE_SUCTION = 128;
    private static final EssentiaPull INTAKE_PULL = EssentiaPull.any(INTAKE_SUCTION);
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
        this.sources = new EssentiaSources(pos, SOURCE_RANGE)
                .facing(state.getValue(BlockStateProperties.FACING))
                .drainEffectTarget(Vec3.atCenterOf(pos));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaPort port) {
        if (++port.ticks % WORK_INTERVAL != 0
                || !(level instanceof ServerLevel server)
                || server.getGameTime() < port.resumeAt) {
            return;
        }
        Direction toTube = port.tubeSide();
        Direction tubeFace = toTube.getOpposite();
        IEssentiaConduit tube = EssentiaFlowHandler.conduit(server, pos.relative(toTube), tubeFace);
        if (tube == null || !tube.connectsOn(tubeFace)) {
            return;
        }
        if (port.input) {
            port.pushIntoSources(server, tube, tubeFace);
        } else {
            port.pullFromSources(server, tube, tubeFace);
        }
        if (port.searchFailed) {
            port.resumeAt = server.getGameTime() + SEARCH_BACKOFF_TICKS;
        }
        port.searchFailed = false;
    }

    private boolean pushIntoSources(ServerLevel server, IEssentiaConduit tube, Direction tubeFace) {
        if (!tube.releasesOn(tubeFace)
                || tube.heldAmount(tubeFace) < SINGLE_POINT
                || tube.pull(tubeFace).strength() >= INTAKE_SUCTION
                || tube.pullThreshold() > INTAKE_SUCTION) {
            return false;
        }
        Holder<IAspect> aspect = tube.heldAspect(tubeFace);
        if (aspect == null || tube.extract(aspect, SINGLE_POINT, tubeFace, true) != SINGLE_POINT) {
            return false;
        }
        if (!sources.insert(server, aspect, VISUAL_EXTENSION)) {
            searchFailed = true;
            return false;
        }
        tube.extract(aspect, SINGLE_POINT, tubeFace, false);
        return true;
    }

    private boolean pullFromSources(ServerLevel server, IEssentiaConduit tube, Direction tubeFace) {
        EssentiaPull tubePull = tube.pull(tubeFace);
        Holder<IAspect> wanted = tubePull.aspect();
        if (wanted == null
                || !tube.receivesOn(tubeFace)
                || !tubePull.isActive()
                || tube.insert(wanted, SINGLE_POINT, tubeFace, true) < SINGLE_POINT) {
            return false;
        }
        if (!sources.drain(server, wanted, VISUAL_EXTENSION)) {
            searchFailed = true;
            return false;
        }
        if (tube.insert(wanted, SINGLE_POINT, tubeFace, false) == SINGLE_POINT) {
            return true;
        }
        sources.insert(server, wanted, VISUAL_EXTENSION);
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
    public boolean connectsOn(Direction face) {
        return touchesTube(face);
    }

    @Override
    public boolean receivesOn(Direction face) {
        return touchesTube(face) && input;
    }

    @Override
    public boolean releasesOn(Direction face) {
        return touchesTube(face) && !input;
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        return input && touchesTube(face) ? INTAKE_PULL : EssentiaPull.NONE;
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        if (amount != SINGLE_POINT || !input || !touchesTube(face)) {
            return 0;
        }
        if (simulate) {
            return SINGLE_POINT;
        }
        if (level instanceof ServerLevel server && sources.insert(server, aspect, VISUAL_EXTENSION)) {
            return SINGLE_POINT;
        }
        return 0;
    }

    @Override
    public int extract(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        return 0;
    }

    @Override
    public int roomFor(Holder<IAspect> aspect, Direction face) {
        return input && touchesTube(face) ? SINGLE_POINT : 0;
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        return null;
    }

    @Override
    public int heldAmount(@Nullable Direction face) {
        return 0;
    }

    @Override
    public int pullThreshold() {
        return 0;
    }
}
