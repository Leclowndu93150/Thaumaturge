package com.leclowndu93150.thaumaturge.content.spell.manipulator;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.spell.FocusTier;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.spell.item.FocusItems;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Optional;
import com.leclowndu93150.thaumaturge.api.spell.event.SpellInscribeEvent;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityFocalManipulator extends BlockEntity implements MenuProvider {
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

    public ItemStacksResourceHandler items() {
        return drawer;
    }

    public ItemStack focusStack() {
        ItemResource resource = drawer.getResource(SLOT_FOCUS);
        int amount = drawer.getAmountAsInt(SLOT_FOCUS);
        return resource.isEmpty() || amount <= 0 ? ItemStack.EMPTY : resource.toStack(amount);
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
        name = newName;
        changed();
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
        if (NeoForge.EVENT_BUS.post(new SpellInscribeEvent(player, worldPosition, focus, draft, summary)).isCanceled()) {
            return false;
        }
        if (!creative) {
            player.giveExperienceLevels(-summary.xp());
            for (AspectInstance instance : cost.entries()) {
                take(player, EssentiaCrystalFactory.of(instance.aspect(), instance.amount()));
            }
        }
        crystals = cost;
        visTotal = summary.complexity() * VIS_PER_COMPLEXITY + (float) tier.get().complexity() / VIS_PER_BUDGET_DIVISOR;
        vis = visTotal;
        changed();
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
            Vec3 from = new Vec3(worldPosition.getX() + random.nextInt(SPARKLE_SPREAD) - random.nextInt(SPARKLE_SPREAD), worldPosition.getY() + random.nextInt(SPARKLE_SPREAD),
                    worldPosition.getZ() + random.nextInt(SPARKLE_SPREAD) - random.nextInt(SPARKLE_SPREAD));
            EffectDispatch.spawnVisSparkle(server, from, Vec3.atBottomCenterOf(worldPosition.above()));
            vis -= drained;
            changed();
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
                remaining -= AuraHelper.drainVis(server, worldPosition.offset(dx * CHUNK, 0, dz * CHUNK), Math.min(amount / chunks, remaining), false);
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
        changed();
    }

    private void abort(ServerLevel server) {
        vis = 0.0F;
        visTotal = 0.0F;
        crystals = AspectList.EMPTY;
        server.playSound(null, worldPosition, TTSounds.WANDFAIL.get(), SoundSource.BLOCKS, 0.33F, 1.0F);
        changed();
    }

    private void shimmer(Level clientLevel) {
        RandomSource random = clientLevel.getRandom();
        ShieldSparkParticleOptions spark = new ShieldSparkParticleOptions(
                ARGB.colorFromFloat(1.0F, 0.5F + random.nextFloat() * 0.4F, 1.0F - random.nextFloat() * 0.4F, 1.0F - random.nextFloat() * 0.4F), 0.8F, 0.3F + random.nextFloat() * 0.3F,
                6 + random.nextInt(5), 0, true);
        clientLevel.addParticle(spark, worldPosition.getX() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3F, worldPosition.getY() + 1.4 + (random.nextFloat() - random.nextFloat()) * 0.3F,
                worldPosition.getZ() + 0.5 + (random.nextFloat() - random.nextFloat()) * 0.3F, 0.0, 0.0, 0.0);
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

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        ItemStack focus = focusStack();
        if (level != null && !level.isClientSide() && !focus.isEmpty()) {
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
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        drawer.serialize(output);
        output.store("Draft", Spell.CODEC, draft);
        output.putString("Name", name);
        output.putFloat("Vis", vis);
        output.putFloat("VisTotal", visTotal);
        output.store("Crystals", AspectList.CODEC, crystals);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        drawer.deserialize(input);
        draft = input.read("Draft", Spell.CODEC).orElse(Spell.empty());
        name = input.getStringOr("Name", "");
        vis = input.getFloatOr("Vis", 0.0F);
        visTotal = input.getFloatOr("VisTotal", 0.0F);
        crystals = input.read("Crystals", AspectList.CODEC).orElse(AspectList.EMPTY);
    }

    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            tag.merge(output.buildResult());
        }
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private final class Drawer extends ItemStacksResourceHandler {
        Drawer() {
            super(NonNullList.withSize(1, ItemStack.EMPTY));
        }

        void place(ItemStack stack) {
            set(SLOT_FOCUS, ItemResource.of(stack), stack.getCount());
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
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
            changed();
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return Spells.tierOf(resource.toStack(1)).isPresent();
        }
    }
}
