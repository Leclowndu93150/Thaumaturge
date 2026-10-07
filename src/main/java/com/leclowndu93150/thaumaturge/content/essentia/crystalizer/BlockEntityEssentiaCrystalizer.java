package com.leclowndu93150.thaumaturge.content.essentia.crystalizer;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.mojang.serialization.Codec;
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
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaCrystalizer extends BlockEntity implements IEssentiaTransport {
    public static final int TARGET_PROGRESS = 200;
    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;
    private static final int DRAW_INTERVAL = 5;
    private static final int SUCTION_EMPTY = 128;
    private static final int SUCTION_BUSY = 64;
    private static final int MAX_CENTIVIS_DRAIN = 20;
    private static final int PROGRESS_PER_CENTIVIS = 2;
    private static final double EJECT_OFFSET = 0.65;
    private static final double EJECT_SPEED = 0.04;
    private static final float FINISH_VOLUME = 0.25F;
    private static final float FINISH_PITCH = 2.6F;
    private static final float FINISH_PITCH_SPREAD = 0.8F;
    private static final int VENT_TICKS = 7;
    private static final double VENT_JITTER = 0.1;
    private static final double VENT_REACH = 2.1;
    private static final double VENT_SPEED = 4.0;
    private static final int VENT_COLOR = 0xFFFFFF;
    private static final float VENT_SCALE = 4.0F;
    private static final float COLOR_STEP = 0.05F;
    private static final float MAX_SPIN = 20.0F;
    private static final float SPIN_UP = 0.1F;
    private static final float SPIN_DOWN = 0.2F;
    private static final float FULL_TURN = 360.0F;

    private @Nullable ResourceKey<IAspect> aspect;
    private int progress;
    private int venting;
    private float rotation;
    private float rotationSpeed;
    private float crystalRed = 1.0F;
    private float crystalGreen = 1.0F;
    private float crystalBlue = 1.0F;

    public BlockEntityEssentiaCrystalizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ESSENTIA_CRYSTALIZER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaCrystalizer crystalizer) {
        if (level instanceof ServerLevel serverLevel) {
            crystalizer.tickServer(serverLevel, pos);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaCrystalizer crystalizer) {
        crystalizer.tickClient(level, pos);
    }

    private void tickServer(ServerLevel level, BlockPos pos) {
        if (venting > 0) {
            venting--;
            vent(level, pos);
        }
        if (level.getGameTime() % DRAW_INTERVAL != 0 || level.hasNeighborSignal(pos)) {
            return;
        }
        if (aspect == null) {
            drawEssentia(level, pos);
            return;
        }
        int requested = Math.min(MAX_CENTIVIS_DRAIN, Math.max(1, (TARGET_PROGRESS - progress) / 2));
        float drained = AuraHelper.drainVis(level, pos, requested / (float) WandEconomy.CENTIVIS_PER_VIS, false);
        progress += 1 + Math.round(drained * WandEconomy.CENTIVIS_PER_VIS) * PROGRESS_PER_CENTIVIS;
        setChanged();
        if (progress >= TARGET_PROGRESS) {
            finishCrystal(level, pos);
        }
    }

    private void tickClient(Level level, BlockPos pos) {
        int color = 0xFFFFFF;
        Holder<IAspect> holder = aspect == null ? null : EssentiaTransportHelper.resolve(level, aspect);
        if (holder != null) {
            color = holder.value().color();
        }
        crystalRed = approach(crystalRed, ((color >> 16) & 0xFF) / 255.0F);
        crystalGreen = approach(crystalGreen, ((color >> 8) & 0xFF) / 255.0F);
        crystalBlue = approach(crystalBlue, (color & 0xFF) / 255.0F);
        rotation = (rotation + rotationSpeed) % FULL_TURN;
        boolean active = aspect != null && !level.hasNeighborSignal(pos);
        if (active) {
            rotationSpeed = Math.min(MAX_SPIN, rotationSpeed + SPIN_UP);
        } else {
            rotationSpeed = Math.max(0.0F, rotationSpeed - SPIN_DOWN);
        }
    }

    private static float approach(float current, float target) {
        return current < target ? Math.min(target, current + COLOR_STEP) : Math.max(target, current - COLOR_STEP);
    }

    private Direction inputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private void drawEssentia(Level level, BlockPos pos) {
        Direction face = inputFace();
        Direction remoteFace = face.getOpposite();
        IEssentiaTransport remote = EssentiaFlowHandler.transport(level, pos.relative(face), remoteFace);
        if (remote == null || !remote.canOutputTo(remoteFace) || remote.getEssentiaAmount(remoteFace) <= 0) {
            return;
        }
        int suction = getSuctionAmount(face);
        if (remote.getSuctionAmount(remoteFace) >= suction || suction < remote.getMinimumSuction()) {
            return;
        }
        Holder<IAspect> available = remote.getEssentiaType(remoteFace);
        if (available != null && remote.takeEssentia(available, 1, remoteFace) == 1) {
            accept(available);
        }
    }

    private boolean accept(Holder<IAspect> incoming) {
        ResourceKey<IAspect> key = incoming.unwrapKey().orElse(null);
        if (key == null) {
            return false;
        }
        aspect = key;
        progress = 0;
        markUpdated();
        return true;
    }

    private void finishCrystal(ServerLevel level, BlockPos pos) {
        Holder<IAspect> holder = aspect == null ? null : EssentiaTransportHelper.resolve(level, aspect);
        aspect = null;
        progress = 0;
        markUpdated();
        if (holder == null) {
            return;
        }
        Direction output = inputFace().getOpposite();
        ItemStack remainder = InvHelper.insertStackAt(level, pos.relative(output), inputFace(), EssentiaCrystalFactory.of(holder), false);
        if (!remainder.isEmpty()) {
            ItemEntity item = new ItemEntity(level, pos.getX() + 0.5 + output.getStepX() * EJECT_OFFSET, pos.getY() + 0.5 + output.getStepY() * EJECT_OFFSET,
                    pos.getZ() + 0.5 + output.getStepZ() * EJECT_OFFSET, remainder);
            item.setDeltaMovement(output.getStepX() * EJECT_SPEED, output.getStepY() * EJECT_SPEED, output.getStepZ() * EJECT_SPEED);
            level.addFreshEntity(item);
            venting = VENT_TICKS;
        }
        RandomSource random = level.getRandom();
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, FINISH_VOLUME, FINISH_PITCH + (random.nextFloat() - random.nextFloat()) * FINISH_PITCH_SPREAD);
    }

    private void vent(ServerLevel level, BlockPos pos) {
        Direction output = inputFace().getOpposite();
        RandomSource random = level.getRandom();
        Vec3 origin = new Vec3(pos.getX() + 0.5 + jitter(random) + output.getStepX() / VENT_REACH, pos.getY() + 0.5 + jitter(random) + output.getStepY() / VENT_REACH,
                pos.getZ() + 0.5 + jitter(random) + output.getStepZ() / VENT_REACH);
        Effects.vent(level, origin).motion(output.getStepX() / VENT_SPEED + jitter(random), output.getStepY() / VENT_SPEED + jitter(random), output.getStepZ() / VENT_SPEED + jitter(random))
                .color(VENT_COLOR).scale(VENT_SCALE).send();
    }

    private static double jitter(RandomSource random) {
        return VENT_JITTER - random.nextFloat() * VENT_JITTER * 2.0;
    }

    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    public int progress() {
        return progress;
    }

    public float rotation() {
        return rotation;
    }

    public float rotationSpeed() {
        return rotationSpeed;
    }

    public float crystalRed() {
        return crystalRed;
    }

    public float crystalGreen() {
        return crystalGreen;
    }

    public float crystalBlue() {
        return crystalBlue;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == inputFace();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return isConnectable(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (!isConnectable(face) || level == null || level.hasNeighborSignal(worldPosition)) {
            return 0;
        }
        return aspect == null ? SUCTION_EMPTY : SUCTION_BUSY;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return aspect == null || level == null ? null : EssentiaTransportHelper.resolve(level, aspect);
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return aspect == null ? 0 : 1;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> incoming, int amount, Direction face) {
        if (!canInputFrom(face) || amount <= 0 || aspect != null) {
            return 0;
        }
        return accept(incoming) ? 1 : 0;
    }

    @Override
    public int spaceFor(Holder<IAspect> incoming, Direction face) {
        return canInputFrom(face) && aspect == null ? 1 : 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read("Aspect", ASPECT_KEY_CODEC).orElse(null);
        progress = aspect == null ? 0 : Math.clamp(input.getIntOr("Progress", 0), 0, TARGET_PROGRESS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspect != null) {
            output.store("Aspect", ASPECT_KEY_CODEC, aspect);
            output.putInt("Progress", progress);
        }
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
