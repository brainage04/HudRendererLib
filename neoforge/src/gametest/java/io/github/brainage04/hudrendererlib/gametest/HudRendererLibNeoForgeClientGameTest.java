package io.github.brainage04.hudrendererlib.gametest;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

/** Test-only integrated-world runner; no game-test code is included in the published library. */
@EventBusSubscriber(modid = HudRendererLib.MOD_ID, value = Dist.CLIENT)
public final class HudRendererLibNeoForgeClientGameTest {
    private static final AtomicLong TICKS = new AtomicLong();
    private static boolean worldRequested;
    private static boolean started;

    private HudRendererLibNeoForgeClientGameTest() {}

    @SubscribeEvent
    public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("hudrendererlib.clientGameTest")) return;
        TICKS.incrementAndGet();
        Minecraft client = Minecraft.getInstance();
        try {
            if (!worldRequested && client.gui.screen() != null) {
                worldRequested = true;
                LevelSettings settings = new LevelSettings("HudRendererLib GameTest", GameType.CREATIVE,
                        new LevelSettings.DifficultySettings(Difficulty.PEACEFUL, false, false),
                        true, WorldDataConfiguration.DEFAULT);
                client.createWorldOpenFlows().createFreshLevel("hudrendererlib-client-gametest", settings,
                        new WorldOptions(0L, false, false),
                        registries -> registries.lookupOrThrow(Registries.WORLD_PRESET)
                                .getOrThrow(WorldPresets.FLAT).value().createWorldDimensions(),
                        client.gui.screen());
            }
            if (!started && client.player != null && client.level != null && client.gui.screen() == null) {
                started = true;
                Thread.ofVirtual().name("hudrendererlib-client-gametest").start(() -> run(client));
            }
        } catch (Throwable failure) {
            fail(failure);
        }
    }

    private static void run(Minecraft client) {
        try {
            HudClientScenarios.run(new HudClientScenarios.Driver() {
                @Override
                public void onClient(Consumer<Minecraft> action) {
                    CompletableFuture<Void> completed = new CompletableFuture<>();
                    client.execute(() -> {
                        try {
                            action.accept(client);
                            completed.complete(null);
                        } catch (Throwable failure) {
                            completed.completeExceptionally(failure);
                        }
                    });
                    try {
                        completed.get(30, TimeUnit.SECONDS);
                    } catch (Exception failure) {
                        throw new AssertionError("Client action failed", failure);
                    }
                }

                @Override
                public void waitTicks(int ticks) {
                    long target = TICKS.get() + ticks;
                    long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
                    while (TICKS.get() < target) {
                        if (System.nanoTime() >= deadline) throw new AssertionError("Client ticks stalled");
                        try {
                            Thread.sleep(5);
                        } catch (InterruptedException failure) {
                            Thread.currentThread().interrupt();
                            throw new AssertionError("Client test interrupted", failure);
                        }
                    }
                }
            });
            HudRendererLib.LOGGER.info("HUD_NEOFORGE_CLIENT_GAMETEST_PASS");
            client.execute(client::stop);
        } catch (Throwable failure) {
            fail(failure);
        }
    }

    private static void fail(Throwable failure) {
        HudRendererLib.LOGGER.error("HUD_NEOFORGE_CLIENT_GAMETEST_FAIL", failure);
        System.exit(1);
    }
}
