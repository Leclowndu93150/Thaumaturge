package com.leclowndu93150.thaumaturge.gametest.base;

import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class TTInlineTest extends GameTestInstance {
    private static final MapCodec<TTInlineTest> CODEC = MapCodec.unit((TTInlineTest) null);

    private final Consumer<GameTestHelper> body;

    public TTInlineTest(TestData<Holder<TestEnvironmentDefinition<?>>> info, Consumer<GameTestHelper> body) {
        super(info);
        this.body = body;
    }

    @Override
    public void run(GameTestHelper helper) {
        body.accept(helper);
    }

    @Override
    public MapCodec<? extends GameTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal("thaumaturge:inline");
    }
}
