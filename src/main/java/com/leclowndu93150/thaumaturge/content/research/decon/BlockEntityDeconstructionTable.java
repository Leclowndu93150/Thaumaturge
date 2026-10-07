package com.leclowndu93150.thaumaturge.content.research.decon;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityDeconstructionTable extends BlockEntity implements MenuProvider {
    public static final int SLOT_INPUT = 0;
    public static final int BREAK_TIME_TICKS = 40;
    private static final int YIELD_ROLL = 80;

    private static final Component TITLE = Component.translatable("gui.thaumaturge.deconstruction_table.title");

    private final TableInventory inventory = new TableInventory();
    private @Nullable ResourceLocation resultAspect;
    private int breakTime;

    public BlockEntityDeconstructionTable(BlockPos pos, BlockState state) {
        super(TTBlockEntities.DECONSTRUCTION_TABLE.get(), pos, state);
    }

    public ItemStackHandler items() {
        return inventory;
    }

    public @Nullable ResourceLocation resultAspect() {
        return resultAspect;
    }

    public int breakTime() {
        return breakTime;
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuDeconstructionTable(containerId, playerInventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityDeconstructionTable table) {
        if (!table.canBreak()) {
            if (table.breakTime != BREAK_TIME_TICKS) {
                table.breakTime = BREAK_TIME_TICKS;
                table.setChanged();
                table.syncToClient();
            }
            return;
        }
        if (--table.breakTime <= 0) {
            table.breakTime = BREAK_TIME_TICKS;
            table.breakItem(level, pos);
        }
        if (table.breakTime % 10 == 0) {
            table.syncToClient();
        }
    }

    private boolean canBreak() {
        if (resultAspect != null) {
            return false;
        }
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT);
        return !input.isEmpty() && !AspectIndexAccess.index().of(input).isEmpty();
    }

    private void breakItem(Level level, BlockPos pos) {
        ItemStack input = inventory.getStackInSlot(SLOT_INPUT).copy();
        if (input.isEmpty()) {
            return;
        }
        AspectList primals = AspectPrimals.reduce(AspectIndexAccess.index().of(input.copyWithCount(1)));
        if (!primals.isEmpty() && level.getRandom().nextInt(YIELD_ROLL) < primals.totalAmount()) {
            List<Holder<IAspect>> types = List.copyOf(primals.aspects());
            Holder<IAspect> picked = types.get(level.getRandom().nextInt(types.size()));
            resultAspect = AspectPools.idOf(picked);
        }
        input.shrink(1);
        inventory.setStackInSlot(SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
        level.playSound(
                null,
                pos,
                SoundEvents.WOOD_HIT,
                SoundSource.BLOCKS,
                0.3F,
                0.9F + level.getRandom().nextFloat() * 0.2F);
        setChanged();
        syncToClient();
    }

    public void collect(ServerPlayer player) {
        if (resultAspect == null || getLevel() == null) {
            return;
        }
        HolderLookup.RegistryLookup<IAspect> lookup = player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        ResourceLocation collected = resultAspect;
        lookup.get(ResourceKey.create(IAspect.REGISTRY_KEY, collected)).ifPresent(holder -> {
            AspectPools.grant(player, holder, 1);
            resultAspect = null;
            setChanged();
            syncToClient();
        });
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(1);
        ItemStack stack = inventory.getStackInSlot(SLOT_INPUT);
        if (!stack.isEmpty()) {
            container.setItem(0, stack.copy());
        }
        Containers.dropContents(level, pos, container);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            BlockState current = getBlockState();
            level.sendBlockUpdated(getBlockPos(), current, current, 3);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        inventory.deserializeNBT(registries, input.getCompound("inventory"));
        resultAspect = TTNbt.read(input, "result_aspect", LegacyIds.IDENTIFIER_CODEC, registries)
                .orElse(null);
        breakTime = input.contains("break_time") ? input.getInt("break_time") : BREAK_TIME_TICKS;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put("inventory", inventory.serializeNBT(registries));
        if (resultAspect != null) {
            TTNbt.store(output, "result_aspect", ResourceLocation.CODEC, registries, resultAspect);
        }
        output.putInt("break_time", breakTime);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        CompoundTag out = new CompoundTag();
        saveAdditional(out, registries);
        nbt.merge(out);
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private final class TableInventory extends ItemStackHandler {
        TableInventory() {
            super(NonNullList.withSize(1, ItemStack.EMPTY));
        }

        @Override
        protected void onContentsChanged(int index) {
            setChanged();
            syncToClient();
        }

        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return !AspectIndexAccess.index().of(resource).isEmpty();
        }
    }
}
