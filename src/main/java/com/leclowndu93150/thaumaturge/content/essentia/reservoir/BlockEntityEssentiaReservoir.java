package com.leclowndu93150.thaumaturge.content.essentia.reservoir;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaReservoir extends BlockEntity implements IEssentiaTransport {
    public static final int CAPACITY = 256;
    private static final int SUCTION = 24;
    private static final int DRAW_INTERVAL = 5;

    private AspectList contents = AspectList.EMPTY;
    private int tickCount;

    // Client-only display interpolation, kept here so the renderer remains stateless.
    private int displayIndex;
    private float cr = 1.0F;
    private float cg = 1.0F;
    private float cb = 1.0F;
    private float tr = 1.0F;
    private float tg = 1.0F;
    private float tb = 1.0F;
    private float tri;
    private float tgi;
    private float tbi;

    public BlockEntityEssentiaReservoir(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ESSENTIA_RESERVOIR.get(), pos, state);
    }

    public AspectList contents() {
        return contents;
    }

    public int getStoredAmount() {
        return contents.totalAmount();
    }

    public int displayColor() {
        int r = Math.max(0, Math.min(255, Math.round(cr * 255.0F)));
        int g = Math.max(0, Math.min(255, Math.round(cg * 255.0F)));
        int b = Math.max(0, Math.min(255, Math.round(cb * 255.0F)));
        return (r << 16) | (g << 8) | b;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaReservoir reservoir) {
        if (++reservoir.tickCount % DRAW_INTERVAL == 0 && reservoir.getStoredAmount() < CAPACITY) {
            reservoir.pullOne(level, pos, reservoir.facing());
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaReservoir reservoir) {
        List<AspectInstance> entries = reservoir.contents.entries();
        int stored = reservoir.getStoredAmount();
        if (stored <= 0 || entries.isEmpty()) {
            reservoir.displayIndex = 0;
            reservoir.tr = reservoir.tg = reservoir.tb = 1.0F;
            reservoir.tri = reservoir.tgi = reservoir.tbi = 0.0F;
            return;
        }

        // TC4 made a stressed-metal creak increasingly likely as the reservoir filled.
        if (level.getRandom().nextInt(500 - stored) == 0) {
            level.playLocalSound(
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    TCSounds.CREAK.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.4F + level.getRandom().nextFloat() * 0.2F,
                    false);
        }

        if (level.getGameTime() % 20L == 0L) {
            reservoir.displayIndex %= entries.size();
            int color = entries.get(reservoir.displayIndex).aspect().value().color();
            reservoir.displayIndex = (reservoir.displayIndex + 1) % entries.size();
            reservoir.tr = ((color >> 16) & 0xFF) / 255.0F;
            reservoir.tg = ((color >> 8) & 0xFF) / 255.0F;
            reservoir.tb = (color & 0xFF) / 255.0F;
            reservoir.tri = (reservoir.cr - reservoir.tr) / 20.0F;
            reservoir.tgi = (reservoir.cg - reservoir.tg) / 20.0F;
            reservoir.tbi = (reservoir.cb - reservoir.tb) / 20.0F;
        }
        reservoir.cr -= reservoir.tri;
        reservoir.cg -= reservoir.tgi;
        reservoir.cb -= reservoir.tbi;
    }

    private Direction facing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private void pullOne(Level level, BlockPos pos, Direction face) {
        BlockPos neighbourPos = pos.relative(face);
        IEssentiaTransport remote = EssentiaFlowHandler.transport(level, neighbourPos, face.getOpposite());
        if (remote == null
                || !remote.canOutputTo(face.getOpposite())
                || remote.getEssentiaAmount(face.getOpposite()) <= 0) return;
        int remoteSuction = remote.getSuctionAmount(face.getOpposite());
        if (remoteSuction >= SUCTION || SUCTION < remote.getMinimumSuction()) return;
        Holder<IAspect> aspect = remote.getEssentiaType(face.getOpposite());
        if (aspect != null && remote.takeEssentia(aspect, 1, face.getOpposite()) == 1) {
            contents = contents.add(new AspectInstance(aspect, 1));
            changedAndSync();
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && face == facing();
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
    public void setSuction(Holder<IAspect> aspect, int amount) {}

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
        // TC4 used ForgeDirection.UNKNOWN as its wildcard extraction query. Modern transport
        // always supplies the connected side, so expose the first compartment on that side.
        if (!canOutputTo(face)) return null;
        List<AspectInstance> entries = contents.entries();
        return entries.isEmpty() ? null : entries.getFirst().aspect();
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return getStoredAmount();
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || aspect == null || amount <= 0) return 0;
        if (contents.amountOf(aspect) < amount) return 0;
        contents = contents.remove(aspect, amount);
        changedAndSync();
        return amount;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canInputFrom(face) || aspect == null || amount <= 0) return 0;
        int accepted = Math.min(amount, CAPACITY - getStoredAmount());
        if (accepted <= 0) return 0;
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
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        contents = TCNbt.read(input, "Essentia", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        if (contents.totalAmount() > CAPACITY) contents = AspectList.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TCNbt.store(output, "Essentia", AspectList.CODEC, registries, contents);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        CompoundTag output = new CompoundTag();
        saveAdditional(output, registries);
        tag.merge(output);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
