package io.github.brainage04.hudrendererlib.gametest;

import io.github.brainage04.minecraftmodconventions.ClientGameTestServers;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.Minecraft;

@SuppressWarnings("UnstableApiUsage")
public final class HudRendererLibClientGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        ClientGameTestServers.withDedicatedServer(context, ClientGameTestServers.flatServerProperties(),
                "HudRendererLib HUD API GameTest", server -> {
                    ClientGameTestServers.assertClientWorldAndPlayerAvailable(context);
                    HudClientScenarios.run(new HudClientScenarios.Driver() {
                        @Override
                        public void onClient(Consumer<Minecraft> action) {
                            context.runOnClient(action::accept);
                        }

                        @Override
                        public void waitTicks(int ticks) {
                            context.waitTicks(ticks);
                        }
                    });
                });
    }
}
