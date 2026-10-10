package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectSource;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
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
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaReservoir extends BlockEntity implements IEssentiaTransport, IAspectSource {
    public static final int CAPACITY = 256;

    private static final String ESSENTIA_KEY = "Essentia";
    private static final int SUCTION = 24;
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
            level.playSound(null, pos, TTSounds.CREAK.get(), SoundSource.BLOCKS, CREAK_VOLUME, CREAK_PITCH_BASE + random.nextFloat() * CREAK_PITCH_SPREAD);
        }
    }

    private void pullFromNeighbour(Level level, BlockPos pos) {
        Direction face = facing();
        Direction back = face.getOpposite();
        IEssentiaTransport source = EssentiaFlowHandler.transport(level, pos.relative(face), back);
        if (source == null || !source.isConnectable(back) || !sourceCanOutput(source, back) || !sourceHasStock(source, back) || !withinSuctionWindow(source, back)) {
            return;
        }
        Holder<IAspect> offered = source.getEssentiaType(back);
        if (offered != null && source.takeEssentia(offered, UNIT, back) == UNIT) {
            store(offered, UNIT);
        }
    }

    private static boolean withinSuctionWindow(IEssentiaTransport source, Direction face) {
        return source.getSuctionAmount(face) < SUCTION && source.getMinimumSuction() <= SUCTION;
    }

    private static boolean sourceCanOutput(IEssentiaTransport source, Direction face) {
        return source.canOutputTo(face);
    }

    private static boolean sourceHasStock(IEssentiaTransport source, Direction face) {
        return source.getEssentiaAmount(face) > 0;
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

    private void rupture(ServerLevel server, BlockPos pos, int total) {
        if (total < ESSENTIA_PER_POCKET) {
            releaseFlux(server, pos, total);
            return;
        }
        burst(server, pos, total);
    }

    private static void burst(ServerLevel server, BlockPos pos, int total) {
        Vec3 center = Vec3.atCenterOf(pos);
        server.explode(null, center.x, center.y, center.z, RUPTURE_EXPLOSION_RADIUS, Level.ExplosionInteraction.NONE);
        FluxPocketScatter.scatter(server, pos, total / ESSENTIA_PER_POCKET + 1);
    }

    @Override
    public boolean accepts(Holder<IAspect> aspect) {
        return getStoredAmount() < CAPACITY;
    }

    @Override
    public AspectList getAspects() {
        return contents;
    }

    @Override
    public void setAspects(AspectList aspects) {
        contents = keepWithinCapacity(aspects);
        notifyChanged();
    }

    @Override
    public int fill(Holder<IAspect> aspect, int amount) {
        int accepted = Math.clamp(amount, 0, Math.max(0, CAPACITY - getStoredAmount()));
        if (accepted > 0) {
            store(aspect, accepted);
        }
        return amount - accepted;
    }

    @Override
    public boolean drain(Holder<IAspect> aspect, int amount) {
        if (amount <= 0 || contents.amountOf(aspect) < amount) {
            return false;
        }
        withdraw(aspect, amount);
        return true;
    }

    @Override
    public boolean isBlocked() {
        return false;
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
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return null;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == facing();
    }

    @Override
    public int getMinimumSuction() {
        return SUCTION;
    }

    @Override
    public int spaceFor(Holder<IAspect> aspect, Direction face) {
        if (!canInputFrom(face)) {
            return 0;
        }
        return Math.max(0, CAPACITY - getStoredAmount());
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (!isOutlet(face) || getStoredAmount() >= CAPACITY) {
            return 0;
        }
        return SUCTION;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        AspectInstance lead = leadingStock(face);
        return lead == null ? null : lead.aspect();
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        AspectInstance lead = leadingStock(face);
        return lead == null ? 0 : lead.amount();
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        int room = isOutlet(face) ? spaceFor(aspect, face) : 0;
        int accepted = Math.clamp(amount, 0, room);
        if (accepted > 0) {
            store(aspect, accepted);
        }
        return accepted;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        boolean available = isOutlet(face) && amount > 0 && contents.amountOf(aspect) >= amount;
        if (available) {
            withdraw(aspect, amount);
        }
        return available ? amount : 0;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        AspectList loaded = input.read(ESSENTIA_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        contents = keepWithinCapacity(loaded);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(ESSENTIA_KEY, AspectList.CODEC, contents);
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
