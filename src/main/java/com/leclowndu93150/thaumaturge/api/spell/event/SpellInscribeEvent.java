package com.leclowndu93150.thaumaturge.api.spell.event;

import com.leclowndu93150.thaumaturge.api.spell.Spell;
import com.leclowndu93150.thaumaturge.api.spell.SpellSummary;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Fired on the game bus when a player asks a focal manipulator to inscribe a spell, after the
 * spell has been validated and before any experience or crystals are taken. Server side only.
 *
 * <p>Cancelling the event refuses the inscription; nothing is paid and the focus is left as it was.
 *
 * @since 1.0.0
 */
public final class SpellInscribeEvent extends Event implements ICancellableEvent {
    private final Player player;
    private final BlockPos pos;
    private final ItemStack focus;
    private final Spell spell;
    private final SpellSummary summary;

    /**
     * Creates the event.
     *
     * @param player  the inscribing player
     * @param pos     the focal manipulator's position
     * @param focus   the focus about to receive the spell; read only
     * @param spell   the validated spell
     * @param summary the spell's analysis against the focus tier
     */
    public SpellInscribeEvent(Player player, BlockPos pos, ItemStack focus, Spell spell, SpellSummary summary) {
        this.player = player;
        this.pos = pos;
        this.focus = focus;
        this.spell = spell;
        this.summary = summary;
    }

    /**
     * The inscribing player.
     *
     * @return the player
     */
    public Player player() {
        return player;
    }

    /**
     * Where the focal manipulator stands.
     *
     * @return the block position
     */
    public BlockPos pos() {
        return pos;
    }

    /**
     * The focus about to receive the spell. Modifying the stack has no defined effect.
     *
     * @return the focus stack
     */
    public ItemStack focus() {
        return focus;
    }

    /**
     * The spell to inscribe.
     *
     * @return the spell
     */
    public Spell spell() {
        return spell;
    }

    /**
     * The spell's costs and limits against the focus tier.
     *
     * @return the analysis
     */
    public SpellSummary summary() {
        return summary;
    }
}
