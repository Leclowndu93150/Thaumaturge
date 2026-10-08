package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellInscribeEvent;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityFocalManipulator extends AbstractSyncedBlockEntity implements MenuProvider {
    public static final int SLOT_FOCUS = 0;
    public static final int MAX_NAME_LENGTH = 50;
    private static final int DRAIN_INTERVAL = 20;
    private static final float DRAIN_PER_CYCLE = 20.0F;
    private static final int VIS_PER_COMPLEXITY = 10;
    private static final int VIS_PER_BUDGET_DIVISOR = 5;
    private static final int CHARGER_REACH = 1;
    private static final int CHUNK = 16;
    private static final int SPARKLE_SPREAD = 3;
    private static final Component TITLE = Component.translatable("block.thaumaturge.focal_manipulator");

    private final Drawer drawer = new Drawer();
    private Spell draft = Spell.empty();
    private String name = "";
    private float vis;
    private float visTotal;
    private AspectList crystals = AspectList.EMPTY;
    private int ticks;

    public BlockEntityFocalManipulator(BlockPos pos, BlockState state) {
        super(TTBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    public ItemStackHandler items() {
        return drawer;
    }

    public ItemStack focusStack() {
        return drawer.getStackInSlot(SLOT_FOCUS).copy();
    }

    public Spell draft() {
        return draft;
    }

    public String name() {
        return name;
    }

    public float vis() {
        return vis;
    }

    public float progress() {
        return visTotal <= 0.0F ? 0.0F : 1.0F - vis / visTotal;
    }

    public boolean inscribing() {
        return vis > 0.0F;
    }

    public AspectList crystals() {
        return crystals;
    }

    public void acceptDraft(Spell spell, String newName) {
        if (inscribing()) {
            return;
        }
        draft = spell;
        name = StringUtil.filterText(newName).strip();
        setChangedAndSync();
    }

    public boolean startInscribing(Player player) {
        ItemStack focus = focusStack();
        Optional<FocusTier> tier = Spells.tierOf(focus);
        if (inscribing() || tier.isEmpty() || level == null) {
            return false;
        }
        SpellSummary summary = Spells.analyze(draft, tier.get(), level.registryAccess(), player);
        AspectList cost = FocusItems.aspects(summary, level.registryAccess());
        if (!InscriptionCheck.of(player, true, false, summary, cost).ready()) {
            return false;
        }
        boolean creative = player.getAbilities().instabuild;
        if (NeoForge.EVENT_BUS
                .post(new SpellInscribeEvent(player, worldPosition, focus, draft, summary))
                .isCanceled()) {
            return false;
        }
        if (!creative) {
            player.giveExperienceLevels(-summary.xp());
            for (AspectInstance instance : cost.entries()) {
                take(player, EssentiaCrystalFactory.of(instance.aspect(), instance.amount()));
            }
        }
        crystals = cost;
        visTotal =
                summary.complexity() * VIS_PER_COMPLEXITY + (float) tier.get().complexity() / VIS_PER_BUDGET_DIVISOR;
        vis = visTotal;
        setChangedAndSync();
        level.playSound(null, worldPosition, TTSounds.CRAFTSTART.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityFocalManipulator table) {
        if (level instanceof ServerLevel server) {
            table.tickServer(server);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityFocalManipulator table) {
        if (table.inscribing()) {
            table.shimmer(level);
        }
    }

    private void tickServer(ServerLevel server) {
        if (!inscribing() || ++ticks % DRAIN_INTERVAL != 0) {
            return;
        }
        if (!FocusItems.isFocus(focusStack())) {
            abort(server);
            return;
        }
        float drained = drain(server, Math.min(DRAIN_PER_CYCLE, vis));
        if (drained > 0.0F) {
            RandomSource random = server.getRandom();
            Vec3 from = new Vec3(
                    worldPosition.getX() + random.nextInt(SPARKLE_SPREAD) - random.nextInt(SPARKLE_SPREAD),
                    worldPosition.getY() + random.nextInt(SPARKLE_SPREAD),
                    worldPosition.getZ() + random.nextInt(SPARKLE_SPREAD) - random.nextInt(SPARKLE_SPREAD));
            EffectDispatch.spawnVisSparkle(server, from, Vec3.atBottomCenterOf(worldPosition.above()));
            vis -= drained;
            setChangedAndSync();
        }
        if (vis <= 0.0F) {
            finish(server);
        }
    }

    private float drain(ServerLevel server, float amount) {
        if (!server.getBlockState(worldPosition.above()).is(TTBlocks.ARCANE_WORKBENCH_CHARGER.get())) {
            return AuraHelper.drainVis(server, worldPosition, amount, false);
        }
        int chunks = (CHARGER_REACH * 2 + 1) * (CHARGER_REACH * 2 + 1);
        float remaining = amount;
        for (int dx = -CHARGER_REACH; dx <= CHARGER_REACH && remaining > 0.0F; dx++) {
            for (int dz = -CHARGER_REACH; dz <= CHARGER_REACH && remaining > 0.0F; dz++) {
                remaining -= AuraHelper.drainVis(
                        server,
                        worldPosition.offset(dx * CHUNK, 0, dz * CHUNK),
                        Math.min(amount / chunks, remaining),
                        false);
            }
        }
        return amount - remaining;
    }

    private void finish(ServerLevel server) {
        ItemStack focus = focusStack();
        vis = 0.0F;
        visTotal = 0.0F;
        crystals = AspectList.EMPTY;
        if (FocusItems.isFocus(focus)) {
            Spells.setSpell(focus, draft);
            if (name.isBlank()) {
                focus.remove(DataComponents.CUSTOM_NAME);
            } else {
                focus.set(DataComponents.CUSTOM_NAME, Component.literal(name));
            }
            drawer.place(focus);
            server.playSound(null, worldPosition, TTSounds.WAND.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        setChangedAndSync();
    }

    private void abort(ServerLevel server) {
        vis = 0.0F;
        visTotal = 0.0F;
        crystals = AspectList.EMPTY;
        server.playSound(null, worldPosition, TTSounds.WANDFAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
        setChangedAndSync();
    }

    private void shimmer(Level clientLevel) {
        RandomSource random = clientLevel.getRandom();
        ShieldSparkParticleOptions spark = new ShieldSparkParticleOptions(
                ARGB32.color(
                        (int) ((1.0F) * 255.0F),
                        (int) ((0.5F + random.nextFloat() * 0.4F) * 255.0F),
                        (int) ((1.0F - random.nextFloat() * 0.4F) * 255.0F),
                        (int) ((1.0F - random.nextFloat() * 0.4F) * 255.0F)),
                0.8F,
                0.3F + random.nextFloat() * 0.3F,
                6 + random.nextInt(5),
                0,
                true);
        clientLevel.addParticle(
                spark,
                worldPosition.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                worldPosition.getY() + 1.4 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                worldPosition.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                0.0,
                0.0,
                0.0);
    }

    private void loadFocusDesign() {
        ItemStack focus = focusStack();
        Spell spell = Spells.spellOf(focus);
        if (spell != null) {
            draft = spell;
            name = focus.has(DataComponents.CUSTOM_NAME) ? focus.getHoverName().getString() : "";
        }
    }

    private static void take(Player player, ItemStack wanted) {
        int remaining = wanted.getCount();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && remaining > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                int taken = Math.min(remaining, stack.getCount());
                stack.shrink(taken);
                remaining -= taken;
            }
        }
    }

    public void dropContents() {
        BlockPos pos = worldPosition;
        ItemStack focus = focusStack();
        if (level != null && !level.isClientSide() && !focus.isEmpty()) {
            drawer.setStackInSlot(SLOT_FOCUS, ItemStack.EMPTY);
            Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, focus);
        }
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MenuFocalManipulator(containerId, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put("inventory", drawer.serializeNBT(registries));
        TTNbt.store(output, "Draft", Spell.CODEC, registries, draft);
        output.putString("Name", name);
        output.putFloat("Vis", vis);
        output.putFloat("VisTotal", visTotal);
        TTNbt.store(output, "Crystals", AspectList.CODEC, registries, crystals);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        drawer.deserializeNBT(registries, input.getCompound("inventory"));
        draft = TTNbt.read(input, "Draft", Spell.CODEC, registries).orElse(Spell.empty());
        name = (input.contains("Name") ? input.getString("Name") : "");
        vis = (input.contains("Vis") ? input.getFloat("Vis") : 0.0F);
        visTotal = (input.contains("VisTotal") ? input.getFloat("VisTotal") : 0.0F);
        crystals = TTNbt.read(input, "Crystals", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
    }

    private final class Drawer extends ItemStackHandler {
        private ItemStack previous = ItemStack.EMPTY;

        @Override
        protected void onLoad() {
            previous = getStackInSlot(SLOT_FOCUS).copy();
        }

        Drawer() {
            super(NonNullList.withSize(1, ItemStack.EMPTY));
        }

        void place(ItemStack stack) {
            setStackInSlot(SLOT_FOCUS, stack);
        }

        @Override
        protected void onContentsChanged(int index) {
            ItemStack previousContents = previous;
            previous = getStackInSlot(SLOT_FOCUS).copy();
            if (level != null && !level.isClientSide()) {
                if (inscribing() && !ItemStack.isSameItemSameComponents(previousContents, focusStack())) {
                    vis = 0.0F;
                    visTotal = 0.0F;
                    crystals = AspectList.EMPTY;
                }
                if (!inscribing()) {
                    loadFocusDesign();
                }
            }
            setChangedAndSync();
        }

        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return Spells.tierOf(resource).isPresent();
        }
    }
}
