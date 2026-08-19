package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCParticles;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityLevitator extends BlockEntity {
    private static final int[] RANGES = {4, 8, 16, 32};
    private static final int VIS_BUFFER_MIN = 10;
    private static final float VIS_PER_DRAIN = 1200.0F;
    private static final double PUSH_STRENGTH = 0.1;
    private static final double SPEED_CAP = 0.35;
    private static final double AIRBORNE_GRAVITY_COMP = 0.08;
    private static final int PARTICLE_GRID = 16;
    private static final int PARTICLE_START = 56;

    private int range = 1;
    private int rangeActual;
    private int counter;
    private int vis;

    public BlockEntityLevitator(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LEVITATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityLevitator levitator) {
        levitator.tickInternal(level, pos, state);
    }

    private void tickInternal(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        if (rangeActual > RANGES[range]) {
            rangeActual = 0;
        }
        int probe = counter % RANGES[range];
        if (level.getBlockState(pos.relative(facing, 1 + probe)).isSolidRender()) {
            if (1 + probe < rangeActual) {
                rangeActual = 1 + probe;
            }
            counter = -1;
        } else if (1 + probe > rangeActual) {
            rangeActual = 1 + probe;
        }
        counter++;
        if (!level.isClientSide() && vis < VIS_BUFFER_MIN) {
            vis = (int) (vis + AuraHelper.drainVis(level, pos, 1.0F, false) * VIS_PER_DRAIN);
            setChanged();
            syncToClient();
        }
        if (rangeActual <= 0 || vis <= 0 || !state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        AABB area = new AABB(pos.getX() - (facing.getStepX() < 0 ? rangeActual : 0), pos.getY() - (facing.getStepY() < 0 ? rangeActual : 0), pos.getZ() - (facing.getStepZ() < 0 ? rangeActual : 0),
                pos.getX() + 1 + (facing.getStepX() > 0 ? rangeActual : 0), pos.getY() + 1 + (facing.getStepY() > 0 ? rangeActual : 0), pos.getZ() + 1 + (facing.getStepZ() > 0 ? rangeActual : 0));
        List<Entity> targets = level.getEntitiesOfClass(Entity.class, area);
        boolean lifted = false;
        for (Entity entity : targets) {
            if (!(entity instanceof ItemEntity) && !entity.isPushable() && !(entity instanceof AbstractHorse)) {
                continue;
            }
            lifted = true;
            drawFXAt(level, entity);
            drawFX(level, pos, facing, 0.6);
            Vec3 motion = entity.getDeltaMovement();
            if (!entity.isShiftKeyDown() || facing != Direction.UP) {
                double dx = motion.x + PUSH_STRENGTH * facing.getStepX();
                double dy = motion.y + PUSH_STRENGTH * facing.getStepY();
                double dz = motion.z + PUSH_STRENGTH * facing.getStepZ();
                if (facing.getAxis() != Direction.Axis.Y && !entity.onGround()) {
                    if (dy < 0.0) {
                        dy *= 0.9;
                    }
                    dy += AIRBORNE_GRAVITY_COMP;
                }
                entity.setDeltaMovement(Mth.clamp(dx, -SPEED_CAP, SPEED_CAP), Mth.clamp(dy, -SPEED_CAP, SPEED_CAP), Mth.clamp(dz, -SPEED_CAP, SPEED_CAP));
            } else if (motion.y < 0.0) {
                entity.setDeltaMovement(motion.x, motion.y * 0.9, motion.z);
            }
            entity.resetFallDistance();
            vis -= getCost();
            if (vis <= 0) {
                break;
            }
        }
        drawFX(level, pos, facing, 0.1);
        if (lifted && !level.isClientSide() && counter % 20 == 0) {
            setChanged();
            syncToClient();
        }
    }

    private static void drawFX(Level level, BlockPos pos, Direction facing, double chance) {
        if (!level.isClientSide() || level.getRandom().nextFloat() >= chance) {
            return;
        }
        RandomSource rand = level.getRandom();
        spawnParticle(level, rand, pos.getX() + 0.25F + rand.nextFloat() * 0.5F, pos.getY() + 0.25F + rand.nextFloat() * 0.5F, pos.getZ() + 0.25F + rand.nextFloat() * 0.5F, facing.getStepX() / 50.0,
                facing.getStepY() / 50.0, facing.getStepZ() / 50.0);
    }

    private static void drawFXAt(Level level, Entity entity) {
        if (!level.isClientSide() || level.getRandom().nextFloat() >= 0.1F) {
            return;
        }
        RandomSource rand = level.getRandom();
        spawnParticle(level, rand, entity.getX() + (rand.nextFloat() - rand.nextFloat()) * entity.getBbWidth(), entity.getY() + rand.nextFloat() * entity.getBbHeight(),
                entity.getZ() + (rand.nextFloat() - rand.nextFloat()) * entity.getBbWidth(), (rand.nextFloat() - rand.nextFloat()) * 0.01, (rand.nextFloat() - rand.nextFloat()) * 0.01,
                (rand.nextFloat() - rand.nextFloat()) * 0.01);
    }

    private static void spawnParticle(Level level, RandomSource rand, double x, double y, double z, double vx, double vy, double vz) {
        level.addParticle(TCParticles.LEVITATOR_MIST.get(), x, y, z, vx, vy, vz);
    }

    public int getCost() {
        return RANGES[range] * 2;
    }

    public void increaseRange(Player player) {
        rangeActual = 0;
        if (level == null || level.isClientSide()) {
            return;
        }
        range++;
        if (range >= RANGES.length) {
            range = 0;
        }
        setChanged();
        syncToClient();
        player.sendSystemMessage(Component.translatable("gui.thaumaturge.levitator", RANGES[range], getCost()));
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            BlockState current = getBlockState();
            level.sendBlockUpdated(getBlockPos(), current, current, 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("range", (byte) range);
        output.putInt("vis", vis);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        range = input.getByteOr("range", (byte) 1);
        vis = input.getIntOr("vis", 0);
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
