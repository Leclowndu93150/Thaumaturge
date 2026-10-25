package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.crucible.CrucibleEvent;
import com.leclowndu93150.thaumaturge.content.aspect.ReadOnlyAspectContainer;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class BlockEntityCrucible extends AbstractSyncedBlockEntity implements ReadOnlyAspectContainer {

    public static final int TANK_CAPACITY = 1000;
    public static final int MAX_ASPECT = 100;
    private static final int BOIL_HEAT = 150;

    private static final int LEAK_THRESHOLD = 100;
    private static final int LEAK_DELAY_AFTER_DISSOLVE = 250;
    private static final int LEAK_DELAY_AFTER_CRAFT = 350;
    private static final int DISSOLVE_LEAK_RESET = LEAK_THRESHOLD - LEAK_DELAY_AFTER_DISSOLVE;
    private static final int CRAFT_LEAK_RESET = LEAK_THRESHOLD - LEAK_DELAY_AFTER_CRAFT;
    private static final int OVERFLOW_INTERVAL = 5;

    private static final float SURFACE_FLOOR = 0.3F;
    private static final float SURFACE_WATER_RISE = 0.5F;
    private static final float SURFACE_OVERFULL = 1.001F;
    private static final float SURFACE_EXACT_FULL = 0.9999F;
    private static final float SURFACE_RIM = 1.0F;

    private final FluidTank tank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
            syncToClient();
        }
    };
    private AspectList aspects = AspectList.EMPTY;
    private short heat = 0;
    private int leakCounter = -LEAK_THRESHOLD;

    // FX Infos
    int prevcolor = 0;
    int prevx = 0;
    int prevy = 0;
    int bellows = -1;
    private int delay = 0;

    public BlockEntityCrucible(BlockPos worldPosition, BlockState blockState) {
        super(TTBlockEntities.CRUCIBLE.get(), worldPosition, blockState);
    }

    private void tick() {
        if (level == null) return;
        int prevHeat = heat;
        if (!level.isClientSide()) {
            if (tank.getFluidAmount() > 0) {
                BlockState below = level.getBlockState(getBlockPos().below());
                boolean hasHeatBelow = below.is(TTBlockTags.CRUCIBLE_HEAT_SOURCES);
                if (!hasHeatBelow) {
                    if (heat > 0) {
                        heat--;
                    }
                } else if (heat < 200) {
                    heat++;
                }
            } else if (heat > 0) {
                heat--;
            }

            if (boilingStateChanged(prevHeat, heat, tank.getFluidAmount())) {
                setChanged();
                syncToClient();
            } else if (heat != prevHeat && (heat == 0 || heat == 200)) {
                setChanged();
            }

            ServerLevel serverLevel = (ServerLevel) level;
            if (aspects.totalAmount() > MAX_ASPECT && serverLevel.getGameTime() % OVERFLOW_INTERVAL == 0L) {
                spillOverflow();
            }

            leakCounter++;
            if (leakCounter >= LEAK_THRESHOLD) {
                leakCounter = 0;
                if (!aspects.isEmpty()) spillRandom();
            }

            if (tank.getFluidAmount() > 0) {
                this.sendEffects();
            }
        }

        if (level.isClientSide() && prevHeat < 151 && this.heat >= 151) {
            this.heat++;
        }
    }

    private void sendEffects() {
        if (level == null || level.isClientSide()) return;
        ServerLevel level = (ServerLevel) this.level;
        float surface = getFluidHeight();
        if (this.heat > 150) {
            CrucibleFx.froth(level, getBlockPos(), surface, this.aspects.totalAmount() > MAX_ASPECT);
        }
        CrucibleFx.bubble(level, getBlockPos(), surface, aspects);
    }

    @Override
    public AspectList getAspects() {
        return aspects;
    }

    public int addAspects(@Nullable Holder<IAspect> aspect, int amount) {
        if (aspect == null
                || amount <= 0
                || !(level instanceof ServerLevel serverLevel)
                || !serverLevel.getServer().isSameThread()) {
            return 0;
        }

        int currentAmount = aspects.amountOf(aspect);
        if (amount > Integer.MAX_VALUE - currentAmount) {
            return 0;
        }

        aspects = aspects.add(aspect, amount);
        setChanged();
        syncToClient();
        return amount;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TTNbt.store(output, "Aspects", AspectList.CODEC, registries, aspects);
        output.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        output.putShort("Heat", heat);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        if (input.contains("Tank")) {
            tank.readFromNBT(registries, input.getCompound("Tank"));
        }
        aspects = TTNbt.read(input, "Aspects", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        heat = (short) input.getShort("Heat");
    }

    public FluidTank getTank() {
        return tank;
    }

    public float getFluidHeight() {
        return fluidSurfaceHeight(tank.getFluidAmount(), aspects.totalAmount());
    }

    static float fluidSurfaceHeight(int waterAmount, int aspectAmount) {
        if (aspectAmount > MAX_ASPECT) return SURFACE_OVERFULL;
        float water = (float) waterAmount / TANK_CAPACITY;
        float essentia = (float) aspectAmount / MAX_ASPECT;
        float waterSurface = SURFACE_FLOOR + water * SURFACE_WATER_RISE;
        float surface = waterSurface + essentia * (SURFACE_RIM - waterSurface);
        return aspectAmount == MAX_ASPECT || surface >= SURFACE_RIM ? SURFACE_EXACT_FULL : surface;
    }

    boolean isBoiling() {
        return isBoiling(heat, tank.getFluidAmount());
    }

    static boolean isBoiling(int heat, int waterAmount) {
        return heat > BOIL_HEAT && waterAmount > 0;
    }

    static boolean boilingStateChanged(int previousHeat, int currentHeat, int waterAmount) {
        return isBoiling(previousHeat, waterAmount) != isBoiling(currentHeat, waterAmount);
    }

    public void spillRemnants() {
        if (!(level instanceof ServerLevel serverLevel)) return;
        int total = aspects.totalAmount();
        if (tank.getFluidAmount() > 0 || total > 0) {
            tank.setFluid(FluidStack.EMPTY);
            // Each discarded essentia has one consequence: a physical spill or one unit
            // of Aura Flux. Physical Flux therefore does not double-count as Aura Flux.
            int successfulPhysicalSpills = 0;
            for (int i = 0; i < total; i++) {
                if (serverLevel.getRandom().nextInt(4) == 0
                        && PhysicalFlux.spill(serverLevel, getBlockPos(), serverLevel.getRandom())) {
                    successfulPhysicalSpills++;
                }
            }

            float auraFlux = Math.max(0.0F, total - successfulPhysicalSpills);
            if (auraFlux > 0.0F) {
                AuraHelper.polluteAura(serverLevel, getBlockPos(), auraFlux, true);
            }

            this.aspects = AspectList.EMPTY;
            serverLevel.blockEvent(getBlockPos(), getBlockState().getBlock(), 2, 5);
            setChanged();
            syncToClient();
        }
    }

    private void spillOverflow() {
        if (!(level instanceof ServerLevel serverLevel) || aspects.isEmpty()) return;
        Holder<IAspect> randAspect = aspects.entries()
                .get(serverLevel.getRandom().nextInt(aspects.size()))
                .aspect();
        aspects = aspects.reduce(randAspect, 1);

        // Overflow is a one-unit budget: either a physical spill or Aura Flux.
        boolean physical = serverLevel.getRandom().nextInt(4) == 0
                && PhysicalFlux.spill(serverLevel, getBlockPos(), serverLevel.getRandom());
        if (!physical) {
            AuraHelper.polluteAura(serverLevel, getBlockPos(), 1.0F, true);
        }
        setChanged();
        syncToClient();
    }

    public void spillRandom() {
        if (level == null || level.isClientSide()) return;
        if (!aspects.isEmpty()) {
            Holder<IAspect> randAspect = aspects.entries()
                    .get(level.getRandom().nextInt(aspects.size()))
                    .aspect();
            aspects = aspects.reduce(randAspect, 1);
            // Boiling leaks vent physical Flux; cold neglect only pollutes the local aura.
            if (ventsPhysicalFlux(heat)) {
                PhysicalFlux.spill((ServerLevel) level, getBlockPos(), level.getRandom());
            } else {
                AuraHelper.polluteAura(level, getBlockPos(), 0.25F, true);
            }
        }
        setChanged();
        syncToClient();
    }

    static boolean ventsPhysicalFlux(int heat) {
        return heat > BOIL_HEAT;
    }

    public short getHeat() {
        return heat;
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntityCrucible crucible) {
        crucible.tick();
    }

    @Override
    public boolean triggerEvent(int event, int data) {
        if (level == null) return false;
        if (event == 99) {
            level.playLocalSound(
                    getBlockPos().getX() + 0.5f,
                    getBlockPos().getY() + 0.5,
                    getBlockPos().getZ() + 0.5,
                    TTSounds.SPILL.get(),
                    SoundSource.BLOCKS,
                    0.2f,
                    1.0F,
                    false);
            if (!level.isClientSide()) {
                CrucibleFx.craftComplete((ServerLevel) level, getBlockPos());
            }
            return true;
        } else if (event != 2) {
            return super.triggerEvent(event, data);
        } else {
            level.playLocalSound(
                    getBlockPos().getX() + 0.5f,
                    getBlockPos().getY() + 0.5,
                    getBlockPos().getZ() + 0.5,
                    TTSounds.SPILL.get(),
                    SoundSource.BLOCKS,
                    0.2f,
                    1.0F,
                    false);
            if (!level.isClientSide()) {
                CrucibleFx.boilBurst((ServerLevel) level, getBlockPos(), getFluidHeight(), aspects, data);
            }
            return true;
        }
    }

    public void ejectItem(ItemStack items) {
        if (level == null || level.isClientSide()) return;
        boolean first = true;

        do {
            ItemStack spitout = items.copy();
            if (spitout.getCount() > spitout.getMaxStackSize()) {
                spitout.setCount(spitout.getMaxStackSize());
            }

            items.shrink(spitout.getCount());
            EntitySpecialItem entityitem = new EntitySpecialItem(
                    level,
                    getBlockPos().getX() + 0.5F,
                    getBlockPos().getY() + 0.71F,
                    getBlockPos().getZ() + 0.5F,
                    spitout);
            entityitem.setDeltaMovement(
                    first
                            ? 0.0
                            : (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.01F,
                    0.075F,
                    first
                            ? 0.0
                            : (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.01F);
            level.addFreshEntity(entityitem);
            first = false;
        } while (items.getCount() > 0);
    }

    public ItemStack attemptSmelt(ItemStack stack, Player owner) {
        return attemptSmelt(stack, owner, null, false);
    }

    /** Native Crucible crafting for automation, with output capture and optional effect deferral. */
    public ItemStack attemptSmeltForAutomation(
            ItemStack stack, Player owner, Consumer<ItemStack> outputSink, boolean deferCompletionEffect) {
        return attemptSmelt(stack, owner, outputSink, deferCompletionEffect);
    }

    private ItemStack attemptSmelt(
            ItemStack stack, Player owner, @Nullable Consumer<ItemStack> outputSink, boolean deferCompletionEffect) {
        if (level == null || level.isClientSide()) return stack;
        if (owner == null || owner.isDeadOrDying()) return stack;
        if (tank.getFluidAmount() <= 0) return stack;
        boolean bubble = false;
        boolean craftDone = false;
        int count = stack.getCount();

        for (int i = 0; i < count && tank.getFluidAmount() > 0; i++) {
            CrucibleRecipe recipe = ThaumaturgeCraftingManager.findMatchingCrucibleRecipe(
                    (ServerLevel) level, owner, this.aspects, stack);
            if (recipe != null) {
                ItemStack out = recipe.assemble(new CrucibleRecipeInput(stack, this.aspects), level.registryAccess());
                this.aspects = recipe.removeMatching(aspects);
                {
                    tank.drain(50, IFluidHandler.FluidAction.EXECUTE);
                }
                if (outputSink == null) {
                    ejectItem(out.copy());
                } else {
                    outputSink.accept(out.copy());
                }
                NeoForge.EVENT_BUS.post(new CrucibleEvent.Crafted(
                        owner, getBlockPos(), getBlockState(), this, out.copy(), recipe.aspects()));
                craftDone = true;
                count--;
                this.leakCounter = CRAFT_LEAK_RESET;
            } else {
                AspectList aspects = AspectIndexAccess.index().of(stack);
                CrucibleEvent.Dissolve event =
                        new CrucibleEvent.Dissolve(owner, getBlockPos(), getBlockState(), this, stack, aspects);
                NeoForge.EVENT_BUS.post(event);
                aspects = event.getAspects();
                if (!aspects.isEmpty() && !event.isCanceled()) {
                    for (AspectInstance aspect : aspects.entries()) {
                        this.aspects = this.aspects.add(aspect);
                    }
                    bubble = true;
                    count--;
                    this.leakCounter = DISSOLVE_LEAK_RESET;
                }
            }
        }

        if (bubble) {
            level.playSound(
                    null,
                    getBlockPos(),
                    TTSounds.BUBBLE.get(),
                    SoundSource.BLOCKS,
                    0.2F,
                    1.0F + level.getRandom().nextFloat() * 0.4F);
            syncToClient();
            level.blockEvent(getBlockPos(), getBlockState().getBlock(), 2, 1);
        }

        if (craftDone) {
            syncToClient();
            if (!deferCompletionEffect) {
                level.blockEvent(getBlockPos(), getBlockState().getBlock(), 99, 0);
            }
        }

        setChanged();
        if (count <= 0) return null;
        return stack.copyWithCount(count);
    }

    public void attemptSmelt(ItemEntity entity) {
        if (entity.level().isClientSide()) return;
        Entity throwerRef = entity.getOwner();
        if (!(throwerRef instanceof Player player)) return;
        ItemStack stack = entity.getItem();
        int offeredCount = thrownShare(stack.getCount());
        if (offeredCount <= 0) return;
        ItemStack offered = stack.copyWithCount(offeredCount);
        ItemStack remainder = attemptSmelt(offered, player);
        int consumed = offeredCount - (remainder == null ? 0 : remainder.getCount());
        if (consumed <= 0) return;
        int left = stack.getCount() - consumed;
        if (left <= 0) {
            entity.discard();
        } else {
            entity.setItem(stack.copyWithCount(left));
        }
    }

    static int thrownShare(int count) {
        return count <= 0 ? 0 : Mth.positiveCeilDiv(count, 2);
    }
}
