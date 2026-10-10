package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.crucible.CrucibleEvent;
import com.leclowndu93150.thaumaturge.content.aspect.ReadOnlyAspectContainer;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.mixin.world.entity.item.ItemEntityAccessor;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jspecify.annotations.Nullable;

public class BlockEntityCrucible extends AbstractSyncedBlockEntity implements ReadOnlyAspectContainer {
    public static final int TANK_CAPACITY = 1000;
    public static final int MAX_ASPECT = 100;

    static final int BOIL_HEAT = 150;

    private static final String KEY_ASPECTS = "Aspects";
    private static final String KEY_TANK = "Tank";
    private static final String KEY_HEAT = "Heat";

    private static final int TANK_SLOTS = 1;
    private static final int TANK_SLOT = 0;
    private static final int CRAFT_WATER_COST = 50;
    private static final int MAX_HEAT = 200;

    private static final int LEAK_THRESHOLD = 100;
    private static final int OVERFLOW_INTERVAL = 5;
    private static final int LEAK_SPILL_CHANCE_NUMERATOR = 3;
    private static final int LEAK_SPILL_CHANCE_DENOMINATOR = 4;
    private static final int OVERFLOW_SPILL_ODDS = 4;
    private static final int POINT_SPILL_ODDS = 4;
    private static final float LEAK_FLUX = 0.25F;
    private static final float OVERFLOW_FLUX = 1.0F;

    private static final int EVENT_BOIL_BURST = 10;
    private static final int EVENT_CRAFT_COMPLETE = 11;
    private static final int DISSOLVE_BURST_INTENSITY = 1;
    private static final int SPILL_BURST_INTENSITY = 5;

    private static final double BLOCK_CENTER = 0.5;

    private static final int LEAK_RESTART = 0;
    private static final int LEAK_DELAY_AFTER_DISSOLVE = 250;
    private static final int LEAK_DELAY_AFTER_CRAFT = 350;
    private static final int DISSOLVE_LEAK_RESET = LEAK_THRESHOLD - LEAK_DELAY_AFTER_DISSOLVE;
    private static final int CRAFT_LEAK_RESET = LEAK_THRESHOLD - LEAK_DELAY_AFTER_CRAFT;

    private static final float SURFACE_FLOOR = 0.25F;
    private static final float SURFACE_WATER_RISE = 0.5F;
    private static final float SURFACE_ESSENTIA_RISE = 0.2F;
    private static final float SURFACE_OVERFULL = 1.01F;

    private static final double RESULT_SPAWN_HEIGHT = 0.8;
    private static final double RESULT_HOP = 0.15;
    private static final double RESULT_DRIFT = 0.03;

    private static final int THROWN_SHARE_DIVISOR = 2;

    private static final float DISSOLVE_SOUND_VOLUME = 0.25F;
    private static final float DISSOLVE_SOUND_PITCH = 0.9F;
    private static final float DISSOLVE_SOUND_PITCH_JITTER = 0.2F;
    private static final float SPILL_SOUND_VOLUME = 0.3F;
    private static final float SPILL_SOUND_PITCH = 1.1F;
    private static final float SPILL_SOUND_PITCH_JITTER = 0.15F;

    private final FluidStacksResourceHandler tank = new CrucibleTank();
    private final BlockPos.MutableBlockPos belowPos = new BlockPos.MutableBlockPos();
    private AspectList aspects = AspectList.EMPTY;
    private int heat;
    private int leakCounter = LEAK_RESTART;

    public BlockEntityCrucible(BlockPos pos, BlockState state) {
        super(TTBlockEntities.CRUCIBLE.get(), pos, state);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        emptyOut();
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        super.saveAdditional(view);
        view.putShort(KEY_HEAT, getHeat());
        tank.serialize(view.child(KEY_TANK));
        view.store(KEY_ASPECTS, AspectList.CODEC, aspects);
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        super.loadAdditional(view);
        restoreContents(view);
    }

    private void restoreContents(ValueInput view) {
        heat = readHeat(view);
        view.child(KEY_TANK).ifPresent(tank::deserialize);
        aspects = view.read(KEY_ASPECTS, AspectList.CODEC).orElse(AspectList.EMPTY);
    }

    private static int readHeat(ValueInput view) {
        return Mth.clamp(view.getShortOr(KEY_HEAT, (short) 0), 0, MAX_HEAT);
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    public short getHeat() {
        return (short) heat;
    }

    boolean isBoiling() {
        return heat > BOIL_HEAT && hasWater();
    }

    private void tickServer(ServerLevel serverLevel) {
        updateHeat(serverLevel);
        emitParticles(serverLevel);
        runLeaks(serverLevel);
    }

    static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntityCrucible crucible) {
        if (level instanceof ServerLevel serverLevel) {
            crucible.tickServer(serverLevel);
        }
    }

    private void updateHeat(ServerLevel serverLevel) {
        boolean wasBoiling = isBoiling();
        int previous = heat;
        int step = hasWater() && sitsOnHeat(serverLevel) ? 1 : -1;
        heat = Mth.clamp(heat + step, 0, MAX_HEAT);
        if (wasBoiling != isBoiling()) {
            setChangedAndSync();
        } else if (heat != previous && (heat == 0 || heat == MAX_HEAT)) {
            setChanged();
        }
    }

    private boolean sitsOnHeat(ServerLevel serverLevel) {
        belowPos.setWithOffset(worldPosition, Direction.DOWN);
        return serverLevel.getBlockState(belowPos).is(TTBlockTags.CRUCIBLE_HEAT_SOURCES);
    }

    private void runLeaks(ServerLevel serverLevel) {
        if (isOverfilled() && serverLevel.getGameTime() % OVERFLOW_INTERVAL == 0) {
            shedOverflow(serverLevel);
        }
        leakCounter++;
        if (leakCounter < LEAK_THRESHOLD) {
            return;
        }
        leakCounter = LEAK_RESTART;
        if (!aspects.isEmpty()) {
            leakDrop();
        }
    }

    public float surfaceLevel() {
        if (isOverfilled()) {
            return SURFACE_OVERFULL;
        }
        float water = (float) tank.getAmountAsInt(TANK_SLOT) / TANK_CAPACITY;
        float essentia = (float) aspects.totalAmount() / MAX_ASPECT;
        return SURFACE_FLOOR + water * SURFACE_WATER_RISE + essentia * SURFACE_ESSENTIA_RISE;
    }

    private boolean hasWater() {
        return tank.getAmountAsInt(TANK_SLOT) > 0;
    }

    private boolean isOverfilled() {
        return aspects.totalAmount() > MAX_ASPECT;
    }

    private void emitParticles(ServerLevel serverLevel) {
        if (!hasWater()) {
            return;
        }
        float height = surfaceLevel();
        if (heat > BOIL_HEAT) {
            CrucibleFx.froth(serverLevel, worldPosition, height, isOverfilled());
        }
        CrucibleFx.bubble(serverLevel, worldPosition, height, aspects);
    }

    private void takeOnePoint(RandomSource random) {
        AspectInstance picked = aspects.entries().get(random.nextInt(aspects.size()));
        aspects = aspects.remove(picked.aspect(), 1);
    }

    private void shedOverflow(ServerLevel serverLevel) {
        if (aspects.isEmpty()) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        takeOnePoint(random);
        if (random.nextInt(OVERFLOW_SPILL_ODDS) != 0 || !PhysicalFlux.spill(serverLevel, worldPosition, random)) {
            AuraHelper.polluteAura(serverLevel, worldPosition, OVERFLOW_FLUX, true);
        }
        setChangedAndSync();
    }

    public void leakDrop() {
        if (level instanceof ServerLevel serverLevel && !aspects.isEmpty()) {
            RandomSource random = serverLevel.getRandom();
            takeOnePoint(random);
            boolean attempt = random.nextInt(LEAK_SPILL_CHANCE_DENOMINATOR) < LEAK_SPILL_CHANCE_NUMERATOR;
            if (!attempt || !PhysicalFlux.spill(serverLevel, worldPosition, random)) {
                AuraHelper.polluteAura(serverLevel, worldPosition, LEAK_FLUX, true);
            }
        }
        if (level instanceof ServerLevel) {
            setChangedAndSync();
        }
    }

    public void emptyOut() {
        if (level instanceof ServerLevel serverLevel) {
            spillEverything(serverLevel);
        }
    }

    private void spillEverything(ServerLevel serverLevel) {
        int points = aspects.totalAmount();
        if (points <= 0 && !hasWater()) {
            return;
        }
        tank.set(TANK_SLOT, FluidResource.EMPTY, 0);
        aspects = AspectList.EMPTY;
        int lost = points - spillPoints(serverLevel, points);
        if (lost > 0) {
            AuraHelper.polluteAura(serverLevel, worldPosition, lost, true);
        }
        serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_BOIL_BURST, SPILL_BURST_INTENSITY);
        setChangedAndSync();
    }

    private int spillPoints(ServerLevel serverLevel, int points) {
        RandomSource random = serverLevel.getRandom();
        int placed = 0;
        for (int point = 0; point < points; point++) {
            boolean attempt = random.nextInt(POINT_SPILL_ODDS) == 0;
            if (attempt && PhysicalFlux.spill(serverLevel, worldPosition, random)) {
                placed++;
            }
        }
        return placed;
    }

    @Override
    public boolean triggerEvent(int id, int data) {
        boolean known = id == EVENT_BOIL_BURST || id == EVENT_CRAFT_COMPLETE;
        if (level == null || !known) {
            return false;
        }
        if (level instanceof ServerLevel serverLevel) {
            runServerFx(serverLevel, id, data);
        } else {
            playSpillSound();
        }
        return true;
    }

    private void runServerFx(ServerLevel serverLevel, int id, int data) {
        if (id == EVENT_BOIL_BURST) {
            CrucibleFx.boilBurst(serverLevel, worldPosition, surfaceLevel(), aspects, data);
        } else {
            CrucibleFx.craftComplete(serverLevel, worldPosition);
        }
    }

    public void absorbThrown(ItemEntity itemEntity) {
        Entity thrower = EntityReference.getEntity(((ItemEntityAccessor) itemEntity).thaumaturge$getThrower(), itemEntity.level());
        if (thrower instanceof Player player && player.isAlive()) {
            settleThrown(itemEntity, player);
        }
    }

    private void settleThrown(ItemEntity itemEntity, Player player) {
        ItemStack held = itemEntity.getItem();
        ItemStack remainder = dropIn(held, player);
        if (remainder == null || remainder.isEmpty()) {
            itemEntity.discard();
            return;
        }
        if (remainder != held) {
            itemEntity.setItem(remainder);
        }
    }

    @Nullable
    ItemStack dropIn(ItemStack stack, Player player) {
        if (!(level instanceof ServerLevel serverLevel) || stack.isEmpty()) {
            return stack;
        }
        int share = Mth.positiveCeilDiv(stack.getCount(), THROWN_SHARE_DIVISOR);
        int taken = 0;
        boolean dissolvedAny = false;
        while (taken < share && isBoiling()) {
            Intake intake = takeIn(serverLevel, stack.copyWithCount(1), player);
            if (intake == Intake.REJECTED) {
                break;
            }
            dissolvedAny |= intake == Intake.DISSOLVED;
            taken++;
        }
        if (taken == 0) {
            return stack;
        }
        afterBatch(serverLevel, dissolvedAny);
        int left = stack.getCount() - taken;
        return left > 0 ? stack.copyWithCount(left) : null;
    }

    private Intake takeIn(ServerLevel serverLevel, ItemStack single, Player player) {
        CrucibleRecipe recipe = ThaumaturgeCraftingManager.findMatchingCrucibleRecipe(serverLevel, player, aspects, single);
        if (recipe != null && craft(serverLevel, recipe, single, player)) {
            return Intake.CRAFTED;
        }
        AspectList gained = collectDissolved(single, player);
        if (gained.isEmpty()) {
            return Intake.REJECTED;
        }
        aspects = aspects.add(gained);
        leakCounter = DISSOLVE_LEAK_RESET;
        return Intake.DISSOLVED;
    }

    private void afterBatch(ServerLevel serverLevel, boolean dissolvedAny) {
        if (dissolvedAny) {
            float pitch = DISSOLVE_SOUND_PITCH + serverLevel.getRandom().nextFloat() * DISSOLVE_SOUND_PITCH_JITTER;
            serverLevel.playSound(null, worldPosition, TTSounds.BUBBLE.get(), SoundSource.BLOCKS, DISSOLVE_SOUND_VOLUME, pitch);
            serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_BOIL_BURST, DISSOLVE_BURST_INTENSITY);
        }
        setChangedAndSync();
    }

    private void popOut(ItemStack result) {
        if (!(level instanceof ServerLevel serverLevel) || result.isEmpty()) {
            return;
        }
        RandomSource random = serverLevel.getRandom();
        double x = worldPosition.getX() + BLOCK_CENTER;
        double y = worldPosition.getY() + RESULT_SPAWN_HEIGHT;
        double z = worldPosition.getZ() + BLOCK_CENTER;
        ItemStack remaining = result.copy();
        boolean first = true;
        while (!remaining.isEmpty()) {
            ItemStack part = remaining.split(remaining.getMaxStackSize());
            EntitySpecialItem entity = new EntitySpecialItem(serverLevel, x, y, z, part);
            double driftX = first ? 0.0 : random.triangle(0.0, RESULT_DRIFT);
            double driftZ = first ? 0.0 : random.triangle(0.0, RESULT_DRIFT);
            entity.setDeltaMovement(driftX, RESULT_HOP, driftZ);
            serverLevel.addFreshEntity(entity);
            first = false;
        }
    }

    private void playSpillSound() {
        if (level == null) {
            return;
        }
        float pitch = SPILL_SOUND_PITCH + level.getRandom().nextFloat() * SPILL_SOUND_PITCH_JITTER;
        level.playLocalSound(worldPosition, TTSounds.SPILL.get(), SoundSource.BLOCKS, SPILL_SOUND_VOLUME, pitch, false);
    }

    private boolean craft(ServerLevel serverLevel, CrucibleRecipe recipe, ItemStack single, Player player) {
        ItemStack result = recipe.assemble(new CrucibleRecipeInput(single, aspects));
        popOut(result);
        aspects = recipe.removeMatching(aspects);
        drawWater();
        NeoForge.EVENT_BUS.post(new CrucibleEvent.Crafted(player, worldPosition, getBlockState(), this, result.copy(), recipe.aspects()));
        leakCounter = CRAFT_LEAK_RESET;
        serverLevel.blockEvent(worldPosition, getBlockState().getBlock(), EVENT_CRAFT_COMPLETE, 0);
        return true;
    }

    private void drawWater() {
        try (Transaction tx = Transaction.openRoot()) {
            FluidResource water = FluidResource.of(Fluids.WATER);
            tank.extract(TANK_SLOT, water, CRAFT_WATER_COST, tx);
            tx.commit();
        }
    }

    private AspectList collectDissolved(ItemStack single, Player player) {
        AspectList natural = AspectIndexAccess.of(single);
        CrucibleEvent.Dissolve event = NeoForge.EVENT_BUS.post(new CrucibleEvent.Dissolve(player, worldPosition, getBlockState(), this, single, natural));
        return event.isCanceled() ? AspectList.EMPTY : event.getAspects();
    }

    private enum Intake {
        CRAFTED, DISSOLVED, REJECTED
    }

    private final class CrucibleTank extends FluidStacksResourceHandler {
        private CrucibleTank() {
            super(TANK_SLOTS, TANK_CAPACITY);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            setChangedAndSync();
        }
    }
}
