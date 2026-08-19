package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.api.casters.FocusElement;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class FocusElementNode {
    public static final Codec<FocusElementNode> CODEC = RecordCodecBuilder
            .create(inst -> inst.group(Codec.INT.fieldOf("x").forGetter(n -> n.x), Codec.INT.fieldOf("y").forGetter(n -> n.y), Codec.INT.fieldOf("id").forGetter(n -> n.id),
                    Codec.BOOL.optionalFieldOf("target", false).forGetter(n -> n.target), Codec.BOOL.optionalFieldOf("trajectory", false).forGetter(n -> n.trajectory),
                    Codec.INT.optionalFieldOf("parent", -1).forGetter(n -> n.parent), Codec.INT.listOf().optionalFieldOf("children", List.of()).forGetter(FocusElementNode::childList),
                    Codec.FLOAT.optionalFieldOf("complexity", 1.0F).forGetter(n -> n.complexityMultiplier), Identifier.CODEC.optionalFieldOf("key").forGetter(n -> Optional.ofNullable(n.element)),
                    Codec.unboundedMap(Codec.STRING, Codec.INT).optionalFieldOf("settings", Map.of()).forGetter(n -> n.settings)).apply(inst, FocusElementNode::decode));

    public int x;
    public int y;
    public int id;
    public boolean target;
    public boolean trajectory;
    public int parent = -1;
    public int[] children = new int[0];
    public float complexityMultiplier = 1.0F;
    public @Nullable Identifier element;
    public Map<String, Integer> settings = new HashMap<>();

    public @Nullable FocusElement resolve() {
        return element != null ? FocusEngine.element(element) : null;
    }

    public FocusSettings resolvedSettings() {
        FocusElement resolved = resolve();
        return resolved != null ? FocusSettings.of(resolved, settings) : FocusSettings.empty();
    }

    public float getPower(Map<Integer, FocusElementNode> data) {
        FocusElement resolved = resolve();
        if (resolved == null) {
            return 1.0F;
        }
        float pow = resolved.powerMultiplier(resolvedSettings());
        FocusElementNode p = data.get(parent);
        if (p != null && p.element != null) {
            pow *= p.getPower(data);
        }
        return pow;
    }

    private List<Integer> childList() {
        List<Integer> list = new ArrayList<>(children.length);
        for (int c : children) {
            list.add(c);
        }
        return list;
    }

    private static FocusElementNode decode(int x, int y, int id, boolean target, boolean trajectory, int parent, List<Integer> children, float complexity, Optional<Identifier> key, Map<String, Integer> settings) {
        FocusElementNode result = new FocusElementNode();
        result.x = x;
        result.y = y;
        result.id = id;
        result.target = target;
        result.trajectory = trajectory;
        result.parent = parent;
        result.children = new int[children.size()];
        for (int i = 0; i < children.size(); i++) {
            result.children[i] = children.get(i);
        }
        result.complexityMultiplier = complexity;
        result.element = key.orElse(null);
        result.settings = new HashMap<>(settings);
        return result;
    }
}
