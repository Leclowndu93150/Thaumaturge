package com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityJarNode;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Controller state for the TC4-style Advanced Alchemical Furnace.
 *
 * <p>The surrounding casing is deliberately checked only through already-loaded neighbouring
 * blocks. It never requests chunks while validating or drawing node charge.
 */
public final class BlockEntityAdvancedAlchemicalFurnace extends BlockEntity implements IEssentiaTransport {
    public static final int MAX_ESSENTIA = 500;
    public static final int MAX_POWER = 500;
    private static final int POWER_DRAW_INTERVAL = 5;
    private static final int NODE_DRAW_RANGE = 8;
    private static final int POWER_REFILL_REQUEST = 50;

    private AspectList aspects = AspectList.EMPTY;
    private ItemStack input = ItemStack.EMPTY;
    private int heat;
    private int perditio;
    private int aqua;
    private int cooldown;
    private int ticks;
    private boolean assembled;

    public BlockEntityAdvancedAlchemicalFurnace(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ADVANCED_ALCHEMICAL_FURNACE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityAdvancedAlchemicalFurnace furnace) {
        if (level instanceof ServerLevel serverLevel) {
            furnace.tickServer(serverLevel);
        }
    }

    private void tickServer(ServerLevel level) {
        ticks++;
        boolean wasAssembled = assembled;
        assembled = validateStructure(level);
        boolean changed = wasAssembled != assembled;
        if (!assembled) {
            updateLit(false);
            if (changed) sync();
            return;
        }
        if (ticks % POWER_DRAW_INTERVAL == 0) {
            changed |= charge(level);
        }
        if (cooldown > 0) {
            cooldown--;
            changed = true;
        }
        if (cooldown == 0 && !input.isEmpty()) {
            changed |= processInput();
        }
        updateLit(cooldown > 0);
        if (changed) {
            setChanged();
            sync();
        }
    }

    /**
     * The controller occupies the centre of the lower layer. Four adjacent nozzle block entities
     * expose its shared essentia store; the upper centre remains open.
     */
    private boolean validateStructure(ServerLevel level) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!level.getBlockState(worldPosition.relative(direction))
                    .is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE)) {
                return false;
            }
        }
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                BlockPos lower = worldPosition.offset(x, 0, z);
                BlockPos upper = worldPosition.offset(x, 1, z);
                if (!level.isLoaded(lower) || !level.isLoaded(upper)) return false;
                boolean corner = x != 0 && z != 0;
                if (!(corner
                                ? level.getBlockState(lower)
                                        .is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER)
                                : level.getBlockState(lower).is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE))
                        || !(corner
                                ? level.getBlockState(upper)
                                        .is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER)
                                : level.getBlockState(upper)
                                        .is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void restoreStructure(LevelAccessor level, BlockPos controllerPos, BlockPos excludedPos) {
        if (level.isClientSide()) return;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                restorePart(level, controllerPos.offset(x, 0, z), excludedPos);
                restorePart(level, controllerPos.offset(x, 1, z), excludedPos);
            }
        }
    }

    private static void restorePart(LevelAccessor level, BlockPos target, BlockPos excludedPos) {
        if (target.equals(excludedPos)) return;
        BlockState state = level.getBlockState(target);
        if (state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER.get())) {
            level.setBlock(target, TCBlocks.ALEMBIC.get().defaultBlockState(), Block.UPDATE_ALL);
        } else if (state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER.get())) {
            level.setBlock(target, TCBlocks.ALCHEMICAL_CONSTRUCT.get().defaultBlockState(), Block.UPDATE_ALL);
        } else if (state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER.get())
                || state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get())) {
            level.setBlock(target, TCBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private boolean charge(ServerLevel level) {
        boolean changed = heat > 0;
        heat = Math.max(0, heat - 1);
        HolderLookup.RegistryLookup<IAspect> aspects = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        changed |= refill(level, Power.HEAT, aspects.get(TCAspects.IGNIS).orElse(null), TCAspects.IGNIS);
        changed |= refill(level, Power.PERDITIO, aspects.get(TCAspects.PERDITIO).orElse(null), TCAspects.PERDITIO);
        changed |= refill(level, Power.AQUA, aspects.get(TCAspects.AQUA).orElse(null), TCAspects.AQUA);
        return changed;
    }

    private boolean refill(
            ServerLevel level,
            Power power,
            @Nullable Holder<IAspect> aspect,
            net.minecraft.resources.ResourceKey<IAspect> key) {
        if (aspect == null || power.amount(this) >= MAX_POWER) return false;
        int request = Math.min(POWER_REFILL_REQUEST, MAX_POWER - power.amount(this));
        int drained = VisRelayHelper.drainCentivis(level, worldPosition, key, request, false);
        int remaining = request - drained;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int x = -NODE_DRAW_RANGE; remaining > 0 && x <= NODE_DRAW_RANGE; x++) {
            for (int y = -NODE_DRAW_RANGE; y <= NODE_DRAW_RANGE; y++) {
                for (int z = -NODE_DRAW_RANGE; z <= NODE_DRAW_RANGE; z++) {
                    cursor.setWithOffset(worldPosition, x, y, z);
                    if (!level.isLoaded(cursor)) continue;
                    if (!(level.getBlockEntity(cursor) instanceof BlockEntityNode node)
                            || node instanceof BlockEntityJarNode) continue;
                    int taken = node.drainCentivis(aspect, remaining);
                    drained += taken;
                    remaining -= taken;
                    if (remaining == 0) break;
                }
            }
        }
        if (drained <= 0) return false;
        power.add(this, drained);
        return true;
    }

    private boolean processInput() {
        AspectList inputAspects = AspectIndexAccess.index().of(input.copy());
        int amount = inputAspects.totalAmount();
        if (amount <= 0
                || aspects.totalAmount() + amount > MAX_ESSENTIA
                || heat < amount * 2
                || perditio < amount
                || aqua < amount) {
            return false;
        }
        heat -= amount * 2;
        perditio -= amount;
        aqua -= amount;
        aspects = aspects.add(inputAspects);
        input.shrink(1);
        cooldown = 5 + Math.round((1.0F - heat / (float) MAX_POWER) * 100.0F);
        return true;
    }

    public boolean insertInput(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!insertInput(held)) return false;
        if (!player.getAbilities().instabuild) held.shrink(1);
        return true;
    }

    /** Accepts one valid item for the next furnace cycle without consuming the supplied stack. */
    public boolean insertInput(ItemStack stack) {
        if (!assembled
                || !input.isEmpty()
                || AspectIndexAccess.index().of(stack.copy()).isEmpty()) return false;
        input = stack.copyWithCount(1);
        setChanged();
        sync();
        return true;
    }

    public boolean assembled() {
        return assembled;
    }

    public AspectList aspects() {
        return aspects;
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

    public void dropContents() {
        if (level == null || level.isClientSide()) return;
        if (!input.isEmpty()) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), input);
            input = ItemStack.EMPTY;
        }
        if (!aspects.isEmpty()) {
            AuraHelper.polluteAura(level, worldPosition, aspects.totalAmount(), true);
            aspects = AspectList.EMPTY;
        }
    }

    private void updateLit(boolean lit) {
        if (getBlockState().getValue(BlockAdvancedAlchemicalFurnace.LIT) != lit) {
            level.setBlock(worldPosition, getBlockState().setValue(BlockAdvancedAlchemicalFurnace.LIT, lit), 3);
        }
    }

    private void sync() {
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return false;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || amount <= 0 || aspects.amountOf(aspect) <= 0) return 0;
        int taken = Math.min(amount, aspects.amountOf(aspect));
        aspects = aspects.reduce(aspect, taken);
        setChanged();
        sync();
        return taken;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return aspects.entries().isEmpty() ? null : aspects.entries().getFirst().aspect();
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return aspects.totalAmount();
    }

    public int takeEssentiaFromNozzle(Holder<IAspect> aspect, int amount) {
        if (amount <= 0 || aspects.amountOf(aspect) <= 0) return 0;
        int taken = Math.min(amount, aspects.amountOf(aspect));
        aspects = aspects.reduce(aspect, taken);
        setChanged();
        sync();
        return taken;
    }

    public @Nullable Holder<IAspect> firstEssentia() {
        return aspects.entries().isEmpty() ? null : aspects.entries().getFirst().aspect();
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TCNbt.store(output, "Aspects", AspectList.CODEC, registries, aspects);
        if (!input.isEmpty()) {
            output.put("Input", input.save(registries));
        }
        output.putInt("Heat", heat);
        output.putInt("Perditio", perditio);
        output.putInt("Aqua", aqua);
        output.putInt("Cooldown", cooldown);
        output.putBoolean("Assembled", assembled);
    }

    @Override
    protected void loadAdditional(CompoundTag inputTag, HolderLookup.Provider registries) {
        super.loadAdditional(inputTag, registries);
        aspects = TCNbt.read(inputTag, "Aspects", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        input = inputTag.contains("Input")
                ? ItemStack.parse(registries, inputTag.getCompound("Input")).orElse(ItemStack.EMPTY)
                : ItemStack.EMPTY;
        heat = Math.min(MAX_POWER, inputTag.getInt("Heat"));
        perditio = Math.min(MAX_POWER, inputTag.getInt("Perditio"));
        aqua = Math.min(MAX_POWER, inputTag.getInt("Aqua"));
        cooldown = Math.max(0, inputTag.getInt("Cooldown"));
        assembled = inputTag.getBoolean("Assembled");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        CompoundTag output = new CompoundTag();
        saveAdditional(output, registries);
        tag.merge(output);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private enum Power {
        HEAT {
            @Override
            int amount(BlockEntityAdvancedAlchemicalFurnace furnace) {
                return furnace.heat;
            }

            @Override
            void add(BlockEntityAdvancedAlchemicalFurnace furnace, int amount) {
                furnace.heat = Math.min(MAX_POWER, furnace.heat + amount);
            }
        },
        PERDITIO {
            @Override
            int amount(BlockEntityAdvancedAlchemicalFurnace furnace) {
                return furnace.perditio;
            }

            @Override
            void add(BlockEntityAdvancedAlchemicalFurnace furnace, int amount) {
                furnace.perditio = Math.min(MAX_POWER, furnace.perditio + amount);
            }
        },
        AQUA {
            @Override
            int amount(BlockEntityAdvancedAlchemicalFurnace furnace) {
                return furnace.aqua;
            }

            @Override
            void add(BlockEntityAdvancedAlchemicalFurnace furnace, int amount) {
                furnace.aqua = Math.min(MAX_POWER, furnace.aqua + amount);
            }
        };

        abstract int amount(BlockEntityAdvancedAlchemicalFurnace furnace);

        abstract void add(BlockEntityAdvancedAlchemicalFurnace furnace, int amount);
    }
}
