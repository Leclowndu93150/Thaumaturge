package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityCondenser extends BlockEntity implements IEssentiaTransport {
    private static final int MAX_BUFFER = 100;
    private static final int FILL_INTERVAL = 5;
    private static final int RECHECK_INTERVAL = 100;
    private static final float MAX_LATTICE = 40.0F;
    private static final double MAX_DIST_SQ = 74.0;
    private static final int DIRTY_CHANCE = 50;
    private static final int SUCTION = 128;

    private int essentia;
    private int flux;
    private int count;

    private float latticeCount = -1.0F;
    private int interval;
    private int cost;

    public int interval() {
        return interval;
    }

    public int cost() {
        return cost;
    }

    private final List<BlockPos> unclogged = new ArrayList<>();
    private int latticeBlocks;

    public BlockEntityCondenser(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CONDENSER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityCondenser condenser) {
        condenser.count++;
        if (condenser.latticeCount < 0.0F || condenser.count % RECHECK_INTERVAL == 0) {
            condenser.triggerCheck();
        }
        if (!state.getValue(BlockStateProperties.ENABLED) || condenser.latticeCount <= 0.0F || !(level instanceof ServerLevel server)) {
            return;
        }
        if (condenser.count % FILL_INTERVAL == 0 && condenser.essentia < MAX_BUFFER) {
            condenser.fill(server);
        }
        if (condenser.interval > 0 && condenser.essentia >= condenser.cost && condenser.flux < MAX_BUFFER && condenser.count % condenser.interval == 0 && AuraHelper.getFlux(server, pos) >= 1.0F) {
            AuraHelper.drainFlux(server, pos, 1.0F, false);
            condenser.essentia -= condenser.cost;
            condenser.flux++;
            if (server.getRandom().nextInt(DIRTY_CHANCE) == 0) {
                condenser.makeLatticeDirty(server);
            }
            condenser.setChanged();
        }
    }

    private void makeLatticeDirty(ServerLevel server) {
        if (unclogged.isEmpty()) {
            return;
        }
        int q = server.getRandom().nextInt(unclogged.size());
        if (q == 0) {
            q = server.getRandom().nextInt(unclogged.size());
        }
        BlockPos p = unclogged.get(q);
        if (server.getBlockState(p).is(TCBlocks.CONDENSER_LATTICE.get())) {
            server.setBlock(p, TCBlocks.CONDENSER_LATTICE_DIRTY.get().defaultBlockState(), Block.UPDATE_ALL);
            latticeCount = -1.0F;
        }
    }

    private void fill(ServerLevel server) {
        for (Direction face : Direction.Plane.HORIZONTAL) {
            IEssentiaTransport ic = EssentiaFlowHandler.transport(server, getBlockPos().relative(face), face.getOpposite());
            if (ic == null) {
                continue;
            }
            if (!ic.canOutputTo(face.getOpposite())) {
                return;
            }
            Holder<IAspect> aspect = null;
            if (ic.getEssentiaAmount(face.getOpposite()) > 0 && ic.getSuctionAmount(face.getOpposite()) < getSuctionAmount(face) && getSuctionAmount(face) >= ic.getMinimumSuction()) {
                aspect = ic.getEssentiaType(face.getOpposite());
            }
            if (aspect == null) {
                continue;
            }
            if (aspect.is(TCAspects.VITIUM)) {
                makeLatticeDirty(server);
            } else {
                essentia += ic.takeEssentia(aspect, 1, face.getOpposite());
            }
            setChanged();
            if (essentia >= MAX_BUFFER) {
                return;
            }
        }
    }

    public void triggerCheck() {
        if (level == null) {
            return;
        }
        Set<BlockPos> history = new HashSet<>();
        unclogged.clear();
        latticeCount = 0.0F;
        interval = 0;
        latticeBlocks = 0;
        performCheck(history, getBlockPos(), true, false);
        if (latticeCount <= 0.0F) {
            latticeCount = 0.0F;
            return;
        }
        if (latticeCount > MAX_LATTICE) {
            latticeCount = MAX_LATTICE;
        }
        interval = Math.max(5, Math.round(600.0F - latticeCount * 15.0F));
        cost = (int) (4.0 + Math.sqrt(latticeBlocks));
    }

    private void performCheck(Set<BlockPos> history, BlockPos pos, boolean skip, boolean clogged) {
        if (latticeCount < 0.0F) {
            return;
        }
        history.add(pos.immutable());
        boolean found = false;
        int sides = 0;
        for (Direction face : Direction.values()) {
            if (skip && face != Direction.UP) {
                continue;
            }
            BlockPos p2 = pos.relative(face);
            BlockState bs = level.getBlockState(p2);
            boolean lattice = bs.is(TCBlocks.CONDENSER_LATTICE.get());
            boolean latticeDirty = bs.is(TCBlocks.CONDENSER_LATTICE_DIRTY.get());
            if (skip && latticeDirty) {
                clogged = true;
            }
            if (lattice || latticeDirty) {
                sides++;
            }
            if (history.contains(p2)) {
                continue;
            }
            if (face == Direction.DOWN && bs.is(TCBlocks.CONDENSER.get())) {
                latticeCount = -99.0F;
                return;
            }
            if (getBlockPos().getY() < p2.getY() && getBlockPos().distSqr(p2) <= MAX_DIST_SQ && (lattice || latticeDirty)) {
                latticeBlocks++;
                if (lattice) {
                    unclogged.add(p2.immutable());
                }
                found = true;
                performCheck(history, p2, false, clogged || latticeDirty);
                if (skip) {
                    break;
                }
            }
        }
        if (found && !clogged) {
            latticeCount += 1.0F - 0.15F * sides;
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != Direction.UP && face != Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return face != Direction.DOWN && essentia < MAX_BUFFER ? SUCTION : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return face == Direction.DOWN && flux > 0 && level != null ? level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TCAspects.VITIUM) : null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return face == Direction.DOWN ? flux : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || aspect != null && !aspect.is(TCAspects.VITIUM)) {
            return 0;
        }
        int amt = Math.min(amount, flux);
        if (amt > 0) {
            flux -= amt;
            setChanged();
        }
        return amt;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canInputFrom(face)) {
            return 0;
        }
        if (aspect != null && aspect.is(TCAspects.VITIUM)) {
            if (level instanceof ServerLevel server) {
                makeLatticeDirty(server);
            }
            return amount;
        }
        int amt = Math.min(amount, MAX_BUFFER - essentia);
        if (amt > 0) {
            essentia += amt;
            setChanged();
        }
        return amt;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        essentia = input.getIntOr("essentia", 0);
        flux = input.getIntOr("flux", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("essentia", essentia);
        output.putInt("flux", flux);
    }
}
