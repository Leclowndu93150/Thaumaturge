package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaPull;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaConduit;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityPotionSprayer extends AbstractSyncedBlockEntity implements IEssentiaConduit {
    public static final int MAX_CHARGES = 8;

    private static final int CHARGE_INTERVAL = 5;
    private static final int SUCTION = 128;
    private static final int PULL_AMOUNT = 1;
    private static final int NO_POTION_COLOR = 0x3A3A3A;
    private static final int SPRAY_DISTANCE = 2;
    private static final double SPRAY_BOX_SIZE = 3.0;
    private static final float INSTANT_EFFECT_SCALE = 1.0F;
    private static final float FIZZ_VOLUME = 0.25F;
    private static final float FIZZ_PITCH_BASE = 2.6F;
    private static final float FIZZ_PITCH_SPREAD = 0.8F;
    private static final int MIST_DURATION = 15;
    private static final int MIST_PUFF_DIVISOR = 2;
    private static final double MIST_FACE_OFFSET = 0.5;
    private static final double MIST_SPEED = 0.25;
    private static final double MIST_SPREAD = 0.06;
    private static final double MIST_JITTER = 0.1;
    private static final float MIST_SCALE = 4.0F;
    private static final String POTION_KEY = "Potion";
    private static final String RECIPE_KEY = "Recipe";
    private static final String PROGRESS_KEY = "Progress";
    private static final String CHARGES_KEY = "Charges";
    private static final String COLOR_KEY = "Color";
    private static final Direction[] DIRECTIONS = Direction.values();

    private ItemStack potion = ItemStack.EMPTY;
    private AspectList needed = AspectList.EMPTY;
    private AspectList stored = AspectList.EMPTY;
    private int charges;
    private int color = NO_POTION_COLOR;
    private EssentiaPull wanted = EssentiaPull.NONE;
    private boolean powered;
    private boolean redstoneKnown;
    private int ticks;
    private int mistLeft;

    public BlockEntityPotionSprayer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.POTION_SPRAYER.get(), pos, state);
    }

    public static boolean holdsPotionItem(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof PotionItem;
    }

    public ItemStack getPotion() {
        return potion;
    }

    public void setPotion(ItemStack stack) {
        if (stack == potion && !stack.isEmpty()) {
            setChangedAndSync();
            return;
        }
        potion = stack;
        charges = 0;
        stored = AspectList.EMPTY;
        needed = level instanceof ServerLevel serverLevel && !potion.isEmpty()
                ? PotionAspects.of(serverLevel, potion)
                : AspectList.EMPTY;
        color = potion.isEmpty() ? NO_POTION_COLOR : contents().getColor();
        setChangedAndSync();
    }

    public AspectList essentiaNeeded() {
        return needed;
    }

    public AspectList essentiaStored() {
        return stored;
    }

    public int sprayShots() {
        return charges;
    }

    public int color() {
        return color;
    }

    private PotionContents contents() {
        return potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
    }

    private Direction front() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    public void tick(Level tickLevel, BlockPos pos, BlockState state) {
        if (!(tickLevel instanceof ServerLevel serverLevel)) {
            return;
        }
        watchRedstone(serverLevel, state);
        if (mistLeft > 0) {
            puffMist(serverLevel, state.getValue(BlockStateProperties.FACING));
        }
        if (++ticks % CHARGE_INTERVAL == 0) {
            charge(serverLevel);
        }
    }

    private void watchRedstone(ServerLevel serverLevel, BlockState state) {
        boolean now = !state.getValue(BlockStateProperties.ENABLED);
        boolean risen = redstoneKnown && now && !powered;
        powered = now;
        redstoneKnown = true;
        if (risen) {
            spray(serverLevel, state.getValue(BlockStateProperties.FACING));
        }
    }

    private void charge(ServerLevel serverLevel) {
        if (potion.isEmpty() || charges >= MAX_CHARGES) {
            dropSuction();
            return;
        }
        if (needed.isEmpty()) {
            needed = PotionAspects.of(serverLevel, potion);
            setChangedAndSync();
        }
        AspectInstance shortfall = firstShortfall();
        if (shortfall == null) {
            charges++;
            stored = AspectList.EMPTY;
            dropSuction();
            setChangedAndSync();
            return;
        }
        wanted = EssentiaPull.of(shortfall.aspect(), SUCTION);
        drawAspect(serverLevel, shortfall.aspect());
    }

    private @Nullable AspectInstance firstShortfall() {
        for (AspectInstance entry : needed.entries()) {
            if (stored.amountOf(entry.aspect()) < entry.amount()) {
                return entry;
            }
        }
        return null;
    }

    private int shortBy(Holder<IAspect> aspect) {
        return Math.max(0, needed.amountOf(aspect) - stored.amountOf(aspect));
    }

    private void dropSuction() {
        wanted = EssentiaPull.NONE;
    }

    private void drawAspect(ServerLevel serverLevel, Holder<IAspect> aspect) {
        Direction front = front();
        for (Direction side : DIRECTIONS) {
            if (side != front && pullFrom(serverLevel, worldPosition.relative(side), side.getOpposite(), aspect)) {
                return;
            }
        }
        BlockPos above = worldPosition.above();
        for (Direction side : DIRECTIONS) {
            if (side != Direction.DOWN
                    && side != front
                    && pullFrom(serverLevel, above.relative(side), side.getOpposite(), aspect)) {
                return;
            }
        }
    }

    private boolean pullFrom(ServerLevel serverLevel, BlockPos sourcePos, Direction face, Holder<IAspect> aspect) {
        if (!LoadedChunks.contains(serverLevel, sourcePos)) {
            return false;
        }
        IEssentiaConduit source = EssentiaAccess.conduit(serverLevel, sourcePos, face);
        if (source == null
                || !source.releasesOn(face)
                || source.heldAmount(face) <= 0
                || !aspect.equals(source.heldAspect(face))) {
            return false;
        }
        if (source.pull(face).strength() >= SUCTION || SUCTION < source.pullThreshold()) {
            return false;
        }
        int taken = source.extract(aspect, PULL_AMOUNT, face, false);
        if (taken <= 0) {
            return false;
        }
        stored = stored.add(aspect, taken);
        setChangedAndSync();
        return true;
    }

    private void spray(ServerLevel serverLevel, Direction facing) {
        if (charges <= 0) {
            return;
        }
        charges--;
        setChangedAndSync();
        Vec3 center = LevelSpaces.toWorld(serverLevel, Vec3.atCenterOf(worldPosition.relative(facing, SPRAY_DISTANCE)));
        AABB area = AABB.ofSize(center, SPRAY_BOX_SIZE, SPRAY_BOX_SIZE, SPRAY_BOX_SIZE);
        PotionContents effects = contents();
        for (LivingEntity target :
                serverLevel.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAffectedByPotions)) {
            effects.forEachEffect(effect -> affect(serverLevel, target, effect));
        }
        RandomSource random = serverLevel.getRandom();
        float pitch = FIZZ_PITCH_BASE + (random.nextFloat() - random.nextFloat()) * FIZZ_PITCH_SPREAD;
        serverLevel.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, FIZZ_VOLUME, pitch);
        mistLeft = MIST_DURATION;
    }

    private static void affect(ServerLevel serverLevel, LivingEntity target, MobEffectInstance effect) {
        MobEffect kind = effect.getEffect().value();
        if (kind.isInstantenous()) {
            kind.applyInstantenousEffect(null, null, target, effect.getAmplifier(), INSTANT_EFFECT_SCALE);
        } else {
            target.addEffect(new MobEffectInstance(effect));
        }
    }

    private void puffMist(ServerLevel serverLevel, Direction facing) {
        RandomSource random = serverLevel.getRandom();
        mistLeft--;
        Vec3 out = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vec3 mouth = Vec3.atCenterOf(worldPosition).add(out.scale(MIST_FACE_OFFSET));
        int puffs = mistLeft / MIST_PUFF_DIVISOR;
        for (int puff = 0; puff < puffs; puff++) {
            Vec3 at = mouth.add(jitter(random), jitter(random), jitter(random));
            Vec3 motion = out.scale(MIST_SPEED)
                    .add(
                            random.nextGaussian() * MIST_SPREAD,
                            random.nextGaussian() * MIST_SPREAD,
                            random.nextGaussian() * MIST_SPREAD);
            Effects.vent(serverLevel, at)
                    .motion(motion.x, motion.y, motion.z)
                    .color(color)
                    .scale(MIST_SCALE)
                    .send();
        }
    }

    private static double jitter(RandomSource random) {
        return (random.nextDouble() * 2.0 - 1.0) * MIST_JITTER;
    }

    @Override
    public int insert(Holder<IAspect> type, int count, Direction side, boolean simulate) {
        if (!receivesOn(side) || potion.isEmpty() || charges >= MAX_CHARGES || count <= 0) {
            return 0;
        }
        int accepted = Math.min(count, shortBy(type));
        if (accepted > 0 && !simulate) {
            stored = stored.add(type, accepted);
            setChangedAndSync();
        }
        return accepted;
    }

    @Override
    public int roomFor(Holder<IAspect> type, Direction side) {
        return receivesOn(side) && !potion.isEmpty() && charges < MAX_CHARGES ? shortBy(type) : 0;
    }

    @Override
    public int extract(Holder<IAspect> type, int count, Direction side, boolean simulate) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> heldAspect(@Nullable Direction side) {
        return null;
    }

    @Override
    public int heldAmount(@Nullable Direction side) {
        return 0;
    }

    @Override
    public EssentiaPull pull(@Nullable Direction side) {
        return side == front() ? EssentiaPull.NONE : wanted;
    }

    @Override
    public int pullThreshold() {
        return 0;
    }

    @Override
    public boolean releasesOn(Direction side) {
        return false;
    }

    @Override
    public boolean receivesOn(Direction side) {
        return side != front();
    }

    @Override
    public boolean connectsOn(Direction side) {
        return side != front();
    }

    void dropPotionOnBreak(BlockPos pos) {
        if (level != null && !level.isClientSide() && !potion.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), potion);
            potion = ItemStack.EMPTY;
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        potion = TTNbt.read(input, POTION_KEY, ItemStack.OPTIONAL_CODEC, registries)
                .orElse(ItemStack.EMPTY);
        needed = TTNbt.read(input, RECIPE_KEY, AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        stored = TTNbt.read(input, PROGRESS_KEY, AspectList.CODEC, registries).orElse(AspectList.EMPTY);
        charges = Math.clamp(input.getInt(CHARGES_KEY), 0, MAX_CHARGES);
        color = input.contains(COLOR_KEY) ? input.getInt(COLOR_KEY) : NO_POTION_COLOR;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (!potion.isEmpty()) TTNbt.store(output, POTION_KEY, ItemStack.CODEC, registries, potion);
        TTNbt.store(output, RECIPE_KEY, AspectList.CODEC, registries, needed);
        TTNbt.store(output, PROGRESS_KEY, AspectList.CODEC, registries, stored);
        output.putInt(CHARGES_KEY, charges);
        output.putInt(COLOR_KEY, color);
    }
}
