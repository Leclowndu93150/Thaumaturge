package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaReservoir extends BlockEntity implements IEssentiaConduit {
    public static final int CAPACITY = 256;

    private static final String ESSENTIA_KEY = "Essentia";
    private static final int SUCTION = 24;
    private static final EssentiaPull ACTIVE_PULL = EssentiaPull.any(SUCTION);
    private static final int PULL_INTERVAL = 5;
    private static final float CREAK_VOLUME = 1.0F;
    private static final float FLUX_PER_ESSENTIA = 0.25F;
    private static final float MAX_RUPTURE_FLUX = 64.0F;
    private static final int ESSENTIA_PER_POCKET = 16;
    private static final float RUPTURE_EXPLOSION_RADIUS = 1.0F;
    private static final int UNIT = 1;
    private static final float CREAK_SPARSE_TICKS = 500.0F;
    private static final float CREAK_FREQUENT_TICKS = 240.0F;
    private static final float CREAK_PITCH_BASE = 1.4F;
    private static final float CREAK_PITCH_SPREAD = 0.3F;

    private AspectList contents = AspectList.EMPTY;
    private int ticks;

    public BlockEntityEssentiaReservoir(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ESSENTIA_RESERVOIR.get(), pos, state);
    }

    public AspectList contents() {
        return contents;
    }

    public int getStoredAmount() {
        return contents.totalAmount();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaReservoir reservoir) {
        reservoir.creak(level, pos);
        if (++reservoir.ticks % PULL_INTERVAL == 0 && reservoir.getStoredAmount() < CAPACITY) {
            reservoir.pullFromNeighbour(level, pos);
        }
    }

    private void creak(Level level, BlockPos pos) {
        int stored = getStoredAmount();
        if (stored <= 0) {
            return;
        }
        float fullness = Math.min(1.0F, stored / (float) CAPACITY);
        int interval = Math.round(Mth.lerp(fullness, CREAK_SPARSE_TICKS, CREAK_FREQUENT_TICKS));
        RandomSource random = level.getRandom();
        if (random.nextInt(interval) == 0) {
            level.playSound(
                    null,
                    pos,
                    TTSounds.CREAK.get(),
                    SoundSource.BLOCKS,
                    CREAK_VOLUME,
                    CREAK_PITCH_BASE + random.nextFloat() * CREAK_PITCH_SPREAD);
        }
    }

    private void pullFromNeighbour(Level level, BlockPos pos) {
        Direction face = facing();
        Direction back = face.getOpposite();
        IEssentiaConduit source = EssentiaFlowHandler.conduit(level, pos.relative(face), back);
        if (source == null
                || !source.connectsOn(back)
                || !sourceCanOutput(source, back)
                || !sourceHasStock(source, back)
                || !withinSuctionWindow(source, back)) {
            return;
        }
        Holder<IAspect> offered = source.heldAspect(back);
        if (offered != null && source.extract(offered, UNIT, back, false) == UNIT) {
            store(offered, UNIT);
        }
    }

    private static boolean withinSuctionWindow(IEssentiaConduit source, Direction face) {
        return source.pull(face).strength() < SUCTION && source.pullThreshold() <= SUCTION;
    }

    private static boolean sourceCanOutput(IEssentiaConduit source, Direction face) {
        return source.releasesOn(face);
    }

    private static boolean sourceHasStock(IEssentiaConduit source, Direction face) {
        return source.heldAmount(face) > 0;
    }

    private static AspectList keepWithinCapacity(AspectList loaded) {
        if (loaded.totalAmount() <= CAPACITY) {
            return loaded;
        }
        AspectList kept = AspectList.EMPTY;
        int room = CAPACITY;
        for (AspectInstance entry : loaded.entries()) {
            int portion = Math.min(room, entry.amount());
            if (portion > 0) {
                kept = kept.add(entry.aspect(), portion);
                room -= portion;
            }
        }
        return kept;
    }

    private static void rupture(ServerLevel server, BlockPos pos, int total) {
        releaseFlux(server, pos, total);
        int pockets = total / ESSENTIA_PER_POCKET;
        if (pockets > 0) {
            burst(server, pos, pockets);
        }
    }

    private static void burst(ServerLevel server, BlockPos pos, int pockets) {
        Vec3 center = Vec3.atCenterOf(pos);
        server.explode(null, center.x, center.y, center.z, RUPTURE_EXPLOSION_RADIUS, Level.ExplosionInteraction.NONE);
        FluxPocketScatter.scatter(server, pos, pockets + 1);
    }

    private void store(Holder<IAspect> aspect, int amount) {
        contents = contents.add(aspect, amount);
        notifyChanged();
    }

    private void withdraw(Holder<IAspect> aspect, int amount) {
        contents = contents.remove(aspect, amount);
        notifyChanged();
    }

    private @Nullable AspectInstance leadingStock(@Nullable Direction face) {
        if (!isOutlet(face) || contents.isEmpty()) {
            return null;
        }
        return contents.entries().getFirst();
    }

    private boolean isOutlet(@Nullable Direction face) {
        return face != null && face == facing();
    }

    private void notifyChanged() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        }
    }

    private Direction facing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public boolean connectsOn(Direction face) {
        return face == facing();
    }

    @Override
    public boolean receivesOn(Direction face) {
        return face == facing();
    }

    @Override
    public boolean releasesOn(Direction face) {
        return face == facing();
    }

    @Override
    public int pullThreshold() {
        return SUCTION;
    }

    @Override
    public int roomFor(Holder<IAspect> aspect, Direction face) {
        if (!receivesOn(face)) {
            return 0;
        }
        return Math.max(0, CAPACITY - getStoredAmount());
    }

    @Override
    public EssentiaPull pull(@Nullable Direction face) {
        if (!isOutlet(face) || getStoredAmount() >= CAPACITY) {
            return EssentiaPull.NONE;
        }
        return ACTIVE_PULL;
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction face) {
        AspectInstance lead = leadingStock(face);
        return lead == null ? null : lead.aspect();
    }

    @Override
    public int heldAmount(@Nullable Direction face) {
        AspectInstance lead = leadingStock(face);
        return lead == null ? 0 : lead.amount();
    }

    @Override
    public int insert(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        int room = isOutlet(face) ? roomFor(aspect, face) : 0;
        int accepted = Math.clamp(amount, 0, room);
        if (accepted > 0 && !simulate) {
            store(aspect, accepted);
        }
        return accepted;
    }

    @Override
    public int extract(Holder<IAspect> aspect, int amount, Direction face, boolean simulate) {
        boolean available = isOutlet(face) && amount > 0 && contents.amountOf(aspect) >= amount;
        if (available && !simulate) {
            withdraw(aspect, amount);
        }
        return available ? amount : 0;
    }

    public void releaseContents(BlockPos pos) {
        int total = getStoredAmount();
        if (total > 0 && level instanceof ServerLevel server) {
            rupture(server, pos, total);
        }
    }

    private static void releaseFlux(ServerLevel server, BlockPos pos, int total) {
        float flux = Math.min(total * FLUX_PER_ESSENTIA, MAX_RUPTURE_FLUX);
        AuraHelper.polluteAura(server, pos, flux, true);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        AspectList loaded =
                TTNbt.read(input, ESSENTIA_KEY, AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        contents = keepWithinCapacity(loaded);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TTNbt.store(output, ESSENTIA_KEY, AspectList.CODEC, registries, contents);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
