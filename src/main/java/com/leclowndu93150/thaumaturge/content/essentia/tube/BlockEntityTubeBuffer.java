package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class BlockEntityTubeBuffer extends BlockEntity implements IEssentiaTransport, IInteractWithCaster {
    public static final int MAX_AMOUNT = 10;
    private static final Codec<List<Integer>> CHOKED_CODEC = Codec.INT.listOf();
    private static final Codec<List<Boolean>> OPEN_CODEC = Codec.BOOL.listOf();

    private AspectList contents = AspectList.EMPTY;
    private final int[] chokedSides = new int[] {0, 0, 0, 0, 0, 0};
    private final boolean[] openSides = new boolean[] {true, true, true, true, true, true};
    private Direction facing = Direction.NORTH;
    private int tickCount;
    private int bellows = -1;

    public BlockEntityTubeBuffer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.TUBE_BUFFER.get(), pos, state);
    }

    public AspectList contents() {
        return contents;
    }

    public int chokedSide(Direction face) {
        return chokedSides[face.ordinal()];
    }

    public void cycleChokedSide(Direction face) {
        int i = face.ordinal();
        chokedSides[i]++;
        if (chokedSides[i] > 2) {
            chokedSides[i] = 0;
        }
        setChanged();
        sync();
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
        sync();
    }

    public boolean toggleOpenSide(Direction face) {
        if (face == null) return false;
        int i = face.ordinal();
        openSides[i] = !openSides[i];
        if (level != null) {
            BlockPos neighbour = getBlockPos().relative(face);
            BlockEntity tile = level.getBlockEntity(neighbour);
            if (tile instanceof BlockEntityTube tube) {
                tube.setOpenSide(face.getOpposite(), openSides[i]);
                BlockEntityTube.pushUpdate(tube);
            } else if (tile instanceof BlockEntityTubeBuffer buffer) {
                buffer.openSides[face.getOpposite().ordinal()] = openSides[i];
                buffer.setChanged();
                buffer.sync();
            }
        }
        setChanged();
        sync();
        return openSides[i];
    }

    public Direction facing() {
        return facing;
    }

    public void setFacingForPlacement(LivingEntity placer) {
        if (placer == null) {
            this.facing = Direction.NORTH;
        } else {
            this.facing = Direction.orderedByNearest(placer)[0].getOpposite();
        }
        setChanged();
        sync();
    }

    @Override
    public boolean onCasterRightClick(
            Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (!(level instanceof ServerLevel)) {
            return true;
        }
        if (!(player.pick(player.blockInteractionRange(), 0.0F, false) instanceof BlockHitResult hit)
                || !hit.getBlockPos().equals(pos)) {
            return false;
        }
        if (!handleCasterClick(
                BlockEssentiaTransport.resolveSubHit(getBlockState(), hit, pos), player.isShiftKeyDown())) {
            return false;
        }
        player.swing(hand);
        return true;
    }

    public boolean handleCasterClick(int subHit, boolean sneaking) {
        if (level == null || subHit < 0 || subHit >= 6) return false;
        Direction dir = Direction.values()[subHit];
        if (sneaking) {
            cycleChokedSide(dir);
            level.playSound(
                    null,
                    getBlockPos(),
                    TTSounds.SQUEEK.get(),
                    SoundSource.BLOCKS,
                    0.6F,
                    2.0F + level.getRandom().nextFloat() * 0.2F);
            return true;
        }
        toggleOpenSide(dir);
        BlockEssentiaTransport.refreshConnections(level, getBlockPos());
        BlockEssentiaTransport.refreshConnections(level, getBlockPos().relative(dir));
        level.playSound(
                null,
                getBlockPos(),
                TTSounds.TOOL.get(),
                SoundSource.BLOCKS,
                0.5F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
        return true;
    }

    public int visSize() {
        return contents.totalAmount();
    }

    public int fill(ResourceKey<IAspect> aspectKey, int amount) {
        if (amount != 1) return amount;
        if (visSize() < MAX_AMOUNT) {
            Holder<IAspect> holder = EssentiaTransportHelper.resolve(level, aspectKey);
            if (holder == null) return amount;
            contents = contents.add(new AspectInstance(holder, amount));
            setChanged();
            sync();
            return 0;
        }
        return amount;
    }

    public boolean drain(Holder<IAspect> aspect, int amount) {
        if (contents.amountOf(aspect) >= amount) {
            contents = contents.remove(aspect, amount);
            setChanged();
            sync();
            return true;
        }
        return false;
    }

    void sync() {
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && openSides[face.ordinal()];
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != null && openSides[face.ordinal()];
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != null && openSides[face.ordinal()];
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        int choke = chokedSides[face.ordinal()];
        if (choke == 2) return 0;
        if (bellows > 0 && choke != 1) return bellows * 32;
        return 1;
    }

    public int bellowsCount() {
        return bellows;
    }

    private void refreshBellows() {
        if (level == null) {
            bellows = 0;
            return;
        }
        bellows = BellowsHelper.countBellows(level, getBlockPos(), Direction.values());
    }

    @Override
    public Holder<IAspect> getEssentiaType(Direction face) {
        List<AspectInstance> entries = contents.entries();
        if (entries.isEmpty()) return null;
        int idx = level == null ? 0 : level.getRandom().nextInt(entries.size());
        return entries.get(idx).aspect();
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return visSize();
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || level == null) return 0;
        BlockPos neighbourPos = getBlockPos().relative(face);
        IEssentiaTransport remote = EssentiaFlowHandler.transport(level, neighbourPos, face.getOpposite());
        int suction = remote == null ? 0 : remote.getSuctionAmount(face.getOpposite());
        for (Direction dir : Direction.values()) {
            if (!canOutputTo(dir) || dir == face) continue;
            BlockPos otherPos = getBlockPos().relative(dir);
            IEssentiaTransport other = EssentiaFlowHandler.transport(level, otherPos, dir.getOpposite());
            if (other == null) continue;
            int otherSuck = other.getSuctionAmount(dir.getOpposite());
            Holder<IAspect> otherType = other.getSuctionType(dir.getOpposite());
            if ((otherType == null || otherType.equals(aspect))
                    && suction < otherSuck
                    && getSuctionAmount(dir) < otherSuck) {
                return 0;
            }
        }
        int available = contents.amountOf(aspect);
        int taken = Math.min(amount, available);
        if (taken <= 0) return 0;
        return drain(aspect, taken) ? taken : 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canInputFrom(face)) return 0;
        ResourceKey<IAspect> key = aspect == null ? null : aspect.unwrapKey().orElse(null);
        if (key == null) return 0;
        return amount - fill(key, amount);
    }

    public void tickServer(Level level, BlockPos pos, BlockState state) {
        tickCount++;
        if (bellows < 0 || tickCount % 20 == 0) {
            refreshBellows();
        }
        if (tickCount % 5 == 0 && visSize() < MAX_AMOUNT) {
            fillBuffer(level, pos);
        }
    }

    private void fillBuffer(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            if (!canInputFrom(dir)) continue;
            BlockPos neighbourPos = pos.relative(dir);
            IEssentiaTransport remote = EssentiaFlowHandler.transport(level, neighbourPos, dir.getOpposite());
            if (remote == null) continue;
            if (remote.getEssentiaAmount(dir.getOpposite()) > 0
                    && remote.getSuctionAmount(dir.getOpposite()) < getSuctionAmount(dir)
                    && getSuctionAmount(dir) >= remote.getMinimumSuction()) {
                Holder<IAspect> ta = remote.getEssentiaType(dir.getOpposite());
                if (ta == null) continue;
                ResourceKey<IAspect> key = ta.unwrapKey().orElse(null);
                if (key == null) continue;
                int taken = remote.takeEssentia(ta, 1, dir.getOpposite());
                if (taken > 0) {
                    fill(key, taken);
                }
                return;
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        contents = TTNbt.read(input, "Contents", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        List<Integer> choked =
                TTNbt.read(input, "Choked", CHOKED_CODEC, registries).orElse(List.of());
        for (int i = 0; i < 6 && i < choked.size(); i++) {
            chokedSides[i] = choked.get(i);
        }
        List<Boolean> open = TTNbt.read(input, "Open", OPEN_CODEC, registries).orElse(List.of());
        for (int i = 0; i < 6; i++) {
            openSides[i] = i < open.size() ? open.get(i) : true;
        }
        int facingOrdinal = (input.contains("Facing") ? input.getInt("Facing") : Direction.NORTH.ordinal());
        if (facingOrdinal >= 0 && facingOrdinal < 6) {
            facing = Direction.values()[facingOrdinal];
        } else {
            facing = Direction.NORTH;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TTNbt.store(output, "Contents", AspectList.CODEC, registries, contents);
        List<Integer> choked =
                List.of(chokedSides[0], chokedSides[1], chokedSides[2], chokedSides[3], chokedSides[4], chokedSides[5]);
        TTNbt.store(output, "Choked", CHOKED_CODEC, registries, choked);
        List<Boolean> open =
                List.of(openSides[0], openSides[1], openSides[2], openSides[3], openSides[4], openSides[5]);
        TTNbt.store(output, "Open", OPEN_CODEC, registries, open);
        output.putInt("Facing", facing.ordinal());
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
