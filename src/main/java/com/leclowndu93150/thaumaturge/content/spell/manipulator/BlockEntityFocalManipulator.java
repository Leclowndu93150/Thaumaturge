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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;

public final class BlockEntityFocalManipulator extends AbstractSyncedBlockEntity implements MenuProvider {
    public static final int SLOT_FOCUS = 0;
    public static final int MAX_NAME_LENGTH = 50;

    private static final String DRAFT_KEY = "Draft";
    private static final String NAME_KEY = "Name";
    private static final String VIS_KEY = "Vis";
    private static final String VIS_TOTAL_KEY = "VisTotal";
    private static final String CRYSTALS_KEY = "Crystals";
    private static final int SLOT_COUNT = 1;
    private static final int COMPLEXITY_VIS = 10;
    private static final float TIER_VIS_DIVISOR = 5.0F;
    private static final int WORK_INTERVAL = 20;
    private static final float VIS_PER_SECOND = 20.0F;
    private static final float MIN_ANCHOR_REQUEST = 1.0F;
    private static final float DRAIN_EPSILON = 0.01F;
    private static final int CHARGER_RADIUS_CHUNKS = 1;
    private static final double SPARKLE_SPREAD = 2.0;
    private static final double SPARKLE_RISE = 2.0;
    private static final double BLOCK_CENTRE = 0.5;
    private static final double DROP_HEIGHT = 1.0;
    private static final float START_VOLUME = 1.0F;
    private static final float DONE_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final float ABORT_VOLUME = 0.5F;
    private static final int SHIMMER_DELAY = 0;
    private static final int SHIMMER_PER_TICK = 2;
    private static final double SHIMMER_HEIGHT = 1.0;
    private static final double SHIMMER_SPREAD = 0.18;
    private static final double SHIMMER_DRIFT = 0.004;
    private static final int SHIMMER_COLOR = ARGB.colorFromFloat(1.0F, 0.78F, 0.88F, 1.0F);
    private static final float SHIMMER_ALPHA = 0.7F;
    private static final float SHIMMER_SCALE_BASE = 0.08F;
    private static final float SHIMMER_SCALE_RANGE = 0.06F;
    private static final int SHIMMER_AGE_BASE = 6;
    private static final int SHIMMER_AGE_RANGE = 8;

    private final FocusSlot items = new FocusSlot();
    private Spell draft = Spell.empty();
    private String name = "";
    private float vis;
    private float visTotal;
    private AspectList crystals = AspectList.EMPTY;
    private int pulse;

    public BlockEntityFocalManipulator(BlockPos pos, BlockState state) {
        super(TTBlockEntities.FOCAL_MANIPULATOR.get(), pos, state);
    }

    public ItemStacksResourceHandler items() {
        return items;
    }

    public ItemStack focusStack() {
        ItemResource resource = items.getResource(SLOT_FOCUS);
        int amount = items.getAmountAsInt(SLOT_FOCUS);
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

    public void acceptDraft(Spell spell, String text) {
        if (inscribing()) {
            return;
        }
        draft = spell;
        name = StringUtil.filterText(text).strip();
        setChangedAndSync();
    }

    public boolean startInscribing(Player player) {
        Level world = level;
        ItemStack focus = focusStack();
        Optional<FocusTier> tier = Spells.tierOf(focus);
        if (world == null || inscribing() || tier.isEmpty()) {
            return false;
        }
        SpellSummary summary = Spells.analyze(draft, tier.get(), world.registryAccess(), player);
        AspectList cost = FocusItems.aspects(summary, world.registryAccess());
        if (cost.isEmpty() || !InscriptionCheck.of(player, true, false, summary, cost).ready()
                || NeoForge.EVENT_BUS.post(new SpellInscribeEvent(player, worldPosition, focus, draft, summary)).isCanceled()) {
            return false;
        }
        if (!player.getAbilities().instabuild) {
            player.giveExperienceLevels(-summary.xp());
            for (AspectInstance instance : cost.entries()) {
                take(player, EssentiaCrystalFactory.of(instance.aspect(), instance.amount()));
            }
        }
        visTotal = summary.complexity() * COMPLEXITY_VIS + tier.get().complexity() / TIER_VIS_DIVISOR;
        vis = visTotal;
        crystals = cost;
        play(world, TTSounds.CRAFTSTART.get(), START_VOLUME);
        setChangedAndSync();
        return true;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityFocalManipulator table) {
        if (!(level instanceof ServerLevel server) || ++table.pulse < WORK_INTERVAL) {
            return;
        }
        table.pulse = 0;
        if (table.inscribing()) {
            table.work(server, pos);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityFocalManipulator table) {
        if (!table.inscribing() || table.focusStack().isEmpty()) {
            return;
        }
        RandomSource random = level.getRandom();
        for (int i = 0; i < SHIMMER_PER_TICK; i++) {
            double x = pos.getX() + BLOCK_CENTRE + random.nextGaussian() * SHIMMER_SPREAD;
            double y = pos.getY() + SHIMMER_HEIGHT + random.nextGaussian() * SHIMMER_SPREAD;
            double z = pos.getZ() + BLOCK_CENTRE + random.nextGaussian() * SHIMMER_SPREAD;
            float scale = SHIMMER_SCALE_BASE + random.nextFloat() * SHIMMER_SCALE_RANGE;
            int age = SHIMMER_AGE_BASE + random.nextInt(SHIMMER_AGE_RANGE);
            ShieldSparkParticleOptions spark = new ShieldSparkParticleOptions(SHIMMER_COLOR, SHIMMER_ALPHA, scale, age, SHIMMER_DELAY, true);
            level.addParticle(spark, x, y, z, random.nextGaussian() * SHIMMER_DRIFT, random.nextGaussian() * SHIMMER_DRIFT, random.nextGaussian() * SHIMMER_DRIFT);
        }
    }

    private void work(ServerLevel server, BlockPos pos) {
        if (focusStack().isEmpty()) {
            clearInscription();
            play(server, TTSounds.WANDFAIL.get(), ABORT_VOLUME);
            setChangedAndSync();
            return;
        }
        float drawn = drawAura(server, Math.min(VIS_PER_SECOND, vis));
        if (drawn > 0.0F) {
            vis -= drawn;
            RandomSource random = server.getRandom();
            Vec3 from = new Vec3(pos.getX() + BLOCK_CENTRE + (random.nextDouble() * 2.0 - 1.0) * SPARKLE_SPREAD, pos.getY() + random.nextDouble() * SPARKLE_RISE,
                    pos.getZ() + BLOCK_CENTRE + (random.nextDouble() * 2.0 - 1.0) * SPARKLE_SPREAD);
            EffectDispatch.spawnVisSparkle(server, from, new Vec3(pos.getX() + BLOCK_CENTRE, pos.getY() + DROP_HEIGHT, pos.getZ() + BLOCK_CENTRE));
            setChangedAndSync();
        }
        if (vis <= 0.0F) {
            complete(server);
        }
    }

    private float drawAura(ServerLevel server, float wanted) {
        List<BlockPos> anchors = anchors(server);
        if (anchors.isEmpty()) {
            return 0.0F;
        }
        float share = Math.max(MIN_ANCHOR_REQUEST, wanted / anchors.size());
        float owed = wanted;
        boolean progressed = true;
        while (owed > DRAIN_EPSILON && progressed) {
            progressed = false;
            for (BlockPos anchor : anchors) {
                if (owed <= DRAIN_EPSILON) {
                    break;
                }
                float drained = AuraHelper.drainVis(server, anchor, Math.min(owed, share), false);
                owed -= drained;
                progressed |= drained > DRAIN_EPSILON;
            }
        }
        return wanted - owed;
    }

    private List<BlockPos> anchors(ServerLevel server) {
        List<BlockPos> anchors = new ArrayList<>();
        if (server.getBlockState(worldPosition.above()).is(TTBlocks.ARCANE_WORKBENCH_CHARGER.get())) {
            int y = worldPosition.getY();
            ChunkPos.rangeClosed(ChunkPos.containing(worldPosition), CHARGER_RADIUS_CHUNKS).forEach(chunk -> anchors.add(chunk.getMiddleBlockPosition(y)));
        } else {
            anchors.add(worldPosition);
        }
        anchors.removeIf(anchor -> !server.hasChunkAt(anchor));
        anchors.sort(Comparator.comparingLong(BlockPos::asLong));
        return anchors;
    }

    private void complete(ServerLevel server) {
        ItemStack focus = focusStack();
        Spell keptDraft = draft;
        String keptName = name;
        Spells.setSpell(focus, draft);
        if (name.isBlank()) {
            focus.remove(DataComponents.CUSTOM_NAME);
        } else {
            focus.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        }
        clearInscription();
        items.set(SLOT_FOCUS, ItemResource.of(focus), focus.getCount());
        draft = keptDraft;
        name = keptName;
        play(server, TTSounds.WAND.get(), DONE_VOLUME);
        setChangedAndSync();
    }

    private void clearInscription() {
        vis = 0.0F;
        visTotal = 0.0F;
        crystals = AspectList.EMPTY;
    }

    private void play(Level world, SoundEvent sound, float volume) {
        world.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, SOUND_PITCH);
    }

    private static void take(Player player, ItemStack wanted) {
        int owed = wanted.getCount();
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize() && owed > 0; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                int removed = Math.min(owed, stack.getCount());
                stack.shrink(removed);
                owed -= removed;
            }
        }
    }

    private void onFocusChanged(ItemStack previous) {
        ItemStack current = focusStack();
        if (!ItemStack.isSameItemSameComponents(previous, current)) {
            clearInscription();
            Spell carried = current.isEmpty() ? null : Spells.spellOf(current);
            if (carried != null) {
                Component custom = current.get(DataComponents.CUSTOM_NAME);
                draft = carried;
                name = custom != null ? custom.getString() : "";
            }
        }
        if (level != null && !level.isClientSide()) {
            setChangedAndSync();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        ItemStack focus = focusStack();
        if (level instanceof ServerLevel server && !focus.isEmpty()) {
            Containers.dropItemStack(server, pos.getX() + BLOCK_CENTRE, pos.getY() + DROP_HEIGHT, pos.getZ() + BLOCK_CENTRE, focus);
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new MenuFocalManipulator(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        items.serialize(output);
        output.store(DRAFT_KEY, Spell.CODEC, draft);
        output.putString(NAME_KEY, name);
        output.putFloat(VIS_KEY, vis);
        output.putFloat(VIS_TOTAL_KEY, visTotal);
        output.store(CRYSTALS_KEY, AspectList.CODEC, crystals);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items.deserialize(input);
        draft = input.read(DRAFT_KEY, Spell.CODEC).orElse(Spell.empty());
        name = input.getStringOr(NAME_KEY, "");
        vis = input.getFloatOr(VIS_KEY, 0.0F);
        visTotal = input.getFloatOr(VIS_TOTAL_KEY, 0.0F);
        crystals = input.read(CRYSTALS_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
    }

    private final class FocusSlot extends ItemStacksResourceHandler {
        FocusSlot() {
            super(SLOT_COUNT);
        }

        @Override
        public boolean isValid(int index, ItemResource resource) {
            return FocusItems.isFocus(resource.toStack(1));
        }

        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            onFocusChanged(previousContents);
        }
    }
}
