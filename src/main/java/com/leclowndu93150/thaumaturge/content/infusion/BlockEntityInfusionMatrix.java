package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.api.infusion.InfusionCraftedEvent;
import com.leclowndu93150.thaumaturge.api.items.IGogglesReadout;
import com.leclowndu93150.thaumaturge.content.aspect.ReadOnlyAspectContainer;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.particle.BoreSparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.InfusionCrumbsParticleOptions;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public final class BlockEntityInfusionMatrix extends BlockEntity implements IGogglesReadout, IInteractWithCaster, ReadOnlyAspectContainer {
    public static final float STABILITY_CAP = 25.0F;
    private static final float STABILITY_FLOOR = -100.0F;
    private static final int IDLE_VALIDATE_INTERVAL = 100;
    private static final int CRAFT_VALIDATE_INTERVAL = 20;
    private static final int ITEM_PULL_TICKS = 5;
    private static final int FINISH_GRACE_CYCLES = 2;
    private static final int INSTABILITY_ROLL_BOUND = 1500;
    private static final float MIN_COST_MULT = 0.5F;
    private static final float ESSENTIA_STARVE_PENALTY = 0.25F;
    private static final int ESSENTIA_FX_RANGE_TICKS = 12;
    private static final double GOGGLES_TEXT_Y_OFFSET = 1.5;

    private static final DecimalFormat STABILITY_FORMAT = new DecimalFormat("#######.##");
    private static final String STABILITY_LANG_PREFIX = "gui.thaumaturge.infusion.stability.";

    private boolean active;
    private float stability;
    private float stabilityReplenish;
    private @Nullable InfusionCraftJob job;
    private int count;
    private int itemPullCountdown;
    private int finishGrace;
    private boolean checkSurroundings = true;
    private @Nullable MatrixEnvironment environment;
    private final EssentiaSources essentiaSources = new EssentiaSources(this.worldPosition);

    public float clientStartUp;
    public int clientCraftTicks;
    private final Map<BlockPos, Integer> clientSourceFX = new HashMap<>();

    public BlockEntityInfusionMatrix(BlockPos pos, BlockState state) {
        super(TCBlockEntities.INFUSION_MATRIX.get(), pos, state);
    }

    public boolean isActive() {
        return active;
    }

    public boolean isCrafting() {
        return job != null;
    }

    public float stability() {
        return stability;
    }

    public AspectList remainingEssentia() {
        return job == null ? AspectList.EMPTY : job.essentia();
    }

    @Override
    public AspectList getAspects() {
        return remainingEssentia();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityInfusionMatrix matrix) {
        if (level instanceof ServerLevel serverLevel) {
            matrix.tickServer(serverLevel);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityInfusionMatrix matrix) {
        matrix.tickClient();
    }

    private MatrixEnvironment environment(ServerLevel level) {
        if (environment == null || checkSurroundings) {
            checkSurroundings = false;
            environment = MatrixEnvironment.survey(level, worldPosition);
            essentiaSources.invalidate();
            if (stabilityReplenish != environment.stabilityReplenish()) {
                stabilityReplenish = environment.stabilityReplenish();
                setChanged();
                syncToClient();
            }
        }
        return environment;
    }

    private void tickServer(ServerLevel level) {
        count++;
        MatrixEnvironment env = environment(level);
        int interval = isCrafting() ? CRAFT_VALIDATE_INTERVAL : IDLE_VALIDATE_INTERVAL;
        if (count % interval == 0 && !MatrixEnvironment.validLocation(level, worldPosition)) {
            active = false;
            job = null;
            setChanged();
            syncToClient();
            return;
        }
        int countDelay = Math.max(1, env.cycleTime() / 2);
        if (active && !isCrafting() && stability < STABILITY_CAP && count % Math.max(5, countDelay) == 0) {
            stability = Math.min(STABILITY_CAP, stability + Math.max(0.1F, env.stabilityReplenish()));
            setChanged();
            syncToClient();
        }
        if (active && isCrafting() && count % countDelay == 0) {
            craftCycle(level, env, countDelay);
            setChanged();
            syncToClient();
        }
        if (active && isCrafting()) {
            if (count % 5 != 0) {
                return;
            }
            if (count % 65 == 0) {
                level.playSound(null, worldPosition, TCSounds.INFUSER.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            }
            RandomSource rand = level.getRandom();
            Effects.blockRunes(level, Vec3.atLowerCornerOf(centralPedestal())).color(0.5F + rand.nextFloat() * 0.2F, 0.1F, 0.7F + rand.nextFloat() * 0.3F).duration(25).gravity(-0.03F).send();
        }
    }

    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (level instanceof ServerLevel serverLevel) {
            onRightClick(serverLevel, player);
        }
        return true;
    }

    public void onRightClick(ServerLevel level, Player player) {
        if (active && !isCrafting()) {
            tryStartCraft(level, player);
        } else if (!active && MatrixEnvironment.validLocation(level, worldPosition)) {
            level.playSound(null, worldPosition, TCSounds.CRAFTSTART.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
            active = true;
            setChanged();
            syncToClient();
        }
    }

    public boolean tryStartCraft(ServerLevel level, Player player) {
        checkSurroundings = true;
        if (!MatrixEnvironment.validLocation(level, worldPosition)) {
            active = false;
            setChanged();
            syncToClient();
            return false;
        }
        MatrixEnvironment env = environment(level);
        InfusionInput input = stagedInput(level, env);
        if (input.catalyst().isEmpty() || input.components().isEmpty()) {
            return false;
        }
        InfusionRecipeMatcher.Match match = InfusionRecipeMatcher.find(level, player, input);
        if (match == null || match.locked()) {
            return false;
        }
        InfusionJobRecipe recipe = match.recipe();
        job = new InfusionCraftJob(recipe.jobComponents(input), scaledCost(env, recipe.jobEssentia(input)), recipe.jobResult(input, level.getRandom()), input.catalyst().copyWithCount(1),
                recipe.jobInstability(input), Optional.of(player.getUUID()));
        level.playSound(null, worldPosition, TCSounds.CRAFTSTART.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        setChanged();
        syncToClient();
        return true;
    }

    InfusionInput stagedInput(ServerLevel level) {
        return stagedInput(level, environmentSnapshot(level));
    }

    AspectList projectedCost(ServerLevel level, AspectList essentia) {
        return scaledCost(environmentSnapshot(level), essentia);
    }

    private MatrixEnvironment environmentSnapshot(ServerLevel level) {
        return environment != null && !checkSurroundings ? environment : MatrixEnvironment.survey(level, worldPosition);
    }

    private InfusionInput stagedInput(ServerLevel level, MatrixEnvironment env) {
        List<ItemStack> components = new ArrayList<>();
        for (BlockPos pedestalPos : env.pedestals()) {
            ItemStack stack = pedestalItem(level, pedestalPos);
            if (!stack.isEmpty()) {
                components.add(stack.copyWithCount(1));
            }
        }
        return new InfusionInput(pedestalItem(level, centralPedestal()), components);
    }

    private static AspectList scaledCost(MatrixEnvironment env, AspectList essentia) {
        float costMult = Math.max(MIN_COST_MULT, env.costMult());
        AspectList scaled = AspectList.EMPTY;
        for (AspectInstance instance : essentia.entries()) {
            int amount = (int) (instance.amount() * costMult);
            if (amount > 0) {
                scaled = scaled.add(instance.aspect(), amount);
            }
        }
        return scaled;
    }

    static ItemStack withCatalystWear(ItemStack result, ItemStack catalyst) {
        ItemStack worn = result.copy();
        if (catalyst.isDamageableItem() && catalyst.getDamageValue() > 0 && worn.isDamageableItem() && worn.getDamageValue() == 0) {
            float damageRatio = (float) catalyst.getDamageValue() / catalyst.getMaxDamage();
            worn.setDamageValue((int) (worn.getMaxDamage() * damageRatio));
        }
        return worn;
    }

    private void craftCycle(ServerLevel level, MatrixEnvironment env, int countDelay) {
        if (job == null) {
            return;
        }
        RandomSource rand = level.getRandom();
        stability -= rand.nextFloat() * lossPerCycle();
        stability = Mth.clamp(stability + env.stabilityReplenish(), STABILITY_FLOOR, STABILITY_CAP);
        boolean valid = catalystStillPresent(level);
        if (!valid || (stability < 0.0F && rand.nextInt(INSTABILITY_ROLL_BOUND) <= Math.abs(stability))) {
            InstabilityEvents.trigger(level, worldPosition, env.pedestals());
            stability += 5.0F + rand.nextFloat() * 5.0F;
            if (valid) {
                syncToClient();
                return;
            }
        }
        if (!valid) {
            failCraft(level);
            return;
        }
        if (!job.essentia().isEmpty()) {
            drainEssentiaCycle(level, countDelay);
            return;
        }
        if (job.ingredients().isEmpty()) {
            if (finishGrace++ >= FINISH_GRACE_CYCLES) {
                finishGrace = 0;
                finishCraft(level);
            }
            return;
        }
        pullIngredientCycle(level, env);
    }

    private float lossPerCycle() {
        if (job == null) {
            return 0.0F;
        }
        return job.instability() / stabilityModifier();
    }

    private float stabilityModifier() {
        if (stability > STABILITY_CAP / 2.0F) {
            return 5.0F;
        }
        if (stability >= 0.0F) {
            return 6.0F;
        }
        return stability > -25.0F ? 7.0F : 8.0F;
    }

    private String stabilityTierKey() {
        if (stability > STABILITY_CAP / 2.0F) {
            return "very_stable";
        }
        if (stability >= 0.0F) {
            return "stable";
        }
        return stability > -25.0F ? "unstable" : "very_unstable";
    }

    @Override
    public Vec3 readoutAnchor() {
        return new Vec3(0.0, GOGGLES_TEXT_Y_OFFSET, 0.0);
    }

    @Override
    public List<Component> readout() {
        Component tier = Component.translatable(STABILITY_LANG_PREFIX + stabilityTierKey()).withStyle(ChatFormatting.BOLD);
        Component gain = Component.literal(STABILITY_FORMAT.format(stabilityReplenish) + " ").append(Component.translatable(STABILITY_LANG_PREFIX + "gain")).withStyle(ChatFormatting.GOLD,
                ChatFormatting.ITALIC);
        float lpc = lossPerCycle();
        if (lpc == 0.0F) {
            return List.of(tier, gain);
        }
        Component loss = Component.translatable(STABILITY_LANG_PREFIX + "range")
                .append(Component.literal(STABILITY_FORMAT.format(lpc) + " ").append(Component.translatable(STABILITY_LANG_PREFIX + "loss")).withStyle(ChatFormatting.ITALIC))
                .withStyle(ChatFormatting.RED);
        return List.of(tier, gain, loss);
    }

    private boolean catalystStillPresent(ServerLevel level) {
        if (job == null) {
            return false;
        }
        ItemStack current = pedestalItem(level, centralPedestal());
        return !current.isEmpty() && ItemStack.isSameItemSameComponents(current, job.catalyst());
    }

    private void drainEssentiaCycle(ServerLevel level, int countDelay) {
        for (AspectInstance instance : job.essentia().entries()) {
            if (instance.amount() <= 0) {
                continue;
            }
            if (essentiaSources.drain(level, instance.aspect(), instance.amount() > 1 ? countDelay : 0)) {
                job.setEssentia(job.essentia().reduce(instance.aspect(), 1));
                syncToClient();
                return;
            }
            stability -= ESSENTIA_STARVE_PENALTY;
            syncToClient();
        }
        checkSurroundings = true;
    }

    private void pullIngredientCycle(ServerLevel level, MatrixEnvironment env) {
        List<ItemStack> ingredients = job.ingredients();
        for (int a = 0; a < ingredients.size(); a++) {
            for (BlockPos pedestalPos : env.pedestals()) {
                if (!(level.getBlockEntity(pedestalPos) instanceof BlockEntityPedestal pedestal)) {
                    continue;
                }
                ItemStack stack = pedestal.getItem();
                if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, ingredients.get(a))) {
                    continue;
                }
                if (itemPullCountdown == 0) {
                    itemPullCountdown = ITEM_PULL_TICKS;
                    InfusionFx.itemStream(level, worldPosition, pedestalPos);
                } else if (--itemPullCountdown < 1) {
                    ItemStackTemplate remainder = stack.getItem().getCraftingRemainder(stack);
                    pedestal.setItem(remainder == null ? ItemStack.EMPTY : remainder.create());
                    ingredients.remove(a);
                    setChanged();
                }
                return;
            }
            AspectList essentia = job.essentia();
            if (!essentia.isEmpty() && level.getRandom().nextInt(1 + a) == 0) {
                List<AspectInstance> entries = essentia.entries();
                AspectInstance random = entries.get(level.getRandom().nextInt(entries.size()));
                job.setEssentia(essentia.add(random.aspect(), 1));
                stability -= ESSENTIA_STARVE_PENALTY;
                syncToClient();
            }
        }
    }

    private void failCraft(ServerLevel level) {
        job = null;
        level.playSound(null, worldPosition, TCSounds.CRAFTFAIL.get(), SoundSource.BLOCKS, 1.0F, 0.6F);
        setChanged();
        syncToClient();
    }

    private void finishCraft(ServerLevel level) {
        if (!(level.getBlockEntity(centralPedestal()) instanceof BlockEntityPedestal pedestal)) {
            job = null;
            syncToClient();
            return;
        }
        ItemStack catalyst = pedestal.getItem();
        ServerPlayer crafter = job.player().map(uuid -> level.getServer().getPlayerList().getPlayer(uuid)).orElse(null);
        InfusionCraftedEvent event = NeoForge.EVENT_BUS.post(new InfusionCraftedEvent(level, worldPosition, crafter, catalyst.copy(), withCatalystWear(job.result(), catalyst)));
        ItemStack result = event.getResult();
        pedestal.setItem(result);
        job = null;
        if (crafter != null && !result.isEmpty()) {
            awardCraft(crafter, result);
        }
        InfusionFx.pedestalBamf(level, centralPedestal());
        level.playSound(null, worldPosition, TCSounds.WAND.get(), SoundSource.BLOCKS, 0.5F, 1.0F);
        setChanged();
        syncToClient();
    }

    private void awardCraft(ServerPlayer player, ItemStack result) {
        player.awardStat(Stats.ITEM_CRAFTED.get(result.getItem()), result.getCount());
        ResearchProgressionEvents.recordCrafted(player, result);
    }

    private ItemStack pedestalItem(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BlockEntityPedestal pedestal ? pedestal.getItem() : ItemStack.EMPTY;
    }

    private BlockPos centralPedestal() {
        return worldPosition.below(2);
    }

    public void refreshSurroundings() {
        checkSurroundings = true;
    }

    private void tickClient() {
        if (isCrafting()) {
            if (clientCraftTicks == 0 && level != null) {
                level.playLocalSound(worldPosition, TCSounds.INFUSERSTART.get(), SoundSource.BLOCKS, 0.5F, 1.0F, false);
            }
            clientCraftTicks++;
        } else if (clientCraftTicks > 0) {
            clientCraftTicks = Math.min(50, Math.max(0, clientCraftTicks - 2));
        }
        if (active && clientStartUp < 1.0F) {
            clientStartUp = Math.min(1.0F, clientStartUp + Math.max(clientStartUp / 10.0F, 0.001F));
        } else if (!active && clientStartUp > 0.0F) {
            clientStartUp -= clientStartUp / 10.0F;
            if (clientStartUp < 0.001F) {
                clientStartUp = 0.0F;
            }
        }
        tickClientSourceFX();
    }

    public void addClientSourceFX(BlockPos source, int ticks) {
        clientSourceFX.put(source.immutable(), ticks);
    }

    private void tickClientSourceFX() {
        if (clientSourceFX.isEmpty() || level == null) {
            return;
        }
        RandomSource rand = level.getRandom();
        Iterator<Map.Entry<BlockPos, Integer>> it = clientSourceFX.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Integer> entry = it.next();
            if (entry.getValue() <= 0) {
                it.remove();
                continue;
            }
            BlockPos loc = entry.getKey();
            if (level.getBlockEntity(loc) instanceof BlockEntityPedestal pedestal) {
                ItemStack stack = pedestal.getItem();
                if (!stack.isEmpty()) {
                    spawnPullFX(rand, loc, stack);
                }
                entry.setValue(entry.getValue() - 1);
            } else {
                entry.setValue(0);
            }
        }
    }

    private void spawnPullFX(RandomSource rand, BlockPos loc, ItemStack stack) {
        double tx = worldPosition.getX() + 0.5;
        double ty = worldPosition.getY() - 0.5;
        double tz = worldPosition.getZ() + 0.5;
        if (rand.nextInt(3) == 0) {
            level.addParticle(new BoreSparkleParticleOptions(tx, ty, tz, 0.4F + rand.nextFloat() * 0.2F, 0.2F, 0.6F + rand.nextFloat() * 0.3F), loc.getX() + rand.nextFloat(),
                    loc.getY() + rand.nextFloat() + 1.0F, loc.getZ() + rand.nextFloat(), 0.0, 0.0, 0.0);
            return;
        }
        ItemStackTemplate template = new ItemStackTemplate(stack.getItem());
        if (stack.getItem() instanceof BlockItem) {
            for (int a = 0; a < 4; a++) {
                level.addParticle(new InfusionCrumbsParticleOptions(template, tx, ty, tz, 0.0, 0.0, 0.0), loc.getX() + rand.nextFloat(), loc.getY() + rand.nextFloat() + 1.0F,
                        loc.getZ() + rand.nextFloat(), 0.0, 0.0, 0.0);
            }
        } else {
            for (int a = 0; a < 4; a++) {
                level.addParticle(new InfusionCrumbsParticleOptions(template, tx, ty, tz, rand.nextGaussian() * 0.03F, rand.nextGaussian() * 0.03F, rand.nextGaussian() * 0.03F),
                        loc.getX() + 0.4F + rand.nextFloat() * 0.2F, loc.getY() + 1.23F + rand.nextFloat() * 0.2F, loc.getZ() + 0.4F + rand.nextFloat() * 0.2F, 0.0, 0.0, 0.0);
            }
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("Active", active);
        output.putFloat("Stability", stability);
        output.putFloat("Replenish", stabilityReplenish);
        if (job != null) {
            output.store("Job", InfusionCraftJob.CODEC, job);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        active = input.getBooleanOr("Active", false);
        stability = input.getFloatOr("Stability", 0.0F);
        stabilityReplenish = input.getFloatOr("Replenish", 0.0F);
        job = input.read("Job", InfusionCraftJob.CODEC).orElse(null);
    }

    private void syncToClient() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
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
}
