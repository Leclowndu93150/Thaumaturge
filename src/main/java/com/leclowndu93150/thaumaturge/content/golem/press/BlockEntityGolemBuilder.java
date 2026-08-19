package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.ItemGolemPlacer;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityGolemBuilder extends BlockEntity implements IEssentiaTransport, MenuProvider {
    public static final int SLOT_OUTPUT = 0;
    private static final int WORK_INTERVAL_TICKS = 5;
    private static final int SUCTION = 128;
    private static final int PRESS_MAX = 90;
    private static final int PRESS_STEP = 6;
    private static final int PRESS_RETRACT = 3;
    private static final int PRESS_SLAM = 60;
    private static final int VENT_COLOR = 11184810;

    private final ItemStacksResourceHandler output = new ItemStacksResourceHandler(1) {
        @Override
        public boolean isValid(int index, ItemResource resource) {
            return resource.toStack(1).getItem() instanceof ItemGolemPlacer;
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
        }
    };

    private GolemProperties pendingGolem;
    private int cost;
    private int maxCost;
    private boolean bufferedEssentia;
    private int ticks;
    public int press;
    public boolean[] hasStuff;

    public BlockEntityGolemBuilder(BlockPos pos, BlockState state) {
        super(TCBlockEntities.GOLEM_BUILDER.get(), pos, state);
    }

    public ItemStacksResourceHandler output() {
        return output;
    }

    public int cost() {
        return cost;
    }

    public int maxCost() {
        return maxCost;
    }

    public @Nullable GolemProperties pendingGolem() {
        return pendingGolem;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityGolemBuilder builder) {
        builder.ticks++;
        if (builder.ticks % WORK_INTERVAL_TICKS != 0 || builder.cost <= 0 || builder.pendingGolem == null) {
            return;
        }
        if (builder.bufferedEssentia || builder.drawEssentia(level, pos)) {
            builder.bufferedEssentia = false;
            builder.cost--;
            builder.setChanged();
        }
        if (builder.cost <= 0) {
            builder.finishCraft(level, pos);
        }
    }

    private void finishCraft(Level level, BlockPos pos) {
        ItemStack placer = new ItemStack(TCItems.GOLEM_PLACER.get());
        placer.set(TCDataComponents.GOLEM_PROPERTIES.get(), pendingGolem.copy());
        ItemStack current = output.getResource(SLOT_OUTPUT).toStack(output.getAmountAsInt(SLOT_OUTPUT));
        if (current.isEmpty()) {
            output.set(SLOT_OUTPUT, ItemResource.of(placer), 1);
        } else if (current.getCount() < current.getMaxStackSize() && ItemStack.isSameItemSameComponents(current, placer)) {
            output.set(SLOT_OUTPUT, output.getResource(SLOT_OUTPUT), current.getCount() + 1);
        } else {
            return;
        }
        level.playSound(null, pos, TCSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        cost = 0;
        maxCost = 0;
        pendingGolem = null;
        setChanged();
        syncToClient();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityGolemBuilder builder) {
        if (builder.press < PRESS_MAX && builder.cost > 0 && builder.pendingGolem != null) {
            builder.press += PRESS_STEP;
            if (builder.press >= PRESS_SLAM) {
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.66F, 1.0F + level.getRandom().nextFloat() * 0.1F, false);
                for (int i = 0; i < 16; i++) {
                    builder.spawnVent(level, pos);
                }
            }
        }
        if (builder.press >= PRESS_MAX && level.getRandom().nextInt(8) == 0) {
            builder.spawnVent(level, pos);
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.1F, 1.0F + level.getRandom().nextFloat() * 0.1F, false);
        }
        if (builder.press > 0 && (builder.cost <= 0 || builder.pendingGolem == null)) {
            if (builder.press >= PRESS_MAX) {
                for (int i = 0; i < 10; i++) {
                    builder.spawnVent(level, pos);
                }
            }
            builder.press -= PRESS_RETRACT;
        }
    }

    private void spawnVent(Level level, BlockPos pos) {
        level.addParticle(new VentParticleOptions(level.getRandom().nextGaussian() * 0.1, 0.0, level.getRandom().nextGaussian() * 0.1, VENT_COLOR, 1.0F, false), pos.getX() + 0.5, pos.getY() + 1,
                pos.getZ() + 0.5, 0.0, 0.0, 0.0);
    }

    private boolean drawEssentia(Level level, BlockPos pos) {
        Holder<IAspect> machina = EssentiaTransportHelper.resolve(level, TCAspects.MACHINA);
        if (machina == null) {
            return false;
        }
        for (Direction face : Direction.values()) {
            if (!isConnectable(face)) {
                continue;
            }
            IEssentiaTransport transport = level.getCapability(EssentiaCapabilities.TRANSPORT, pos.relative(face), face.getOpposite());
            if (transport == null || !transport.isConnectable(face.getOpposite())) {
                continue;
            }
            if (!transport.canOutputTo(face.getOpposite())) {
                return false;
            }
            if (transport.getSuctionAmount(face.getOpposite()) < getSuctionAmount(face) && transport.takeEssentia(machina, 1, face.getOpposite()) == 1) {
                return true;
            }
        }
        return false;
    }

    public boolean[] checkCraft(GolemProperties props) {
        List<ItemStack> components = props.generateComponents();
        boolean[] result = new boolean[components.size()];
        for (int i = 0; i < components.size(); i++) {
            result[i] = InvHelper.checkAdjacentChests(level, worldPosition, components.get(i));
        }
        return result;
    }

    public boolean startCraft(GolemProperties props, Player player) {
        ItemStack placer = new ItemStack(TCItems.GOLEM_PLACER.get());
        placer.set(TCDataComponents.GOLEM_PROPERTIES.get(), props.copy());
        ItemStack current = output.getResource(SLOT_OUTPUT).toStack(output.getAmountAsInt(SLOT_OUTPUT));
        boolean slotFree = current.isEmpty() || current.getCount() < current.getMaxStackSize() && ItemStack.isSameItemSameComponents(current, placer);
        if (!slotFree) {
            reset();
            return false;
        }
        pendingGolem = props.copy();
        List<ItemStack> componentList = props.generateComponents();
        ItemStack[] components = componentList.toArray(new ItemStack[0]);
        if (!InvHelper.consumeItemsFromAdjacentInventoryOrPlayer(level, worldPosition, player, true, components)) {
            reset();
            return false;
        }
        cost = pendingGolem.getTraits().size() * 2;
        for (ItemStack stack : components) {
            cost += stack.getCount();
        }
        InvHelper.consumeItemsFromAdjacentInventoryOrPlayer(level, worldPosition, player, false, components);
        maxCost = cost;
        setChanged();
        syncToClient();
        level.playSound(null, worldPosition, TCSounds.WAND.get(), SoundSource.BLOCKS, 0.25F, 1.0F);
        return true;
    }

    private void reset() {
        cost = 0;
        maxCost = 0;
        pendingGolem = null;
    }

    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition.getX() - 1, worldPosition.getY(), worldPosition.getZ() - 1, worldPosition.getX() + 2, worldPosition.getY() + 2, worldPosition.getZ() + 2);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumaturge.golem_builder");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MenuGolemBuilder(containerId, inventory, this);
    }

    private void syncToClient() {
        if (level != null && !level.isClientSide()) {
            BlockState current = getBlockState();
            level.sendBlockUpdated(getBlockPos(), current, current, 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.output.serialize(output);
        if (pendingGolem != null) {
            output.store("golem", GolemProperties.CODEC, pendingGolem);
        }
        output.putInt("cost", cost);
        output.putInt("mcost", maxCost);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        output.deserialize(input);
        pendingGolem = input.read("golem", GolemProperties.CODEC).orElse(null);
        cost = input.getIntOr("cost", 0);
        maxCost = input.getIntOr("mcost", 0);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput out = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(out);
            nbt.merge(out.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != Direction.UP;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return isConnectable(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {}

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        return EssentiaTransportHelper.resolve(level, TCAspects.MACHINA);
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return cost > 0 && pendingGolem != null ? SUCTION : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!bufferedEssentia && cost > 0 && pendingGolem != null && aspect.is(TCAspects.MACHINA)) {
            bufferedEssentia = true;
            return 1;
        }
        return 0;
    }
}
