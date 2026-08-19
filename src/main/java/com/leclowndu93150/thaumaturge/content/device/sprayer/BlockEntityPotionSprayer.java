package com.leclowndu93150.thaumaturge.content.device.sprayer;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class BlockEntityPotionSprayer extends BlockEntity implements IEssentiaTransport {
    public static final int MAX_CHARGES = 8;
    private static final int SUCTION_CYCLE_TICKS = 5;
    private static final int SUCTION_STRENGTH = 128;
    private static final int SPRAY_REACH = 2;
    private static final int SPRAY_AREA = 1;
    private static final int VENT_EVENT = 0;
    private static final int VENT_TICKS = 15;
    private static final float VENT_SPEED = 0.25F;
    private static final int DEFAULT_COLOR = 0x333333;

    private ItemStack potion = ItemStack.EMPTY;
    private AspectList recipe = AspectList.EMPTY;
    private AspectList progress = AspectList.EMPTY;
    private int charges;
    private int color = DEFAULT_COLOR;
    private int counter;
    private boolean activated;
    private int venting;
    private @Nullable Holder<IAspect> currentSuction;

    public BlockEntityPotionSprayer(BlockPos pos, BlockState state) {
        super(TCBlockEntities.POTION_SPRAYER.get(), pos, state);
    }

    public static boolean isValidPotion(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof PotionItem;
    }

    public ItemStack getPotion() {
        return potion;
    }

    public void setPotion(ItemStack stack) {
        potion = stack;
        recalcAspects();
    }

    public AspectList recipe() {
        return recipe;
    }

    public AspectList progress() {
        return progress;
    }

    public int charges() {
        return charges;
    }

    public int color() {
        return color;
    }

    void tick(Level level, BlockPos pos, BlockState state) {
        counter++;
        Direction facing = state.getValue(BlockPotionSprayer.FACING);
        if (level.isClientSide()) {
            clientVent(level, pos, facing);
            return;
        }
        ServerLevel server = (ServerLevel) level;
        if (counter % SUCTION_CYCLE_TICKS == 0) {
            currentSuction = null;
            if (!potion.isEmpty() && charges < MAX_CHARGES) {
                boolean done = true;
                for (AspectInstance entry : recipe.entries()) {
                    if (progress.amountOf(entry.aspect()) < entry.amount()) {
                        currentSuction = entry.aspect();
                        done = false;
                        break;
                    }
                }
                if (done && !recipe.isEmpty()) {
                    progress = AspectList.EMPTY;
                    charges++;
                    setChanged();
                    syncToClient();
                } else if (currentSuction != null) {
                    fill(server, facing);
                }
            }
        }
        boolean enabled = state.getValue(BlockStateProperties.ENABLED);
        if (!enabled) {
            if (!activated && charges > 0) {
                charges--;
                spray(server, pos, facing);
                server.playSound(null, pos, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, 0.25F, 2.6F + (server.getRandom().nextFloat() - server.getRandom().nextFloat()) * 0.8F);
                server.blockEvent(pos, state.getBlock(), VENT_EVENT, 0);
                setChanged();
                syncToClient();
            }
            activated = true;
        } else if (activated) {
            activated = false;
        }
    }

    private void spray(ServerLevel server, BlockPos pos, Direction facing) {
        List<MobEffectInstance> effects = new ArrayList<>();
        potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getAllEffects().forEach(effects::add);
        if (effects.isEmpty()) {
            return;
        }
        BlockPos center = pos.relative(facing, SPRAY_REACH);
        AABB box = new AABB(center.getX() - SPRAY_AREA, center.getY() - SPRAY_AREA, center.getZ() - SPRAY_AREA, center.getX() + 1 + SPRAY_AREA, center.getY() + 1 + SPRAY_AREA,
                center.getZ() + 1 + SPRAY_AREA);
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, box)) {
            if (!target.isAlive() || !target.isAffectedByPotions()) {
                continue;
            }
            for (MobEffectInstance instance : effects) {
                Holder<MobEffect> effect = instance.getEffect();
                if (effect.value().isInstantenous()) {
                    effect.value().applyInstantenousEffect(server, null, null, target, instance.getAmplifier(), 1.0);
                } else {
                    target.addEffect(new MobEffectInstance(effect, instance.getDuration(), instance.getAmplifier()));
                }
            }
        }
    }

    private void clientVent(Level level, BlockPos pos, Direction facing) {
        if (venting <= 0) {
            return;
        }
        venting--;
        RandomSource rand = level.getRandom();
        for (int i = 0; i < venting / 2; i++) {
            float fx = 0.1F - rand.nextFloat() * 0.2F;
            float fy = 0.1F - rand.nextFloat() * 0.2F;
            float fz = 0.1F - rand.nextFloat() * 0.2F;
            double mx = rand.nextGaussian() * 0.06 + facing.getStepX() * VENT_SPEED;
            double my = rand.nextGaussian() * 0.06 + facing.getStepY() * VENT_SPEED;
            double mz = rand.nextGaussian() * 0.06 + facing.getStepZ() * VENT_SPEED;
            level.addParticle(new VentParticleOptions(mx, my, mz, color, 4.0F, false), pos.getX() + 0.5F + fx + facing.getStepX() / 2.0F, pos.getY() + 0.5F + fy + facing.getStepY() / 2.0F,
                    pos.getZ() + 0.5F + fz + facing.getStepZ() / 2.0F, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean triggerEvent(int event, int param) {
        if (event == VENT_EVENT) {
            if (level != null && level.isClientSide()) {
                venting = VENT_TICKS;
            }
            return true;
        }
        return super.triggerEvent(event, param);
    }

    private void recalcAspects() {
        if (level == null || level.isClientSide()) {
            return;
        }
        color = DEFAULT_COLOR;
        if (!potion.isEmpty()) {
            recipe = PotionAspects.of((ServerLevel) level, potion);
            color = potion.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY).getColor();
        } else {
            recipe = AspectList.EMPTY;
        }
        charges = 0;
        progress = AspectList.EMPTY;
        setChanged();
        syncToClient();
    }

    private void fill(ServerLevel server, Direction facing) {
        for (int y = 0; y <= 1; y++) {
            for (Direction dir : Direction.values()) {
                if (dir == facing) {
                    continue;
                }
                BlockPos from = getBlockPos().above(y);
                IEssentiaTransport ic = EssentiaFlowHandler.transport(server, from.relative(dir), dir.getOpposite());
                if (ic == null) {
                    continue;
                }
                if (ic.getEssentiaAmount(dir.getOpposite()) > 0 && ic.getSuctionAmount(dir.getOpposite()) < getSuctionAmount(dir) && getSuctionAmount(dir) >= ic.getMinimumSuction()) {
                    int taken = ic.takeEssentia(currentSuction, 1, dir.getOpposite());
                    if (taken > 0) {
                        acceptEssentia(currentSuction, taken);
                        return;
                    }
                }
            }
        }
    }

    private int acceptEssentia(Holder<IAspect> aspect, int amount) {
        int needed = recipe.amountOf(aspect) - progress.amountOf(aspect);
        if (needed <= 0) {
            return 0;
        }
        int added = Math.min(needed, amount);
        progress = progress.add(aspect, added);
        setChanged();
        syncToClient();
        return added;
    }

    void syncToClient() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide() && !potion.isEmpty()) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), potion);
            potion = ItemStack.EMPTY;
        }
        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!potion.isEmpty()) {
            output.store("Potion", ItemStack.CODEC, potion);
        }
        output.store("Recipe", AspectList.CODEC, recipe);
        output.store("Progress", AspectList.CODEC, progress);
        output.putInt("Charges", charges);
        output.putInt("Color", color);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        potion = input.read("Potion", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        recipe = input.read("Recipe", AspectList.CODEC).orElse(AspectList.EMPTY);
        progress = input.read("Progress", AspectList.CODEC).orElse(AspectList.EMPTY);
        charges = input.getIntOr("Charges", 0);
        color = input.getIntOr("Color", DEFAULT_COLOR);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private Direction facing() {
        return getBlockState().getValue(BlockPotionSprayer.FACING);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != facing();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != facing();
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return false;
    }

    @Override
    public void setSuction(Holder<IAspect> aspect, int amount) {
        currentSuction = aspect;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return currentSuction;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return currentSuction != null ? SUCTION_STRENGTH : 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return canInputFrom(face) ? acceptEssentia(aspect, amount) : 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }
}
