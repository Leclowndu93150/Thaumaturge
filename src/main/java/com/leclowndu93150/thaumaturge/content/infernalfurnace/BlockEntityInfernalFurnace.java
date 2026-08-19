package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.essentia.BellowsHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class BlockEntityInfernalFurnace extends BlockEntity {

    private final ItemStacksResourceHandler inventory = new ItemStacksResourceHandler(32) {
        @Override
        public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
            return 0;
        }
    };
    public int furnaceCookTime = 0;
    public int furnaceMaxCookTime = 0;
    public int speedyTime = 0;
    public int facingX = -5;
    public int facingZ = -5;

    public BlockEntityInfernalFurnace(BlockPos worldPosition, BlockState blockState) {
        super(TCBlockEntities.INFERNAL_FURNACE.get(), worldPosition, blockState);
    }

    public static void staticTick(Level level, BlockPos pos, BlockState state, BlockEntityInfernalFurnace furnace) {
        furnace.tick();
    }

    public static ItemStack ejectStackAt(Level level, BlockPos pos, Direction side, ItemStack out) {
        if (!level.isEmptyBlock(pos.relative(side))) {
            pos = pos.relative(side.getOpposite());
        }
        ItemEntity entity = new ItemEntity(level, pos.getX() + 0.5 + side.getStepX(), (float) pos.getY() + side.getStepY(), pos.getZ() + 0.5 + side.getStepZ(), out);
        entity.setDeltaMovement(0.3 * side.getStepX(), 0.3 * side.getStepY(), 0.3 * side.getStepZ());
        level.addFreshEntity(entity);
        return ItemStack.EMPTY;
    }

    public ItemStacksResourceHandler inventory() {
        return inventory;
    }

    protected void syncToClient() {
        if (level == null || level.isClientSide())
            return;
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        furnaceCookTime = input.getShortOr("CookTime", (short) 0);
        speedyTime = input.getShortOr("SpeedyTime", (short) 0);
        inventory.deserialize(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putShort("CookTime", (short) furnaceCookTime);
        output.putShort("SpeedyTime", (short) speedyTime);
        inventory.serialize(output);
    }

    private void tick() {
        if (level == null || level.isClientSide())
            return;
        if (facingX == -5) {
            setFacing();
        }

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
            for (int slot = 0; slot < inventory.size(); slot++) {
                if (inventory.getAmountAsInt(slot) > 0) {
                    ItemStack inputStack = inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot));
                    Optional<AbstractCookingRecipe> recipe = getCookingRecipe(inputStack);
                    if (recipe.isPresent()) {
                        if (speedyTime > 0)
                            speedyTime--;

                        ejectItem(recipe.get().assemble(new SingleRecipeInput(inputStack)), inputStack.copy(), recipe.get());

                        RandomSource rand = level.getRandom();

                        float qx = facingX == 0 ? (rand.nextFloat() - rand.nextFloat()) * 0.5F : facingX * rand.nextFloat();
                        float qz = facingZ == 0 ? (rand.nextFloat() - rand.nextFloat()) * 0.5F : facingZ * rand.nextFloat();
                        double x = getBlockPos().getX() + 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.3F + -facingX;
                        double y = getBlockPos().getY() + 0.3F;
                        double z = getBlockPos().getZ() + 0.5F + (rand.nextFloat() - rand.nextFloat()) * 0.3F + -facingZ;

                        ((ServerLevel) level).sendParticles(ParticleTypes.LAVA, x, y, z, 4, qx, 0.2 * rand.nextFloat(), qz, 0.001);
                        level.playSound(null, getBlockPos(), SoundEvents.LAVA_POP, SoundSource.BLOCKS, 0.1F + rand.nextFloat() * 0.1F, 0.9F + rand.nextFloat() * 0.15F);

                        if (level.getRandom().nextInt(20) == 0)
                            AuraHelper.polluteAura(level, getBlockPos().relative(getBlockState().getValue(BlockInfernalFurnace.FACING).getOpposite()), 1.0F, true);

                        // Remove after smelting
                        inventory.set(slot, ItemResource.of(inputStack), inventory.getAmountAsInt(slot) - 1);
                        break;
                    }

                    // Destroy item if no recipe
                    inventory.set(slot, ItemResource.of(inputStack), inventory.getAmountAsInt(slot) - 1);
                }
            }
        }

        if (speedyTime <= 0)
            this.speedyTime = (int) AuraHelper.drainVis(level, getBlockPos(), 20, false);

        if (this.furnaceCookTime == 0 && !cooking) {
            for (int slot = 0; slot < inventory.size(); slot++) {
                if (inventory.getAmountAsInt(slot) > 0) {
                    ItemStack inputStack = inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot));
                    if (canSmelt(inputStack)) {
                        furnaceMaxCookTime = calculateCookTime();
                        furnaceCookTime = furnaceMaxCookTime;
                        break;
                    }
                }
            }
        }
        setChanged();
    }

    public ItemStack addItemsToInventory(ItemStack item) {
        if (canSmelt(item)) {
            Transaction transaction = Transaction.openRoot();
            item = item.copyWithCount(item.getCount() - inventory.insert(ItemResource.of(item), item.getCount(), transaction));
            transaction.commit();
        } else {
            destroyItemEffects();
            item = ItemStack.EMPTY;
        }
        return item;
    }

    private void destroyItemEffects() {
        if (level == null || level.isClientSide())
            return;
        level.playSound(null, getBlockPos(), SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.3F, 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
        double x = getBlockPos().getX() + level.getRandom().nextFloat();
        double y = getBlockPos().getY() + 1;
        double z = getBlockPos().getZ() + level.getRandom().nextFloat();
        ((ServerLevel) level).sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0, 0, 0, 1);
    }

    public void ejectItem(ItemStack item, ItemStack furnaceStack, AbstractCookingRecipe recipe) {
        if (item == null || item.isEmpty())
            return;
        if (level == null || level.isClientSide())
            return;
        ArrayList<ItemStack> toEject = new ArrayList<>();
        toEject.add(item.copy());
        int bellows = getBellows() + 1;
        float lx = 0.5F + -facingX * 1.2F;
        float lz = 0.5F + -facingZ * 1.2F;
        float mx = 0.0F;
        float mz = 0.0F;

        for (int a = 0; a < bellows; a++) {
            ItemStack[] bonuses = getSmeltingBonus(furnaceStack);
            if (bonuses != null) {
                for (ItemStack bonus : bonuses) {
                    if (bonus != null && !bonus.isEmpty()) {
                        toEject.add(bonus.copy());
                    }
                }
            }
        }

        for (ItemStack stack : toEject) {
            if (!stack.isEmpty()) {
                Direction direction = getBlockState().getValue(BlockInfernalFurnace.FACING).getOpposite();
                ejectStackAt(level, getBlockPos(), direction, stack);
            }
        }

        int count = item.getCount();
        float xp = recipe.experience() * count;
        if (xp == 0.0F) {
            return;
        } else if (xp < 1.0F) {
            int i = Mth.floor(xp);
            if (i < Mth.ceil(xp) && Math.random() < (double) (xp - (float) i)) {
                ++i;
            }

            xp = (float) i;
        }

        while (xp > 0) {
            int splitted = ExperienceOrb.getExperienceValue((int) xp);
            xp -= splitted;

            ExperienceOrb orb = new ExperienceOrb(level, getBlockPos().getX() + lx, getBlockPos().getY() + 0.4F, getBlockPos().getZ() + lz, splitted);
            mx = facingX == 0 ? (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.025F : facingX * 0.13F;
            mz = facingZ == 0 ? (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.025F : facingZ * 0.13F;
            orb.setDeltaMovement(mx, 0, mz);
            level.addFreshEntity(orb);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide())
            return;
        for (int slot = 0; slot < inventory.size(); slot++) {
            Direction direction = getBlockState().getValue(BlockInfernalFurnace.FACING).getOpposite();
            ItemEntity entity = new ItemEntity(level, pos.getX() + direction.getStepX(), (float) pos.getY() + direction.getStepY(), pos.getZ() + direction.getStepZ(),
                    inventory.getResource(slot).toStack(inventory.getAmountAsInt(slot)));
            entity.setDeltaMovement(0.3 * direction.getStepX(), 0.3 * direction.getStepY(), 0.3 * direction.getStepZ());
            level.addFreshEntity(entity);
        }
    }

    private ItemStack[] getSmeltingBonus(ItemStack in) {
        if (level == null)
            return new ItemStack[0];
        List<InfernalBonus> bonuses = in.getData(InfernalBonus.DATA_MAP);
        if (bonuses == null || bonuses.isEmpty())
            return new ItemStack[0];
        ArrayList<ItemStack> out = new ArrayList<>();
        for (InfernalBonus bonus : bonuses) {
            if (level.getRandom().nextFloat() > bonus.chance())
                continue;
            if (!bonus.items().isBound())
                continue;
            Optional<Holder<Item>> optItem = getRandomBoundItem(bonus);
            if (optItem.isEmpty())
                continue;

            int count = Math.max(1, bonus.count().sample(level.getRandom()));
            out.add(new ItemStack(optItem.get().value(), count));
        }
        return out.toArray(new ItemStack[0]);
    }

    private Optional<Holder<Item>> getRandomBoundItem(InfernalBonus bonus) {
        if (!bonus.items().isBound())
            return Optional.empty();
        if (level == null)
            return Optional.empty();

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

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector problemreporter$scopedcollector = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput tagvalueoutput = TagValueOutput.createWithContext(problemreporter$scopedcollector, registries);
            saveAdditional(tagvalueoutput);
            nbt.merge(tagvalueoutput.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private int getBellows() {
        if (level == null)
            return 0;
        Direction[] directions = Arrays.stream(Direction.values()).filter(dir -> dir != Direction.UP && dir != getBlockState().getValue(BlockInfernalFurnace.FACING).getOpposite())
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
        if (level == null || level.isClientSide())
            return Optional.empty();
        return ((ServerLevel) level).recipeAccess().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level).map(RecipeHolder::value);
    }

    private boolean canSmelt(ItemStack stack) {
        return getCookingRecipe(stack).isPresent();
    }
}
