package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public class BlockEntitySmelter extends AbstractSyncedBlockEntity implements MenuProvider {
    public int fuelCapacity;
    public int fuelRemaining;
    public int cookElapsed;
    public int cookTarget;
    public AspectList essentiaStock = AspectList.EMPTY;
    public int vis;

    static final int INPUT_SLOT = 0;
    static final int FUEL_SLOT = 1;
    static final int SLOT_COUNT = 2;

    private static final int MAX_ESSENTIA = 256;
    private static final int DEFAULT_BURN_TIME = 200;
    private static final int BASE_COOK_PER_ESSENTIA = 2;
    private static final double BELLOWS_COOK_REDUCTION = 0.125;
    private static final double SPEED_BOOST_FACTOR = 0.8;
    private static final float VITIUM_RETENTION_FACTOR = 0.66F;
    private static final int VENT_ODDS = 3;
    private static final double VENT_FACE_OFFSET = 0.5;
    private static final double VENT_PIPE_SHIFT = 0.125;
    private static final double VENT_MOUTH_HEIGHT = 1.0;
    private static final float VENT_SOUND_VOLUME = 0.15F;
    private static final float VENT_SOUND_PITCH_LOW = 1.8F;
    private static final float VENT_SOUND_PITCH_RANGE = 0.2F;
    private static final int VENT_PUFFS = 3;
    private static final double VENT_PUFF_SPREAD = 0.06;
    private static final double VENT_PUFF_LIFT = 1.0;
    private static final double VENT_PUFF_OUTWARD = 0.35;
    private static final double VENT_PUFF_SWAY = 0.25;
    private static final float VENT_PUFF_SCALE = 0.5F;
    private static final int VENT_COLOR = 0xC8C8CC;
    private static final String ASPECTS_KEY = "Aspects";
    private static final String BURN_TIME_KEY = "BurnTime";
    private static final String COOK_TIME_KEY = "CookTime";
    private static final String SMELT_TIME_KEY = "SmeltTime";
    private static final String CURRENT_BURN_TIME_KEY = "CurrentItemBurnTime";
    private static final String SPEED_BOOST_KEY = "SpeedBoost";
    private static final int UNKNOWN_BELLOWS = -1;

    private final SmelterItems items = new SmelterItems();
    private boolean speedBoost;
    private int tickCounter;
    private int bellows = UNKNOWN_BELLOWS;

    public BlockEntitySmelter(BlockPos pos, BlockState state) {
        this(TTBlockEntities.SMELTER.get(), pos, state);
    }

    protected BlockEntitySmelter(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStacksResourceHandler itemSlots() {
        return items;
    }

    static boolean isInputValid(ItemStack stack) {
        return !stack.isEmpty() && !AspectIndexAccess.of(stack).isEmpty();
    }

    static boolean isFuelValid(Level level, ItemStack stack) {
        return !stack.isEmpty() && burnTimeOf(level, stack) > 0;
    }

    private static int burnTimeOf(Level level, ItemStack stack) {
        return stack.getBurnTime(null, level.fuelValues());
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntitySmelter smelter) {
        if (level instanceof ServerLevel server) {
            smelter.serverTick(server, pos, state);
        }
    }

    private void serverTick(ServerLevel server, BlockPos pos, BlockState state) {
        tickCounter++;
        if (bellows < 0) {
            refreshBellows();
        }
        boolean wasLit = state.getValue(BlockSmelter.LIT);
        boolean dirty = false;
        if (fuelRemaining > 0) {
            fuelRemaining--;
            dirty = true;
        }
        int smeltPoints = smeltablePoints();
        boolean canSmelt = smeltPoints > 0;
        if (fuelRemaining == 0 && canSmelt && igniteFuel(server)) {
            dirty = true;
        }
        if (fuelRemaining > 0 && canSmelt) {
            cookTarget = cookTimeFor(smeltPoints);
            cookElapsed++;
            dirty = true;
            if (cookElapsed >= cookTarget) {
                cookElapsed = 0;
                smeltOne(server, pos, state);
                dirty = true;
            }
        } else {
            dirty |= cookElapsed != 0;
            cookElapsed = 0;
        }
        dirty |= handOverEssentia(server, pos);
        boolean lit = fuelRemaining > 0;
        if (lit != wasLit) {
            server.setBlock(pos, state.setValue(BlockSmelter.LIT, lit), Block.UPDATE_ALL);
            dirty = true;
        }
        if (dirty) {
            setChanged();
        }
        syncToClient();
    }

    private int smeltablePoints() {
        ItemStack input = stackIn(INPUT_SLOT);
        if (input.isEmpty()) {
            return 0;
        }
        int points = AspectIndexAccess.of(input).totalAmount();
        return points <= MAX_ESSENTIA - vis ? points : 0;
    }

    private int cookTimeFor(int points) {
        int counted = Math.max(bellows, 0);
        double speedUp = 1.0 - counted * BELLOWS_COOK_REDUCTION;
        return (int) (points * BASE_COOK_PER_ESSENTIA * speedUp);
    }

    private void smeltOne(ServerLevel server, BlockPos pos, BlockState state) {
        ItemStack input = stackIn(INPUT_SLOT);
        AspectList content = AspectIndexAccess.of(input);
        if (content.isEmpty()) {
            return;
        }
        Direction[] vents = sidesWith(state.getValue(BlockSmelter.FACING), false);
        RandomSource random = server.getRandom();
        float efficiency = essentiaYield();
        AspectList kept = AspectList.EMPTY;
        FateTally tally = new FateTally();
        for (AspectInstance entry : content.entries()) {
            float survival = entry.aspect().is(TTAspects.VITIUM) ? efficiency * VITIUM_RETENTION_FACTOR : efficiency;
            int survivors = 0;
            for (int point = 0; point < entry.amount(); point++) {
                PointFate fate = rollFate(server, pos, vents, random, survival);
                tally.record(fate);
                if (fate == PointFate.KEPT) {
                    survivors++;
                }
            }
            if (survivors > 0) {
                kept = kept.add(entry.aspect(), survivors);
            }
        }
        essentiaStock = essentiaStock.add(kept);
        vis = essentiaStock.totalAmount();
        pollute(server, pos, tally.polluted);
        input.shrink(1);
        putStack(INPUT_SLOT, input);
        syncToClient();
    }

    private PointFate rollFate(ServerLevel server, BlockPos pos, Direction[] vents, RandomSource random, float survival) {
        if (random.nextFloat() < survival) {
            return PointFate.KEPT;
        }
        return ventAbsorbs(server, pos, vents, random) ? PointFate.VENTED : PointFate.POLLUTED;
    }

    private static boolean ventAbsorbs(ServerLevel server, BlockPos pos, Direction[] vents, RandomSource random) {
        for (Direction side : vents) {
            if (random.nextInt(VENT_ODDS) == 0) {
                emitVent(server, pos.relative(side), side, random);
                return true;
            }
        }
        return false;
    }

    private static void emitVent(ServerLevel server, BlockPos ventPos, Direction outward, RandomSource random) {
        Vec3 mouth = Vec3.atCenterOf(ventPos).add(outward.getStepX() * -VENT_PIPE_SHIFT, VENT_MOUTH_HEIGHT - VENT_FACE_OFFSET, outward.getStepZ() * -VENT_PIPE_SHIFT);
        playVentSound(server, mouth, VENT_SOUND_PITCH_LOW + random.nextFloat() * VENT_SOUND_PITCH_RANGE);
        emitPuffs(server, mouth, outward, random);
    }

    private static void emitPuffs(ServerLevel server, Vec3 mouth, Direction outward, RandomSource random) {
        for (int puff = 0; puff < VENT_PUFFS; puff++) {
            emitPuff(server, mouth.add(jitterVector(random, VENT_PUFF_SPREAD)), outward, random);
        }
    }

    private static void emitPuff(ServerLevel server, Vec3 at, Direction outward, RandomSource random) {
        double mx = outward.getStepX() * VENT_PUFF_OUTWARD + jitter(random, VENT_PUFF_SWAY);
        double mz = outward.getStepZ() * VENT_PUFF_OUTWARD + jitter(random, VENT_PUFF_SWAY);
        Effects.vent(server, at).motion(mx, VENT_PUFF_LIFT, mz).color(VENT_COLOR).scale(VENT_PUFF_SCALE).send();
    }

    private int handOverInterval() {
        int interval = stats().smeltInterval();
        return speedBoost ? Math.max(1, (int) (interval * SPEED_BOOST_FACTOR)) : interval;
    }

    private ItemStack stackIn(int slot) {
        return items.getResource(slot).toStack(items.getAmountAsInt(slot));
    }

    private void putStack(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            items.set(slot, ItemResource.EMPTY, 0);
        } else {
            items.set(slot, ItemResource.of(stack), stack.getCount());
        }
    }

    private boolean igniteFuel(Level level) {
        ItemStack fuel = stackIn(FUEL_SLOT);
        int burn = fuel.isEmpty() ? 0 : burnTimeOf(level, fuel);
        if (burn <= 0) {
            return false;
        }
        ItemStackTemplate leftover = fuel.getCraftingRemainder();
        boolean boosted = fuel.is(TTItems.ALUMENTUM.get());
        fuel.shrink(1);
        ItemStack remaining = fuel.isEmpty() && leftover != null ? leftover.create() : fuel;
        putStack(FUEL_SLOT, remaining);
        speedBoost = boosted;
        fuelCapacity = burn;
        fuelRemaining = burn;
        return true;
    }

    private static void pollute(ServerLevel server, BlockPos pos, int points) {
        if (points > 0) {
            AuraHelper.polluteAura(server, pos, points, true);
        }
    }

    private Direction[] sidesWith(Direction facing, boolean aux) {
        Direction[] found = new Direction[Direction.Plane.HORIZONTAL.length()];
        int count = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side != facing && isPartFacingBack(side, aux)) {
                found[count++] = side;
            }
        }
        Direction[] result = new Direction[count];
        System.arraycopy(found, 0, result, 0, count);
        return result;
    }

    private boolean isPartFacingBack(Direction side, boolean aux) {
        BlockState neighbour = level.getBlockState(worldPosition.relative(side));
        if (aux) {
            return neighbour.getBlock() instanceof BlockSmelterAux && neighbour.getValue(BlockSmelterAux.FACING) == side.getOpposite();
        }
        return neighbour.getBlock() instanceof BlockSmelterVent && neighbour.getValue(BlockSmelterVent.FACING) == side.getOpposite();
    }

    private static void playVentSound(ServerLevel server, Vec3 at, float pitch) {
        server.playSound(null, at.x, at.y, at.z, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, VENT_SOUND_VOLUME, pitch);
    }

    private static Vec3 jitterVector(RandomSource random, double range) {
        double x = jitter(random, range);
        double y = jitter(random, range);
        double z = jitter(random, range);
        return new Vec3(x, y, z);
    }

    private static double jitter(RandomSource random, double range) {
        return (random.nextDouble() * 2.0 - 1.0) * range;
    }

    private boolean handOverEssentia(ServerLevel server, BlockPos pos) {
        if (vis <= 0 || tickCounter % handOverInterval() != 0) {
            return false;
        }
        Direction[] auxSides = sidesWith(getBlockState().getValue(BlockSmelter.FACING), true);
        boolean moved = feedColumn(server, pos);
        for (int index = 0; index < auxSides.length; index++) {
            moved = feedColumn(server, pos.relative(auxSides[index])) || moved;
        }
        return moved;
    }

    private boolean feedColumn(ServerLevel server, BlockPos start) {
        for (AspectInstance entry : essentiaStock.sortedByAmount()) {
            if (BlockEntityAlembic.feedColumn(server, start, entry.aspect())) {
                essentiaStock = essentiaStock.remove(entry.aspect(), 1);
                vis = essentiaStock.totalAmount();
                setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(ASPECTS_KEY, AspectList.CODEC, essentiaStock);
        output.putInt(BURN_TIME_KEY, fuelRemaining);
        output.putInt(COOK_TIME_KEY, cookElapsed);
        output.putInt(SMELT_TIME_KEY, cookTarget);
        output.putInt(CURRENT_BURN_TIME_KEY, fuelCapacity);
        output.putBoolean(SPEED_BOOST_KEY, speedBoost);
        items.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.deserialize(input);
        loadFurnaceState(input);
        essentiaStock = input.read(ASPECTS_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        vis = essentiaStock.totalAmount();
    }

    private void loadFurnaceState(ValueInput input) {
        cookElapsed = input.getIntOr(COOK_TIME_KEY, 0);
        speedBoost = input.getBooleanOr(SPEED_BOOST_KEY, false);
        loadFuelState(input);
        cookTarget = input.getIntOr(SMELT_TIME_KEY, 0);
    }

    private void loadFuelState(ValueInput input) {
        fuelCapacity = input.getIntOr(CURRENT_BURN_TIME_KEY, 0);
        fuelRemaining = input.getIntOr(BURN_TIME_KEY, 0);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel server) {
            spillItems(server, pos);
            releaseEssentia(server, pos);
        }
    }

    private void spillItems(ServerLevel server, BlockPos pos) {
        for (int slot = 0; slot < SLOT_COUNT; slot++) {
            Containers.dropItemStack(server, pos.getX(), pos.getY(), pos.getZ(), stackIn(slot));
            putStack(slot, ItemStack.EMPTY);
        }
    }

    private void releaseEssentia(ServerLevel server, BlockPos pos) {
        if (vis <= 0) {
            return;
        }
        AuraHelper.polluteAura(server, pos, vis, true);
        essentiaStock = AspectList.EMPTY;
        vis = 0;
        setChangedAndSync();
    }

    public boolean takeAspect(Holder<IAspect> aspect, int amount) {
        if (essentiaStock.amountOf(aspect) < amount) {
            return false;
        }
        essentiaStock = essentiaStock.remove(aspect, amount);
        vis = essentiaStock.totalAmount();
        setChangedAndSync();
        return true;
    }

    public void refreshBellows() {
        if (level == null) {
            return;
        }
        Direction facing = getBlockState().getValue(BlockSmelter.FACING);
        Direction[] sides = new Direction[Direction.Plane.HORIZONTAL.length() - 1];
        int count = 0;
        for (Direction side : Direction.Plane.HORIZONTAL) {
            if (side != facing) {
                sides[count++] = side;
            }
        }
        bellows = BellowsHelper.countBellows(level, worldPosition, sides);
    }

    private SmelterStats stats() {
        SmelterStats stats = getBlockState().getBlock().builtInRegistryHolder().getData(SmelterDataMaps.SMELTER_STATS);
        return stats == null ? SmelterStats.DEFAULT : stats;
    }

    public int ventInterval() {
        return stats().smeltInterval();
    }

    public float essentiaYield() {
        return stats().efficiency();
    }

    public int scaled(SmelterGauge gauge, int scale) {
        return switch (gauge) {
            case COOK -> cookElapsed * scale / Math.max(cookTarget, 1);
            case ESSENTIA -> vis * scale / MAX_ESSENTIA;
            case FUEL -> fuelRemaining * scale / (fuelCapacity == 0 ? DEFAULT_BURN_TIME : fuelCapacity);
        };
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuSmelter(containerId, playerInventory, this);
    }

    private enum PointFate {
        KEPT, VENTED, POLLUTED
    }

    private static final class FateTally {
        private int kept;
        private int polluted;

        void record(PointFate fate) {
            switch (fate) {
                case KEPT -> kept++;
                case POLLUTED -> polluted++;
                case VENTED -> {
                }
            }
        }
    }

    private final class SmelterItems extends ItemStacksResourceHandler {
        SmelterItems() {
            super(SLOT_COUNT);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            ItemStack stack = resource.toStack(1);
            return index == INPUT_SLOT ? isInputValid(stack) : level != null && isFuelValid(level, stack);
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    }
}
