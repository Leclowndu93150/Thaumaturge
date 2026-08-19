package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerInput;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.registry.TCRecipeTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.RecipeBookCategories;
import net.minecraft.world.item.crafting.RecipeBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public final class DustTriggerMultiblockRecipe implements DustTrigger {

    public static final MapCodec<DustTriggerMultiblockRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Identifier.CODEC.fieldOf("blueprint").forGetter(r -> r.blueprintId),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(r -> r.result), ResearchGate.CODEC.optionalFieldOf("research").forGetter(r -> r.research)).apply(i, DustTriggerMultiblockRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, DustTriggerMultiblockRecipe> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, r -> r.blueprintId,
            ItemStackTemplate.STREAM_CODEC, r -> r.result, ByteBufCodecs.optional(ResearchGate.STREAM_CODEC), r -> r.research, DustTriggerMultiblockRecipe::new);

    public static final RecipeSerializer<DustTriggerMultiblockRecipe> SERIALIZER = new RecipeSerializer<>(MAP_CODEC, STREAM_CODEC);

    private final Identifier blueprintId;
    private final ItemStackTemplate result;
    private final Optional<ResearchGate> research;

    public DustTriggerMultiblockRecipe(Identifier blueprintId, ItemStackTemplate result, Optional<ResearchGate> research) {
        this.blueprintId = blueprintId;
        this.result = result;
        this.research = research;
    }

    public Identifier blueprintId() {
        return this.blueprintId;
    }

    public ItemStack result() {
        return this.result.create();
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(new MultiblockRecipeDisplay(blueprintId, new SlotDisplay.ItemStackSlotDisplay(result)));
    }

    @Override
    public Optional<ResearchGate> researchGate() {
        return this.research;
    }

    @Override
    public boolean isMultiblock() {
        return true;
    }

    @Override
    public boolean matches(DustTriggerInput input, Level level) {
        return findPlacement(input) != null;
    }

    @Override
    public @Nullable DustTriggerPlacement findPlacement(DustTriggerInput input) {
        Blueprint blueprint = lookupBlueprint(input.level());
        if (blueprint == null) {
            return null;
        }
        return MultiblockMatcher.find(input.level(), input.pos(), blueprint);
    }

    @Override
    public ItemStack assemble(DustTriggerInput input) {
        return this.result.create();
    }

    @Override
    public List<BlockPos> sparkle(Level level, Player player, BlockPos pos, DustTriggerPlacement placement) {
        Blueprint blueprint = lookupBlueprint(level);
        if (blueprint == null || placement == null || placement.facing() == null) {
            return List.of(pos);
        }
        int ys = blueprint.ySize();
        int rotations = MultiblockMatcher.rotationsFor(placement.facing());
        BlockPos origin = pos.offset(placement.xOffset(), placement.yOffset(), placement.zOffset());
        List<BlockPos> out = new java.util.ArrayList<>();
        for (int y = 0; y < ys; y++) {
            BlueprintMatrix matrix = new BlueprintMatrix(blueprint, y);
            matrix.rotate90DegRight(rotations);
            for (int x = 0; x < matrix.rows(); x++) {
                for (int z = 0; z < matrix.cols(); z++) {
                    BlueprintPart part = matrix.get(x, z);
                    if (part == null) {
                        continue;
                    }
                    out.add(origin.offset(x, -y + (ys - 1), z));
                }
            }
        }
        return out;
    }

    @Override
    public void execute(DustTriggerInput input, Player player, @Nullable DustTriggerPlacement placement, Direction useFace) {
        Level level = input.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (placement == null || placement.facing() == null) {
            return;
        }
        Blueprint blueprint = lookupBlueprint(level);
        if (blueprint == null) {
            return;
        }
        int ys = blueprint.ySize();
        int rotations = MultiblockMatcher.rotationsFor(placement.facing());
        BlockPos origin = input.pos().offset(placement.xOffset(), placement.yOffset(), placement.zOffset());
        for (int y = 0; y < ys; y++) {
            BlueprintMatrix matrix = new BlueprintMatrix(blueprint, y);
            matrix.rotate90DegRight(rotations);
            for (int x = 0; x < matrix.rows(); x++) {
                for (int z = 0; z < matrix.cols(); z++) {
                    BlueprintPart part = matrix.get(x, z);
                    if (part == null) {
                        continue;
                    }
                    BlockPos cellPos = origin.offset(x, -y + (ys - 1), z);
                    int delay = part.priority();
                    BlockState original = level.getBlockState(cellPos);
                    enqueueForPart(serverLevel, cellPos, original, part, placement.facing(), useFace, player, delay);
                }
            }
        }
    }

    private static void enqueueForPart(ServerLevel level, BlockPos pos, BlockState original, BlueprintPart part, Direction placementFacing, Direction useFace, Player player, int delay) {
        BlueprintTarget target = part.target();
        if (target instanceof BlueprintTarget.Keep) {
            return;
        }
        if (target instanceof BlueprintTarget.Air) {
            DustTriggerSwapQueue.enqueueClear(level, pos, original, delay);
            return;
        }
        if (target instanceof BlueprintTarget.BlockTarget blockTarget) {
            BlockState placed = blockTarget.block().defaultBlockState();
            Direction facing;
            if (blockTarget.applyPlayerFacing()) {
                Direction side2 = useFace.getAxis().isHorizontal() ? useFace : player.getDirection().getOpposite();
                facing = side2;
            } else if (blockTarget.opposite()) {
                facing = placementFacing.getOpposite();
            } else {
                facing = placementFacing;
            }
            if (placed.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                placed = placed.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
            } else if (placed.hasProperty(BlockStateProperties.FACING)) {
                placed = placed.setValue(BlockStateProperties.FACING, facing);
            }
            DustTriggerSwapQueue.enqueuePlace(level, pos, original, placed, delay);
            return;
        }
        if (target instanceof BlueprintTarget.StateTarget stateTarget) {
            DustTriggerSwapQueue.enqueuePlace(level, pos, original, stateTarget.state(), delay);
            return;
        }
        if (target instanceof BlueprintTarget.StackTarget stackTarget) {
            DustTriggerSwapQueue.enqueueDrop(level, pos, original, stackTarget.stack(), delay);
        }
    }

    private @Nullable Blueprint lookupBlueprint(Level level) {
        ResourceKey<Blueprint> key = ResourceKey.create(Blueprint.REGISTRY_KEY, this.blueprintId);
        Registry<Blueprint> registry = level.registryAccess().lookup(Blueprint.REGISTRY_KEY).orElse(null);
        if (registry == null) {
            return null;
        }
        Holder<Blueprint> holder = registry.get(key).orElse(null);
        return holder == null ? null : holder.value();
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    @Override
    public RecipeSerializer<DustTriggerMultiblockRecipe> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public RecipeType<DustTrigger> getType() {
        return TCRecipeTypes.DUST_TRIGGER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.NOT_PLACEABLE;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }
}
