package com.leclowndu93150.thaumaturge.content.essentia;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityCentrifuge extends AbstractSyncedBlockEntity implements IEssentiaTransport {
    private static final String INPUT_KEY = "AspectIn";
    private static final String OUTPUT_KEY = "AspectOut";
    private static final Direction INTAKE = Direction.DOWN;
    private static final Direction OUTLET = Direction.UP;
    private static final int PULL_INTERVAL = 5;
    private static final int PROCESS_TICKS = 39;
    private static final int EMPTY_SUCTION = 128;
    private static final int BUSY_SUCTION = 64;
    private static final int NO_SUCTION = 0;
    private static final int ONE_POINT = 1;
    private static final int NONE = 0;
    private static final int BREAK_FLUX = 1;
    private static final float TOP_SPEED = 24.0F;
    private static final float SPIN_UP_STEP = 2.5F;
    private static final float SPIN_DOWN_STEP = 0.6F;
    private static final float HALF_TURN = 180.0F;
    private static final float FULL_TURN = 360.0F;
    private static final float PUMP_VOLUME = 0.2F;
    private static final float PUMP_PITCH_LOW = 0.85F;
    private static final float PUMP_PITCH_RANGE = 0.3F;

    public float rotation;
    public float rotationSpeed;

    private @Nullable ResourceKey<IAspect> intake;
    private @Nullable ResourceKey<IAspect> product;
    private int processLeft;

    public BlockEntityCentrifuge(BlockPos pos, BlockState state) {
        super(TTBlockEntities.CENTRIFUGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityCentrifuge centrifuge) {
        if (level instanceof ServerLevel server) {
            centrifuge.runServer(server, pos);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityCentrifuge centrifuge) {
        centrifuge.turnRotor(centrifuge.isSpinning());
    }

    public boolean isSpinning() {
        return intake != null && !isPowered();
    }

    private boolean isPowered() {
        return level != null && level.hasNeighborSignal(worldPosition);
    }

    private void runServer(ServerLevel server, BlockPos pos) {
        boolean powered = server.hasNeighborSignal(pos);
        boolean driven = intake != null && !powered;
        if (turnRotor(driven)) {
            playPump(server, pos);
        }
        if (powered) {
            return;
        }
        if (intake != null) {
            advanceProcess(server);
        } else if (product == null && server.getGameTime() % PULL_INTERVAL == 0) {
            drawFromBelow(server, pos);
        }
    }

    private boolean turnRotor(boolean driven) {
        float target = driven ? TOP_SPEED : 0.0F;
        if (rotationSpeed < target) {
            rotationSpeed = Math.min(target, rotationSpeed + SPIN_UP_STEP);
        } else if (rotationSpeed > target) {
            rotationSpeed = Math.max(target, rotationSpeed - SPIN_DOWN_STEP);
        }
        int halfBefore = (int) (rotation / HALF_TURN);
        rotation += rotationSpeed;
        boolean crossedHalf = (int) (rotation / HALF_TURN) != halfBefore;
        if (rotation >= FULL_TURN) {
            rotation -= FULL_TURN;
        }
        return driven && crossedHalf;
    }

    private static void playPump(ServerLevel server, BlockPos pos) {
        RandomSource random = server.getRandom();
        float pitch = PUMP_PITCH_LOW + random.nextFloat() * PUMP_PITCH_RANGE;
        server.playSound(null, pos, TTSounds.PUMP.get(), SoundSource.BLOCKS, PUMP_VOLUME, pitch);
    }

    private void advanceProcess(ServerLevel server) {
        if (processLeft > 0) {
            processLeft--;
            return;
        }
        if (product != null || intake == null) {
            return;
        }
        Holder<IAspect> held = Aspects.resolve(server, intake);
        if (held == null) {
            intake = null;
            setChangedAndSync();
            return;
        }
        List<Holder<IAspect>> parts = held.value().components();
        if (parts.isEmpty()) {
            intake = null;
            setChangedAndSync();
            return;
        }
        Holder<IAspect> chosen = parts.get(server.getRandom().nextInt(parts.size()));
        product = chosen.unwrapKey().orElse(null);
        intake = null;
        setChangedAndSync();
    }

    private void drawFromBelow(ServerLevel server, BlockPos pos) {
        Direction touching = INTAKE.getOpposite();
        IEssentiaTransport source = EssentiaFlowHandler.transport(server, pos.relative(INTAKE), touching);
        if (source == null || !source.isConnectable(touching) || !source.canOutputTo(touching)) {
            return;
        }
        Holder<IAspect> offered = source.getEssentiaType(touching);
        if (!isCompound(offered) || source.getEssentiaAmount(touching) < ONE_POINT) {
            return;
        }
        int pull = getSuctionAmount(INTAKE);
        if (source.getSuctionAmount(touching) >= pull || pull < source.getMinimumSuction()) {
            return;
        }
        if (source.takeEssentia(offered, ONE_POINT, touching) == ONE_POINT) {
            begin(offered);
        }
    }

    private static boolean isCompound(@Nullable Holder<IAspect> aspect) {
        return aspect != null && !aspect.value().isPrimal() && aspect.unwrapKey().isPresent();
    }

    private void begin(Holder<IAspect> aspect) {
        intake = aspect.unwrapKey().orElse(null);
        processLeft = PROCESS_TICKS;
        setChangedAndSync();
    }

    private boolean holdsNothing() {
        return intake == null && product == null;
    }

    private @Nullable Holder<IAspect> resolve(@Nullable ResourceKey<IAspect> key) {
        return key == null ? null : Aspects.resolve(level, key);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server && product != null) {
            AuraHelper.polluteAura(server, pos, BREAK_FLUX, true);
            product = null;
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side) {
        if (count < ONE_POINT || side != INTAKE || intake != null || !isCompound(type)) {
            return NONE;
        }
        begin(type);
        return ONE_POINT;
    }

    @Override
    public int spaceFor(Holder<IAspect> type, Direction side) {
        return side == INTAKE && intake == null && isCompound(type) ? ONE_POINT : NONE;
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side) {
        if (count < ONE_POINT || side != OUTLET || product == null || !type.is(product)) {
            return NONE;
        }
        product = null;
        setChangedAndSync();
        return ONE_POINT;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction side) {
        return side == INTAKE ? resolve(intake) : resolve(product);
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction side) {
        ResourceKey<IAspect> held = side == INTAKE ? intake : product;
        return held == null ? NONE : ONE_POINT;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int count) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction side) {
        return null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction side) {
        if (side == OUTLET || isPowered()) {
            return NO_SUCTION;
        }
        return holdsNothing() ? EMPTY_SUCTION : BUSY_SUCTION;
    }

    @Override
    public int getMinimumSuction() {
        return NO_SUCTION;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return side == OUTLET;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return side == INTAKE;
    }

    @Override
    public boolean isConnectable(Direction side) {
        return side == INTAKE || side == OUTLET;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(INPUT_KEY, LegacyIds.ASPECT_KEY_CODEC, intake);
        output.storeNullable(OUTPUT_KEY, LegacyIds.ASPECT_KEY_CODEC, product);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        intake = input.read(INPUT_KEY, LegacyIds.ASPECT_KEY_CODEC).orElse(null);
        product = input.read(OUTPUT_KEY, LegacyIds.ASPECT_KEY_CODEC).orElse(null);
        processLeft = intake == null ? NONE : PROCESS_TICKS;
    }
}
