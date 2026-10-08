package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityAdvancedAlchemicalFurnace extends BlockEntity {
    public static final int MAX_ESSENTIA = 500;
    public static final int MAX_POWER = 500;
    public static final int HOT_HEAT = 100;
    public static final int HEAT_PER_ASPECT = 2;
    private static final int POWER_DRAW_INTERVAL = 5;
    private static final int POWER_REQUEST = 50;
    private static final int HEAT_DECAY = 1;
    private static final int MIN_COOLDOWN = 5;
    private static final float COOLDOWN_RANGE = 100.0F;

    private final Map<Direction, AdvancedFurnaceNozzle> nozzles = new EnumMap<>(Direction.class);
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private AspectList aspects = AspectList.EMPTY;
    private ItemStack input = ItemStack.EMPTY;
    private @Nullable AspectList inputAspects;
    private int heat;
    private int perditio;
    private int aqua;
    private int cooldown;
    private int cycleDuration;
    private boolean assembled;

    public BlockEntityAdvancedAlchemicalFurnace(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(), pos, state);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            nozzles.put(direction, new AdvancedFurnaceNozzle(this, direction));
        }
    }

    public static void serverTick(
            Level level, BlockPos pos, BlockState state, BlockEntityAdvancedAlchemicalFurnace furnace) {
        if (level instanceof ServerLevel serverLevel) {
            furnace.tickServer(serverLevel);
        }
    }

    private void tickServer(ServerLevel level) {
        if (!AdvancedAlchemicalFurnaceStructure.isLoaded(level, worldPosition)) {
            return;
        }
        boolean visible = false;
        boolean formed = AdvancedAlchemicalFurnaceStructure.isFormed(level, worldPosition, cursor);
        if (formed != assembled) {
            assembled = formed;
            AdvancedAlchemicalFurnaceStructure.refreshNozzles(level, worldPosition);
            visible = true;
        }
        if (!assembled) {
            setLit(false);
            if (visible) {
                markUpdated();
            }
            return;
        }
        if (level.getGameTime() % POWER_DRAW_INTERVAL == 0) {
            visible |= charge(level);
        }
        if (cooldown > 0) {
            cooldown--;
            if (cooldown == 0) {
                cycleDuration = 0;
            }
            setChanged();
        }
        if (cooldown == 0 && !input.isEmpty()) {
            visible |= processInput();
        }
        setLit(cooldown > 0);
        if (visible) {
            markUpdated();
        }
    }

    private boolean charge(ServerLevel level) {
        int heatBefore = heat;
        int perditioBefore = perditio;
        int aquaBefore = aqua;
        heat = Math.max(0, heat - HEAT_DECAY);
        heat += draw(level, TTAspects.IGNIS, heat);
        perditio += draw(level, TTAspects.PERDITIO, perditio);
        aqua += draw(level, TTAspects.AQUA, aqua);
        return heat != heatBefore || perditio != perditioBefore || aqua != aquaBefore;
    }

    private int draw(ServerLevel level, ResourceKey<IAspect> primal, int stored) {
        if (stored >= MAX_POWER) {
            return 0;
        }
        int request = Math.min(POWER_REQUEST, MAX_POWER - stored);
        int drained = VisRelayNetwork.drainEverySourceNear(level, worldPosition, primal, request);
        if (drained < request) {
            drained += VisRelayNetwork.drainNodesNear(level, worldPosition, primal, request - drained);
        }
        return drained;
    }

    private boolean processInput() {
        AspectList result = inputAspects();
        int amount = result.totalAmount();
        if (aspects.totalAmount() + amount > MAX_ESSENTIA
                || heat < amount * HEAT_PER_ASPECT
                || perditio < amount
                || aqua < amount) {
            return false;
        }
        heat -= amount * HEAT_PER_ASPECT;
        perditio -= amount;
        aqua -= amount;
        aspects = aspects.add(result);
        setInput(ItemStack.EMPTY);
        cooldown = MIN_COOLDOWN + Math.round((1.0F - heat / (float) MAX_POWER) * COOLDOWN_RANGE);
        cycleDuration = cooldown;
        return true;
    }

    public boolean insertInput(ItemStack stack) {
        if (!assembled || !input.isEmpty() || stack.isEmpty()) {
            return false;
        }
        int amount = AspectIndexAccess.of(stack).totalAmount();
        if (amount <= 0 || amount > MAX_ESSENTIA || amount * HEAT_PER_ASPECT > MAX_POWER) {
            return false;
        }
        setInput(stack.copyWithCount(1));
        markUpdated();
        return true;
    }

    private void setInput(ItemStack stack) {
        input = stack;
        inputAspects = null;
    }

    private AspectList inputAspects() {
        if (inputAspects == null) {
            inputAspects = input.isEmpty() ? AspectList.EMPTY : AspectIndexAccess.of(input);
        }
        return inputAspects;
    }

    public int takeEssentia(Holder<IAspect> aspect, int amount) {
        int available = aspects.amountOf(aspect);
        if (amount <= 0 || available <= 0) {
            return 0;
        }
        int taken = Math.min(amount, available);
        aspects = aspects.remove(aspect, taken);
        markUpdated();
        return taken;
    }

    public @Nullable Holder<IAspect> randomEssentia() {
        List<AspectInstance> entries = aspects.entries();
        if (entries.isEmpty()) {
            return null;
        }
        return entries.get(level == null ? 0 : level.getRandom().nextInt(entries.size()))
                .aspect();
    }

    public AdvancedFurnaceNozzle nozzle(Direction outputFace) {
        return nozzles.get(outputFace);
    }

    public boolean isAssembled() {
        return assembled;
    }

    public AspectList aspects() {
        return aspects;
    }

    public ItemStack input() {
        return input;
    }

    public int inputCost() {
        return inputAspects().totalAmount();
    }

    public int heat() {
        return heat;
    }

    public int perditio() {
        return perditio;
    }

    public int aqua() {
        return aqua;
    }

    public int cooldown() {
        return cooldown;
    }

    public int cycleDuration() {
        return cycleDuration;
    }

    private void setLit(boolean lit) {
        BlockState state = getBlockState();
        if (level != null && state.getValue(BlockAdvancedAlchemicalFurnace.LIT) != lit) {
            level.setBlock(worldPosition, state.setValue(BlockAdvancedAlchemicalFurnace.LIT, lit), Block.UPDATE_ALL);
        }
    }

    private void markUpdated() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public void dropContents(BlockPos pos) {
        if (level == null || level.isClientSide()) {
            return;
        }
        if (!input.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), input);
            setInput(ItemStack.EMPTY);
        }
        if (!aspects.isEmpty()) {
            AuraHelper.polluteAura(level, pos, aspects.totalAmount(), true);
            aspects = AspectList.EMPTY;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TTNbt.store(output, "Aspects", AspectList.CODEC, registries, aspects);
        if (!input.isEmpty()) {
            TTNbt.store(output, "Input", ItemStack.CODEC, registries, input);
        }
        output.putInt("Heat", heat);
        output.putInt("Perditio", perditio);
        output.putInt("Aqua", aqua);
        output.putInt("Cooldown", cooldown);
        output.putInt("CycleDuration", cycleDuration);
        output.putBoolean("Assembled", assembled);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspects = TTNbt.read(input, "Aspects", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        setInput(TTNbt.read(input, "Input", ItemStack.CODEC, registries).orElse(ItemStack.EMPTY));
        heat = Math.clamp((input.contains("Heat") ? input.getInt("Heat") : 0), 0, MAX_POWER);
        perditio = Math.clamp((input.contains("Perditio") ? input.getInt("Perditio") : 0), 0, MAX_POWER);
        aqua = Math.clamp((input.contains("Aqua") ? input.getInt("Aqua") : 0), 0, MAX_POWER);
        cooldown = Math.max(0, (input.contains("Cooldown") ? input.getInt("Cooldown") : 0));
        cycleDuration = Math.max(cooldown, (input.contains("CycleDuration") ? input.getInt("CycleDuration") : 0));
        assembled = (input.contains("Assembled") ? input.getBoolean("Assembled") : false);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
