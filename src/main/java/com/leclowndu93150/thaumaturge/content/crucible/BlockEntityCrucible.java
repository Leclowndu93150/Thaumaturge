package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.crucible.CrucibleEvent;
import com.leclowndu93150.thaumaturge.content.aspect.ReadOnlyAspectContainer;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.awt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

public class BlockEntityCrucible extends AbstractSyncedBlockEntity implements ReadOnlyAspectContainer {

    public static final int TANK_CAPACITY = 1000;
    public static final int MAX_ASPECT = 100;

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
    private long counter = -100;

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
        counter++;
        int prevHeat = heat;
        if (!level.isClientSide()) {
            if (tank.getFluidAmount() > 0) {
                BlockState below = level.getBlockState(getBlockPos().below());
                boolean hasHeatBelow = below.is(TTBlockTags.CRUCIBLE_HEAT_SOURCES);
                if (!hasHeatBelow) {
                    if (heat > 0) {
                        heat--;
                        if (heat == 149) {
                            setChanged();
                            syncToClient();
                        }
                    }
                } else if (heat < 200) {
                    heat++;
                    if (prevHeat < 151 && heat >= 151) {
                        setChanged();
                        syncToClient();
                    }
                }
            } else if (heat > 0) {
                heat--;
            }

            if (aspects.totalAmount() > MAX_ASPECT && counter % 5L == 0L) spillOverflow();

            if (counter >= 100L) {
                spillRandom();
                counter = 0L;
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
        if (this.heat > 150) {
            Effects.crucibleFroth(
                            level,
                            new Vec3(
                                    getBlockPos().getX()
                                            + 0.2F
                                            + level.getRandom().nextFloat() * 0.6F,
                                    getBlockPos().getY() + getFluidHeight(),
                                    getBlockPos().getZ()
                                            + 0.2F
                                            + level.getRandom().nextFloat() * 0.6F))
                    .send();
            if (this.aspects.totalAmount() > MAX_ASPECT) {
                for (int a = 0; a < 2; a++) {
                    Effects.crucibleFrothDown(
                                    level,
                                    new Vec3(
                                            getBlockPos().getX(),
                                            getBlockPos().getY() + 1,
                                            getBlockPos().getZ()
                                                    + level.getRandom().nextFloat()))
                            .send();
                    Effects.crucibleFrothDown(
                                    level,
                                    new Vec3(
                                            getBlockPos().getX() + 1,
                                            getBlockPos().getY() + 1,
                                            getBlockPos().getZ()
                                                    + level.getRandom().nextFloat()))
                            .send();
                    Effects.crucibleFrothDown(
                                    level,
                                    new Vec3(
                                            getBlockPos().getX()
                                                    + level.getRandom().nextFloat(),
                                            getBlockPos().getY() + 1,
                                            getBlockPos().getZ()))
                            .send();
                    Effects.crucibleFrothDown(
                                    level,
                                    new Vec3(
                                            getBlockPos().getX()
                                                    + level.getRandom().nextFloat(),
                                            getBlockPos().getY() + 1,
                                            getBlockPos().getZ() + 1))
                            .send();
                }
            }
        }

        if (level.getRandom().nextInt(6) == 0 && !this.aspects.isEmpty()) {
            int color = this.aspects
                            .entries()
                            .get(level.getRandom().nextInt(aspects.size()))
                            .aspect()
                            .value()
                            .color()
                    + -16777216;
            int x = 5 + level.getRandom().nextInt(22);
            int y = 5 + level.getRandom().nextInt(22);
            this.delay = level.getRandom().nextInt(10);
            this.prevcolor = color;
            this.prevx = x;
            this.prevy = y;
            Color c = new Color(color);
            float r = c.getRed() / 255.0F;
            float g = c.getGreen() / 255.0F;
            float b = c.getBlue() / 255.0F;
            Effects.crucibleBubble(
                            level,
                            new Vec3(
                                    getBlockPos().getX() + x / 32.0F + 1 / 64F,
                                    getBlockPos().getY() + 0.05F + getFluidHeight(),
                                    getBlockPos().getZ() + y / 32.0F + 1 / 64F))
                    .color(r, g, b)
                    .send();
        }
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
        float base = 0.3F + 0.5F * ((float) this.tank.getFluidAmount() / TANK_CAPACITY);
        float out = base + (float) this.aspects.totalAmount() / MAX_ASPECT * (1.0F - base);
        if (out > 1.0F) {
            out = 1.001F;
        }

        if (out == 1.0F) {
            out = 0.9999F;
        }

        return out;
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
            // Slow neglect should first look like a leak. Only an actual physical leak adds a
            // very small Aura consequence; blocked leak attempts do not become free Flux.
            if (level.getRandom().nextInt(4) != 0
                    && PhysicalFlux.spill((ServerLevel) level, getBlockPos(), level.getRandom())) {
                AuraHelper.polluteAura(level, getBlockPos(), 0.25F, true);
            }
        }
        setChanged();
        syncToClient();
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
                Effects.bamf((ServerLevel) level, Vec3.atCenterOf(getBlockPos()).add(0F, 0.75F, 0F))
                        .withSound()
                        .fancy()
                        .side(Direction.UP)
                        .send();
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
                for (int q = 0; q < 10; q++) {
                    Color color;
                    if (aspects.isEmpty()) {
                        color = new Color(1.0F, 1.0F, 1.0F);
                    } else {
                        color = new Color(aspects.entries()
                                .get(level.getRandom().nextInt(aspects.size()))
                                .aspect()
                                .value()
                                .color());
                    }
                    Effects.crucibleBoil(
                                    (ServerLevel) level,
                                    new Vec3(
                                            getBlockPos().getX()
                                                    + 0.2F
                                                    - level.getRandom().nextFloat() * 0.6F,
                                            getBlockPos().getY() + 0.1F + getFluidHeight(),
                                            getBlockPos().getZ()
                                                    + 0.2F
                                                    - level.getRandom().nextFloat() * 0.6F))
                            .heat(data)
                            .color(color.getRed() / 255f, color.getGreen() / 255F, color.getBlue() / 255F)
                            .send();
                }
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
        if (level == null || level.isClientSide()) return stack;
        if (owner == null || owner.isDeadOrDying()) return stack;
        if (tank.getFluidAmount() <= 0) return stack;
        boolean bubble = false;
        boolean craftDone = false;
        int count = stack.getCount();

        for (int i = 0; i < count; i++) {
            CrucibleRecipe recipe = ThaumaturgeCraftingManager.findMatchingCrucibleRecipe(
                    (ServerLevel) level, owner, this.aspects, stack);
            if (recipe != null) {
                ItemStack out = recipe.assemble(new CrucibleRecipeInput(stack, this.aspects), level.registryAccess());
                this.aspects = recipe.removeMatching(aspects);
                {
                    tank.drain(50, IFluidHandler.FluidAction.EXECUTE);
                }
                ejectItem(out.copy());
                NeoForge.EVENT_BUS.post(new CrucibleEvent.Crafted(
                        owner, getBlockPos(), getBlockState(), this, out.copy(), recipe.aspects()));
                craftDone = true;
                count--;
                this.counter = -250L;
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
                    this.counter = -150L;
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
            level.blockEvent(getBlockPos(), getBlockState().getBlock(), 99, 0);
        }

        setChanged();
        if (count <= 0) return null;
        return stack.copyWithCount(count);
    }

    public void attemptSmelt(ItemEntity entity) {
        if (entity.level().isClientSide()) return;
        ItemStack stack = entity.getItem();
        Entity throwerRef = entity.getOwner();
        if (!(throwerRef instanceof Player player)) return;
        ItemStack res = attemptSmelt(stack, player);
        if (res != null && res.getCount() > 0) {
            stack.setCount(res.getCount());
            entity.setItem(stack);
        } else {
            entity.discard();
        }
    }
}
