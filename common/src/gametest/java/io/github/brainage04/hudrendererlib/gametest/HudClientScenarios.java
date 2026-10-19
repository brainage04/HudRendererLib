package io.github.brainage04.hudrendererlib.gametest;

import io.github.brainage04.hudrendererlib.hud.core.BasicCoreHudElement;
import io.github.brainage04.hudrendererlib.keys.ModKeys;
import io.github.brainage04.hudrendererlib.util.TextList;
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
import net.minecraft.client.KeyMapping;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.lwjgl.glfw.GLFW;
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
    private static final TextProbe TEXT = new TextProbe();
    private static final boolean[] HIDE_EFFECTS = {false};

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
            HudRendererLib.registerHudElement(TEXT);
            HudRendererLib.registerVanillaStatusEffectsHider(() -> HIDE_EFFECTS[0]);
            HudElementEditor.populateCoreSettingsElements();
            ConfigUtils.saveConfigClasses();
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
            commandsAndKeys(driver);
            editorDrag(driver);
            configSaveAndUndo(driver);
            textWrapping(driver);
            basicAnchorCycle(driver);
            editorLifecycle(driver);
            statusEffects(driver);
            System.out.println("HUD_CLIENT_SCENARIOS_PASS: registration/order, F1, enabled, commands/keys, editor, save/undo, text wrapping, status effects");
        } finally {
            driver.onClient(client -> {
                collectOrder = false;
                if (client.gui.hud.isHidden()) client.gui.hud.toggle();
                if (client.gui.screen() instanceof HudElementEditor editor) editor.closeWithoutSaving();
                for (Probe probe : List.of(BEFORE_FIRST, BEFORE_SECOND, AFTER_FIRST, AFTER_SECOND)) {
                    probe.getElementConfig().getCoreSettings().enabled = false;
                }
                TEXT.getElementConfig().getCoreSettings().enabled = false;
                HIDE_EFFECTS[0] = false;
            });
        }
    }

    private static void editorDrag(Driver driver) {
        driver.onClient(client -> client.setScreenAndShow(new HudElementEditor()));
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
            BEFORE_FIRST.getElementConfig().getCoreSettings().enabled = false;
            check(editor.mouseInElement(x, y) != settings.elementId, "Disabled elements must not remain selectable");
            BEFORE_FIRST.getElementConfig().getCoreSettings().enabled = true;
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

    private static void commandsAndKeys(Driver driver) {
        driver.onClient(client -> client.player.connection.sendCommand("hudelementeditor"));
        driver.waitTicks(4);
        driver.onClient(client -> {
            check(client.gui.screen() instanceof HudElementEditor, "/hudelementeditor must open the editor");
            ((HudElementEditor) client.gui.screen()).closeWithoutSaving();
            client.player.connection.sendCommand("hudrendererlibconfig");
        });
        driver.waitTicks(4);
        driver.onClient(client -> {
            check(client.gui.screen() != null && client.gui.screen().getClass().getName().startsWith("me.shedaniel.clothconfig2."),
                    "/hudrendererlibconfig must open Cloth Config");
            client.gui.screen().onClose();
            KeyMapping.click(ModKeys.openElementEditor.getDefaultKey());
        });
        driver.waitTicks(4);
        driver.onClient(client -> {
            check(client.gui.screen() instanceof HudElementEditor, "Editor key must open the editor");
            ((HudElementEditor) client.gui.screen()).closeWithoutSaving();
            KeyMapping configKey = ModKeys.openConfigKeyMap.entrySet().stream()
                    .filter(entry -> entry.getValue() == io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig.class)
                    .findFirst().orElseThrow().getKey();
            KeyMapping.click(configKey.getDefaultKey());
        });
        driver.waitTicks(4);
        driver.onClient(client -> {
            check(client.gui.screen() != null && client.gui.screen().getClass().getName().startsWith("me.shedaniel.clothconfig2."),
                    "Config key must open Cloth Config");
            client.gui.screen().onClose();
        });
    }

    private static void configSaveAndUndo(Driver driver) {
        driver.onClient(client -> {
            CoreSettings settings = BEFORE_FIRST.getElementConfig().getCoreSettings();
            settings.x = 20;
            settings.y = 24;
            settings.elementAnchor = ElementAnchor.TOP_LEFT;
            ConfigUtils.saveConfigClasses();
            client.setScreenAndShow(new HudElementEditor());
        });
        driver.waitTicks(3);
        driver.onClient(client -> {
            HudElementEditor editor = (HudElementEditor) client.gui.screen();
            CoreSettings settings = BEFORE_FIRST.getElementConfig().getCoreSettings();
            editor.selectedElementIndex = settings.elementId;
            editor.keyPressed(new KeyEvent(GLFW.GLFW_KEY_RIGHT, 0, GLFW.GLFW_MOD_CONTROL | GLFW.GLFW_MOD_SHIFT));
            check(settings.x == 70 && settings.y == 24, "Ctrl+Shift+Right must move 50 pixels");
            int x = HudRenderer.getPosX(settings, 80);
            int y = HudRenderer.getPosY(settings, 16);
            editor.keyPressed(new KeyEvent(GLFW.GLFW_KEY_SPACE, 0, 0));
            check(settings.elementAnchor == ElementAnchor.TOP, "Space must cycle the anchor");
            check(HudRenderer.getPosX(settings, 80) == x && HudRenderer.getPosY(settings, 16) == y,
                    "Cycling the anchor must preserve screen position");
            int savedX = settings.x;
            editor.onClose();
            settings.x = -500;
            ConfigUtils.loadConfigClasses();
            CoreSettings loaded = BEFORE_FIRST.getElementConfig().getCoreSettings();
            check(loaded.x == savedX, "Saving the editor must persist its position");
            check(loaded.elementAnchor == ElementAnchor.TOP, "Saving must persist its anchor");
            check(HudElementEditor.CORE_SETTINGS_ELEMENTS.get(loaded.elementId).coreSettings == loaded,
                    "Reload must rebuild the editor map with the new settings objects");
            client.setScreenAndShow(new HudElementEditor());
            BEFORE_FIRST.getElementConfig().getCoreSettings().x = 1234;
            ((HudElementEditor) client.gui.screen()).closeWithoutSaving();
            check(BEFORE_FIRST.getElementConfig().getCoreSettings().x == savedX,
                    "Undo must restore the saved position");
        });
    }

    private static void textWrapping(Driver driver) {
        int[] originalHeight = new int[1];
        driver.onClient(client -> {
            CoreSettings settings = TEXT.getElementConfig().getCoreSettings();
            settings.elementOverrides.maxWidth.enabled = true;
            settings.elementOverrides.maxWidth.value = 0;
            settings.elementOverrides.padding.enabled = true;
            settings.elementOverrides.padding.value = 3;
        });
        driver.waitTicks(4);
        driver.onClient(client -> {
            CoreSettings settings = TEXT.getElementConfig().getCoreSettings();
            ElementCorners bounds = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId).corners;
            originalHeight[0] = bounds.bottom - bounds.top;
            settings.elementOverrides.maxWidth.value = 45;
        });
        driver.waitTicks(4);
        driver.onClient(client -> {
            CoreSettings settings = TEXT.getElementConfig().getCoreSettings();
            ElementCorners bounds = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId).corners;
            check(bounds.right - bounds.left <= 45 + 12, "Wrapped text must respect maximum width plus padding");
            check(bounds.bottom - bounds.top > originalHeight[0], "Wrapping must increase the text element's height");
        });
    }

    private static void editorLifecycle(Driver driver) {
        driver.onClient(client -> {
            int previous = client.options.menuBackgroundBlurriness().get();
            try {
                client.options.menuBackgroundBlurriness().set(7);
                client.setScreenAndShow(new HudElementEditor());
                check(client.options.menuBackgroundBlurriness().get() == 0, "Editor must disable blur while open");
                client.setScreenAndShow(new HudElementEditor());
                check(client.options.menuBackgroundBlurriness().get() == 0, "Replacing an editor must keep blur disabled");
                client.setScreenAndShow(null);
                check(client.options.menuBackgroundBlurriness().get() == 7, "Replacing the editor must restore the previous blur");
            } finally {
                client.setScreenAndShow(null);
                client.options.menuBackgroundBlurriness().set(previous);
            }
        });
        int[] oldBounds = new int[2];
        driver.onClient(client -> client.setScreenAndShow(new HudElementEditor()));
        driver.waitTicks(4);
        driver.onClient(client -> {
            CoreSettings settings = TEXT.getElementConfig().getCoreSettings();
            ElementCorners bounds = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId).corners;
            oldBounds[0] = bounds.left + 1;
            oldBounds[1] = bounds.top + 1;
            TEXT.empty = true;
        });
        driver.waitTicks(4);
        driver.onClient(client -> {
            HudElementEditor editor = (HudElementEditor) client.gui.screen();
            CoreSettings settings = TEXT.getElementConfig().getCoreSettings();
            check(editor.mouseInElement(oldBounds[0], oldBounds[1]) != settings.elementId,
                    "Empty text must not leave a selectable stale rectangle");
            editor.closeWithoutSaving();
            TEXT.empty = false;
        });
    }

    private static void basicAnchorCycle(Driver driver) {
        driver.onClient(client -> client.setScreenAndShow(new HudElementEditor()));
        driver.waitTicks(4);
        for (int i = 0; i < ElementAnchor.values().length; i++) {
            int[] previous = new int[2];
            driver.onClient(client -> {
                CoreSettings settings = TEXT.getElementConfig().getCoreSettings();
                ElementCorners bounds = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId).corners;
                previous[0] = bounds.left;
                previous[1] = bounds.top;
                HudElementEditor editor = (HudElementEditor) client.gui.screen();
                editor.selectedElementIndex = settings.elementId;
                editor.keyPressed(new KeyEvent(GLFW.GLFW_KEY_SPACE, 0, 0));
            });
            driver.waitTicks(3);
            driver.onClient(client -> {
                CoreSettings settings = TEXT.getElementConfig().getCoreSettings();
                ElementCorners bounds = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(settings.elementId).corners;
                check(bounds.left == previous[0] && bounds.top == previous[1],
                        "Cycling a padded text element's anchor must preserve its screen position");
            });
        }
        driver.onClient(client -> ((HudElementEditor) client.gui.screen()).closeWithoutSaving());
    }

    private static void statusEffects(Driver driver) {
        driver.onClient(client -> {
            var config = ConfigUtils.getConfig();
            boolean show = config.showVanillaStatusEffects;
            boolean adjust = config.adjustTopRightElementsWithStatusEffects;
            int amount = config.adjustTopRightElementsWithStatusEffectsAmount;
            try {
                config.showVanillaStatusEffects = true;
                config.adjustTopRightElementsWithStatusEffects = true;
                config.adjustTopRightElementsWithStatusEffectsAmount = 21;
                CoreSettings settings = new CoreSettings("Effect offset", true, 0, 0, ElementAnchor.TOP_RIGHT);
                int base = HudRenderer.getPosY(settings, 16);
                client.player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200, 0, false, false, true));
                check(HudRenderer.getPosY(settings, 16) == base + 21, "Visible effect icons must shift top-right elements");
                HIDE_EFFECTS[0] = true;
                check(HudRendererLib.shouldHideVanillaStatusEffects(), "A registered hider must hide vanilla status effects");
                check(HudRenderer.getPosY(settings, 16) == base, "Hidden effect icons must not shift top-right elements");
                HIDE_EFFECTS[0] = false;
                config.showVanillaStatusEffects = false;
                check(HudRendererLib.shouldHideVanillaStatusEffects(), "Global toggle must hide vanilla status effects");
                check(HudRenderer.getPosY(settings, 16) == base, "Global hidden icons must not shift elements");
                config.showVanillaStatusEffects = true;
                config.adjustTopRightElementsWithStatusEffects = false;
                check(HudRenderer.getPosY(settings, 16) == base, "Disabled adjustment must leave elements in place");
            } finally {
                HIDE_EFFECTS[0] = false;
                config.showVanillaStatusEffects = show;
                config.adjustTopRightElementsWithStatusEffects = adjust;
                config.adjustTopRightElementsWithStatusEffectsAmount = amount;
                client.player.removeEffect(MobEffects.SPEED);
            }
        });
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
        public Settings text = new Settings("Text", 130, 18);
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
    private static final class TextProbe implements BasicCoreHudElement<Settings> {
        private boolean empty;
        @Override
        public Settings getElementConfig() {
            return AutoConfig.getConfigHolder(FixtureConfig.class).getConfig().text;
        }

        @Override
        public TextList getLines() {
            TextList lines = new TextList();
            if (empty) return lines;
            lines.add("A long HUD line wraps into several short lines");
            lines.addEmpty();
            return lines;
        }
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
