package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaReservoir extends BlockEntity implements IEssentiaTransport {
    public static final int CAPACITY = 256;
    private static final int SUCTION = 24;
    private static final int DRAW_INTERVAL = 5;
    private static final int CREAK_RANGE = 500;
    private static final float CREAK_PITCH = 1.4F;
    private static final float CREAK_PITCH_SPREAD = 0.2F;
    private static final float RUPTURE_FLUX_PER_ESSENTIA = 0.25F;
    private static final float MAX_RUPTURE_FLUX = 64.0F;
    private static final int ESSENTIA_PER_POCKET = 16;
    private static final float RUPTURE_BLAST = 1.0F;
    private static final int RUPTURE_ATTEMPTS = 50;
    private static final int RUPTURE_SPREAD = 5;

    private AspectList contents = AspectList.EMPTY;

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
        int stored = reservoir.getStoredAmount();
        if (stored > 0 && level.getRandom().nextInt(CREAK_RANGE - stored) == 0) {
            level.playSound(null, pos, TTSounds.CREAK.get(), SoundSource.BLOCKS, 1.0F, CREAK_PITCH + level.getRandom().nextFloat() * CREAK_PITCH_SPREAD);
        }
        if (level.getGameTime() % DRAW_INTERVAL == 0 && stored < CAPACITY) {
            reservoir.pullOne(level, pos, reservoir.facing());
        }
    }

    private Direction facing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private void pullOne(Level level, BlockPos pos, Direction face) {
        Direction remoteFace = face.getOpposite();
        IEssentiaTransport remote = EssentiaFlowHandler.transport(level, pos.relative(face), remoteFace);
        if (remote == null || !remote.canOutputTo(remoteFace) || remote.getEssentiaAmount(remoteFace) <= 0) {
            return;
        }
        if (remote.getSuctionAmount(remoteFace) >= SUCTION || SUCTION < remote.getMinimumSuction()) {
            return;
        }
        Holder<IAspect> aspect = remote.getEssentiaType(remoteFace);
        if (aspect != null && remote.takeEssentia(aspect, 1, remoteFace) == 1) {
            contents = contents.add(new AspectInstance(aspect, 1));
            changedAndSync();
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return isConnectable(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return isConnectable(face) && getStoredAmount() < CAPACITY ? SUCTION : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        if (!canOutputTo(face)) {
            return null;
        }
        List<AspectInstance> entries = contents.entries();
        return entries.isEmpty() ? null : entries.getFirst().aspect();
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        Holder<IAspect> aspect = getEssentiaType(face);
        return aspect == null ? 0 : contents.amountOf(aspect);
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || aspect == null || amount <= 0 || contents.amountOf(aspect) < amount) {
            return 0;
        }
        contents = contents.remove(aspect, amount);
        changedAndSync();
        return amount;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canInputFrom(face) || aspect == null || amount <= 0) {
            return 0;
        }
        int accepted = Math.min(amount, CAPACITY - getStoredAmount());
        if (accepted <= 0) {
            return 0;
        }
        contents = contents.add(new AspectInstance(aspect, accepted));
        changedAndSync();
        return accepted;
    }

    @Override
    public int getMinimumSuction() {
        return SUCTION;
    }

    @Override
    public int spaceFor(Holder<IAspect> aspect, Direction face) {
        return canInputFrom(face) && aspect != null ? Math.max(0, CAPACITY - getStoredAmount()) : 0;
    }

    private void changedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
            level.updateNeighbourForOutputSignal(getBlockPos(), state.getBlock());
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel server) {
            rupture(server, pos, getStoredAmount());
        }
    }

    private static void rupture(ServerLevel level, BlockPos pos, int stored) {
        if (stored <= 0) {
            return;
        }
        AuraHelper.polluteAura(level, pos, Math.min(stored * RUPTURE_FLUX_PER_ESSENTIA, MAX_RUPTURE_FLUX), true);
        int pockets = stored / ESSENTIA_PER_POCKET;
        if (pockets <= 0) {
            return;
        }
        level.explode(null, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, RUPTURE_BLAST, Level.ExplosionInteraction.NONE);
        RandomSource random = level.getRandom();
        int placed = 0;
        for (int attempt = 0; attempt < RUPTURE_ATTEMPTS && placed <= pockets; attempt++) {
            BlockPos target = pos.offset(random.nextInt(RUPTURE_SPREAD) - random.nextInt(RUPTURE_SPREAD), random.nextInt(RUPTURE_SPREAD) - random.nextInt(RUPTURE_SPREAD),
                    random.nextInt(RUPTURE_SPREAD) - random.nextInt(RUPTURE_SPREAD));
            if (!level.isEmptyBlock(target)) {
                continue;
            }
            if (target.getY() < pos.getY()) {
                PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA);
            } else {
                PhysicalFlux.placeGas(level, target, PhysicalFlux.MAX_QUANTA);
            }
            placed++;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        contents = input.read("Essentia", AspectList.CODEC).orElse(AspectList.EMPTY);
        if (contents.totalAmount() > CAPACITY) {
            contents = AspectList.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Essentia", AspectList.CODEC, contents);
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
