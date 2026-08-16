package com.leclowndu93150.thaumaturge.content.essentia.smeltery;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public class BlockEntitySmelter extends BlockEntity implements MenuProvider {
    private static final int MAX_VIS = 256;

    public AspectList aspects = AspectList.EMPTY;
    public int vis;
    public int smeltTime = 100;
    public int furnaceBurnTime;
    public int currentItemBurnTime;
    public int furnaceCookTime;
    private final SmelterInventory inventory = new SmelterInventory();
    boolean speedBoost = false;
    int count = 0;
    int bellows = -1;

    public BlockEntitySmelter(BlockPos worldPosition, BlockState blockState) {
        super(TCBlockEntities.SMELTER.get(), worldPosition, blockState);
    }

    protected BlockEntitySmelter(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntitySmelter smelter) {
        smelter.tick();
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TCNbt.store(output, "Aspects", AspectList.CODEC, registries, aspects);
        output.putInt("BurnTime", this.furnaceBurnTime);
        output.putBoolean("SpeedBoost", this.speedBoost);
        output.putInt("CookTime", this.furnaceCookTime);
        output.putInt("SmeltTime", this.smeltTime);
        output.putInt("CurrentItemBurnTime", this.currentItemBurnTime);
        output.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspects = TCNbt.read(input, "Aspects", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        this.vis = aspects.totalAmount();
        this.furnaceBurnTime = input.getInt("BurnTime");
        this.speedBoost = input.getBoolean("SpeedBoost");
        this.furnaceCookTime = input.getInt("CookTime");
        this.smeltTime = input.getInt("SmeltTime");
        this.currentItemBurnTime = input.getInt("CurrentItemBurnTime");
        inventory.deserializeNBT(registries, input.getCompound("inventory"));
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

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

    void dropContents() {
        if (level == null || level.isClientSide()) return;
        NonNullList<ItemStack> drops = NonNullList.withSize(getInventory().getSlots(), ItemStack.EMPTY);
        for (int slot = 0; slot < getInventory().getSlots(); slot++) {
            drops.set(slot, getInventory().getStackInSlot(slot).copy());
            getInventory().setStackInSlot(slot, ItemStack.EMPTY);
        }
        Containers.dropContents(level, getBlockPos(), drops);
        if (aspects.totalAmount() > 0) {
            AuraHelper.polluteAura(level, getBlockPos(), aspects.totalAmount(), true);
            aspects = AspectList.EMPTY;
            vis = 0;
        }
    }

    private void tick() {

        if (level == null || level.isClientSide()) return;

        boolean wasBurning = this.furnaceBurnTime > 0;
        boolean dirty = false;

        if (this.furnaceBurnTime > 0) {
            this.furnaceBurnTime--;
        }

        count++;

        if (bellows < 0) checkNeighbours();

        int speed = this.getSpeed();
        if (this.speedBoost) speed = (int) (speed * 0.8);

        if (this.count % speed == 0 && !this.aspects.isEmpty()) {
            for (AspectInstance instance : aspects.entries()) {
                if (instance.amount() > 0
                        && BlockEntityAlembic.processAlembics(level, getBlockPos(), instance.aspect())) {
                    takeAspect(instance.aspect(), 1);
                    break;
                }
            }

            for (Direction dir : Direction.Plane.HORIZONTAL) {
                if (getBlockState().getValue(BlockSmelter.FACING) != dir) {
                    BlockPos pos = getBlockPos().relative(dir);
                    BlockState state = level.getBlockState(pos);
                    if (state.getBlock() instanceof BlockSmelterAux
                            && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == dir.getOpposite()) {
                        for (AspectInstance instance : aspects.entries()) {
                            if (instance.amount() > 0
                                    && BlockEntityAlembic.processAlembics(
                                            level, getBlockPos().relative(dir), instance.aspect())) {
                                takeAspect(instance.aspect(), 1);
                                break;
                            }
                        }
                    }
                }
            }
        }

        if (furnaceBurnTime == 0) {
            if (canSmelt()) {
                currentItemBurnTime =
                        furnaceBurnTime = inventory.getStackInSlot(1).copy().getBurnTime(RecipeType.SMELTING);
                if (furnaceBurnTime > 0) {
                    level.setBlock(getBlockPos(), getBlockState().setValue(BlockSmelter.LIT, true), 3);
                    dirty = true;
                    this.speedBoost = false;
                    ItemStack fuel = inventory.getStackInSlot(1).copy();
                    ItemStack copy = fuel.copy();
                    if (!fuel.isEmpty()) {
                        if (fuel.is(TCItems.ALUMENTUM)) this.speedBoost = true;

                        Item item = fuel.getItem();
                        inventory.extractItem(1, 1, false);
                        if (inventory.getStackInSlot(1).getCount() <= 0 && item.hasCraftingRemainingItem(copy)) {
                            inventory.setStackInSlot(1, item.getCraftingRemainingItem(copy));
                        }
                    }
                } else {
                    level.setBlock(getBlockPos(), getBlockState().setValue(BlockSmelter.LIT, false), 3);
                    dirty = true;
                }
            } else {
                level.setBlock(getBlockPos(), getBlockState().setValue(BlockSmelter.LIT, false), 3);
                dirty = true;
            }
        }

        if (getBlockState().getValue(BlockSmelter.LIT) && this.canSmelt()) {
            this.furnaceCookTime++;
            if (furnaceCookTime >= smeltTime) {
                furnaceCookTime = 0;
                smeltItem();
            }
            dirty = true;
        } else furnaceCookTime = 0;

        if (wasBurning != furnaceBurnTime > 0) dirty = true;

        if (dirty) {
            setChanged();
        }
        syncToClient();
    }

    private void smeltItem() {
        if (!this.canSmelt()) return;
        int flux = 0;
        AspectList aspects =
                AspectIndexAccess.index().of(inventory.getStackInSlot(0).copy());
        for (AspectInstance instance : aspects.entries()) {
            if (getEfficiency() < 1.0F) {
                int amount = instance.amount();

                for (int q = 0; q < amount; q++) {
                    if (level.getRandom().nextFloat()
                            > (Objects.equals(instance.aspect().getKey(), TCAspects.VITIUM)
                                    ? getEfficiency() * 0.66F
                                    : getEfficiency())) {
                        aspects = aspects.reduce(instance.aspect(), 1);
                        flux++;
                    }
                }
            }
        }

        this.aspects = this.aspects.add(aspects);

        if (flux > 0) {
            int pp = 0;

            ventfor:
            for (int c = 0; c < flux; c++) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    if (getBlockState().getValue(BlockSmelter.FACING) != dir) {
                        BlockPos pos = getBlockPos().relative(dir);
                        BlockState state = level.getBlockState(pos);
                        if (state.getBlock() instanceof BlockSmelterVent
                                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == dir.getOpposite()
                                && level.getRandom().nextFloat() < 1 / 3F) {
                            level.playSound(
                                    null,
                                    getBlockPos().getX() + 0.5D + dir.getStepX(),
                                    getBlockPos().getY() + 0.5D,
                                    getBlockPos().getZ() + 0.5D + dir.getStepZ(),
                                    SoundEvents.LAVA_EXTINGUISH,
                                    SoundSource.BLOCKS,
                                    0.25F,
                                    2.6F
                                            + (level.getRandom().nextFloat()
                                                            - level.getRandom().nextFloat())
                                                    * 0.8F);
                            for (int i = 0; i < 4; i++) {
                                float fx = 0.1F - this.level.getRandom().nextFloat() * 0.2F;
                                float fz = 0.1F - this.level.getRandom().nextFloat() * 0.2F;
                                float fy = 0.1F - this.level.getRandom().nextFloat() * 0.2F;
                                float fx2 = 0.1F - this.level.getRandom().nextFloat() * 0.2F;
                                float fz2 = 0.1F - this.level.getRandom().nextFloat() * 0.2F;
                                float fy2 = 0.1F - this.level.getRandom().nextFloat() * 0.2F;
                                int color = 11184810;
                                Effects.vent(
                                                (ServerLevel) level,
                                                new Vec3(
                                                        this.getBlockPos().getX() + 0.5F + fx + dir.getStepX(),
                                                        this.getBlockPos().getY() + 0.5F + fy,
                                                        this.getBlockPos().getZ() + 0.5F + fz + dir.getStepZ()))
                                        .motion(
                                                dir.getStepX() / 4F + fx2,
                                                dir.getStepY() / 4F + fy2,
                                                dir.getStepZ() / 4F + fz2)
                                        .color(color)
                                        .send();
                            }
                            continue ventfor;
                        }
                    }
                }
                pp++;
            }

            AuraHelper.polluteAura(level, getBlockPos(), pp, true);
        }
        this.vis = this.aspects.totalAmount();
        inventory.extractItem(0, 1, false);
    }

    public boolean takeAspect(Holder<IAspect> aspect, int amount) {
        if (!aspects.isEmpty() && aspects.amountOf(aspect) >= amount) {
            aspects = aspects.remove(aspect, amount);
            this.vis = aspects.totalAmount();
            setChanged();
            syncToClient();
            return true;
        }
        return false;
    }

    private boolean canSmelt() {
        if (inventory.getStackInSlot(0).getCount() <= 0) return false;
        this.vis = aspects.totalAmount();
        AspectList aspects =
                AspectIndexAccess.index().of(inventory.getStackInSlot(0).copy());
        if (!aspects.isEmpty()) {
            int total = aspects.totalAmount();
            if (total > MAX_VIS - vis) return false;

            this.smeltTime = (int) (total * 2 * (1.0F - 0.125F * this.bellows));
            return true;
        }
        return false;
    }

    public void checkNeighbours() {
        List<Direction> facesToCheck =
                new ArrayList<>(Direction.Plane.HORIZONTAL.stream().toList());
        facesToCheck.remove(getBlockState().getValue(BlockSmelter.FACING));
        this.bellows = BellowsHelper.countBellows(level, getBlockPos(), facesToCheck.toArray(new Direction[0]));
    }

    public int getSpeed() {
        return 20 - (getSmelterType() == 1 ? 10 : 5);
    }

    public int getSmelterType() {
        if (getBlockState().is(TCBlocks.SMELTER_VOID)) return 2;
        if (getBlockState().is(TCBlocks.SMELTER_THAUMIUM)) return 1;
        return 0;
    }

    public float getEfficiency() {
        if (getSmelterType() == 2) return 0.95F;
        if (getSmelterType() == 1) return 0.9F;
        return 0.8F;
    }

    public int getCookProgressScaled(int scale) {
        if (smeltTime <= 0) this.smeltTime = 1;
        return this.furnaceCookTime * scale / this.smeltTime;
    }

    public int getVisScaled(int scale) {
        return this.vis * scale / this.MAX_VIS;
    }

    public int getBurnTimeRemainingScaled(int scale) {
        if (this.currentItemBurnTime == 0) {
            this.currentItemBurnTime = 200;
        }

        return this.furnaceBurnTime * scale / this.currentItemBurnTime;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new MenuSmelter(i, inventory, this);
    }

    private final class SmelterInventory extends ItemStackHandler {

        public SmelterInventory() {
            super(2);
        }

        @Override
        protected void onContentsChanged(int index) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return switch (index) {
                case 0 -> !AspectIndexAccess.index().of(resource.copy()).isEmpty();
                case 1 -> resource.copy().getBurnTime(RecipeType.SMELTING) > 0;
                default -> false;
            };
        }
    }
}
