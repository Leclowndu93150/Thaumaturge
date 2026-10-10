package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
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
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityAdvancedAlchemicalFurnace extends BlockEntity {
    public static final int MAX_ESSENTIA = 500;
    public static final int MAX_POWER = 500;
    public static final int HOT_HEAT = 100;
    public static final int HEAT_PER_ASPECT = 2;

    private static final String ASPECTS_KEY = "Aspects";
    private static final String INPUT_KEY = "Input";
    private static final String HEAT_KEY = "Heat";
    private static final String PERDITIO_KEY = "Perditio";
    private static final String AQUA_KEY = "Aqua";
    private static final String COOLDOWN_KEY = "Cooldown";
    private static final String CYCLE_KEY = "CycleDuration";
    private static final String ASSEMBLED_KEY = "Assembled";
    private static final int POWER_INTERVAL = 5;
    private static final int POWER_REQUEST = 50;
    private static final int HEAT_DECAY = 1;
    private static final int MIN_COOLDOWN = 5;
    private static final int COOLDOWN_RANGE = 100;
    private static final int HORIZONTAL_COUNT = 4;
    private static final float BUBBLE_VOLUME = 0.3F;
    private static final float BUBBLE_PITCH_LOW = 0.9F;
    private static final float BUBBLE_PITCH_RANGE = 0.3F;

    private final AdvancedFurnaceNozzle[] nozzles = new AdvancedFurnaceNozzle[HORIZONTAL_COUNT];
    private final BlockPos.MutableBlockPos formedCursor = new BlockPos.MutableBlockPos();
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
            nozzles[direction.get2DDataValue()] = new AdvancedFurnaceNozzle(this, direction);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityAdvancedAlchemicalFurnace furnace) {
        if (!(level instanceof ServerLevel server) || !AdvancedAlchemicalFurnaceStructure.isLoaded(level, pos)) {
            return;
        }
        furnace.syncAssembly(server, pos);
        if (furnace.assembled) {
            furnace.runCycle(server, pos);
        }
        furnace.updateLit(server, pos, state);
    }

    private void syncAssembly(ServerLevel server, BlockPos pos) {
        boolean nowFormed = AdvancedAlchemicalFurnaceStructure.isFormed(server, pos, formedCursor);
        if (nowFormed == assembled) {
            return;
        }
        assembled = nowFormed;
        AdvancedAlchemicalFurnaceStructure.refreshNozzles(server, pos);
        changed();
    }

    private void updateLit(ServerLevel server, BlockPos pos, BlockState state) {
        boolean shouldLight = assembled && cooldown > 0;
        if (state.getValue(BlockAdvancedAlchemicalFurnace.LIT) == shouldLight) {
            return;
        }
        server.setBlock(pos, state.setValue(BlockAdvancedAlchemicalFurnace.LIT, shouldLight), Block.UPDATE_ALL);
    }

    private void runCycle(ServerLevel server, BlockPos pos) {
        boolean dirty = false;
        if (cooldown > 0) {
            cooldown--;
            dirty = true;
        }
        if (server.getGameTime() % POWER_INTERVAL == 0) {
            dirty |= rechargeReserves(server, pos);
        }
        if (cooldown == 0 && tryProcessInput(server, pos)) {
            changed();
            return;
        }
        if (dirty) {
            setChanged();
        }
    }

    private boolean rechargeReserves(ServerLevel server, BlockPos pos) {
        int heatBefore = heat;
        int perditioBefore = perditio;
        int aquaBefore = aqua;
        heat = topUp(server, pos, TTAspects.IGNIS, Math.max(0, heat - HEAT_DECAY));
        perditio = topUp(server, pos, TTAspects.PERDITIO, perditio);
        aqua = topUp(server, pos, TTAspects.AQUA, aqua);
        boolean moved = heat != heatBefore || perditio != perditioBefore || aqua != aquaBefore;
        if (moved) {
            changed();
        }
        return moved;
    }

    private boolean tryProcessInput(ServerLevel server, BlockPos pos) {
        if (input.isEmpty()) {
            return false;
        }
        AspectList content = inputContent();
        int points = content.totalAmount();
        if (points <= 0 || aspects.totalAmount() + points > MAX_ESSENTIA || !canAfford(points)) {
            return false;
        }
        heat -= points * HEAT_PER_ASPECT;
        perditio -= points;
        aqua -= points;
        aspects = aspects.add(content);
        input = ItemStack.EMPTY;
        inputAspects = null;
        cooldown = MIN_COOLDOWN + COOLDOWN_RANGE * (MAX_POWER - heat) / MAX_POWER;
        cycleDuration = cooldown;
        float pitch = BUBBLE_PITCH_LOW + server.getRandom().nextFloat() * BUBBLE_PITCH_RANGE;
        server.playSound(null, pos, TTSounds.BUBBLE.get(), SoundSource.BLOCKS, BUBBLE_VOLUME, pitch);
        return true;
    }

    private boolean canAfford(int points) {
        return heat >= points * HEAT_PER_ASPECT && perditio >= points && aqua >= points;
    }

    private static int topUp(ServerLevel server, BlockPos pos, ResourceKey<IAspect> primal, int level) {
        int headroom = MAX_POWER - level;
        if (headroom <= 0) {
            return level;
        }
        int request = Math.min(headroom, POWER_REQUEST);
        int gained = VisRelayNetwork.drainEverySourceNear(server, pos, primal, request);
        if (gained < request) {
            gained += VisRelayNetwork.drainNodesNear(server, pos, primal, request - gained);
        }
        return Math.min(level + gained, MAX_POWER);
    }

    private AspectList inputContent() {
        AspectList cached = inputAspects;
        if (cached == null) {
            cached = input.isEmpty() ? AspectList.EMPTY : AspectIndexAccess.of(input);
            inputAspects = cached;
        }
        return cached;
    }

    private void changed() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    private boolean canReceiveInput(ItemStack stack) {
        boolean serverSide = level != null && !level.isClientSide();
        return serverSide && assembled && input.isEmpty() && !stack.isEmpty();
    }

    private static boolean fitsFurnace(int points) {
        return points > 0 && points <= MAX_ESSENTIA && points * HEAT_PER_ASPECT <= MAX_POWER;
    }

    public boolean insertInput(ItemStack stack) {
        if (!canReceiveInput(stack)) {
            return false;
        }
        AspectList content = AspectIndexAccess.of(stack);
        if (!fitsFurnace(content.totalAmount())) {
            return false;
        }
        input = stack.copyWithCount(1);
        inputAspects = content;
        changed();
        return true;
    }

    public int takeEssentia(Holder<IAspect> aspect, int amount) {
        int taken = Math.min(amount, aspects.amountOf(aspect));
        if (taken <= 0) {
            return 0;
        }
        aspects = aspects.remove(aspect, taken);
        changed();
        return taken;
    }

    public @Nullable Holder<IAspect> randomEssentia() {
        List<AspectInstance> stored = aspects.entries();
        if (stored.isEmpty()) {
            return null;
        }
        int index = level == null ? 0 : level.getRandom().nextInt(stored.size());
        return stored.get(index).aspect();
    }

    public AdvancedFurnaceNozzle nozzle(Direction outputFace) {
        if (!outputFace.getAxis().isHorizontal()) {
            throw new IllegalArgumentException("Nozzles exist only on horizontal faces, got " + outputFace);
        }
        return nozzles[outputFace.get2DDataValue()];
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
        return input.isEmpty() ? 0 : inputContent().totalAmount();
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level instanceof ServerLevel server) {
            if (!input.isEmpty()) {
                server.addFreshEntity(new ItemEntity(server, pos.getX(), pos.getY(), pos.getZ(), input.copy()));
            }
            int stored = aspects.totalAmount();
            if (stored > 0) {
                AuraHelper.polluteAura(server, pos, stored, true);
            }
            input = ItemStack.EMPTY;
            inputAspects = null;
            aspects = AspectList.EMPTY;
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(ASPECTS_KEY, AspectList.CODEC, aspects);
        if (!input.isEmpty()) {
            output.store(INPUT_KEY, ItemStack.CODEC, input);
        }
        output.putInt(HEAT_KEY, heat);
        output.putInt(PERDITIO_KEY, perditio);
        output.putInt(AQUA_KEY, aqua);
        output.putInt(COOLDOWN_KEY, cooldown);
        output.putInt(CYCLE_KEY, cycleDuration);
        output.putBoolean(ASSEMBLED_KEY, assembled);
    }

    @Override
    protected void loadAdditional(ValueInput stored) {
        super.loadAdditional(stored);
        assembled = stored.getBooleanOr(ASSEMBLED_KEY, false);
        inputAspects = null;
        input = stored.read(INPUT_KEY, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        aspects = stored.read(ASPECTS_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        heat = readReserve(stored, HEAT_KEY);
        perditio = readReserve(stored, PERDITIO_KEY);
        aqua = readReserve(stored, AQUA_KEY);
        cooldown = Math.max(0, stored.getIntOr(COOLDOWN_KEY, 0));
        cycleDuration = Math.max(cooldown, stored.getIntOr(CYCLE_KEY, 0));
    }

    private static int readReserve(ValueInput stored, String key) {
        return Math.clamp(stored.getIntOr(key, 0), 0, MAX_POWER);
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
