package com.leclowndu93150.thaumaturge.content.essentia.crystalizer;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
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
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaCrystalizer extends BlockEntity implements IEssentiaTransport {
    private static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;
    private static final int DRAW_INTERVAL = 5;
    private static final int TARGET_PROGRESS = 200;
    private static final int SUCTION_EMPTY = 128;
    private static final int SUCTION_BUSY = 64;
    private static final int MAX_VIS_DRAIN = 20;
    private static final int VENT_EVENT = 0;
    private static final int VENT_TICKS = 7;

    private @Nullable ResourceKey<IAspect> aspect;
    private int progress;
    private int tickCount;

    public float rotation;
    public float rotationSpeed;
    public float crystalRed = 1.0F;
    public float crystalGreen = 1.0F;
    public float crystalBlue = 1.0F;
    public int venting;

    public BlockEntityEssentiaCrystalizer(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ESSENTIA_CRYSTALIZER.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    public int progress() {
        return progress;
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, BlockEntityEssentiaCrystalizer crystalizer) {
        crystalizer.tickCount++;
        if (crystalizer.tickCount % DRAW_INTERVAL != 0 || level.hasNeighborSignal(pos)) return;

        if (crystalizer.aspect == null) {
            crystalizer.drawEssentia(level, pos);
            crystalizer.progress = 0;
            return;
        }

        int requestedCentivis = Math.min(MAX_VIS_DRAIN, Math.max(1, (TARGET_PROGRESS - crystalizer.progress) / 2));
        // TC4's vis network stored 100 internal units per displayed vis. The modern aura uses
        // displayed vis units directly, so convert the legacy Terra-vis request to preserve cost.
        float drainedVis = AuraHelper.drainVis(level, pos, requestedCentivis / 100.0F, false);
        int drainedCentivis = Math.round(drainedVis * 100.0F);
        crystalizer.progress += 1 + drainedCentivis * 2;
        crystalizer.setChanged();
        if (crystalizer.progress >= TARGET_PROGRESS) {
            crystalizer.finishCrystal(level, pos);
        }
    }

    public static void clientTick(
            Level level, BlockPos pos, BlockState state, BlockEntityEssentiaCrystalizer crystalizer) {
        float targetRed = 1.0F;
        float targetGreen = 1.0F;
        float targetBlue = 1.0F;
        if (crystalizer.aspect != null) {
            Holder<IAspect> holder = crystalizer.resolve(crystalizer.aspect);
            int rgb = holder.value().color();
            targetRed = ((rgb >> 16) & 0xFF) / 255.0F;
            targetGreen = ((rgb >> 8) & 0xFF) / 255.0F;
            targetBlue = (rgb & 0xFF) / 255.0F;
        }
        crystalizer.crystalRed = approach(crystalizer.crystalRed, targetRed, 0.05F);
        crystalizer.crystalGreen = approach(crystalizer.crystalGreen, targetGreen, 0.05F);
        crystalizer.crystalBlue = approach(crystalizer.crystalBlue, targetBlue, 0.05F);

        crystalizer.rotation = (crystalizer.rotation + crystalizer.rotationSpeed) % 360.0F;
        boolean active = crystalizer.aspect != null && !level.hasNeighborSignal(pos);
        if (active && crystalizer.rotationSpeed < 20.0F) {
            crystalizer.rotationSpeed = Math.min(20.0F, crystalizer.rotationSpeed + 0.1F);
        } else if (!active && crystalizer.rotationSpeed > 0.0F) {
            crystalizer.rotationSpeed = Math.max(0.0F, crystalizer.rotationSpeed - 0.2F);
        }
        crystalizer.clientVent(level, pos);
    }

    private static float approach(float current, float target, float step) {
        return current < target ? Math.min(target, current + step) : Math.max(target, current - step);
    }

    private Direction inputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private void drawEssentia(Level level, BlockPos pos) {
        Direction face = inputFace();
        IEssentiaTransport remote = EssentiaFlowHandler.transport(level, pos.relative(face), face.getOpposite());
        if (remote == null
                || !remote.canOutputTo(face.getOpposite())
                || remote.getEssentiaAmount(face.getOpposite()) <= 0) return;
        if (remote.getSuctionAmount(face.getOpposite()) >= getSuctionAmount(face)
                || getSuctionAmount(face) < remote.getMinimumSuction()) return;
        Holder<IAspect> available = remote.getEssentiaType(face.getOpposite());
        if (available != null && remote.takeEssentia(available, 1, face.getOpposite()) == 1) {
            aspect = available.unwrapKey().orElse(null);
            progress = 0;
            changedAndSync();
        }
    }

    private void finishCrystal(Level level, BlockPos pos) {
        if (aspect == null) return;
        Holder<IAspect> holder = resolve(aspect);
        ItemStack crystal = EssentiaCrystalFactory.of(holder);
        Direction outputFace = inputFace().getOpposite();
        BlockPos outputPos = pos.relative(outputFace);
        ItemStack remainder = InvHelper.insertStackAt(level, outputPos, inputFace(), crystal, false);
        if (!remainder.isEmpty()) {
            double x = pos.getX() + 0.5D + outputFace.getStepX() * 0.65D;
            double y = pos.getY() + 0.5D + outputFace.getStepY() * 0.65D;
            double z = pos.getZ() + 0.5D + outputFace.getStepZ() * 0.65D;
            ItemEntity entity = new ItemEntity(level, x, y, z, remainder);
            entity.setDeltaMovement(
                    outputFace.getStepX() * 0.04D, outputFace.getStepY() * 0.04D, outputFace.getStepZ() * 0.04D);
            if (level instanceof net.minecraft.server.level.ServerLevel server) {
                // TC4 fired the vent event as part of the ejection attempt, before spawning the item.
                server.blockEvent(pos, getBlockState().getBlock(), VENT_EVENT, 0);
            }
            level.addFreshEntity(entity);
        }
        level.playSound(
                null,
                pos,
                SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS,
                0.25F,
                2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
        aspect = null;
        progress = 0;
        changedAndSync();
    }

    private void clientVent(Level level, BlockPos pos) {
        if (venting <= 0) return;
        venting--;
        Direction output = inputFace().getOpposite();
        RandomSource random = level.getRandom();
        double fx = 0.1D - random.nextFloat() * 0.2D;
        double fy = 0.1D - random.nextFloat() * 0.2D;
        double fz = 0.1D - random.nextFloat() * 0.2D;
        double vx = output.getStepX() / 4.0D + 0.1D - random.nextFloat() * 0.2D;
        double vy = output.getStepY() / 4.0D + 0.1D - random.nextFloat() * 0.2D;
        double vz = output.getStepZ() / 4.0D + 0.1D - random.nextFloat() * 0.2D;
        level.addParticle(
                new VentParticleOptions(vx, vy, vz, 0xFFFFFF, 4.0F, false),
                pos.getX() + 0.5D + fx + output.getStepX() / 2.1D,
                pos.getY() + 0.5D + fy + output.getStepY() / 2.1D,
                pos.getZ() + 0.5D + fz + output.getStepZ() / 2.1D,
                0.0D,
                0.0D,
                0.0D);
    }

    @Override
    public boolean triggerEvent(int event, int param) {
        if (event == VENT_EVENT) {
            if (level != null && level.isClientSide()) venting = VENT_TICKS;
            return true;
        }
        return super.triggerEvent(event, param);
    }

    private Holder<IAspect> resolve(ResourceKey<IAspect> key) {
        return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && face == inputFace();
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
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (!isConnectable(face) || level == null || level.hasNeighborSignal(getBlockPos())) return 0;
        return aspect == null ? SUCTION_EMPTY : SUCTION_BUSY;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return aspect == null || level == null ? null : resolve(aspect);
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
        if (!canInputFrom(face) || incoming == null || amount <= 0 || aspect != null) return 0;
        aspect = incoming.unwrapKey().orElse(null);
        if (aspect == null) return 0;
        progress = 0;
        changedAndSync();
        return 1;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int spaceFor(Holder<IAspect> incoming, Direction face) {
        if (!canInputFrom(face) || incoming == null || aspect != null) return 0;
        return 1;
    }

    private void changedAndSync() {
        setChanged();
        syncToClient();
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspect = !input.contains("HasAspect") || input.getBoolean("HasAspect")
                ? TCNbt.read(input, "Aspect", ASPECT_KEY_CODEC, registries).orElse(null)
                : null;
        progress = aspect == null ? 0 : Math.max(0, Math.min(TARGET_PROGRESS, input.getInt("Progress")));
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putBoolean("HasAspect", aspect != null);
        if (aspect != null) {
            TCNbt.store(output, "Aspect", ASPECT_KEY_CODEC, registries, aspect);
            output.putInt("Progress", progress);
        }
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
