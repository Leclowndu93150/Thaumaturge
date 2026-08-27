package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.server.TCFakePlayer;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

public final class BlockEntityArcaneBore extends BlockEntity implements ArcaneBoreHost {
    public static final float EYE_HEIGHT = 0.8125F;

    private static final float MAX_HEAD_PITCH = 90.0F;
    private static final double EJECT_DISTANCE = 0.75;

    private final ArcaneBoreCore core = new ArcaneBoreCore();

    private ItemStack tool = ItemStack.EMPTY;
    private @Nullable BlockPos digTarget;
    private boolean digging;
    private int tickCount;
    private float yaw;
    private float pitch;
    private float prevYaw;
    private float prevPitch;

    public BlockEntityArcaneBore(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ARCANE_BORE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityArcaneBore bore) {
        bore.tickCount++;
        bore.core.serverTick(bore, (ServerLevel) level, bore.tickCount);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityArcaneBore bore) {
        bore.prevYaw = bore.yaw;
        bore.prevPitch = bore.pitch;
        BlockPos target = bore.digTarget;
        if (target != null) {
            bore.aimBore(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, ArcaneBoreCore.IDLE_YAW_STEP, ArcaneBoreCore.DIG_PITCH_STEP);
            return;
        }
        Direction facing = bore.boreFacing();
        bore.aimBore(pos.getX() + 0.5 + facing.getStepX(), pos.getY() + facing.getStepY(), pos.getZ() + 0.5 + facing.getStepZ(), ArcaneBoreCore.IDLE_YAW_STEP, ArcaneBoreCore.IDLE_PITCH_STEP);
    }

    public float renderYaw(float partialTicks) {
        return Mth.rotLerp(partialTicks, prevYaw, yaw);
    }

    public float renderPitch(float partialTicks) {
        return Mth.lerp(partialTicks, prevPitch, pitch);
    }

    public boolean digging() {
        return digging;
    }

    @Override
    public Level boreLevel() {
        return Objects.requireNonNull(level);
    }

    @Override
    public BlockPos borePos() {
        return worldPosition;
    }

    @Override
    public Vec3 borePosition() {
        return new Vec3(worldPosition.getX() + 0.5, worldPosition.getY(), worldPosition.getZ() + 0.5);
    }

    @Override
    public float boreEyeHeight() {
        return EYE_HEIGHT;
    }

    @Override
    public Direction boreFacing() {
        return getBlockState().getValue(BlockArcaneBore.FACING);
    }

    @Override
    public boolean boreActive() {
        return getBlockState().getValue(BlockArcaneBore.POWERED);
    }

    @Override
    public RandomSource boreRandom() {
        return boreLevel().getRandom();
    }

    @Override
    public CollisionContext boreCollisionContext() {
        return CollisionContext.empty();
    }

    @Override
    public ItemStack boreTool() {
        return tool;
    }

    @Override
    public void setBoreTool(ItemStack stack) {
        tool = stack;
        setChanged();
    }

    @Override
    public void hurtBoreTool() {
        if (level instanceof ServerLevel serverLevel) {
            tool.hurtAndBreak(1, serverLevel, null, item -> setBoreTool(ItemStack.EMPTY));
        }
    }

    @Override
    public void aimBore(double x, double y, double z, float yawStep, float pitchStep) {
        double dx = x - (worldPosition.getX() + 0.5);
        double dy = y - (worldPosition.getY() + EYE_HEIGHT);
        double dz = z - (worldPosition.getZ() + 0.5);
        float wantedYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        float wantedPitch = (float) (-(Mth.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * Mth.RAD_TO_DEG));
        yaw = approach(yaw, wantedYaw, yawStep);
        pitch = Mth.clamp(approach(pitch, wantedPitch, pitchStep), -MAX_HEAD_PITCH, MAX_HEAD_PITCH);
    }

    private static float approach(float from, float to, float maxDelta) {
        return from + Mth.clamp(Mth.degreesDifference(from, to), -maxDelta, maxDelta);
    }

    @Override
    public void setBoreDigging(boolean digging) {
        BlockPos target = core.digTarget();
        if (this.digging == digging && Objects.equals(this.digTarget, target)) {
            return;
        }
        this.digging = digging;
        this.digTarget = target;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public void playBoreSound(SoundEvent sound, float volume, float pitch) {
        boreLevel().playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, pitch);
    }

    @Override
    public FakePlayer boreDigger(ServerLevel level) {
        return TCFakePlayer.BORE.at(level, borePosition().add(0.0, EYE_HEIGHT, 0.0), yaw, pitch);
    }

    @Override
    public void dropBoreOutput(ServerLevel level, ItemStack stack) {
        Direction back = boreFacing().getOpposite();
        double x = worldPosition.getX() + 0.5 + back.getStepX() * EJECT_DISTANCE;
        double y = worldPosition.getY() + 0.5 + back.getStepY() * EJECT_DISTANCE;
        double z = worldPosition.getZ() + 0.5 + back.getStepZ() * EJECT_DISTANCE;
        level.addFreshEntity(new ItemEntity(level, x, y, z, stack));
    }

    @Override
    public void showBoreDig(ServerLevel level, BlockPos target, int delay) {
        Effects.boreDig(level, target, worldPosition, delay);
    }

    @Override
    public float boreHealth() {
        return 1.0F;
    }

    @Override
    public float boreMaxHealth() {
        return 1.0F;
    }

    @Override
    public boolean boreValid() {
        return !isRemoved();
    }

    @Override
    public Component boreDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public void writeBoreRef(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(true);
        buf.writeBlockPos(worldPosition);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !tool.isEmpty()) {
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, tool);
            tool = ItemStack.EMPTY;
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Tool", ItemStack.OPTIONAL_CODEC, tool);
        output.putFloat("Charge", core.charge());
        writeSyncData(output);
    }

    private void writeSyncData(ValueOutput output) {
        output.putBoolean("Digging", digging);
        if (digTarget != null) {
            output.store("DigTarget", BlockPos.CODEC, digTarget);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        tool = input.read("Tool", ItemStack.OPTIONAL_CODEC).orElse(tool);
        core.setCharge(input.getFloatOr("Charge", core.charge()));
        digging = input.getBooleanOr("Digging", false);
        digTarget = input.read("DigTarget", BlockPos.CODEC).orElse(null);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            writeSyncData(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
