package com.leclowndu93150.thaumaturge.content.crucible;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipe;
import com.leclowndu93150.thaumaturge.content.recipe.crucible.CrucibleRecipeInput;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
import java.awt.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public class BlockEntityCrucible extends BlockEntity implements IAspectContainer {

    public static final int TANK_CAPACITY = 1000;
    public static final int MAX_ASPECT = 500;

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
        super(TCBlockEntities.CRUCIBLE.get(), worldPosition, blockState);
    }

    private void tick() {
        if (level == null) return;
        counter++;
        int prevHeat = heat;
        if (!level.isClientSide()) {
            if (tank.getFluidAmount() > 0) {
                BlockState below = level.getBlockState(getBlockPos().below());
                boolean hasHeatBelow = below.is(TCBlockTags.CRUCIBLE_HEAT_SOURCES);
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

            if (aspects.totalAmount() > MAX_ASPECT) spillRandom();

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

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level instanceof ServerLevel) spillRemnants();
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

    @Override
    public void setAspects(AspectList aspects) {}

    @Override
    public boolean doesContainerAccept(Holder<IAspect> aspect) {
        return false;
    }

    @Override
    public int addToContainer(Holder<IAspect> aspect, int amount) {
        return 0;
    }

    @Override
    public boolean takeFromContainer(Holder<IAspect> aspect, int amount) {
        return false;
    }

    @Override
    public boolean doesContainerContainAmount(Holder<IAspect> aspect, int amount) {
        return false;
    }

    @Override
    public int containerContains(Holder<IAspect> aspect) {
        return 0;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TCNbt.store(output, "Aspects", AspectList.CODEC, registries, aspects);
        output.put("Tank", tank.writeToNBT(registries, new CompoundTag()));
        output.putShort("Heat", heat);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        if (input.contains("Tank")) {
            tank.readFromNBT(registries, input.getCompound("Tank"));
        }
        aspects = TCNbt.read(input, "Aspects", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        heat = (short) input.getShort("Heat");
    }

    private void syncToClient() {
        if (level == null || level.isClientSide()) return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        {
            CompoundTag tagvalueoutput = new CompoundTag();
            saveAdditional(tagvalueoutput, registries);
            nbt.merge(tagvalueoutput);
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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
        if (level == null || level.isClientSide()) return;
        int total = aspects.totalAmount();
        if (tank.getFluidAmount() > 0 || total > 0) {
            tank.setFluid(FluidStack.EMPTY);
            AuraHelper.polluteAura(level, getBlockPos(), total * 0.25f, true);
            int fluxAmount = aspects.amountOf(
                    level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TCAspects.VITIUM));
            if (fluxAmount > 0) AuraHelper.polluteAura(level, getBlockPos(), fluxAmount * 0.75f, false);
            this.aspects = AspectList.EMPTY;
            level.blockEvent(getBlockPos(), getBlockState().getBlock(), 2, 5);
            setChanged();
            syncToClient();
        }
    }

    public void spillRandom() {
        if (level == null || level.isClientSide()) return;
        if (!aspects.isEmpty()) {
            Holder<IAspect> randAspect = aspects.entries()
                    .get(level.getRandom().nextInt(aspects.size()))
                    .aspect();
            aspects = aspects.reduce(randAspect, 1);
            AuraHelper.polluteAura(level, getBlockPos(), randAspect.is(TCAspects.VITIUM) ? 1.0f : 0.25f, true);
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
                    TCSounds.SPILL.get(),
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
                    TCSounds.SPILL.get(),
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
                NeoForge.EVENT_BUS.post(new CrucibleEvent.CrucibleCraftedEvent(
                        owner, getBlockPos(), getBlockState(), this, out.copy(), recipe.aspects()));
                craftDone = true;
                count--;
                this.counter = -250L;
            } else {
                AspectList aspects = AspectIndexAccess.index().of(stack);
                CrucibleEvent.CrucibleDecomposeItemEvent event = new CrucibleEvent.CrucibleDecomposeItemEvent(
                        owner, getBlockPos(), getBlockState(), this, stack, aspects);
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
                    TCSounds.BUBBLE.get(),
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
