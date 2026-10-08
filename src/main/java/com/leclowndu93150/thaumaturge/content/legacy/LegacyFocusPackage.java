package com.leclowndu93150.thaumaturge.content.legacy;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.CastStyle;
import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class LegacyFocusPackage {
    private static final ResourceLocation ROOT = TTIds.rl("root");
    private static final ResourceLocation SPLIT_TARGET = TTIds.rl("split_target");
    private static final ResourceLocation SPLIT_TRAJECTORY = TTIds.rl("split_trajectory");
    private static final ResourceLocation PROJECTILE = TTIds.rl("projectile");
    private static final ResourceLocation BREAK = TTIds.rl("break");
    private static final ResourceLocation EXCHANGE = TTIds.rl("exchange");
    private static final String OPTION = "option";
    private static final String FORTUNE = "fortune";
    private static final String SILK = "silk";
    private static final String LEVEL = "level";
    private static final int OPTION_BOUNCY = 1;
    private static final int OPTION_SEEK_ENEMY = 2;

    private static final Codec<FocusPackage> PACKAGE_CODEC = Codec.recursive("LegacyFocusPackage", self -> {
        Codec<FocusUnit> unit = RecordCodecBuilder.create(i -> i.group(
                        LegacyIds.IDENTIFIER_CODEC.fieldOf("key").forGetter(FocusUnit::key),
                        Codec.unboundedMap(Codec.STRING, Codec.INT)
                                .optionalFieldOf("settings", Map.of())
                                .forGetter(FocusUnit::settings),
                        self.listOf().optionalFieldOf("packages", List.of()).forGetter(FocusUnit::packages))
                .apply(i, FocusUnit::new));
        return RecordCodecBuilder.create(
                i -> i.group(unit.listOf().fieldOf("nodes").forGetter(FocusPackage::nodes))
                        .apply(i, FocusPackage::new));
    });

    private static final Codec<Spell> LEGACY_CODEC = PACKAGE_CODEC.flatXmap(
            LegacyFocusPackage::toSpell, spell -> DataResult.error(() -> "Legacy focus packages are read-only"));

    public static final Codec<Spell> CODEC = Codec.withAlternative(Spell.CODEC, LEGACY_CODEC);

    private LegacyFocusPackage() {}

    private static DataResult<Spell> toSpell(FocusPackage pack) {
        SpellNode root = SpellNode.of(Spell.ORIGIN).withChildren(chain(pack.nodes()));
        if (root.depth() > SpellNode.MAX_DEPTH || !fitsChildLimit(root)) {
            return DataResult.error(() -> "Legacy focus package does not fit the spell limits");
        }
        return DataResult.success(new Spell(CastStyle.INSTANT, root));
    }

    private static boolean fitsChildLimit(SpellNode node) {
        if (node.children().size() > SpellNode.MAX_CHILDREN) {
            return false;
        }
        for (SpellNode child : node.children()) {
            if (!fitsChildLimit(child)) {
                return false;
            }
        }
        return true;
    }

    private static List<SpellNode> chain(List<FocusUnit> units) {
        List<SpellNode> tail = List.of();
        for (int i = units.size() - 1; i >= 0; i--) {
            tail = convert(units.get(i), tail);
        }
        return tail;
    }

    private static List<SpellNode> convert(FocusUnit unit, List<SpellNode> tail) {
        ResourceLocation key = unit.key();
        if (key.equals(ROOT)) {
            return tail;
        }
        if (key.equals(SPLIT_TARGET) || key.equals(SPLIT_TRAJECTORY)) {
            List<SpellNode> branches = new ArrayList<>();
            for (FocusPackage branch : unit.packages()) {
                branches.addAll(chain(branch.nodes()));
            }
            branches.addAll(tail);
            return List.of(SpellNode.of(TTSpellParts.BRANCH).withChildren(branches));
        }
        Map<String, Integer> settings = new HashMap<>(unit.settings());
        List<SpellNode> modifiers = new ArrayList<>();
        if (key.equals(PROJECTILE)) {
            int option = settings.getOrDefault(OPTION, 0);
            settings.remove(OPTION);
            if (option == OPTION_BOUNCY) {
                modifiers.add(SpellNode.of(TTSpellParts.BOUNCE));
            } else if (option == OPTION_SEEK_ENEMY) {
                modifiers.add(SpellNode.of(TTSpellParts.HOMING));
            }
        } else if (key.equals(BREAK) || key.equals(EXCHANGE)) {
            int fortune = settings.getOrDefault(FORTUNE, 0);
            boolean silk = settings.getOrDefault(SILK, 0) > 0;
            settings.remove(FORTUNE);
            settings.remove(SILK);
            if (silk) {
                modifiers.add(SpellNode.of(TTSpellParts.SILK_TOUCH));
            }
            if (fortune > 0) {
                modifiers.add(SpellNode.of(TTSpellParts.FORTUNE).withSetting(LEVEL, fortune));
            }
        }
        SpellNode node =
                new SpellNode(ResourceKey.create(SpellPart.REGISTRY_KEY, key), Optional.empty(), settings, tail);
        for (int i = modifiers.size() - 1; i >= 0; i--) {
            node = modifiers.get(i).withChildren(List.of(node));
        }
        return List.of(node);
    }

    private record FocusPackage(List<FocusUnit> nodes) {}

    private record FocusUnit(ResourceLocation key, Map<String, Integer> settings, List<FocusPackage> packages) {}
}
