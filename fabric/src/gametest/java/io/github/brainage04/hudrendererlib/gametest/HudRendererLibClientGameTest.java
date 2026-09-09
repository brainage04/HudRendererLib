package io.github.brainage04.hudrendererlib.gametest;

import io.github.brainage04.fabricmoddingconventions.ClientGameTestRecorder;
import io.github.brainage04.fabricmoddingconventions.ClientGameTestServers;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.hudrendererlib.util.LayerInfo;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Properties;

@SuppressWarnings("UnstableApiUsage")
public final class HudRendererLibClientGameTest implements FabricClientGameTest {
    private static final DemoHudElement TOP_LEFT = new DemoHudElement(
            "Vanilla chat: before",
            new CoreSettings("Vanilla chat: before", true, 10, 10, ElementAnchor.TOP_LEFT),
            new LayerInfo(Identifier.fromNamespaceAndPath("minecraft", "chat"), true),
            ChatFormatting.AQUA
    );
    private static final DemoHudElement BOTTOM_RIGHT = new DemoHudElement(
            "Vanilla chat: after",
            new CoreSettings("Vanilla chat: after", true, 12, 56, ElementAnchor.BOTTOM_RIGHT),
            new LayerInfo(Identifier.fromNamespaceAndPath("minecraft", "chat"), false),
            ChatFormatting.GOLD
    );

    @Override
    public void runTest(ClientGameTestContext context) {
        Properties serverProperties = ClientGameTestServers.flatServerProperties();

        ClientGameTestServers.withDedicatedServer(context, serverProperties, "HudRendererLib HUD API GameTest", server -> {
            ClientGameTestServers.assertClientWorldAndPlayerAvailable(context);
            context.runOnClient(client -> registerFixture());
            context.waitTicks(20);
        
            assertRegistrations();
            ClientGameTestRecorder.showStep(
                    context,
                    "hud.before-chat",
                    "HUD anchored before vanilla chat",
                    "Aqua demo lines use the top-left anchor and render in the layer immediately before vanilla chat."
            );
            context.waitTicks(60);
        
            context.runOnClient(client -> configureBottomRightFixture());
            ClientGameTestRecorder.showStep(
                    context,
                    "hud.after-chat",
                    "Configurable HUD after vanilla chat",
                    "Gold demo lines use a bottom-right anchor, custom padding, colour, and translucent backdrop after vanilla chat."
            );
            context.waitTicks(60);
        });
    }

    private static void registerFixture() {
        if (!HudRenderer.REGISTERED_ELEMENTS.contains(TOP_LEFT)) {
            HudRendererLib.registerHudElement(TOP_LEFT);
        }
        if (!HudRenderer.REGISTERED_ELEMENTS.contains(BOTTOM_RIGHT)) {
            HudRendererLib.registerHudElement(BOTTOM_RIGHT);
        }
    }

    private static void configureBottomRightFixture() {
        CoreSettings settings = BOTTOM_RIGHT.getElementConfig().getCoreSettings();
        settings.elementOverrides.padding.enabled = true;
        settings.elementOverrides.padding.value = 6;
        settings.elementOverrides.textColour.enabled = true;
        settings.elementOverrides.textColour.value = 0xFFD54F;
        settings.elementOverrides.backdropOpacity.enabled = true;
        settings.elementOverrides.backdropOpacity.value = 65;
    }

    private static void assertRegistrations() {
        long registeredFixtureElements = HudRenderer.REGISTERED_ELEMENTS.stream()
                .filter(element -> element == (Object) TOP_LEFT || element == (Object) BOTTOM_RIGHT)
                .count();
        if (registeredFixtureElements != 2) {
            throw new AssertionError("Expected both HUD API fixture elements to be registered, found "
                    + registeredFixtureElements);
        }
        if (TOP_LEFT.getLayerInfo().before() == BOTTOM_RIGHT.getLayerInfo().before()) {
            throw new AssertionError("Fixture elements must demonstrate both sides of the vanilla chat layer");
        }
    }

    private static final class DemoHudElement implements BasicCoreHudElement<DemoSettings> {
        private final String label;
        private final LayerInfo layerInfo;
        private final ChatFormatting colour;
        private final DemoSettings settings;

        private DemoHudElement(String label, CoreSettings coreSettings, LayerInfo layerInfo, ChatFormatting colour) {
            this.label = label;
            this.settings = new DemoSettings(coreSettings);
            this.layerInfo = layerInfo;
            this.colour = colour;
        }

        @Override
        public TextList getLines() {
            TextList lines = new TextList();
            lines.addHeader(Component.literal("HudRendererLib").withStyle(colour));
            lines.add(Component.literal(label).withStyle(colour));
            lines.add("Public API fixture");
            return lines;
        }

        @Override
        public DemoSettings getElementConfig() {
            return this.settings;
        }

        @Override
        public LayerInfo getLayerInfo() {
            return this.layerInfo;
        }
    }

    private static final class DemoSettings implements ICoreSettingsContainer {
        private CoreSettings coreSettings;

        private DemoSettings(CoreSettings coreSettings) {
            this.coreSettings = coreSettings;
        }

        @Override
        public CoreSettings getCoreSettings() {
            return this.coreSettings;
        }

        @Override
        public void setCoreSettings(CoreSettings coreSettings) {
            this.coreSettings = coreSettings;
        }
    }
}
