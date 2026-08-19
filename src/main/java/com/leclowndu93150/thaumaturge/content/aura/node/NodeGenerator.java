package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAspects;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCDataMaps;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.jspecify.annotations.Nullable;

public final class NodeGenerator {
    public static final int DEFAULT_SPECIAL_RARITY = 15;
    public static final int DEFAULT_BASE_AURA = 100;

    private static final int EXTRA_ASPECT_ROLLS = 3;
    private static final int ENV_SCAN_RANGE = 5;
    private static final int ENV_WATER_THRESHOLD = 100;
    private static final int ENV_LAVA_THRESHOLD = 100;
    private static final int ENV_STONE_THRESHOLD = 500;
    private static final int ENV_FOLIAGE_THRESHOLD = 100;
    private static final int PLACE_FLAGS = 3;

    private NodeGenerator() {}

    public static boolean createRandomNodeAt(ServerLevelAccessor level, BlockPos pos, RandomSource random, boolean silverwood, boolean eerie, boolean small, int specialRarity, int baseAura) {
        NodeData data = rollRandomNodeData(level, pos, random, silverwood, eerie, small, specialRarity, baseAura);
        if (data == null) {
            return false;
        }
        return createNodeAt(level, pos, data.type(), data.modifier().orElse(null), data.aspects());
    }

    public static @Nullable NodeData rollRandomNodeData(ServerLevelAccessor level, BlockPos pos, RandomSource random, boolean silverwood, boolean eerie, boolean small, int specialRarity, int baseAura) {
        HolderLookup.RegistryLookup<IAspect> aspectRegistry = level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        List<Holder<IAspect>> basicAspects = new ArrayList<>();
        List<Holder<IAspect>> complexAspects = new ArrayList<>();
        aspectRegistry.listElements().forEach(holder -> {
            if (holder.value().isPrimal()) {
                basicAspects.add(holder);
            } else {
                complexAspects.add(holder);
            }
        });
        if (basicAspects.isEmpty() || complexAspects.isEmpty()) {
            return null;
        }

        NodeType type = NodeType.NORMAL;
        if (silverwood) {
            type = NodeType.PURE;
        } else if (eerie) {
            type = NodeType.DARK;
        } else if (random.nextInt(specialRarity) == 0) {
            type = switch (random.nextInt(10)) {
                case 0, 1, 2 -> NodeType.DARK;
                case 3, 4, 5 -> NodeType.UNSTABLE;
                case 6, 7, 8 -> NodeType.PURE;
                default -> NodeType.HUNGRY;
            };
        }

        NodeModifier modifier = null;
        if (random.nextInt(Math.max(1, specialRarity / 2)) == 0) {
            modifier = switch (random.nextInt(3)) {
                case 0 -> NodeModifier.BRIGHT;
                case 1 -> NodeModifier.PALE;
                default -> NodeModifier.FADING;
            };
        }

        Holder<Biome> biome = level.getBiome(pos);
        BiomeAuraModifier auraModifier = biome.getData(TCDataMaps.BIOME_AURA_MODIFIER);
        int biomeAura = (int) (baseAura * (auraModifier == null ? 1.0F : auraModifier.value()));
        if (silverwood || small) {
            biomeAura /= 4;
        }
        biomeAura = Math.max(8, biomeAura);
        int value = random.nextInt(biomeAura / 2) + biomeAura / 2;

        AspectList list = AspectList.EMPTY;
        Holder<IAspect> biomeAspect = randomBiomeAspect(aspectRegistry, biome, random);
        if (biomeAspect != null) {
            list = list.add(biomeAspect, 2);
        } else {
            list = list.add(complexAspects.get(random.nextInt(complexAspects.size())), 1);
            list = list.add(basicAspects.get(random.nextInt(basicAspects.size())), 1);
        }
        for (int roll = 0; roll < EXTRA_ASPECT_ROLLS; roll++) {
            if (random.nextBoolean()) {
                if (random.nextInt(specialRarity) == 0) {
                    list = list.add(complexAspects.get(random.nextInt(complexAspects.size())), 1);
                } else {
                    list = list.add(basicAspects.get(random.nextInt(basicAspects.size())), 1);
                }
            }
        }

        list = addTypeFlavor(aspectRegistry, list, type, random);
        list = addEnvironmentFlavor(aspectRegistry, level, pos, list);

        List<AspectInstance> entries = list.entries();
        int[] spread = new int[entries.size()];
        float total = 0.0F;
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).amount() == 2) {
                spread[i] = 50 + random.nextInt(25);
            } else {
                spread[i] = 25 + random.nextInt(50);
            }
            total += spread[i];
        }
        AspectList distributed = list;
        for (int i = 0; i < entries.size(); i++) {
            int amount = (int) (spread[i] / total * value);
            if (amount > 0) {
                distributed = distributed.add(entries.get(i).aspect(), amount);
            }
        }

        return new NodeData(type, Optional.ofNullable(modifier), distributed, distributed);
    }

    public static boolean createNodeAt(ServerLevelAccessor level, BlockPos pos, NodeType type, @Nullable NodeModifier modifier, AspectList aspects) {
        if (level.getBlockEntity(pos) instanceof BlockEntityNode existing) {
            return configureNode(existing, type, modifier, aspects);
        }
        BlockState current = level.getBlockState(pos);
        if (!current.isAir() && !current.canBeReplaced() && !current.is(BlockTags.LEAVES)) {
            return false;
        }
        level.setBlock(pos, TCBlocks.NODE.get().defaultBlockState(), PLACE_FLAGS);
        if (level.getBlockEntity(pos) instanceof BlockEntityNode node) {
            return configureNode(node, type, modifier, aspects);
        }
        return false;
    }

    private static boolean configureNode(BlockEntityNode node, NodeType type, @Nullable NodeModifier modifier, AspectList aspects) {
        node.setNodeType(type);
        node.setNodeModifier(modifier);
        node.setAspects(aspects);
        node.setChanged();
        return true;
    }

    private static @Nullable Holder<IAspect> randomBiomeAspect(HolderLookup.RegistryLookup<IAspect> registry, Holder<Biome> biome, RandomSource random) {
        BiomeAspects aspects = biome.getData(TCDataMaps.BIOME_ASPECTS);
        if (aspects == null || aspects.aspects().isEmpty()) {
            return null;
        }
        ResourceKey<IAspect> key = aspects.aspects().get(random.nextInt(aspects.aspects().size()));
        return registry.get(key).orElse(null);
    }

    private static AspectList addTypeFlavor(HolderLookup.RegistryLookup<IAspect> registry, AspectList list, NodeType type, RandomSource random) {
        switch (type) {
            case HUNGRY -> {
                list = list.add(registry.getOrThrow(TCAspects.DESIDERIUM), 2);
                if (random.nextBoolean()) {
                    list = list.add(registry.getOrThrow(TCAspects.VACUOS), 1);
                }
            }
            case PURE -> list = list.add(registry.getOrThrow(random.nextBoolean() ? TCAspects.VICTUS : TCAspects.ORDO), 2);
            case DARK -> {
                if (random.nextBoolean()) {
                    list = list.add(registry.getOrThrow(TCAspects.MORTUUS), 1);
                }
                if (random.nextBoolean()) {
                    list = list.add(registry.getOrThrow(TCAspects.EXANIMIS), 1);
                }
                if (random.nextBoolean()) {
                    list = list.add(registry.getOrThrow(TCAspects.PERDITIO), 1);
                }
                if (random.nextBoolean()) {
                    list = list.add(registry.getOrThrow(TCAspects.TENEBRAE), 1);
                }
            }
            default -> {
            }
        }
        return list;
    }

    private static AspectList addEnvironmentFlavor(HolderLookup.RegistryLookup<IAspect> registry, ServerLevelAccessor level, BlockPos pos, AspectList list) {
        int water = 0;
        int lava = 0;
        int stone = 0;
        int foliage = 0;
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int xx = -ENV_SCAN_RANGE; xx <= ENV_SCAN_RANGE; xx++) {
            for (int yy = -ENV_SCAN_RANGE; yy <= ENV_SCAN_RANGE; yy++) {
                for (int zz = -ENV_SCAN_RANGE; zz <= ENV_SCAN_RANGE; zz++) {
                    cursor.setWithOffset(pos, xx, yy, zz);
                    BlockState state = level.getBlockState(cursor);
                    if (state.getFluidState().is(Fluids.WATER)) {
                        water++;
                    } else if (state.getFluidState().is(Fluids.LAVA)) {
                        lava++;
                    } else if (state.is(Blocks.STONE) || state.is(Blocks.DEEPSLATE)) {
                        stone++;
                    }
                    if (state.is(BlockTags.LEAVES)) {
                        foliage++;
                    }
                }
            }
        }
        if (water > ENV_WATER_THRESHOLD) {
            list = list.add(registry.getOrThrow(TCAspects.AQUA), 1);
        }
        if (lava > ENV_LAVA_THRESHOLD) {
            list = list.add(registry.getOrThrow(TCAspects.IGNIS), 1);
            list = list.add(registry.getOrThrow(TCAspects.TERRA), 1);
        }
        if (stone > ENV_STONE_THRESHOLD) {
            list = list.add(registry.getOrThrow(TCAspects.TERRA), 1);
        }
        if (foliage > ENV_FOLIAGE_THRESHOLD) {
            list = list.add(registry.getOrThrow(TCAspects.HERBA), 1);
        }
        return list;
    }
}
