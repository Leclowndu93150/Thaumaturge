package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BlockEntityInfernalFurnace extends AbstractSyncedBlockEntity {
    private static final int OUTPUT_INVENTORY_DISTANCE = 2;
    private static final double OUTPUT_TOSS_SPEED = 0.3;
    private static final double ORB_DISTANCE = 1.2;
    private static final double ORB_HEIGHT = 0.4;
    private static final double ORB_RETURN_SPEED = 0.13;
    private static final double ORB_SIDE_SPEED = 0.025;
    private boolean contentsDirty = true;
    private final Set<UUID> ejected = new HashSet<>();

    private final ItemStackHandler inventory = new ItemStackHandler(32) {
        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            contentsDirty = true;
            setChanged();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }
    };
    public int furnaceCookTime = 0;
    public int furnaceMaxCookTime = 0;
    public int speedyTime = 0;
    public int facingX = -5;
    public int facingZ = -5;

    public BlockEntityInfernalFurnace(BlockPos worldPosition, BlockState blockState) {
        super(TTBlockEntities.INFERNAL_FURNACE.get(), worldPosition, blockState);
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntityInfernalFurnace furnace) {
        furnace.tick();
    }

    public static ItemStack ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out) {
        if (!level.isEmptyBlock(pos.relative(side))) {
            pos = pos.relative(side.getOpposite());
        }
        ItemEntity entity = new ItemEntity(
                level,
                pos.getX() + 0.5 + side.getStepX(),
                (float) pos.getY() + side.getStepY(),
                pos.getZ() + 0.5 + side.getStepZ(),
                out);
        entity.setDeltaMovement(0.3 * side.getStepX(), 0.3 * side.getStepY(), 0.3 * side.getStepZ());
        level.addFreshEntity(entity);
        return ItemStack.EMPTY;
    }

    public ItemStackHandler inventory() {
        return inventory;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        furnaceCookTime = input.getShort("CookTime");
        speedyTime = input.getShort("SpeedyTime");
        inventory.deserializeNBT(registries, input.getCompound("inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putShort("CookTime", (short) furnaceCookTime);
        output.putShort("SpeedyTime", (short) speedyTime);
        output.put("inventory", inventory.serializeNBT(registries));
    }

    private void tick() {
        if (level == null || level.isClientSide()) return;
        if (level instanceof ServerLevel serverLevel) {
            pruneEjected(serverLevel);
        }
        if (facingX == -5) {
            setFacing();
        }

        int previousCookTime = furnaceCookTime;
        int previousSpeedyTime = speedyTime;
        boolean cooking = false;
        if (furnaceCookTime > 0) {
            furnaceCookTime--;
            cooking = true;
        }

        if (furnaceMaxCookTime <= 0) {
            furnaceMaxCookTime = calculateCookTime();
        }

        furnaceCookTime = Mth.clamp(furnaceCookTime, 0, furnaceMaxCookTime);
        if (furnaceCookTime <= 0 && cooking) {
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                if (inventory.getStackInSlot(slot).getCount() > 0) {
                    ItemStack inputStack = inventory.getStackInSlot(slot).copy();
                    Optional<AbstractCookingRecipe> recipe = getCookingRecipe(inputStack);
                    if (recipe.isPresent()) {
                        if (speedyTime > 0) speedyTime--;

                        ejectItem(
                                recipe.get().assemble(new SingleRecipeInput(inputStack), level.registryAccess()),
                                inputStack.copy(),
                                recipe.get());

                        RandomSource rand = level.getRandom();

                        float qx = facingX == 0
                                ? (rand.nextFloat() - rand.nextFloat()) * 0.5F
                                : facingX * rand.nextFloat();
                        float qz = facingZ == 0
                                ? (rand.nextFloat() - rand.nextFloat()) * 0.5F
                                : facingZ * rand.nextFloat();
                        double x =
                                getBlockPos().getX() + 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.3F + -facingX;
                        double y = getBlockPos().getY() + 0.3F;
                        double z =
                                getBlockPos().getZ() + 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.3F + -facingZ;

                        ((ServerLevel) level)
                                .sendParticles(ParticleTypes.LAVA, x, y, z, 4, qx, 0.2 * rand.nextFloat(), qz, 0.001);
                        level.playSound(
                                null,
                                getBlockPos(),
                                SoundEvents.LAVA_POP,
                                SoundSource.BLOCKS,
                                0.1F + rand.nextFloat() * 0.1F,
                                0.9F + rand.nextFloat() * 0.15F);

                        if (level.getRandom().nextInt(20) == 0)
                            AuraHelper.polluteAura(
                                    level,
                                    getBlockPos()
                                            .relative(getBlockState()
                                                    .getValue(BlockInfernalFurnace.FACING)
                                                    .getOpposite()),
                                    1.0F,
                                    true);

                        // Remove after smelting
                        inventory.setStackInSlot(
                                slot,
                                inputStack.copyWithCount(
                                        inventory.getStackInSlot(slot).getCount() - 1));
                        break;
                    }
                }
            }
        }

        if (speedyTime <= 0) this.speedyTime = (int) AuraHelper.drainVis(level, getBlockPos(), 20, false);

        if (this.furnaceCookTime == 0 && !cooking && contentsDirty) {
            contentsDirty = false;
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                if (inventory.getStackInSlot(slot).getCount() > 0) {
                    ItemStack inputStack = inventory.getStackInSlot(slot).copy();
                    if (canSmelt(inputStack)) {
                        furnaceMaxCookTime = calculateCookTime();
                        furnaceCookTime = furnaceMaxCookTime;
                        break;
                    }
                }
            }
        }
        if (furnaceCookTime != previousCookTime || speedyTime != previousSpeedyTime) {
            setChanged();
        }
    }

    public ItemStack addItemsToInventory(ItemStack item) {
        if (canSmelt(item)) {
            item = ItemHandlerHelper.insertItem(inventory, item, false);
        } else {
            destroyItemEffects();
            item = ItemStack.EMPTY;
        }
        return item;
    }

    private void destroyItemEffects() {
        if (level == null || level.isClientSide()) return;
        level.playSound(
                null,
                getBlockPos(),
                SoundEvents.LAVA_EXTINGUISH,
                SoundSource.BLOCKS,
                0.3F,
                2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
        double x = getBlockPos().getX() + level.getRandom().nextFloat();
        double y = getBlockPos().getY() + 1;
        double z = getBlockPos().getZ() + level.getRandom().nextFloat();
        ((ServerLevel) level).sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0, 0, 0, 1);
    }

    public void ejectItem(ItemStack item, ItemStack furnaceStack, AbstractCookingRecipe recipe) {
        if (item == null || item.isEmpty()) return;
        if (level == null || level.isClientSide()) return;
        Direction direction =
                getBlockState().getValue(BlockInfernalFurnace.FACING).getOpposite();
        launchStack((ServerLevel) level, direction, item.copy());
        int bellows = getBellows() + 1;

        for (int a = 0; a < bellows; a++) {
            ItemStack[] bonuses = getSmeltingBonus(furnaceStack);
            if (bonuses != null) {
                for (ItemStack bonus : bonuses) {
                    if (bonus != null && !bonus.isEmpty()) {
                        launchStack((ServerLevel) level, direction, bonus.copy());
                    }
                }
            }
        }

        RandomSource random = level.getRandom();
        int xp = rollExperiencePoints(recipe.getExperience() * item.getCount(), random);
        BlockPos pos = getBlockPos();
        double x = pos.getX() + 0.5 + ORB_DISTANCE * direction.getStepX();
        double y = pos.getY() + ORB_HEIGHT;
        double z = pos.getZ() + 0.5 + ORB_DISTANCE * direction.getStepZ();
        while (xp > 0) {
            int orbValue = ExperienceOrb.getExperienceValue(xp);
            xp -= orbValue;
            ExperienceOrb orb = new ExperienceOrb((ServerLevel) level, x, y, z, orbValue);
            double spread = (random.nextDouble() - random.nextDouble()) * ORB_SIDE_SPEED;
            double speedX = direction.getStepX() != 0 ? -ORB_RETURN_SPEED * direction.getStepX() : spread;
            double speedZ = direction.getStepZ() != 0 ? -ORB_RETURN_SPEED * direction.getStepZ() : spread;
            orb.setDeltaMovement(speedX, 0.0, speedZ);
            level.addFreshEntity(orb);
        }
    }

    private void launchStack(ServerLevel serverLevel, Direction side, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        BlockPos inventoryPos = worldPosition.relative(side, OUTPUT_INVENTORY_DISTANCE);
        ItemStack remainder = LoadedChunks.contains(serverLevel, inventoryPos)
                ? InvHelper.insertAt(serverLevel, inventoryPos, side.getOpposite(), stack, false)
                : stack;
        if (remainder.isEmpty()) {
            return;
        }
        BlockPos adjacent = worldPosition.relative(side);
        BlockPos spawnPos = serverLevel.getBlockState(adjacent).isAir() ? adjacent : worldPosition;
        ItemEntity thrown = new ItemEntity(
                serverLevel,
                spawnPos.getX() + 0.5,
                worldPosition.getY(),
                spawnPos.getZ() + 0.5,
                remainder,
                side.getStepX() * OUTPUT_TOSS_SPEED,
                0.0,
                side.getStepZ() * OUTPUT_TOSS_SPEED);
        thrown.setNoPickUpDelay();
        serverLevel.addFreshEntity(thrown);
        ejected.add(thrown.getUUID());
    }

    private void pruneEjected(ServerLevel serverLevel) {
        AABB furnaceBounds = LevelSpaces.worldBox(serverLevel, worldPosition, new AABB(worldPosition));
        ejected.removeIf(uuid -> {
            Entity entity = serverLevel.getEntity(uuid);
            return entity == null
                    || !entity.isAlive()
                    || !entity.getBoundingBox().intersects(furnaceBounds);
        });
    }

    boolean isOwnThrow(ItemEntity item) {
        return ejected.contains(item.getUUID());
    }

    static int rollExperiencePoints(float experience, RandomSource random) {
        if (!(experience > 0.0F)) {
            return 0;
        }
        int whole = Mth.floor(experience);
        if (random.nextFloat() < experience - whole && whole < Integer.MAX_VALUE) {
            whole++;
        }
        return whole;
    }

    private ItemStack[] getSmeltingBonus(ItemStack in) {
        if (level == null) return new ItemStack[0];
        List<InfernalBonus> bonuses = in.getItemHolder().getData(InfernalBonus.DATA_MAP);
        if (bonuses == null || bonuses.isEmpty()) return new ItemStack[0];
        ArrayList<ItemStack> out = new ArrayList<>();
        for (InfernalBonus bonus : bonuses) {
            if (level.getRandom().nextFloat() > bonus.chance()) continue;

            Optional<Holder<Item>> optItem = getRandomBoundItem(bonus);
            if (optItem.isEmpty()) continue;

            int count = Math.max(1, bonus.count().sample(level.getRandom()));
            out.add(new ItemStack(optItem.get().value(), count));
        }
        return out.toArray(new ItemStack[0]);
    }

    private Optional<Holder<Item>> getRandomBoundItem(InfernalBonus bonus) {

        if (level == null) return Optional.empty();

        Optional<Holder<Item>> optItem = Optional.empty();
        int attempts = 0;

        while (attempts < 10 && optItem.filter(Holder::isBound).isEmpty()) {
            optItem = bonus.items().getRandomElement(level.getRandom());
            attempts++;
        }

        return optItem.filter(Holder::isBound);
    }

    private void setFacing() {
        this.facingX = 0;
        this.facingZ = 0;
        Direction dir = getBlockState().getValue(BlockInfernalFurnace.FACING);
        this.facingX = dir.getStepX();
        this.facingZ = dir.getStepZ();
    }

    private int getBellows() {
        if (level == null) return 0;
        Direction[] directions = Arrays.stream(Direction.values())
                .filter(dir -> dir != Direction.UP
                        && dir
                                != getBlockState()
                                        .getValue(BlockInfernalFurnace.FACING)
                                        .getOpposite())
                .toArray(Direction[]::new);
        return Math.min(4, BellowsHelper.countBellows(level, getBlockPos(), directions, 2));
    }

    private int calculateCookTime() {
        int b = this.getBellows();
        if (b > 0) {
            b = (20 - (b - 1)) * b;
        }

        return Math.max(10, (this.speedyTime > 0 ? 80 : 140) - b);
    }

    private Optional<AbstractCookingRecipe> getCookingRecipe(ItemStack input) {
        if (level == null || level.isClientSide()) return Optional.empty();
        return ((ServerLevel) level)
                .getRecipeManager()
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level)
                .map(RecipeHolder::value);
    }

    private boolean canSmelt(ItemStack stack) {
        return getCookingRecipe(stack).isPresent();
    }
}
