package com.leclowndu93150.thaumaturge.content.spell.engine;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.part.AspectInput;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import org.junit.jupiter.api.Test;

class SpellSpreadValidationTest {
    @Test
    void spreadMustLeadToDeliveryAndCannotCombine() {
        HolderLookup.Provider registries = mock(HolderLookup.Provider.class);
        SpellPart spread = part(SpellPartKind.MODIFIER);
        SpellPart delivery = part(SpellPartKind.DELIVERY);
        SpellPart effect = part(SpellPartKind.EFFECT);
        try (var spells = mockStatic(Spells.class)) {
            spells.when(() -> Spells.part(any(), any())).thenAnswer(call -> {
                var key = call.getArgument(1);
                if (key.equals(TTSpellParts.FAN) || key.equals(TTSpellParts.SCATTER)) return Optional.of(spread);
                if (key.equals(TTSpellParts.BOLT)) return Optional.of(delivery);
                if (key.equals(TTSpellParts.FIRE)) return Optional.of(effect);
                return Optional.empty();
            });
            SpellNode fire = SpellNode.of(TTSpellParts.FIRE);
            SpellNode bolt = SpellNode.of(TTSpellParts.BOLT).withChildren(List.of(fire));
            assertTrue(analyze(registries, SpellNode.of(TTSpellParts.FAN).withChildren(List.of(bolt))));
            assertFalse(analyze(registries, SpellNode.of(TTSpellParts.FAN).withChildren(List.of(fire))));
            SpellNode both = SpellNode.of(TTSpellParts.FAN)
                    .withChildren(List.of(SpellNode.of(TTSpellParts.SCATTER).withChildren(List.of(bolt))));
            assertFalse(analyze(registries, both));
        }
    }

    private static boolean analyze(HolderLookup.Provider registries, SpellNode node) {
        return SpellAnalyzer.analyze(
                        Spell.empty().withRoot(SpellNode.of(Spell.ORIGIN).withChildren(List.of(node))),
                        null,
                        registries,
                        null)
                .valid();
    }

    private static SpellPart part(SpellPartKind kind) {
        SpellPart part = mock(SpellPart.class);
        SpellBehavior behavior = mock(SpellBehavior.class);
        when(part.kind()).thenReturn(kind);
        when(part.behavior()).thenReturn(behavior);
        when(behavior.maxChildren()).thenReturn(1);
        when(behavior.standalone()).thenReturn(true);
        when(part.aspect()).thenReturn(AspectInput.NONE);
        when(part.research()).thenReturn(Optional.empty());
        when(part.settings()).thenReturn(List.of());
        when(part.visMultiplier()).thenReturn(1F);
        when(part.cooldownMultiplier()).thenReturn(1F);
        return part;
    }
}
