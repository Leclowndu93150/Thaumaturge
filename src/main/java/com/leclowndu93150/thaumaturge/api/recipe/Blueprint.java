package com.leclowndu93150.thaumaturge.api.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.Registry;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public record Blueprint(int xSize, int ySize, int zSize, Map<Character, BlueprintPart> keys, List<List<String>> pattern) {
    public static final ResourceKey<Registry<Blueprint>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "blueprint"));

    private static final Codec<Character> SYMBOL_CODEC = Codec.STRING.comapFlatMap((symbol) -> {
        if (symbol.length() != 1) {
            return DataResult.error(() -> "Invalid key entry: '" + symbol + "' is an invalid symbol (must be 1 character only).");
        } else {
            return " ".equals(symbol) ? DataResult.error(() -> "Invalid key entry: ' ' is a reserved symbol.") : DataResult.success(symbol.charAt(0));
        }
    }, String::valueOf);

    private static final Codec<Map<Character, BlueprintPart>> KEYS_CODEC = Codec.unboundedMap(SYMBOL_CODEC, BlueprintPart.CODEC);

    public static final Codec<Blueprint> CODEC = RecordCodecBuilder
            .create(i -> i.group(Codec.INT.fieldOf("x_size").forGetter(Blueprint::xSize), Codec.INT.fieldOf("y_size").forGetter(Blueprint::ySize),
                    Codec.INT.fieldOf("z_size").forGetter(Blueprint::zSize), KEYS_CODEC.optionalFieldOf("keys", new HashMap<>()).forGetter(Blueprint::keys),
                    Codec.STRING.listOf().listOf().optionalFieldOf("pattern", new ArrayList<>()).forGetter(Blueprint::pattern)).apply(i, Blueprint::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Blueprint> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, Blueprint::xSize, ByteBufCodecs.VAR_INT, Blueprint::ySize,
            ByteBufCodecs.VAR_INT, Blueprint::zSize, ByteBufCodecs.map(HashMap::new, ByteBufCodecs.fromCodec(SYMBOL_CODEC), BlueprintPart.STREAM_CODEC), Blueprint::keys,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()).apply(ByteBufCodecs.list()), Blueprint::pattern, Blueprint::new);

    public Blueprint {
        if (xSize <= 0 || ySize <= 0 || zSize <= 0) {
            throw new IllegalArgumentException("Blueprint dimensions can't be zero or negative : xSize: " + xSize + ", ySize: " + ySize + ", zSize: " + zSize);
        }
        if (pattern.size() != ySize) {
            throw new IllegalArgumentException("Not enough layers for blueprint : Expected: " + ySize + ", Found: " + pattern.size());
        }
        Set<Character> definedKeys = new HashSet<>();
        for (List<String> layer : pattern) {
            if (layer.size() != xSize) {
                throw new IllegalArgumentException("Not enough rows for blueprint : Expected: " + xSize + ", Found: " + layer.size());
            }
            for (String row : layer) {
                if (row.length() != zSize) {
                    throw new IllegalArgumentException("Not enough columns for blueprint : Expected: " + zSize + ", Found: " + row.length());
                }
                for (char c : row.toCharArray()) {
                    if (c != ' ') {
                        definedKeys.add(c);
                    }
                }
            }
        }

        if (!keys.keySet().containsAll(definedKeys)) {
            Set<Character> missingKeys = new HashSet<>(definedKeys);
            missingKeys.removeAll(keys.keySet());
            throw new IllegalArgumentException("Not all characters in the pattern have a corresponding key defined. Missing keys: " + missingKeys);
        }
        pattern = List.copyOf(pattern);
    }

    public @Nullable BlueprintPart cell(int y, int x, int z) {
        return partForChar(charAt(y, x, z));
    }

    private Character charAt(int y, int x, int z) {
        return pattern.get(y).get(x).charAt(z);
    }

    private @Nullable BlueprintPart partForChar(char c) {
        if (c == ' ')
            return null;
        if (!keys.containsKey(c))
            throw new IllegalArgumentException("No blueprint part defined for character: " + c);
        return keys.get(c);
    }
}
