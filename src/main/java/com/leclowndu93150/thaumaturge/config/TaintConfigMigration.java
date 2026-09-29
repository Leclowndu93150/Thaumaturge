package com.leclowndu93150.thaumaturge.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TaintConfigMigration {
    private static final String SPREAD_RATE = "world.taintSpreadRate";
    private static final String FRONTIER_RATE = "world.taintFrontierRate";

    private TaintConfigMigration() {}

    public static void migrate(Path file) {
        if (!Files.isRegularFile(file)) {
            return;
        }
        try (CommentedFileConfig config =
                CommentedFileConfig.builder(file).preserveInsertionOrder().build()) {
            config.load();
            Object old = config.get(SPREAD_RATE);
            if (!(old instanceof Integer || old instanceof Long) || config.contains(FRONTIER_RATE)) {
                return;
            }
            int frontierRate = ((Number) old).intValue();
            config.set(FRONTIER_RATE, frontierRate);
            config.remove(SPREAD_RATE);
            config.save();
            Thaumaturge.LOGGER.info(
                    "Moved taintSpreadRate {} to taintFrontierRate in {}; taintSpreadRate is now the fibre spread chance",
                    frontierRate,
                    file);
        } catch (ParsingException e) {
            Thaumaturge.LOGGER.error("Could not read {} to move taintSpreadRate to taintFrontierRate", file, e);
        }
    }
}
