package com.leclowndu93150.thaumaturge.data.spell;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.spell.action.DamageAction;
import com.leclowndu93150.thaumaturge.content.spell.engine.SpellAnalyzer;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import org.junit.jupiter.api.Test;

class SpellModifierBalanceTest {
    @Test
    void curseDirectDamageIsHalved() throws Exception {
        var method = AffinityBootstrap.class.getDeclaredMethod("curse");
        method.setAccessible(true);
        var actions = (java.util.List<?>) method.invoke(null);
        var damage = (DamageAction) actions.getFirst();
        assertEquals(0.5F, damage.base());
        assertEquals(0.5F, damage.perPower());
    }

    @Test
    @SuppressWarnings("unchecked")
    void modifierLimitsAndSpreadCostsAreRegistered() throws Exception {
        BootstrapContext<SpellPart> context = mock(BootstrapContext.class);
        Map<ResourceKey<SpellPart>, SpellPart> parts = new HashMap<>();
        when(context.register(any(ResourceKey.class), any(SpellPart.class))).thenAnswer(call -> {
            parts.put(call.getArgument(0), call.getArgument(1));
            return null;
        });
        var method = SpellPartBootstrap.class.getDeclaredMethod("modifiers", BootstrapContext.class);
        method.setAccessible(true);
        method.invoke(null, context);
        assertEquals(1, parts.get(TTSpellParts.AMPLIFY).maxPerSpell());
        assertEquals(1, parts.get(TTSpellParts.EXTEND).maxPerSpell());
        assertEquals(1, parts.get(TTSpellParts.QUICKEN).maxPerSpell());
        assertEquals(0.5F, parts.get(TTSpellParts.QUICKEN).cooldownMultiplier());
        assertTrue(parts.get(TTSpellParts.SCATTER).setting("cone").orElseThrow().complexity() < 0);
        assertTrue(parts.get(TTSpellParts.FAN).setting("arc").orElseThrow().complexity() < 0);
        var registries = mock(HolderLookup.Provider.class);
        for (var entry :
                Map.of(TTSpellParts.SCATTER, "cone", TTSpellParts.FAN, "arc").entrySet()) {
            SpellPart part = parts.get(entry.getKey());
            var setting = part.setting(entry.getValue()).orElseThrow();
            var base = SpellNode.of(entry.getKey()).withSetting(entry.getValue(), setting.min());
            var wider = base.withSetting(entry.getValue(), setting.min() + setting.step());
            var widest = base.withSetting(entry.getValue(), setting.max());
            assertTrue(SpellAnalyzer.nodeComplexity(wider, part, registries)
                    < SpellAnalyzer.nodeComplexity(base, part, registries));
            assertTrue(SpellAnalyzer.nodeComplexity(widest, part, registries) >= 2);
        }
    }
}
