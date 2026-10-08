package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchTableAid;
import com.leclowndu93150.thaumaturge.content.aspect.AspectCombinations;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumaturge.content.research.note.NoteRules;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityResearchTable extends AbstractSyncedBlockEntity implements MenuProvider {
    public static final int SLOT_SCRIBE_TOOLS = 0;
    public static final int SLOT_NOTE = 1;
    public static final int SLOT_COUNT = 2;

    public static final ResourceLocation RESEARCH_EXPERTISE = TTIds.rl("research_expertise");
    public static final ResourceLocation RESEARCH_MASTERY = TTIds.rl("research_mastery");
    public static final ResourceLocation RESEARCH_DUPLICATION = TTIds.rl("research_duplication");

    private static final int RECALC_INTERVAL_TICKS = 600;
    private static final int BONUS_SCAN_RADIUS = 8;
    private static final int BOOKSHELF_BONUS_CHANCE = 300;
    private static final int BRAIN_JAR_BONUS_CHANCE = 200;
    private static final float EXPERTISE_REFUND_CHANCE = 0.25F;
    private static final float MASTERY_REFUND_CHANCE = 0.5F;
    private static final float MASTERY_FREE_CHANCE = 0.1F;
    private static final float MAX_AID_SAVE_CHANCE = 0.5F;

    private static final Component TITLE = Component.translatable("gui.thaumaturge.research_table.title");

    private final TableInventory inventory = new TableInventory();
    private AspectList bonusAspects = AspectList.EMPTY;
    private int recalcCounter;

    public BlockEntityResearchTable(BlockPos pos, BlockState state) {
        super(TTBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    public ItemStackHandler items() {
        return inventory;
    }

    public AspectList bonusAspects() {
        return bonusAspects;
    }

    @Override
    public Component getDisplayName() {
        return TITLE;
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuResearchTable(containerId, playerInventory, this);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityResearchTable table) {
        if (++table.recalcCounter >= RECALC_INTERVAL_TICKS) {
            table.recalcCounter = 0;
            table.recalculateBonus(level, pos);
        }
    }

    private void recalculateBonus(Level level, BlockPos pos) {
        RandomSource random = level.getRandom();
        HolderLookup.RegistryLookup<IAspect> aspects = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        boolean changed = false;
        if (level.getRawBrightness(pos.above(), 0) < 4 && !level.canSeeSky(pos.above()) && random.nextInt(20) == 0) {
            changed |= addBonus(aspects, TTAspects.PERDITIO);
        }
        int height = level.getHeight();
        for (float factor : new float[] {0.5F, 0.66F, 0.75F}) {
            if (pos.getY() > height * factor && random.nextInt(20) == 0) {
                changed |= addBonus(aspects, TTAspects.AER);
            }
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        scan:
        for (int x = -BONUS_SCAN_RADIUS; x <= BONUS_SCAN_RADIUS; x++) {
            for (int y = -BONUS_SCAN_RADIUS; y <= BONUS_SCAN_RADIUS; y++) {
                for (int z = -BONUS_SCAN_RADIUS; z <= BONUS_SCAN_RADIUS; z++) {
                    cursor.setWithOffset(pos, x, y, z);
                    if (!level.isLoaded(cursor)) {
                        continue;
                    }
                    ResourceKey<IAspect> match = bonusFor(level.getBlockState(cursor), random, aspects);
                    if (match != null && addBonus(aspects, match)) {
                        changed = true;
                        break scan;
                    }
                }
            }
        }
        if (changed) {
            setChanged();
            syncToClient();
        }
    }

    private @Nullable ResourceKey<IAspect> bonusFor(
            BlockState state, RandomSource random, HolderLookup.RegistryLookup<IAspect> aspects) {
        if ((state.is(Blocks.BOOKSHELF) && random.nextInt(BOOKSHELF_BONUS_CHANCE) == 0)
                || (state.is(TTBlocks.JAR_BRAIN.get()) && random.nextInt(BRAIN_JAR_BONUS_CHANCE) == 0)) {
            List<Holder.Reference<IAspect>> candidates = aspects.listElements().toList();
            return candidates.isEmpty()
                    ? null
                    : candidates.get(random.nextInt(candidates.size())).key();
        }
        if (state.is(TTBlocks.CRYSTAL_AER.get()) && random.nextInt(10) == 0) return TTAspects.AER;
        if (state.is(TTBlocks.CRYSTAL_IGNIS.get()) && random.nextInt(10) == 0) return TTAspects.IGNIS;
        if (state.is(TTBlocks.CRYSTAL_AQUA.get()) && random.nextInt(10) == 0) return TTAspects.AQUA;
        if (state.is(TTBlocks.CRYSTAL_TERRA.get()) && random.nextInt(10) == 0) return TTAspects.TERRA;
        if (state.is(TTBlocks.CRYSTAL_ORDO.get()) && random.nextInt(10) == 0) return TTAspects.ORDO;
        if (state.is(TTBlocks.CRYSTAL_PERDITIO.get()) && random.nextInt(10) == 0) return TTAspects.PERDITIO;
        if (state.is(BlockTags.DIRT) && random.nextInt(20) == 0) return TTAspects.TERRA;
        if (state.getFluidState().is(FluidTags.WATER) && random.nextInt(15) == 0) return TTAspects.AQUA;
        if ((state.getFluidState().is(FluidTags.LAVA) || state.is(Blocks.FIRE)) && random.nextInt(20) == 0) {
            return TTAspects.IGNIS;
        }
        if (state.is(TTBlockTags.RESEARCH_BONUS_ORDO) && random.nextInt(20) == 0) {
            return TTAspects.ORDO;
        }
        return null;
    }

    private boolean addBonus(HolderLookup.RegistryLookup<IAspect> aspects, ResourceKey<IAspect> key) {
        Holder<IAspect> holder = aspects.get(key).orElse(null);
        if (holder == null || bonusAspects.amountOf(holder) >= 1) {
            return false;
        }
        bonusAspects = bonusAspects.add(holder, 1);
        return true;
    }

    public void ensureNotePuzzle() {
        if (level == null || level.isClientSide()) {
            return;
        }
        ResearchNoteData data = noteData();
        if (data == null || data.complete() || !data.cells().isEmpty()) {
            return;
        }
        IResearchEntry entry = level.registryAccess()
                .lookupOrThrow(IResearchEntry.REGISTRY_KEY)
                .get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, data.entry()))
                .map(Holder.Reference::value)
                .orElse(null);
        if (entry == null) {
            return;
        }
        AspectList anchors = ResearchNotes.anchors(level.registryAccess(), entry);
        writeNoteData(
                NoteGenerator.generate(data.entry(), data.index(), anchors, entry.complexity(), level.getRandom()));
    }

    public @Nullable ResearchNoteData noteData() {
        ItemStack note = inventory.getStackInSlot(SLOT_NOTE).copyWithCount(1);
        return note.isEmpty() ? null : ResearchNotes.dataOf(note);
    }

    private void writeNoteData(ResearchNoteData data) {
        ItemStack resource = inventory.getStackInSlot(SLOT_NOTE);
        int amount = Math.max(1, inventory.getStackInSlot(SLOT_NOTE).getCount());
        ItemStack note = resource.copyWithCount(amount);
        note.set(TTDataComponents.RESEARCH_NOTE.get(), data);
        if (data.complete()) {
            note.set(TTDataComponents.NOTE_COMPLETE.get(), true);
        }
        inventory.setStackInSlot(SLOT_NOTE, note.copyWithCount(amount));
        setChanged();
        syncToClient();
    }

    public void placeAspect(ServerPlayer player, HexGrid.Hex hex, @Nullable Holder<IAspect> aspect) {
        ResearchNoteData data = noteData();
        if (data == null || data.complete() || !hasInkReady()) {
            return;
        }
        ResearchNoteData.Cell cell = data.cellAt(hex);
        if (cell == null || getLevel() == null) {
            return;
        }
        Level level = getLevel();
        RandomSource random = player.getRandom();
        if (aspect != null) {
            if (cell.type() != ResearchNoteData.TYPE_BLANK || !AspectPools.isDiscovered(player, aspect)) {
                return;
            }
            boolean mastery = KnowledgeAccess.of(player).isResearchComplete(RESEARCH_MASTERY);
            if (mastery && random.nextFloat() < MASTERY_FREE_CHANCE) {
                playOrb(level, random);
            } else if (AspectPools.amount(player, aspect) <= 0) {
                if (bonusAspects.amountOf(aspect) <= 0) {
                    return;
                }
                bonusAspects = bonusAspects.remove(aspect, 1);
            } else if (random.nextFloat() < aidSaveChance()) {
                playOrb(level, random);
            } else {
                AspectPools.spend(player, aspect, 1);
            }
            consumeInk();
            data = data.withCell(hex, ResearchNoteData.TYPE_PLACED, aspect);
            level.playSound(null, worldPosition, TTSounds.WRITE.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
        } else {
            if (cell.type() != ResearchNoteData.TYPE_PLACED) {
                return;
            }
            Holder<IAspect> erased = cell.aspectOrNull();
            boolean expertise = KnowledgeAccess.of(player).isResearchComplete(RESEARCH_EXPERTISE);
            boolean mastery = KnowledgeAccess.of(player).isResearchComplete(RESEARCH_MASTERY);
            float refundChance = mastery ? MASTERY_REFUND_CHANCE : expertise ? EXPERTISE_REFUND_CHANCE : 0.0F;
            if (erased != null && random.nextFloat() < refundChance) {
                AspectPools.refund(player, erased, 1);
            }
            consumeInk();
            data = data.withCell(hex, ResearchNoteData.TYPE_BLANK, null);
            level.playSound(
                    null,
                    worldPosition,
                    TTSounds.ERASE.get(),
                    SoundSource.BLOCKS,
                    0.2F,
                    1.0F + random.nextFloat() * 0.1F);
        }
        NoteRules.Completion completion = NoteRules.checkCompletion(data, a -> AspectPools.isDiscovered(player, a));
        if (completion.complete()) {
            data = data.withCells(completion.prunedCells()).asComplete();
            level.playSound(null, worldPosition, TTSounds.LEARN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        writeNoteData(data);
    }

    public void combineAspects(
            ServerPlayer player,
            Holder<IAspect> first,
            Holder<IAspect> second,
            boolean bonusFirst,
            boolean bonusSecond) {
        Holder<IAspect> result = combinationResult(player, first, second);
        if (result == null || !canPayCombination(player, first, bonusFirst, second, bonusSecond)) {
            return;
        }
        if (!consumeCombinationInput(player, first, bonusFirst)
                || !consumeCombinationInput(player, second, bonusSecond)) {
            return;
        }
        setChanged();
        syncToClient();
        if (getLevel() != null) {
            AspectPools.grant(player, result, 1);
            getLevel()
                    .playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.3F, 1.0F);
        }
    }

    private boolean canPayCombination(
            ServerPlayer player,
            Holder<IAspect> first,
            boolean bonusFirst,
            Holder<IAspect> second,
            boolean bonusSecond) {
        int needFirst = first.equals(second) && bonusFirst == bonusSecond ? 2 : 1;
        return available(player, first, bonusFirst) >= needFirst && available(player, second, bonusSecond) >= 1;
    }

    private int available(ServerPlayer player, Holder<IAspect> aspect, boolean fromBonus) {
        return fromBonus ? bonusAspects.amountOf(aspect) : AspectPools.amount(player, aspect);
    }

    private boolean consumeCombinationInput(ServerPlayer player, Holder<IAspect> aspect, boolean fromBonus) {
        if (fromBonus) {
            if (bonusAspects.amountOf(aspect) <= 0) {
                return false;
            }
            bonusAspects = bonusAspects.remove(aspect, 1);
            return true;
        }
        if (AspectPools.amount(player, aspect) > 0 && player.getRandom().nextFloat() < aidSaveChance()) {
            return true;
        }
        return AspectPools.spend(player, aspect, 1);
    }

    private float aidSaveChance() {
        Level level = getLevel();
        if (level == null) {
            return 0.0F;
        }
        Direction facing = getBlockState().getValue(BlockResearchTable.FACING);
        return Math.min(
                MAX_AID_SAVE_CHANCE,
                aidChanceAbove(level, worldPosition) + aidChanceAbove(level, worldPosition.relative(facing)));
    }

    private static float aidChanceAbove(Level level, BlockPos tablePos) {
        BlockPos top = tablePos.above();
        BlockState state = level.getBlockState(top);
        return state.getBlock() instanceof IResearchTableAid aid ? aid.aspectSaveChance(level, top, state) : 0.0F;
    }

    private @Nullable Holder<IAspect> combinationResult(
            ServerPlayer player, Holder<IAspect> first, Holder<IAspect> second) {
        return AspectCombinations.result(player.registryAccess(), first, second);
    }

    public void duplicateNote(ServerPlayer player) {
        if (!KnowledgeAccess.of(player).isResearchComplete(RESEARCH_DUPLICATION) || getLevel() == null) {
            return;
        }
        ResearchNoteData data = noteData();
        if (data == null || !data.complete()) {
            return;
        }
        AspectList cost = duplicationCost(player, data);
        if (cost == null) {
            return;
        }
        if (!ResearchNotes.consumeInk(player, true) || !hasPlayerItem(player, Items.PAPER)) {
            return;
        }
        if (!AspectPools.spendAll(player, cost)) {
            return;
        }
        ResearchNotes.consumeInk(player, false);
        consumePlayerItem(player, Items.PAPER);
        ItemStack copy = inventory.getStackInSlot(SLOT_NOTE).copyWithCount(1);
        copy.set(TTDataComponents.RESEARCH_NOTE.get(), data.withCopies(data.copies() + 1));
        writeNoteData(data.withCopies(data.copies() + 1));
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }
        getLevel().playSound(null, worldPosition, TTSounds.WRITE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
    }

    public @Nullable AspectList duplicationCost(Player player, ResearchNoteData data) {
        IResearchEntry entry = player.registryAccess()
                .lookupOrThrow(IResearchEntry.REGISTRY_KEY)
                .get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, data.entry()))
                .map(Holder.Reference::value)
                .orElse(null);
        if (entry == null) {
            return null;
        }
        AspectList cost = AspectList.EMPTY;
        for (AspectInstance instance : entry.noteAspects().entries()) {
            cost = cost.add(instance.aspect(), instance.amount() + data.copies());
        }
        return cost;
    }

    private static boolean hasPlayerItem(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(item)) {
                return true;
            }
        }
        return false;
    }

    private static void consumePlayerItem(Player player, Item item) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(item)) {
                stack.shrink(1);
                return;
            }
        }
    }

    private void playOrb(Level level, RandomSource random) {
        level.playSound(
                null,
                worldPosition,
                SoundEvents.EXPERIENCE_ORB_PICKUP,
                SoundSource.BLOCKS,
                0.2F,
                0.9F + random.nextFloat() * 0.2F);
    }

    public boolean consumeInk() {
        ItemStack tools = inventory.getStackInSlot(SLOT_SCRIBE_TOOLS).copyWithCount(1);
        if (tools.isEmpty()) return false;
        int damage = tools.getDamageValue();
        int max = tools.getMaxDamage();
        if (max <= 0 || damage >= max) return false;
        tools.setDamageValue(damage + 1);
        inventory.setStackInSlot(SLOT_SCRIBE_TOOLS, tools.copyWithCount(1));
        setChanged();
        return true;
    }

    public boolean hasInkReady() {
        ItemStack tools = inventory.getStackInSlot(SLOT_SCRIBE_TOOLS).copyWithCount(1);
        return !tools.isEmpty() && tools.isDamageableItem() && tools.getDamageValue() < tools.getMaxDamage();
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemStack resource = inventory.getStackInSlot(i);
            int amount = inventory.getStackInSlot(i).getCount();
            if (!resource.isEmpty() && amount > 0) {
                container.setItem(i, resource.copyWithCount(amount));
            }
        }
        Containers.dropContents(level, pos, container);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        inventory.deserializeNBT(registries, input.getCompound("inventory"));
        bonusAspects =
                TTNbt.read(input, "bonus_aspects", AspectList.CODEC, registries).orElse(AspectList.EMPTY);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put("inventory", inventory.serializeNBT(registries));
        TTNbt.store(output, "bonus_aspects", AspectList.CODEC, registries, bonusAspects);
    }

    private final class TableInventory extends ItemStackHandler {
        TableInventory() {
            super(NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY));
        }

        @Override
        protected void onContentsChanged(int index) {
            setChanged();
            syncToClient();
            if (index == SLOT_NOTE) {
                ensureNotePuzzle();
            }
        }

        @Override
        public boolean isItemValid(int index, ItemStack resource) {
            return switch (index) {
                case SLOT_SCRIBE_TOOLS -> resource.is(TTItemTags.SCRIBING_TOOLS);
                case SLOT_NOTE -> resource.has(TTDataComponents.RESEARCH_NOTE.get());
                default -> false;
            };
        }
    }
}
