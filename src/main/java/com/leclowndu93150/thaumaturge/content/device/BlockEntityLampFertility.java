package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntake;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntakeHost;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class BlockEntityLampFertility extends BlockEntity implements EssentiaIntakeHost {
    private static final int INTAKE_SUCTION = 128;
    private static final int SUCTION_PER_CHARGE = 10;
    private static final int INTAKE_INTERVAL = 5;
    private static final int MAX_CHARGES = 10;
    private static final int MIN_RUNNING_CHARGES = 2;
    private static final int PAIRING_INTERVAL = 300;
    private static final double PAIRING_RANGE = 7.0;
    private static final int GROUP_LIMIT = 10;
    private static final int PAIRING_COST = 5;
    private static final String CHARGES_KEY = "charges";

    private final EssentiaIntake intake = new EssentiaIntake(this, TTAspects.DESIDERIUM, INTAKE_SUCTION, INTAKE_INTERVAL);
    private int charges;
    private int runningTicks;

    public BlockEntityLampFertility(BlockPos pos, BlockState state) {
        super(TTBlockEntities.LAMP_FERTILITY.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityLampFertility lamp) {
        lamp.absorbEssentia();
        boolean running = !level.hasNeighborSignal(pos) && lamp.charges >= MIN_RUNNING_CHARGES;
        BlockLamp.showLit(level, pos, state, running);
        if (!running) {
            lamp.runningTicks = 0;
            return;
        }
        if (lamp.runningTicks++ % PAIRING_INTERVAL == 0) {
            lamp.pairAnimals(level, pos);
        }
    }

    private void absorbEssentia() {
        if (charges >= MAX_CHARGES || !intake.pullOne()) {
            return;
        }
        charges++;
        setChanged();
    }

    private void pairAnimals(Level level, BlockPos pos) {
        if (charges < PAIRING_COST) {
            return;
        }
        List<Animal> nearby = level.getEntitiesOfClass(Animal.class, new AABB(pos).inflate(PAIRING_RANGE));
        Couple couple = findCouple(nearby);
        if (couple == null) {
            return;
        }
        couple.first().setInLove(null);
        couple.second().setInLove(null);
        charges -= PAIRING_COST;
        setChanged();
    }

    private static @Nullable Couple findCouple(List<Animal> nearby) {
        for (Animal candidate : nearby) {
            if (!isEligible(candidate)) {
                continue;
            }
            EntityType<?> kind = candidate.getType();
            if (nearby.stream().filter(other -> other.getType() == kind).count() >= GROUP_LIMIT) {
                continue;
            }
            Animal partner = nearby.stream().filter(other -> other != candidate && other.getType() == kind && isEligible(other)).findFirst().orElse(null);
            if (partner != null) {
                return new Couple(candidate, partner);
            }
        }
        return null;
    }

    private static boolean isEligible(Animal animal) {
        return animal.isAlive() && animal.getAge() == 0 && animal.canFallInLove();
    }

    private record Couple(Animal first, Animal second) {
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
        return charges < MAX_CHARGES;
    }

    @Override
    public int intakeSuction(int baseSuction) {
        return baseSuction - SUCTION_PER_CHARGE * charges;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        charges = input.getIntOr(CHARGES_KEY, 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt(CHARGES_KEY, charges);
    }
}
