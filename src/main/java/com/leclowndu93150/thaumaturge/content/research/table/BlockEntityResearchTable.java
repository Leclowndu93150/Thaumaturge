package com.leclowndu93150.thaumaturge.content.research.table;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.items.IScribeTools;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.content.aspect.AspectCombinations;
import com.leclowndu93150.thaumaturge.content.research.note.HexGrid;
import com.leclowndu93150.thaumaturge.content.research.note.NoteGenerator;
import com.leclowndu93150.thaumaturge.content.research.note.NoteRules;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNotes;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ProblemReporter;
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
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jspecify.annotations.Nullable;

public final class BlockEntityResearchTable extends BlockEntity implements MenuProvider {
    public static final int SLOT_SCRIBE_TOOLS = 0;
    public static final int SLOT_NOTE = 1;
    public static final int SLOT_COUNT = 2;

    public static final Identifier RESEARCH_EXPERTISE = TCIds.rl("research_expertise");
    public static final Identifier RESEARCH_MASTERY = TCIds.rl("research_mastery");
    public static final Identifier RESEARCH_DUPLICATION = TCIds.rl("research_duplication");

    private static final int RECALC_INTERVAL_TICKS = 600;
    private static final int BONUS_SCAN_RADIUS = 8;
    private static final float EXPERTISE_REFUND_CHANCE = 0.25F;
    private static final float MASTERY_REFUND_CHANCE = 0.5F;
    private static final float MASTERY_FREE_CHANCE = 0.1F;

    private static final Component TITLE = Component.translatable("gui.thaumaturge.research_table.title");

    private final TableInventory inventory = new TableInventory();
    private AspectList bonusAspects = AspectList.EMPTY;
    private int recalcCounter;

    public BlockEntityResearchTable(BlockPos pos, BlockState state) {
        super(TCBlockEntities.RESEARCH_TABLE.get(), pos, state);
    }

    public ItemStacksResourceHandler items() {
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
            changed |= addBonus(aspects, TCAspects.PERDITIO);
        }
        int height = level.getHeight();
        for (float factor : new float[]{0.5F, 0.66F, 0.75F}) {
            if (pos.getY() > height * factor && random.nextInt(20) == 0) {
                changed |= addBonus(aspects, TCAspects.AER);
            }
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        scan : for (int x = -BONUS_SCAN_RADIUS; x <= BONUS_SCAN_RADIUS; x++) {
            for (int y = -BONUS_SCAN_RADIUS; y <= BONUS_SCAN_RADIUS; y++) {
                for (int z = -BONUS_SCAN_RADIUS; z <= BONUS_SCAN_RADIUS; z++) {
                    cursor.setWithOffset(pos, x, y, z);
                    if (!level.isLoaded(cursor)) {
                        continue;
                    }
                    ResourceKey<IAspect> match = bonusFor(level.getBlockState(cursor), random);
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

    private @Nullable ResourceKey<IAspect> bonusFor(BlockState state, RandomSource random) {
        if (state.is(TCBlocks.CRYSTAL_AER.get()) && random.nextInt(10) == 0)
            return TCAspects.AER;
        if (state.is(TCBlocks.CRYSTAL_IGNIS.get()) && random.nextInt(10) == 0)
            return TCAspects.IGNIS;
        if (state.is(TCBlocks.CRYSTAL_AQUA.get()) && random.nextInt(10) == 0)
            return TCAspects.AQUA;
        if (state.is(TCBlocks.CRYSTAL_TERRA.get()) && random.nextInt(10) == 0)
            return TCAspects.TERRA;
        if (state.is(TCBlocks.CRYSTAL_ORDO.get()) && random.nextInt(10) == 0)
            return TCAspects.ORDO;
        if (state.is(TCBlocks.CRYSTAL_PERDITIO.get()) && random.nextInt(10) == 0)
            return TCAspects.PERDITIO;
        if (state.is(BlockTags.DIRT) && random.nextInt(20) == 0)
            return TCAspects.TERRA;
        if (state.getFluidState().is(FluidTags.WATER) && random.nextInt(15) == 0)
            return TCAspects.AQUA;
        if ((state.getFluidState().is(FluidTags.LAVA) || state.is(Blocks.FIRE)) && random.nextInt(20) == 0) {
            return TCAspects.IGNIS;
        }
        if ((state.getBlock() instanceof RedStoneWireBlock || state.getBlock() instanceof PistonBaseBlock) && random.nextInt(20) == 0) {
            return TCAspects.ORDO;
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
        IResearchEntry entry = level.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, data.entry())).map(Holder.Reference::value)
                .orElse(null);
        if (entry == null) {
            return;
        }
        AspectList anchors = ResearchNotes.anchors(level.registryAccess(), entry);
        writeNoteData(NoteGenerator.generate(data.entry(), data.index(), anchors, entry.complexity(), level.getRandom()));
    }

    public @Nullable ResearchNoteData noteData() {
        ItemStack note = inventory.getResource(SLOT_NOTE).toStack(1);
        return note.isEmpty() ? null : ResearchNotes.dataOf(note);
    }

    private void writeNoteData(ResearchNoteData data) {
        ItemResource resource = inventory.getResource(SLOT_NOTE);
        int amount = Math.max(1, inventory.getAmountAsInt(SLOT_NOTE));
        ItemStack note = resource.toStack(amount);
        note.set(TCDataComponents.RESEARCH_NOTE.get(), data);
        if (data.complete()) {
            note.set(TCDataComponents.NOTE_COMPLETE.get(), true);
        }
        inventory.set(SLOT_NOTE, ItemResource.of(note), amount);
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
            } else {
                AspectPools.spend(player, aspect, 1);
            }
            consumeInk();
            data = data.withCell(hex, ResearchNoteData.TYPE_PLACED, aspect);
            level.playSound(null, worldPosition, TCSounds.WRITE.get(), SoundSource.BLOCKS, 0.2F, 1.0F);
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
            level.playSound(null, worldPosition, TCSounds.ERASE.get(), SoundSource.BLOCKS, 0.2F, 1.0F + random.nextFloat() * 0.1F);
        }
        NoteRules.Completion completion = NoteRules.checkCompletion(data, a -> AspectPools.isDiscovered(player, a));
        if (completion.complete()) {
            data = data.withCells(completion.prunedCells()).asComplete();
            level.playSound(null, worldPosition, TCSounds.LEARN.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        writeNoteData(data);
    }

    public void combineAspects(ServerPlayer player, Holder<IAspect> first, Holder<IAspect> second, boolean bonusFirst, boolean bonusSecond) {
        Holder<IAspect> result = combinationResult(player, first, second);
        if (!consumeCombinationInput(player, first, bonusFirst)) {
            return;
        }
        if (!consumeCombinationInput(player, second, bonusSecond)) {
            return;
        }
        setChanged();
        syncToClient();
        if (result != null && getLevel() != null) {
            AspectPools.grant(player, result, 1);
            getLevel().playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.3F, 1.0F);
        }
    }

    private boolean consumeCombinationInput(ServerPlayer player, Holder<IAspect> aspect, boolean fromBonus) {
        if (fromBonus) {
            if (bonusAspects.amountOf(aspect) <= 0) {
                return false;
            }
            bonusAspects = bonusAspects.remove(aspect, 1);
            return true;
        }
        return AspectPools.spend(player, aspect, 1);
    }

    private @Nullable Holder<IAspect> combinationResult(ServerPlayer player, Holder<IAspect> first, Holder<IAspect> second) {
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
        ItemStack copy = inventory.getResource(SLOT_NOTE).toStack(1);
        copy.set(TCDataComponents.RESEARCH_NOTE.get(), data.withCopies(data.copies() + 1));
        writeNoteData(data.withCopies(data.copies() + 1));
        if (!player.getInventory().add(copy)) {
            player.drop(copy, false);
        }
        getLevel().playSound(null, worldPosition, TCSounds.WRITE.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
    }

    public @Nullable AspectList duplicationCost(Player player, ResearchNoteData data) {
        IResearchEntry entry = player.registryAccess().lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(ResourceKey.create(IResearchEntry.REGISTRY_KEY, data.entry())).map(Holder.Reference::value)
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
        level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 0.2F, 0.9F + random.nextFloat() * 0.2F);
    }

    public boolean consumeInk() {
        ItemStack tools = inventory.getResource(SLOT_SCRIBE_TOOLS).toStack(1);
        if (tools.isEmpty())
            return false;
        int damage = tools.getDamageValue();
        int max = tools.getMaxDamage();
        if (max <= 0 || damage >= max)
            return false;
        tools.setDamageValue(damage + 1);
        inventory.set(SLOT_SCRIBE_TOOLS, ItemResource.of(tools), 1);
        setChanged();
        return true;
    }

    public boolean hasInkReady() {
        ItemStack tools = inventory.getResource(SLOT_SCRIBE_TOOLS).toStack(1);
        return !tools.isEmpty() && tools.isDamageableItem() && tools.getDamageValue() < tools.getMaxDamage();
    }

    public void dropContents(Level level, BlockPos pos) {
        SimpleContainer container = new SimpleContainer(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            ItemResource resource = inventory.getResource(i);
            int amount = inventory.getAmountAsInt(i);
            if (!resource.isEmpty() && amount > 0) {
                container.setItem(i, resource.toStack(amount));
            }
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        inventory.deserialize(input);
        bonusAspects = input.read("bonus_aspects", AspectList.CODEC).orElse(AspectList.EMPTY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        inventory.serialize(output);
        output.store("bonus_aspects", AspectList.CODEC, bonusAspects);
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

    private final class TableInventory extends ItemStacksResourceHandler {
        TableInventory() {
            super(NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY));
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();
            syncToClient();
            if (index == SLOT_NOTE) {
                ensureNotePuzzle();
            }
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return switch (index) {
                case SLOT_SCRIBE_TOOLS -> resource.toStack(1).getItem() instanceof IScribeTools;
                case SLOT_NOTE -> resource.toStack(1).has(TCDataComponents.RESEARCH_NOTE.get());
                default -> false;
            };
        }
    }
}
