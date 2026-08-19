package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

public final class BlockEntityLampFertility extends BlockEntity implements IEssentiaTransport {
    private static final int MAX_CHARGES = 10;
    private static final int BREED_COST = 5;
    private static final int BREED_INTERVAL = 300;
    private static final int RANGE = 7;
    private static final int CROWD_LIMIT = 9;
    private static final int SUCTION = 128;
    private static final int DRAW_INTERVAL = 5;

    private int charges;
    private int count;
    private int drawDelay;

    public BlockEntityLampFertility(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LAMP_FERTILITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityLampFertility lamp) {
        boolean powered = level.hasNeighborSignal(pos);
        if (lamp.charges < MAX_CHARGES) {
            if (lamp.drawEssentia()) {
                lamp.charges++;
                lamp.setChanged();
            }
            state = level.getBlockState(pos);
            if (lamp.charges <= 1) {
                if (state.getValue(BlockStateProperties.ENABLED)) {
                    level.setBlock(pos, state.setValue(BlockStateProperties.ENABLED, false), Block.UPDATE_ALL);
                }
            } else if (!powered && !state.getValue(BlockStateProperties.ENABLED)) {
                level.setBlock(pos, state.setValue(BlockStateProperties.ENABLED, true), Block.UPDATE_ALL);
            }
        }
        if (!powered && lamp.charges > 1 && lamp.count++ % BREED_INTERVAL == 0) {
            lamp.updateAnimals();
        }
    }

    private void updateAnimals() {
        if (level == null) {
            return;
        }
        BlockPos pos = getBlockPos();
        List<Animal> animals = level.getEntitiesOfClass(Animal.class, new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).inflate(RANGE));
        for (Animal candidate : animals) {
            if (candidate.getAge() != 0 || candidate.isInLove()) {
                continue;
            }
            List<Animal> sameKind = new ArrayList<>();
            for (Animal other : animals) {
                if (other.getClass().equals(candidate.getClass())) {
                    sameKind.add(other);
                }
            }
            if (sameKind.size() > CROWD_LIMIT) {
                continue;
            }
            Animal partner = null;
            for (Animal other : sameKind) {
                if (other.getAge() == 0 && !other.isInLove()) {
                    if (partner != null) {
                        charges -= BREED_COST;
                        other.setInLove(null);
                        partner.setInLove(null);
                        setChanged();
                        return;
                    }
                    partner = other;
                }
            }
        }
    }

    private boolean drawEssentia() {
        if (++drawDelay % DRAW_INTERVAL != 0 || level == null) {
            return false;
        }
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        IEssentiaTransport ic = EssentiaFlowHandler.transport(level, getBlockPos().relative(facing), facing.getOpposite());
        if (ic == null || !ic.canOutputTo(facing.getOpposite())) {
            return false;
        }
        if (ic.getSuctionAmount(facing.getOpposite()) >= getSuctionAmount(facing)) {
            return false;
        }
        return ic.takeEssentia(desiderium(), 1, facing.getOpposite()) == 1;
    }

    private Holder<IAspect> desiderium() {
        return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TCAspects.DESIDERIUM);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == getBlockState().getValue(BlockStateProperties.FACING);
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
    public Holder<IAspect> getSuctionType(Direction face) {
        return desiderium();
    }

    @Override
    public int getSuctionAmount(Direction face) {
        if (face != getBlockState().getValue(BlockStateProperties.FACING)) {
            return 0;
        }
        return charges >= MAX_CHARGES ? 0 : SUCTION;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public Holder<IAspect> getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        charges = input.getIntOr("charges", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("charges", charges);
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
