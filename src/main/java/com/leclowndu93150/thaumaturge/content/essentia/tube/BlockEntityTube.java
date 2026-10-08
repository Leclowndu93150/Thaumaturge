package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.network.ClientboundTubeCreakPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundTubeVentPayload;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public class BlockEntityTube extends AbstractSyncedBlockEntity implements IEssentiaTransport {
    protected static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;
    private static final int DEFAULT_GREY = 11184810;
    private static final int VENT_DURATION_TICKS = 40;
    private static final int CREAK_CHANCE = 100;
    private static final double PARTICLE_RADIUS = 32.0;

    protected @Nullable ResourceKey<IAspect> essentiaType;
    protected int essentiaAmount;
    protected @Nullable ResourceKey<IAspect> suctionType;
    protected int suction;
    protected int tickCount;
    protected int venting;
    protected int ventColor = DEFAULT_GREY;
    protected Direction facing = Direction.NORTH;
    protected final boolean[] openSides = new boolean[] {true, true, true, true, true, true};

    public BlockEntityTube(BlockPos pos, BlockState state) {
        this(TTBlockEntities.TUBE.get(), pos, state);
    }

    protected BlockEntityTube(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        this.tickCount = Math.floorMod(pos.getX() * 31 + pos.getY() * 17 + pos.getZ() * 13, 10);
    }

    public void tickServer(Level level, BlockPos pos, BlockState state) {
        tickCount++;
        if (venting > 0) {
            venting--;
            return;
        }
        if ((tickCount & 1) == 0) {
            EssentiaFlowHandler.recalculateSuction(
                    level, pos, this, suctionFilter(), restrictiveSuction(), directionalSuction());
            checkVenting(level, pos);
            if (essentiaType != null && essentiaAmount == 0) {
                essentiaType = null;
                setChanged();
            }
        }
        if (tickCount % 5 == 0 && suction > 0) {
            EssentiaFlowHandler.equalizeWithNeighbours(level, pos, this, directionalEqualize());
        }
    }

    protected void checkVenting(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!isConnectable(dir)) continue;
            IEssentiaTransport neighbour = EssentiaFlowHandler.transport(level, pos.relative(dir), dir.getOpposite());
            if (neighbour == null) continue;
            int neighbourSuck = neighbour.getSuctionAmount(dir.getOpposite());
            if (suction <= 0) continue;
            if (neighbourSuck != suction && neighbourSuck != suction - 1) continue;
            Holder<IAspect> neighbourSuctionType = neighbour.getSuctionType(dir.getOpposite());
            Holder<IAspect> ourSuction = getSuctionType(dir);
            if (sameHolder(ourSuction, neighbourSuctionType)) continue;
            if (neighbour instanceof BlockEntityTubeFilter) continue;
            int color = DEFAULT_GREY;
            if (suctionType != null) {
                Holder<IAspect> resolved = EssentiaTransportHelper.resolve(level, suctionType);
                if (resolved != null) color = resolved.value().color();
            }
            triggerVent(color);
            broadcastVent(level, pos, color);
            return;
        }
    }

    private static boolean sameHolder(@Nullable Holder<IAspect> a, @Nullable Holder<IAspect> b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    public void triggerVent(int color) {
        this.venting = VENT_DURATION_TICKS + 10;
        this.ventColor = color;
    }

    public void broadcastVent(Level level, BlockPos pos, int color) {
        if (!(level instanceof ServerLevel server)) return;
        PacketDistributor.sendToPlayersNear(
                server,
                null,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                PARTICLE_RADIUS,
                new ClientboundTubeVentPayload(pos.immutable(), color));
    }

    public void broadcastCreak(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel server)) return;
        PacketDistributor.sendToPlayersNear(
                server,
                null,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                PARTICLE_RADIUS,
                new ClientboundTubeCreakPayload(pos.immutable()));
    }

    public int ventingTicks() {
        return venting;
    }

    public int ventColor() {
        return ventColor;
    }

    protected @Nullable ResourceKey<IAspect> suctionFilter() {
        return null;
    }

    protected boolean restrictiveSuction() {
        return false;
    }

    protected boolean directionalSuction() {
        return false;
    }

    protected boolean directionalEqualize() {
        return false;
    }

    public Direction facing() {
        return facing;
    }

    public void setFacingForPlacement(@Nullable LivingEntity placer) {
        if (placer == null) {
            this.facing = Direction.NORTH;
        } else {
            this.facing = Direction.orderedByNearest(placer)[0].getOpposite();
        }
        setChanged();
        pushUpdate(this);
    }

    public boolean[] openSides() {
        return openSides;
    }

    public boolean isSideOpen(Direction face) {
        return face != null && openSides[face.ordinal()];
    }

    public void setOpenSide(Direction face, boolean open) {
        openSides[face.ordinal()] = open;
        setChanged();
        pushUpdate(this);
    }

    public boolean toggleSide(Direction face) {
        if (face == null) return false;
        openSides[face.ordinal()] = !openSides[face.ordinal()];
        setChanged();
        pushUpdate(this);
        return openSides[face.ordinal()];
    }

    public boolean rotateFacing() {
        int a = facing.ordinal();
        for (int i = 1; i < 6; i++) {
            int idx = (a + i) % 6;
            Direction candidate = Direction.values()[idx];
            if (isSideOpen(candidate) && hasTransportNeighbour(candidate)) {
                facing = candidate;
                setChanged();
                pushUpdate(this);
                return true;
            }
        }
        for (int i = 1; i < 6; i++) {
            int idx = (a + i) % 6;
            Direction candidate = Direction.values()[idx];
            if (isSideOpen(candidate)) {
                facing = candidate;
                setChanged();
                pushUpdate(this);
                return true;
            }
        }
        return false;
    }

    protected boolean hasTransportNeighbour(Direction dir) {
        if (level == null) return false;
        BlockPos neighbour = getBlockPos().relative(dir);
        return level.getCapability(EssentiaCapabilities.TRANSPORT, neighbour, dir.getOpposite()) != null;
    }

    public boolean handleCasterClick(int subHit) {
        if (level == null) return false;
        if (subHit >= 0 && subHit < 6) {
            Direction dir = Direction.values()[subHit];
            boolean nowOpen = toggleSide(dir);
            BlockEntity other = level.getBlockEntity(getBlockPos().relative(dir));
            if (other instanceof BlockEntityTube otherTube) {
                otherTube.setOpenSide(dir.getOpposite(), nowOpen);
                pushUpdate(otherTube);
            } else if (other instanceof BlockEntityTubeBuffer otherBuffer) {
                otherBuffer.setOpenSide(dir.getOpposite(), nowOpen);
            }
            pushUpdate(this);
            BlockEssentiaTransport.refreshConnections(level, getBlockPos());
            BlockEssentiaTransport.refreshConnections(level, getBlockPos().relative(dir));
            return true;
        }
        if (subHit == 6) {
            if (rotateFacing()) {
                pushUpdate(this);
            }
            return true;
        }
        return false;
    }

    protected static void pushUpdate(BlockEntity be) {
        Level lvl = be.getLevel();
        if (lvl == null || lvl.isClientSide()) return;
        lvl.sendBlockUpdated(be.getBlockPos(), be.getBlockState(), be.getBlockState(), Block.UPDATE_ALL);
    }

    public void playToolSound(Level level, BlockPos pos) {
        level.playSound(
                null,
                pos,
                TTSounds.TOOL.get(),
                SoundSource.BLOCKS,
                0.5F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && openSides[face.ordinal()];
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
    public void setSuction(Holder<IAspect> aspect, int amount) {
        ResourceKey<IAspect> key = aspect == null ? null : aspect.unwrapKey().orElse(null);
        suctionType = key;
        suction = amount;
    }

    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return suctionType == null ? null : EssentiaTransportHelper.resolve(level, suctionType);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return suction;
    }

    @Override
    public Holder<IAspect> getEssentiaType(Direction face) {
        return essentiaType == null ? null : EssentiaTransportHelper.resolve(level, essentiaType);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return essentiaAmount;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || amount <= 0 || essentiaAmount <= 0) return 0;
        ResourceKey<IAspect> key = aspect == null ? null : aspect.unwrapKey().orElse(null);
        if (key == null || !key.equals(essentiaType)) return 0;
        essentiaAmount--;
        if (essentiaAmount <= 0) {
            essentiaType = null;
        }
        setChanged();
        return 1;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canInputFrom(face) || essentiaAmount > 0 || amount <= 0) return 0;
        ResourceKey<IAspect> key = aspect == null ? null : aspect.unwrapKey().orElse(null);
        if (key == null) return 0;
        essentiaType = key;
        essentiaAmount = 1;
        setChanged();
        return 1;
    }

    public @Nullable ResourceKey<IAspect> essentiaKey() {
        return essentiaType;
    }

    public int essentiaAmountRaw() {
        return essentiaAmount;
    }

    public @Nullable ResourceKey<IAspect> suctionKey() {
        return suctionType;
    }

    public int suctionRaw() {
        return suction;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        essentiaType =
                TTNbt.read(input, "EssentiaType", ASPECT_KEY_CODEC, registries).orElse(null);
        essentiaAmount = input.getInt("EssentiaAmount");
        suctionType =
                TTNbt.read(input, "SuctionType", ASPECT_KEY_CODEC, registries).orElse(null);
        suction = input.getInt("Suction");
        int facingOrdinal = (input.contains("Facing") ? input.getInt("Facing") : Direction.NORTH.ordinal());
        if (facingOrdinal >= 0 && facingOrdinal < 6) {
            facing = Direction.values()[facingOrdinal];
        } else {
            facing = Direction.NORTH;
        }
        readOpenSides(input);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (essentiaType != null) TTNbt.store(output, "EssentiaType", ASPECT_KEY_CODEC, registries, essentiaType);
        output.putInt("EssentiaAmount", essentiaAmount);
        if (suctionType != null) TTNbt.store(output, "SuctionType", ASPECT_KEY_CODEC, registries, suctionType);
        output.putInt("Suction", suction);
        output.putInt("Facing", facing.ordinal());
        writeOpenSides(output);
    }

    protected void readOpenSides(CompoundTag input) {
        byte[] data = input.contains("OpenSides") ? input.getByteArray("OpenSides") : null;
        if (data != null && data.length == 6) {
            for (int a = 0; a < 6; a++) {
                openSides[a] = data[a] == 1;
            }
        } else {
            for (int a = 0; a < 6; a++) {
                openSides[a] = true;
            }
        }
    }

    protected void writeOpenSides(CompoundTag output) {
        byte[] data = new byte[6];
        for (int a = 0; a < 6; a++) {
            data[a] = (byte) (openSides[a] ? 1 : 0);
        }
        output.putByteArray("OpenSides", data);
    }
}
