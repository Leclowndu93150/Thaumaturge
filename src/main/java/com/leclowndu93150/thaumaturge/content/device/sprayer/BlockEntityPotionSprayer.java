package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityPotionSprayer extends AbstractSyncedBlockEntity implements IEssentiaTransport {
    public static final int MAX_CHARGES = 8;

    private static final int CHARGE_INTERVAL = 5;
    private static final int SUCTION = 128;
    private static final int PULL_AMOUNT = 1;
    private static final int NO_POTION_COLOR = 0x3A3A3A;
    private static final int SPRAY_DISTANCE = 2;
    private static final double SPRAY_BOX_SIZE = 3.0;
    private static final float INSTANT_EFFECT_SCALE = 1.0F;
    private static final float DURATION_SCALE = 1.0F;
    private static final float FIZZ_VOLUME = 0.25F;
    private static final float FIZZ_PITCH = 2.0F;
    private static final int MIST_DURATION = 15;
    private static final int MIST_PEAK_PUFFS = 6;
    private static final double MIST_FACE_OFFSET = 0.55;
    private static final double MIST_SPEED = 0.12;
    private static final double MIST_SPREAD = 0.05;
    private static final double MIST_PLANE_JITTER = 0.25;
    private static final float MIST_SCALE = 1.2F;
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
    private @Nullable Holder<IAspect> suctionType;
    private int suction;
    private boolean powered;
    private boolean redstoneKnown;
    private int ticks;
    private int mistLeft;

    public BlockEntityPotionSprayer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.POTION_SPRAYER.get(), pos, state);
    }

    public static boolean holdsPotionItem(ItemStack stack) {
        return stack.is(Items.POTION) || stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION);
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
        needed = level instanceof ServerLevel serverLevel && !potion.isEmpty() ? PotionAspects.of(serverLevel, potion) : AspectList.EMPTY;
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
        setSuction(shortfall.aspect(), SUCTION);
        pull(serverLevel, shortfall.aspect());
    }

    private @Nullable AspectInstance firstShortfall() {
        for (AspectInstance entry : needed.sortedByTag()) {
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
        suctionType = null;
        suction = 0;
    }

    private void pull(ServerLevel serverLevel, Holder<IAspect> aspect) {
        Direction front = front();
        for (Direction side : DIRECTIONS) {
            if (side != front && pullFrom(serverLevel, worldPosition.relative(side), side.getOpposite(), aspect)) {
                return;
            }
        }
        BlockPos above = worldPosition.above();
        for (Direction side : DIRECTIONS) {
            if (side != Direction.DOWN && pullFrom(serverLevel, above.relative(side), side.getOpposite(), aspect)) {
                return;
            }
        }
    }

    private boolean pullFrom(ServerLevel serverLevel, BlockPos sourcePos, Direction face, Holder<IAspect> aspect) {
        if (!serverLevel.hasChunkAt(sourcePos)) {
            return false;
        }
        IEssentiaTransport source = EssentiaAccess.transport(serverLevel, sourcePos, face);
        if (source == null || !source.canOutputTo(face) || source.getEssentiaAmount(face) <= 0 || !aspect.equals(source.getEssentiaType(face))) {
            return false;
        }
        if (source.getSuctionAmount(face) >= SUCTION || SUCTION < source.getMinimumSuction()) {
            return false;
        }
        int taken = source.takeEssentia(aspect, PULL_AMOUNT, face);
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
        Vec3 center = Vec3.atCenterOf(worldPosition.relative(facing, SPRAY_DISTANCE));
        AABB area = AABB.ofSize(center, SPRAY_BOX_SIZE, SPRAY_BOX_SIZE, SPRAY_BOX_SIZE);
        PotionContents effects = contents();
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, area, LivingEntity::isAffectedByPotions)) {
            effects.forEachEffect(effect -> affect(serverLevel, target, effect), DURATION_SCALE);
        }
        serverLevel.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, FIZZ_VOLUME, FIZZ_PITCH);
        mistLeft = MIST_DURATION;
    }

    private static void affect(ServerLevel serverLevel, LivingEntity target, MobEffectInstance effect) {
        MobEffect kind = effect.getEffect().value();
        if (kind.isInstantenous()) {
            kind.applyInstantenousEffect(serverLevel, null, null, target, effect.getAmplifier(), INSTANT_EFFECT_SCALE);
        } else {
            target.addEffect(new MobEffectInstance(effect));
        }
    }

    private void puffMist(ServerLevel serverLevel, Direction facing) {
        RandomSource random = serverLevel.getRandom();
        int puffs = Math.max(1, MIST_PEAK_PUFFS * mistLeft / MIST_DURATION);
        mistLeft--;
        Vec3 out = facing.getUnitVec3();
        Vec3 mouth = Vec3.atCenterOf(worldPosition).add(out.scale(MIST_FACE_OFFSET));
        for (int puff = 0; puff < puffs; puff++) {
            Vec3 at = mouth.add(planeJitter(facing, random));
            Vec3 motion = out.scale(MIST_SPEED).add(random.triangle(0.0, MIST_SPREAD), random.triangle(0.0, MIST_SPREAD), random.triangle(0.0, MIST_SPREAD));
            Effects.vent(serverLevel, at).motion(motion.x, motion.y, motion.z).color(color).scale(MIST_SCALE).send();
        }
    }

    private static Vec3 planeJitter(Direction facing, RandomSource random) {
        double a = random.triangle(0.0, MIST_PLANE_JITTER);
        double b = random.triangle(0.0, MIST_PLANE_JITTER);
        return switch (facing.getAxis()) {
            case X -> new Vec3(0.0, a, b);
            case Y -> new Vec3(a, 0.0, b);
            case Z -> new Vec3(a, b, 0.0);
        };
    }

    @Override
    public int addEssentia(Holder<IAspect> type, int count, Direction side) {
        if (!canInputFrom(side) || potion.isEmpty() || charges >= MAX_CHARGES || count <= 0) {
            return 0;
        }
        int accepted = Math.min(count, shortBy(type));
        if (accepted > 0) {
            stored = stored.add(type, accepted);
            setChangedAndSync();
        }
        return accepted;
    }

    @Override
    public int spaceFor(Holder<IAspect> type, Direction side) {
        return canInputFrom(side) && !potion.isEmpty() && charges < MAX_CHARGES ? shortBy(type) : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> type, int count, Direction side) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction side) {
        return null;
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction side) {
        return 0;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int count) {
        suctionType = type;
        suction = Math.max(0, count);
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction side) {
        return suctionType;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction side) {
        return side == front() ? 0 : suction;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return false;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return side != front();
    }

    @Override
    public boolean isConnectable(Direction side) {
        return side != front();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level != null && !level.isClientSide() && !potion.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), potion);
            potion = ItemStack.EMPTY;
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        potion = input.read(POTION_KEY, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        needed = input.read(RECIPE_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        stored = input.read(PROGRESS_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        charges = Math.clamp(input.getIntOr(CHARGES_KEY, 0), 0, MAX_CHARGES);
        color = input.getIntOr(COLOR_KEY, NO_POTION_COLOR);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!potion.isEmpty()) {
            output.store(POTION_KEY, ItemStack.CODEC, potion);
        }
        output.store(RECIPE_KEY, AspectList.CODEC, needed);
        output.store(PROGRESS_KEY, AspectList.CODEC, stored);
        output.putInt(CHARGES_KEY, charges);
        output.putInt(COLOR_KEY, color);
    }
}
