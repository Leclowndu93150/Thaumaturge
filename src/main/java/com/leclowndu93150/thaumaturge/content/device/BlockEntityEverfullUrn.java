package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class BlockEntityEverfullUrn extends BlockEntity {
    public static final int CAPACITY = 1000;
    private static final int WORK_INTERVAL = 5;
    private static final int PUSH_AMOUNT = 25;
    private static final int CAULDRON_COST = 333;
    private static final float MAX_REFILL_VIS = 0.1F;
    private static final int ZONE_XZ = 5;
    private static final int ZONE_Y = 3;
    private static final int STREAM_PARTICLES = 10;

    private final FluidTank tank = new FluidTank(CAPACITY) {
        @Override
        public boolean isFluidValid(FluidStack resource) {
            return resource.is(Fluids.WATER);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
        }
    };

    private final List<Integer> handlers = new ArrayList<>();
    private int zone;
    private int counter;

    public BlockEntityEverfullUrn(BlockPos pos, BlockState state) {
        super(TCBlockEntities.EVERFULL_URN.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public int waterAmount() {
        return tank.getFluidAmount();
    }

    public void drainWater(int amount) {
        {
            tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
        }
    }

    private void fillWater(int amount) {
        {
            tank.fill(new FluidStack(Fluids.WATER, amount), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEverfullUrn urn) {
        urn.counter++;
        if (urn.counter % WORK_INTERVAL != 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        urn.zone++;
        BlockPos probe = urn.zonePos(urn.zone);
        if (!urn.handlers.contains(urn.zone % 75) && urn.acceptsWater(server, probe)) {
            urn.handlers.add(urn.zone % 75);
            urn.setChanged();
        }
        int i = 0;
        while (i < urn.handlers.size() && urn.waterAmount() >= PUSH_AMOUNT) {
            int zz = urn.handlers.get(i);
            BlockPos target = urn.zonePos(zz);
            BlockState targetState = server.getBlockState(target);
            IFluidHandler handler = server.getCapability(Capabilities.FluidHandler.BLOCK, target, Direction.UP);
            if (handler == null) {
                if (!targetState.is(Blocks.CAULDRON) && !targetState.is(Blocks.WATER_CAULDRON)
                        || urn.waterAmount() < CAULDRON_COST) {
                    urn.handlers.remove(i);
                    urn.setChanged();
                    continue;
                }
                if (targetState.is(Blocks.CAULDRON)) {
                    server.setBlock(target, Blocks.WATER_CAULDRON.defaultBlockState(), Block.UPDATE_CLIENTS);
                    urn.drainWater(CAULDRON_COST);
                    urn.splashAt(server, target);
                } else if (targetState.getValue(LayeredCauldronBlock.LEVEL) < LayeredCauldronBlock.MAX_FILL_LEVEL) {
                    server.setBlock(target, targetState.cycle(LayeredCauldronBlock.LEVEL), Block.UPDATE_CLIENTS);
                    server.updateNeighbourForOutputSignal(target, targetState.getBlock());
                    urn.drainWater(CAULDRON_COST);
                    urn.splashAt(server, target);
                }
            } else {
                int moved;
                {
                    moved = handler.fill(new FluidStack(Fluids.WATER, PUSH_AMOUNT), IFluidHandler.FluidAction.EXECUTE);
                    if (moved > 0) {}
                }
                if (moved > 0) {
                    urn.drainWater(moved);
                    urn.splashAt(server, target);
                    break;
                }
            }
            i++;
        }
        if (urn.waterAmount() < CAPACITY) {
            float demand = Math.min(MAX_REFILL_VIS, (CAPACITY - urn.waterAmount()) / (float) CAPACITY);
            if (demand > 0.0F) {
                float drawn = AuraHelper.drainVis(server, pos, demand, false);
                int water = (int) (CAPACITY * drawn);
                if (water > 0) {
                    urn.fillWater(water);
                }
            }
        }
    }

    private BlockPos zonePos(int zone) {
        int x = zone / ZONE_XZ % ZONE_XZ;
        int y = zone / ZONE_XZ / ZONE_XZ % ZONE_Y;
        int z = zone % ZONE_XZ;
        return getBlockPos().offset(x - 2, y - 1, z - 2);
    }

    private boolean acceptsWater(ServerLevel server, BlockPos pos) {
        if (server.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP) != null) {
            return true;
        }
        BlockState state = server.getBlockState(pos);
        return state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON);
    }

    private void splashAt(ServerLevel server, BlockPos target) {
        Vec3 from = Vec3.atCenterOf(getBlockPos()).add(0.0, 0.25, 0.0);
        Vec3 to = Vec3.atCenterOf(target).add(0.0, 0.5, 0.0);
        for (int step = 1; step <= STREAM_PARTICLES; step++) {
            Vec3 point = from.lerp(to, step / (double) STREAM_PARTICLES);
            server.sendParticles(ParticleTypes.SPLASH, point.x, point.y, point.z, 1, 0.03, 0.03, 0.03, 0.0);
        }
        server.sendParticles(
                ParticleTypes.SPLASH,
                target.getX() + 0.5,
                target.getY() + 1.0,
                target.getZ() + 0.5,
                4,
                0.2,
                0.1,
                0.2,
                0.0);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        int[] zones = new int[handlers.size()];
        for (int i = 0; i < zones.length; i++) {
            zones[i] = handlers.get(i);
        }
        output.putIntArray("Handlers", zones);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        if (input.contains("Tank")) {
            tank.readFromNBT(registries, input.getCompound("Tank"));
        }
        handlers.clear();
        int[] zones = input.getIntArray("Handlers");
        for (int zone : zones) {
            handlers.add(zone);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        {
            CompoundTag output = new CompoundTag();
            saveAdditional(output, registries);
            nbt.merge(output);
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
