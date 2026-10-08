package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntake;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntakeHost;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

public final class BlockEntityLampFertility extends BlockEntity implements EssentiaIntakeHost {
    private static final String CHARGE_KEY = "charges";
    private static final int CAPACITY = 10;
    private static final int RUNNING_CHARGE = 2;
    private static final int PAIRING_COST = 5;
    private static final int PAIRING_INTERVAL = 300;
    private static final int REACH = 7;
    private static final int CROWDED_HERD = 10;
    private static final int PAIR = 2;
    private static final int SUCTION = 128;
    private static final int INTAKE_INTERVAL = 5;

    private final EssentiaIntake intake = new EssentiaIntake(this, TTAspects.DESIDERIUM, SUCTION, INTAKE_INTERVAL);
    private int charge;
    private int pairingCooldown;

    public BlockEntityLampFertility(BlockPos pos, BlockState state) {
        super(TTBlockEntities.LAMP_FERTILITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityLampFertility lamp) {
        if (lamp.wantsEssentia() && lamp.intake.pullOne()) {
            lamp.charge++;
            lamp.setChanged();
        }
        boolean running = lamp.charge >= RUNNING_CHARGE && !level.hasNeighborSignal(pos);
        BlockLamp.showLit(level, pos, state, running);
        if (running && --lamp.pairingCooldown <= 0) {
            lamp.pairingCooldown = PAIRING_INTERVAL;
            lamp.matchmake(level, pos);
        }
    }

    public EssentiaIntake intake() {
        return intake;
    }

    @Override
    public Direction intakeFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public boolean wantsEssentia() {
        return charge < CAPACITY;
    }

    private void matchmake(Level level, BlockPos pos) {
        Map<EntityType<?>, List<Animal>> herds = new LinkedHashMap<>();
        for (Animal animal : level.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(REACH))) {
            herds.computeIfAbsent(animal.getType(), type -> new ArrayList<>()).add(animal);
        }
        for (List<Animal> herd : herds.values()) {
            if (herd.size() >= CROWDED_HERD) {
                continue;
            }
            List<Animal> couple = herd.stream()
                    .filter(BlockEntityLampFertility::readyToMate)
                    .limit(PAIR)
                    .toList();
            if (couple.size() == PAIR) {
                couple.forEach(animal -> animal.setInLove(null));
                charge -= PAIRING_COST;
                setChanged();
                return;
            }
        }
    }

    private static boolean readyToMate(Animal animal) {
        return animal.getAge() == 0 && !animal.isInLove();
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        charge = (input.contains(CHARGE_KEY) ? input.getInt(CHARGE_KEY) : 0);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt(CHARGE_KEY, charge);
    }
}
