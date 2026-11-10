package com.leclowndu93150.thaumaturge.content.device.mirror;

import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityMirror extends BlockEntityMirrorBase {
    public static final int TRANSPORT_EVENT = 1;

    private static final int INSTABILITY_THRESHOLD = 128;
    private static final int EJECT_WARMUP = 20;
    private static final float EJECT_SPEED = 0.15F;
    private static final int ITEM_PORTAL_COOLDOWN = 20;
    private static final String INBOX_KEY = "Inbox";
    private static final int INBOX_SLOT = 0;

    private List<ItemStack> outputStacks = new ArrayList<>();
    private final MirrorInbox inbox = new MirrorInbox();

    public BlockEntityMirror(BlockPos pos, BlockState state) {
        super(TTBlockEntities.MIRROR.get(), pos, state);
    }

    BlockEntityMirror(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected int instabilityThreshold() {
        return INSTABILITY_THRESHOLD;
    }

    @Override
    protected boolean isSameKind(BlockEntity target) {
        return target instanceof BlockEntityMirror;
    }

    public IItemHandler inbox() {
        return inbox;
    }

    public void serverTick(Level level, BlockPos pos) {
        forwardInbox();
        eject();
        tickLink();
    }

    private void forwardInbox() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        ItemStack stack = inbox.takeForForwarding();
        if (stack.isEmpty()) {
            return;
        }

        BlockEntityMirror target = linkedMirror();
        if (target == null) {
            if (!spawnItem(stack)) {
                inbox.restore(stack);
            } else {
                level.blockEvent(worldPosition, getBlockState().getBlock(), TRANSPORT_EVENT, 0);
            }
            return;
        }

        target.addStack(stack);
        addInstability(1);
        target.setChanged();
        server.blockEvent(worldPosition, getBlockState().getBlock(), TRANSPORT_EVENT, 0);
    }

    public boolean transport(ItemEntity itemEntity) {
        if (!linked || !isLinkValid() || level == null) {
            return false;
        }
        BlockEntityMirror target = (BlockEntityMirror) target();
        if (target == null) {
            return false;
        }
        target.addStack(itemEntity.getItem());
        addInstability(itemEntity.getItem().getCount());
        itemEntity.discard();
        setChanged();
        target.setChanged();
        level.blockEvent(worldPosition, getBlockState().getBlock(), TRANSPORT_EVENT, 0);
        return true;
    }

    public boolean transportDirect(ItemStack items) {
        if (items.isEmpty()) {
            return false;
        }
        addStack(items.copy());
        setChanged();
        return true;
    }

    public void addStack(ItemStack stack) {
        outputStacks.add(stack);
        setChanged();
    }

    private void eject() {
        if (level == null || outputStacks.isEmpty() || count <= EJECT_WARMUP) {
            return;
        }
        int i = level.getRandom().nextInt(outputStacks.size());
        ItemStack stored = outputStacks.get(i);
        if (stored.isEmpty()) {
            outputStacks.remove(i);
            setChanged();
            return;
        }
        ItemStack out = stored.copyWithCount(1);
        if (spawnItem(out)) {
            stored.shrink(1);
            addInstability(1);
            level.blockEvent(worldPosition, getBlockState().getBlock(), TRANSPORT_EVENT, 0);
            if (stored.isEmpty()) {
                outputStacks.remove(i);
            }
        }
        setChanged();
    }

    private boolean spawnItem(ItemStack stack) {
        if (level == null) {
            return false;
        }
        Direction face = getBlockState().getValue(BlockMirror.FACING);
        ItemEntity entity = new ItemEntity(
                level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.25, worldPosition.getZ() + 0.5, stack);
        entity.setDeltaMovement(
                face.getStepX() * EJECT_SPEED, face.getStepY() * EJECT_SPEED, face.getStepZ() * EJECT_SPEED);
        entity.setPortalCooldown(ITEM_PORTAL_COOLDOWN);
        return level.addFreshEntity(entity);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        outputStacks = new ArrayList<>(
                TTNbt.read(input, "Items", ItemStack.CODEC.listOf(), registries).orElse(List.of()));
        if (input.contains(INBOX_KEY)) {
            inbox.deserializeNBT(registries, input.getCompound(INBOX_KEY));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        List<ItemStack> filtered =
                outputStacks.stream().filter(stack -> !stack.isEmpty()).toList();
        TTNbt.store(output, "Items", ItemStack.CODEC.listOf(), registries, filtered);
        output.put(INBOX_KEY, inbox.serializeNBT(registries));
    }

    private boolean acceptsInboxItems() {
        return level != null && !level.isClientSide() && linkedMirror() != null;
    }

    private @Nullable BlockEntityMirror linkedMirror() {
        if (link == null) {
            return null;
        }
        ServerLevel targetLevel = targetLevel();
        if (targetLevel == null || !LoadedChunks.contains(targetLevel, link.pos()) || !isLinkValidSimple()) {
            return null;
        }
        return target() instanceof BlockEntityMirror mirror ? mirror : null;
    }

    private final class MirrorInbox extends ItemStackHandler {
        private MirrorInbox() {
            super(1);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == INBOX_SLOT && !stack.isEmpty() && acceptsInboxItems();
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        private ItemStack takeForForwarding() {
            return super.extractItem(INBOX_SLOT, Integer.MAX_VALUE, false);
        }

        private void restore(ItemStack stack) {
            super.setStackInSlot(INBOX_SLOT, stack);
        }
    }
}
