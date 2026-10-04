package com.leclowndu93150.thaumaturge.api.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.TCResearchCategories;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Factories for the common scan subjects. Every subject returned here grants one fixed research key on a match and does nothing
 * else unless stated; hand it to {@link ScanningManager#register}.
 *
 * <p>Tests run on both sides: the client asks before it starts the thaumometer animation, the server asks again when the scan
 * completes. They must not change state.
 *
 * @since 1.0.0
 */
public final class Scans {
    private static final int ASPECT_OBSERVATION = 1;

    private Scans() {}

    /**
     * @param research the research granted on a match
     * @param test     decides whether a target matches
     * @return a subject granting {@code research} whenever {@code test} holds
     */
    public static IScannable matching(Identifier research, BiPredicate<Player, ScanTarget> test) {
        return new PredicateScan(research, test, PredicateScan.NO_EFFECT);
    }

    /**
     * Matches the blocks placed in the world, and their item forms wherever a stack can be scanned.
     *
     * @param research the research granted on a match
     * @param blocks   the blocks matched
     * @return the subject
     */
    public static IScannable blocks(Identifier research, Block... blocks) {
        List<Block> placed = List.of(blocks);
        List<Item> forms = itemForms(placed.stream());
        return matching(research,
                (player, target) -> target instanceof ScannedBlock(var pos) ? placed.contains(player.level().getBlockState(pos).getBlock()) : forms.contains(target.carriedStack().getItem()));
    }

    /**
     * Matches one exact block state placed in the world.
     *
     * @param research     the research granted on a match
     * @param state        the state matched
     * @param withItemForm whether the block's item form matches as well
     * @return the subject
     */
    public static IScannable blockState(Identifier research, BlockState state, boolean withItemForm) {
        List<Item> forms = withItemForm ? itemForms(Stream.of(state.getBlock())) : List.of();
        return matching(research, (player, target) -> target instanceof ScannedBlock(var pos) ? player.level().getBlockState(pos) == state : forms.contains(target.carriedStack().getItem()));
    }

    /**
     * Matches placed blocks in a tag. Item forms do not match.
     *
     * @param research the research granted on a match
     * @param tag      the block tag
     * @return the subject
     */
    public static IScannable blockTag(Identifier research, TagKey<Block> tag) {
        return matching(research, (player, target) -> target instanceof ScannedBlock(var pos) && player.level().getBlockState(pos).is(tag));
    }

    /**
     * Matches a stack held, in an inventory slot or dropped in the world.
     *
     * @param research the research granted on a match
     * @param item     the item matched
     * @return the subject
     */
    public static IScannable item(Identifier research, ItemLike item) {
        Item matched = item.asItem();
        return matching(research, (player, target) -> target.carriedStack().is(matched));
    }

    /**
     * Matches stacks in a tag, including the item form of a placed block.
     *
     * @param research the research granted on a match
     * @param tag      the item tag
     * @return the subject
     */
    public static IScannable itemTag(Identifier research, TagKey<Item> tag) {
        return matching(research, (player, target) -> ScanningManager.stackOf(player, target).is(tag));
    }

    /**
     * @param research the research granted on a match
     * @param type     the entity type matched
     * @return the subject
     */
    public static IScannable entityType(Identifier research, EntityType<?> type) {
        return entities(research, entity -> entity.getType() == type);
    }

    /**
     * Matches entities in the world, dropped item entities included.
     *
     * @param research the research granted on a match
     * @param test     decides whether an entity matches
     * @return the subject
     */
    public static IScannable entities(Identifier research, Predicate<Entity> test) {
        return matching(research, (player, target) -> target instanceof ScannedEntity(var entity) && test.test(entity));
    }

    /**
     * Matches anything whose aspects include {@code aspect}. A successful scan also grants one observation point in Auromancy,
     * Basics and Alchemy.
     *
     * @param research the research granted on a match
     * @param aspect   the aspect looked for
     * @return the subject
     */
    public static IScannable aspect(Identifier research, Holder<IAspect> aspect) {
        return new PredicateScan(research, (player, target) -> ScanningManager.aspectsOf(player, target).amountOf(aspect) > 0, (player, target) -> {
            ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION, TCResearchCategories.AUROMANCY.identifier(), ASPECT_OBSERVATION);
            ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION, TCResearchCategories.BASICS.identifier(), ASPECT_OBSERVATION);
            ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION, TCResearchCategories.ALCHEMY.identifier(), ASPECT_OBSERVATION);
        });
    }

    private static List<Item> itemForms(Stream<Block> blocks) {
        return blocks.map(Block::asItem).filter(item -> item != Items.AIR).toList();
    }
}
