package io.github.brainage04.hudrendererlib.gametest;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.CoreSettingsIdAssigner;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.hud.core.CoreHudElement;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import io.github.brainage04.hudrendererlib.util.ConfigUtils;
import io.github.brainage04.hudrendererlib.util.LayerInfo;
import io.github.brainage04.hudrendererlib.util.VanillaHudLayer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;

/** The same assertions run against each loader's actual HUD and editor. */
public final class HudClientScenarios {
    public interface Driver {
        void onClient(Consumer<Minecraft> action);
        void waitTicks(int ticks);
    }

    private static final List<String> ORDER = new ArrayList<>();
    private static final Probe BEFORE_FIRST = new Probe("before-first", true, 0);
    private static final Probe BEFORE_SECOND = new Probe("before-second", true, 1);
    private static final Probe AFTER_FIRST = new Probe("after-first", false, 2);
    private static final Probe AFTER_SECOND = new Probe("after-second", false, 3);
    private static boolean collectOrder;

    private HudClientScenarios() {}

    public static void run(Driver driver) {
        driver.onClient(client -> {
            check(client.player != null && client.level != null, "Client world and player must be available");
            if (client.gui.hud.isHidden()) client.gui.hud.toggle();
            HudRendererLib.register(FixtureConfig.class, GsonConfigSerializer::new);
            var holder = AutoConfig.getConfigHolder(FixtureConfig.class);
            holder.setConfig(new FixtureConfig());
            CoreSettingsIdAssigner.assignElementIds(holder.getConfig());
            // These deliberately register after the GUI-layer registration event on NeoForge.
            HudRendererLib.registerHudElement(BEFORE_FIRST);
            HudRendererLib.registerHudElement(BEFORE_SECOND);
            HudRendererLib.registerHudElement(AFTER_FIRST);
            HudRendererLib.registerHudElement(AFTER_SECOND);
            HudElementEditor.populateCoreSettingsElements();
            ORDER.clear();
            collectOrder = true;
        });
        try {
            driver.waitTicks(8);
            driver.onClient(client -> {
                check(ORDER.size() >= 8, "Late-registered elements must actually render");
                List<String> expected = List.of("before-first", "before-second", "after-second", "after-first");
                for (int i = 0; i < ORDER.size(); i++) {
                    check(ORDER.get(i).equals(expected.get(i % 4)), "Incorrect HUD extraction order: " + ORDER);
                }
                collectOrder = false;
                BEFORE_FIRST.getElementConfig().getCoreSettings().enabled = false;
            });
            assertStopped(driver, BEFORE_FIRST, "Disabled elements must not render");
            driver.onClient(client -> BEFORE_FIRST.getElementConfig().getCoreSettings().enabled = true);
            assertRendering(driver, BEFORE_FIRST, "Re-enabled elements must resume rendering");
            driver.onClient(client -> client.gui.hud.toggle());
            assertStopped(driver, BEFORE_FIRST, "F1 must hide chat-attached elements");
            driver.onClient(client -> client.gui.hud.toggle());
            assertRendering(driver, BEFORE_FIRST, "Restoring the HUD must resume rendering");
            editorDrag(driver);
            System.out.println("HUD_CLIENT_SCENARIOS_PASS: late registration, layer order, enable/disable, F1, custom bounds and editor drag");
        } finally {
            driver.onClient(client -> {
                collectOrder = false;
                if (client.gui.hud.isHidden()) client.gui.hud.toggle();
                if (client.gui.screen() instanceof HudElementEditor editor) editor.closeWithoutSaving();
                for (Probe probe : List.of(BEFORE_FIRST, BEFORE_SECOND, AFTER_FIRST, AFTER_SECOND)) {
                    probe.getElementConfig().getCoreSettings().enabled = false;
                }
            });
        }
    }

    private static void editorDrag(Driver driver) {
        driver.onClient(client -> client.setScreen(new HudElementEditor()));
        driver.waitTicks(4);
        driver.onClient(client -> {
            HudElementEditor editor = (HudElementEditor) client.gui.screen();
            CoreSettings settings = BEFORE_FIRST.getElementConfig().getCoreSettings();
            ElementCorners bounds = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId).corners;
            check(bounds.right - bounds.left == 80 && bounds.bottom - bounds.top == 16,
                    "Custom-rendered bounds must reach the editor");
            double x = bounds.left + 2;
            double y = bounds.top + 2;
            int oldX = settings.x;
            int oldY = settings.y;
            editor.mouseClicked(mouse(x, y), false);
            check(editor.selectedElementIndex == settings.elementId, "Click must select the custom HUD element");
            editor.mouseDragged(mouse(x + 12, y + 9), 12, 9);
            check(settings.x == oldX + 12 && settings.y == oldY + 9, "Dragging must move the selected element");
            editor.closeWithoutSaving();
        });
    }

    private static MouseButtonEvent mouse(double x, double y) {
        return new MouseButtonEvent(x, y, new MouseButtonInfo(0, 0));
    }

    private static void assertStopped(Driver driver, Probe probe, String message) {
        int[] before = new int[1];
        driver.onClient(client -> before[0] = probe.renders);
        driver.waitTicks(5);
        driver.onClient(client -> check(probe.renders == before[0], message));
    }

    private static void assertRendering(Driver driver, Probe probe, String message) {
        int[] before = new int[1];
        driver.onClient(client -> before[0] = probe.renders);
        driver.waitTicks(5);
        driver.onClient(client -> check(probe.renders > before[0], message));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    @Config(name = "hudrendererlib-gametest")
    public static final class FixtureConfig implements ConfigData {
        public Settings first = new Settings("First", 16, 18);
        public Settings second = new Settings("Second", 16, 42);
        public Settings third = new Settings("Third", 16, 66);
        public Settings fourth = new Settings("Fourth", 16, 90);
    }

    public static final class Settings implements ICoreSettingsContainer {
        private CoreSettings coreSettings;

        public Settings(String name, int x, int y) {
            coreSettings = new CoreSettings(name, true, x, y, ElementAnchor.TOP_LEFT);
        }

        @Override
        public CoreSettings getCoreSettings() { return coreSettings; }

        @Override
        public void setCoreSettings(CoreSettings settings) { coreSettings = settings; }
    }

    private static final class Probe implements CoreHudElement<Settings> {
        private final String label;
        private final boolean before;
        private final int index;
        private int renders;

        private Probe(String label, boolean before, int index) {
            this.label = label;
            this.before = before;
            this.index = index;
        }

        @Override
        public Settings getElementConfig() {
            FixtureConfig config = AutoConfig.getConfigHolder(FixtureConfig.class).getConfig();
            return switch (index) {
                case 0 -> config.first;
                case 1 -> config.second;
                case 2 -> config.third;
                default -> config.fourth;
            };
        }

        @Override
        public LayerInfo getLayerInfo() { return new LayerInfo(VanillaHudLayer.CHAT, before); }

        @Override
        public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
            renders++;
            if (collectOrder) ORDER.add(label);
            CoreSettings settings = getElementConfig().getCoreSettings();
            int x = HudRenderer.getPosX(settings, 80);
            int y = HudRenderer.getPosY(settings, 16);
            HudRenderer.setElementBounds(settings, x, y, x + 80, y + 16);
            graphics.fill(x, y, x + 80, y + 16, 0x80000000);
            graphics.text(Minecraft.getInstance().font, label, x + 2, y + 2, 0xFFFFFFFF);
        }
    }
}
