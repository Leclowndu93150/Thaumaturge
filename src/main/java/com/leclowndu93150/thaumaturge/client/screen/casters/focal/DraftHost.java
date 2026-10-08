package com.leclowndu93150.thaumaturge.client.screen.casters.focal;

import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellNode;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.spell.manipulator.InscriptionCheck;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import net.minecraft.client.gui.Font;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;

public interface DraftHost {
    Spell draft();

    void edit(UnaryOperator<Spell> change);

    Selection selection();

    void select(Selection selection);

    SpellSummary summary();

    InscriptionCheck inscribeCheck();

    RegistryAccess registries();

    Player player();

    Font font();

    Optional<SpellPart> part(ResourceKey<SpellPart> key);

    int maxChildren(SpellNode node);

    void place(ResourceKey<SpellPart> key);

    void playClick();

    void tooltip(List<Component> lines, int x, int y);

    default void tooltip(Component text, int x, int y) {
        tooltip(List.of(text), x, y);
    }
}
