package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityCentrifuge extends BlockEntity implements IEssentiaTransport {
    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;
    private static final int PROCESS_TICKS = 39;
    private static final int DRAW_INTERVAL = 5;
    private static final int SUCTION_EMPTY = 128;
    private static final int SUCTION_BUSY = 64;
    private static final float MAX_SPIN_SPEED = 20.0F;

    private @Nullable ResourceKey<IAspect> aspectIn;
    private @Nullable ResourceKey<IAspect> aspectOut;
    private int count;
    private int process;

    public float rotation;
    public float rotationSpeed;

    public BlockEntityCentrifuge(BlockPos pos, BlockState state) {
        super(TCBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    public boolean isSpinning() {
        return aspectIn != null;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityCentrifuge centrifuge) {
        if (level.hasNeighborSignal(pos)) {
            return;
        }
        if (centrifuge.aspectOut == null && centrifuge.aspectIn == null && ++centrifuge.count % DRAW_INTERVAL == 0) {
            centrifuge.drawEssentia();
        }
        if (centrifuge.process > 0) {
            centrifuge.process--;
        }
        if (centrifuge.aspectOut == null && centrifuge.aspectIn != null && centrifuge.process == 0) {
            centrifuge.processEssentia();
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityCentrifuge centrifuge) {
        boolean powered = level.hasNeighborSignal(pos);
        if (centrifuge.aspectIn != null && !powered && centrifuge.rotationSpeed < MAX_SPIN_SPEED) {
            centrifuge.rotationSpeed += 2.0F;
        }
        if ((centrifuge.aspectIn == null || powered) && centrifuge.rotationSpeed > 0.0F) {
            centrifuge.rotationSpeed -= 0.5F;
        }
        int previous = (int) centrifuge.rotation;
        centrifuge.rotation += centrifuge.rotationSpeed;
        if (centrifuge.rotation % 180.0F <= 20.0F && previous % 180 >= 160 && centrifuge.rotationSpeed > 0.0F) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.PUMP.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }
    }

    private void processEssentia() {
        if (level == null || aspectIn == null) {
            return;
        }
        Holder<IAspect> input = resolve(aspectIn);
        List<Holder<IAspect>> components = input.value().components();
        if (components.size() >= 2) {
            aspectOut = components.get(level.getRandom().nextInt(2)).unwrapKey().orElse(null);
        }
        aspectIn = null;
        setChanged();
        syncToClient();
    }

    private void drawEssentia() {
        if (level == null) {
            return;
        }
        IEssentiaTransport ic = EssentiaFlowHandler.transport(level, getBlockPos().below(), Direction.UP);
        if (ic == null || !ic.canOutputTo(Direction.UP)) {
            return;
        }
        Holder<IAspect> available = null;
        if (ic.getEssentiaAmount(Direction.UP) > 0 && ic.getSuctionAmount(Direction.UP) < getSuctionAmount(Direction.DOWN) && getSuctionAmount(Direction.DOWN) >= ic.getMinimumSuction()) {
            available = ic.getEssentiaType(Direction.UP);
        }
        if (available != null && !available.value().isPrimal() && ic.takeEssentia(available, 1, Direction.UP) == 1) {
            aspectIn = available.unwrapKey().orElse(null);
            process = PROCESS_TICKS;
            setChanged();
            syncToClient();
        }
    }

    private Holder<IAspect> resolve(ResourceKey<IAspect> key) {
        return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }

    private void syncToClient() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == Direction.UP || face == Direction.DOWN;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == Direction.DOWN;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face == Direction.UP;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (face != Direction.DOWN || level == null) {
            return 0;
        }
        if (level.hasNeighborSignal(getBlockPos())) {
            return 0;
        }
        return aspectIn == null ? SUCTION_EMPTY : SUCTION_BUSY;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return aspectOut == null || level == null ? null : resolve(aspectOut);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return aspectOut != null ? 1 : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || aspectOut == null || amount != 1) {
            return 0;
        }
        if (!aspect.unwrapKey().map(aspectOut::equals).orElse(false)) {
            return 0;
        }
        aspectOut = null;
        setChanged();
        syncToClient();
        return 1;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (aspectIn == null && !aspect.value().isPrimal() && amount > 0) {
            aspectIn = aspect.unwrapKey().orElse(null);
            process = PROCESS_TICKS;
            setChanged();
            syncToClient();
            return 1;
        }
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspectIn = input.read("AspectIn", ASPECT_KEY_CODEC).orElse(null);
        aspectOut = input.read("AspectOut", ASPECT_KEY_CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspectIn != null) {
            output.store("AspectIn", ASPECT_KEY_CODEC, aspectIn);
        }
        if (aspectOut != null) {
            output.store("AspectOut", ASPECT_KEY_CODEC, aspectOut);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
