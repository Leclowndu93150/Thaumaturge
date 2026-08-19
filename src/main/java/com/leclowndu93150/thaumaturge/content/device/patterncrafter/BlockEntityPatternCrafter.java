package com.leclowndu93150.thaumaturge.content.device.patterncrafter;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class BlockEntityPatternCrafter extends BlockEntity {
    public static final int PATTERN_COUNT = 10;
    private static final int WORK_INTERVAL = 20;
    private static final float VIS_DRAIN = 5.0F;
    private static final int SPIN_EVENT = 1;
    private static final int SPIN_TICKS = 10;
    private static final int[] PATTERN_INPUT_COUNTS = {9, 1, 2, 2, 4, 3, 3, 6, 6, 8};
    private static final int[][] PATTERN_SLOTS = {{0, 1, 2, 3, 4, 5, 6, 7, 8}, {0}, {0, 1}, {0, 3}, {0, 1, 3, 4}, {0, 1, 2}, {0, 3, 6}, {0, 1, 2, 3, 4, 5}, {0, 1, 3, 4, 6, 7},
            {0, 1, 2, 3, 5, 6, 7, 8}};

    private byte patternType;
    private float power;
    private int counter;

    public float rot;
    public float rotSpeed;
    public int rotTicks;

    public BlockEntityPatternCrafter(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PATTERN_CRAFTER.get(), pos, state);
    }

    public byte patternType() {
        return patternType;
    }

    public void cycle() {
        patternType++;
        if (patternType >= PATTERN_COUNT) {
            patternType = 0;
        }
        setChanged();
        syncToClient();
    }

    void tick(Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) {
            if (rotTicks > 0) {
                rotTicks--;
                if (rotTicks % Math.floor(Math.max(1.0F, rotSpeed)) == 0.0) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.CLACK.get(), SoundSource.BLOCKS, 0.2F, 1.7F, false);
                }
                rotSpeed++;
            } else {
                rotSpeed *= 0.8F;
            }
            rot += rotSpeed;
            return;
        }
        if (counter++ % WORK_INTERVAL != 0 || !state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        ServerLevel server = (ServerLevel) level;
        if (power <= 0.0F) {
            power += AuraHelper.drainVis(server, pos, VIS_DRAIN, false);
        }
        int inputCount = PATTERN_INPUT_COUNTS[patternType];
        ResourceHandler<ItemResource> above = InvHelper.getItemHandlerAt(server, pos.above(), Direction.DOWN);
        ResourceHandler<ItemResource> below = InvHelper.getItemHandlerAt(server, pos.below(), Direction.UP);
        if (above == null || below == null) {
            return;
        }
        for (int slot = 0; slot < above.size(); slot++) {
            ItemResource resource = above.getResource(slot);
            if (resource.isEmpty())
                continue;
            ItemStack testStack = resource.toStack(inputCount);
            ItemStack removed = InvHelper.removeStackFrom(server, pos.above(), Direction.DOWN, testStack.copy(), InvHelper.InvFilter.STRICT, true);
            if (removed.getCount() != inputCount)
                continue;
            CraftResult result = craft(server, testStack);
            if (result == null || power < 1.0F)
                continue;
            if (!InvHelper.insertStack(below, result.output.copy(), true).isEmpty())
                continue;
            boolean remaindersFit = true;
            for (ItemStack remainder : result.remainders) {
                if (!InvHelper.insertStack(below, remainder.copy(), true).isEmpty()) {
                    remaindersFit = false;
                    break;
                }
            }
            if (!remaindersFit)
                continue;
            InvHelper.insertStack(below, result.output.copy(), false);
            for (ItemStack remainder : result.remainders) {
                InvHelper.insertStack(below, remainder.copy(), false);
            }
            InvHelper.removeStackFrom(server, pos.above(), Direction.DOWN, testStack, InvHelper.InvFilter.STRICT, false);
            server.blockEvent(pos, state.getBlock(), SPIN_EVENT, 0);
            power--;
            setChanged();
            break;
        }
    }

    private CraftResult craft(ServerLevel server, ItemStack input) {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(ItemStack.EMPTY);
        }
        for (int slot : PATTERN_SLOTS[patternType]) {
            grid.set(slot, input.copyWithCount(1));
        }
        CraftingInput craftingInput = CraftingInput.of(3, 3, grid);
        Optional<RecipeHolder<CraftingRecipe>> match = server.recipeAccess().getRecipeFor(RecipeType.CRAFTING, craftingInput, server);
        if (match.isEmpty()) {
            return null;
        }
        CraftingRecipe recipe = match.get().value();
        ItemStack output = recipe.assemble(craftingInput);
        if (output.isEmpty()) {
            return null;
        }
        List<ItemStack> remainders = new ArrayList<>();
        for (ItemStack remainder : recipe.getRemainingItems(craftingInput)) {
            if (!remainder.isEmpty()) {
                remainders.add(remainder);
            }
        }
        return new CraftResult(output, remainders);
    }

    @Override
    public boolean triggerEvent(int event, int param) {
        if (event == SPIN_EVENT) {
            if (level != null && level.isClientSide()) {
                rotTicks = SPIN_TICKS;
            }
            return true;
        }
        return super.triggerEvent(event, param);
    }

    void syncToClient() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putByte("Type", patternType);
        output.putFloat("Power", power);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        patternType = input.getByteOr("Type", (byte) 0);
        power = input.getFloatOr("Power", 0.0F);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private record CraftResult(ItemStack output, List<ItemStack> remainders) {
    }
}
