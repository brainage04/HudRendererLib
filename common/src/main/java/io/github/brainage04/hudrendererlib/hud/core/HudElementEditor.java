package io.github.brainage04.hudrendererlib.hud.core;

import static io.github.brainage04.hudrendererlib.config.core.CoreSettingsIdAssigner.INVALID_ID;
import static io.github.brainage04.hudrendererlib.util.ConfigUtils.getConfig;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.CoreSettingsElement;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.ConfigUtils;
import io.github.brainage04.hudrendererlib.util.TextList;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class HudElementEditor extends Screen {
    public static final Map<Integer, CoreSettingsElement> CORE_SETTINGS_ELEMENTS = new HashMap<>();

    static {
        populateCoreSettingsElements();
    }

    public int selectedElementIndex = INVALID_ID;

    public double selectedElementX = -1;
    public double selectedElementY = -1;

    public double selectedMouseX = -1;
    public double selectedMouseY = -1;

    public int highlightedElementIndex = INVALID_ID;

    /** The narrowest max width the editor sets, in GUI pixels. */
    public static final int MIN_MAX_WIDTH = 10;

    /** How far from the max width handle a click still grabs it, in GUI pixels. */
    private static final int HANDLE_REACH = 2;

    private static final int HANDLE_COLOUR = 0xFF_FF_AA_00;

    /** Whether the left button is dragging the selected element's max width handle. */
    public boolean resizingMaxWidth;

    private Integer previousMenuBackgroundBlurriness;

    public HudElementEditor() {
        super(Component.literal("HUD Element Editor"));

        populateCoreSettingsElements();
    }

    public static void populateCoreSettingsElements() {
        CORE_SETTINGS_ELEMENTS.clear();

        for (CoreHudElement<? extends ICoreSettingsContainer> coreHudElement :
                HudRenderer.REGISTERED_ELEMENTS) {
            CORE_SETTINGS_ELEMENTS.put(
                    coreHudElement.getElementConfig().getCoreSettings().elementId,
                    new CoreSettingsElement(
                            new ElementCorners(),
                            coreHudElement.getElementConfig().getCoreSettings()));
        }
    }

    public static boolean mouseInRect(
            int x1, int y1, int x2, int y2, double mouseX, double mouseY) {
        return (x1 <= mouseX && mouseX <= x2 && y1 <= mouseY && mouseY <= y2);
    }

    public void disableBlur() {
        OptionInstance<Integer> menuBackgroundBlurriness =
                Minecraft.getInstance().options.menuBackgroundBlurriness();
        if (previousMenuBackgroundBlurriness == null) {
            previousMenuBackgroundBlurriness = menuBackgroundBlurriness.get();
        }
        menuBackgroundBlurriness.set(0);
    }

    public void revertBlur() {
        if (previousMenuBackgroundBlurriness != null) {
            Minecraft.getInstance()
                    .options
                    .menuBackgroundBlurriness()
                    .set(previousMenuBackgroundBlurriness);
            previousMenuBackgroundBlurriness = null;
        }
    }

    public int mouseInElement(double mouseX, double mouseY) {
        for (int i : CORE_SETTINGS_ELEMENTS.keySet()) {
            CoreSettingsElement element = CORE_SETTINGS_ELEMENTS.get(i);
            if (!element.coreSettings.enabled) continue;
            ElementCorners corners = element.corners;

            if (mouseInRect(
                    corners.left, corners.top, corners.right, corners.bottom, mouseX, mouseY)) {
                return i;
            }
        }

        return INVALID_ID;
    }

    /** Whether the element with {@code elementId} wraps at its max width. */
    public static boolean supportsMaxWidth(int elementId) {
        CoreHudElement<? extends ICoreSettingsContainer> element =
                HudRenderer.findElement(elementId);
        return element != null && element.supportsMaxWidth();
    }

    /**
     * The element's max width, or the width of its content while it has none, so that the handle
     * starts at the element's edge.
     */
    private static int getEffectiveMaxWidth(CoreSettingsElement element) {
        int maxWidth = HudRendererLib.getMaxWidth(element.coreSettings);
        return maxWidth > 0
                ? maxWidth
                : HudRenderer.getContentWidth(element.coreSettings, element.corners);
    }

    /** The padding on each side of the element's content inside its bounds. */
    private static int getSidePadding(CoreSettingsElement element) {
        ElementCorners corners = element.corners;
        int contentWidth = HudRenderer.getContentWidth(element.coreSettings, corners);
        return (corners.right - corners.left - contentWidth) / 2;
    }

    /**
     * The x of the max width handle: the element's free edge at its max width. A right-anchored
     * element keeps its right edge, so the handle is on its left; a centred one grows both ways, so
     * the handle is on its right and moves half as far as the width changes.
     */
    public static int getMaxWidthHandleX(CoreSettingsElement element) {
        ElementCorners corners = element.corners;
        int maxWidth = getEffectiveMaxWidth(element);
        int padding = getSidePadding(element);
        return switch (element.coreSettings.elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> corners.right - padding * 2 - maxWidth;
            case TOP, CENTER, BOTTOM -> (corners.left + corners.right + maxWidth) / 2 + padding;
            default -> corners.left + padding * 2 + maxWidth;
        };
    }

    /** The max width that puts the handle of {@code element} at {@code mouseX}. */
    private static int getMaxWidthAt(CoreSettingsElement element, double mouseX) {
        ElementCorners corners = element.corners;
        int padding = getSidePadding(element);
        double width =
                switch (element.coreSettings.elementAnchor) {
                    case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> corners.right - padding * 2 - mouseX;
                    case TOP, CENTER, BOTTOM ->
                            (mouseX - padding - (corners.left + corners.right) / 2.0) * 2;
                    default -> mouseX - corners.left - padding * 2;
                };
        return (int) Math.round(width);
    }

    /** Turns the element's own max width on at {@code maxWidth} GUI pixels. */
    public static void setMaxWidth(CoreSettings coreSettings, int maxWidth) {
        coreSettings.elementOverrides.maxWidth.enabled = true;
        coreSettings.elementOverrides.maxWidth.value =
                Mth.clamp(
                        maxWidth,
                        MIN_MAX_WIDTH,
                        Math.max(MIN_MAX_WIDTH, HudRenderer.getScaledWidth()));
    }

    /**
     * Turns the element's own max width off, back to the global Max Element Width or, without one,
     * the element's default.
     */
    public static void clearMaxWidth(CoreSettings coreSettings) {
        coreSettings.elementOverrides.maxWidth.enabled = false;
    }

    /** Whether the element is drawn and wraps at its max width, so the editor shows its handle. */
    private static boolean hasMaxWidthHandle(int elementId) {
        if (elementId == INVALID_ID || !supportsMaxWidth(elementId)) return false;
        ElementCorners corners = CORE_SETTINGS_ELEMENTS.get(elementId).corners;
        return corners.right > corners.left;
    }

    private boolean mouseOnMaxWidthHandle(double mouseX, double mouseY) {
        if (!hasMaxWidthHandle(selectedElementIndex)) return false;
        CoreSettingsElement element = CORE_SETTINGS_ELEMENTS.get(selectedElementIndex);
        ElementCorners corners = element.corners;
        return Math.abs(mouseX - getMaxWidthHandleX(element)) <= HANDLE_REACH
                && corners.top <= mouseY
                && mouseY <= corners.bottom;
    }

    private static String describeMaxWidth(CoreSettings coreSettings) {
        int maxWidth = HudRendererLib.getMaxWidth(coreSettings);
        if (maxWidth <= 0) return "Max Width: off";
        if (coreSettings.elementOverrides.maxWidth.enabled) {
            return "Max Width: %d".formatted(maxWidth);
        }
        return ConfigUtils.getConfig().maxElementWidth > 0
                ? "Max Width: %d (global)".formatted(maxWidth)
                : "Max Width: %d (default)".formatted(maxWidth);
    }

    public final Button button1 =
            Button.builder(Component.literal("Undo & Close"), button -> closeWithoutSaving())
                    .size(200, 20)
                    .tooltip(
                            Tooltip.create(
                                    Component.literal(
                                            "Reverts the current positions to what they were before and closes the screen.")))
                    .build();
    public final Button button2 =
            Button.builder(Component.literal("Save & Close"), button -> this.onClose())
                    .size(200, 20)
                    .tooltip(
                            Tooltip.create(
                                    Component.literal(
                                            "Saves the current positions and closes the screen.")))
                    .build();

    public void updateElementPosition(int deltaX, int deltaY) {
        CoreSettingsElement coreSettingsElement = CORE_SETTINGS_ELEMENTS.get(selectedElementIndex);
        ElementCorners corners = coreSettingsElement.corners;
        CoreSettings coreSettings = coreSettingsElement.coreSettings;
        int elementWidth = corners.right - corners.left;
        int elementHeight = corners.bottom - corners.top;

        coreSettings.x =
                HudLayout.editorX(
                        coreSettings.elementAnchor,
                        selectedElementX - deltaX,
                        getConfig().screenMargin,
                        HudRenderer.getScaledWidth(),
                        elementWidth);
        coreSettings.y =
                HudLayout.editorY(
                        coreSettings.elementAnchor,
                        selectedElementY - deltaY,
                        getConfig().screenMargin,
                        HudRenderer.getScaledHeight(),
                        elementHeight);
    }

    @Override
    protected void init() {
        disableBlur();
        // init() runs again on every resize and GUI scale change, so place the buttons here.
        button1.setPosition(width / 2 - 210, height - 40);
        button2.setPosition(width / 2 + 10, height - 40);
        addRenderableWidget(button1);
        addRenderableWidget(button2);
    }

    @Override
    public void onClose() {
        ConfigUtils.saveConfigClasses();
        revertBlur();
        super.onClose();
    }

    @Override
    public void removed() {
        revertBlur();
        super.removed();
    }

    public void closeWithoutSaving() {
        ConfigUtils.loadConfigClasses();
        revertBlur();
        super.onClose();
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int scancode = event.key();

        if (event.isEscape() && this.shouldCloseOnEsc()) {
            closeWithoutSaving();
            return true;
        }

        if (selectedElementIndex != INVALID_ID) {
            CoreSettingsElement coreSettingsElement =
                    CORE_SETTINGS_ELEMENTS.get(selectedElementIndex);
            CoreSettings coreSettings = coreSettingsElement.coreSettings;

            if (scancode == InputConstants.KEY_SPACE) {
                ElementCorners corners = coreSettingsElement.corners;

                int beforeX = HudRenderer.getEditorPosX(coreSettings, corners);
                int beforeY = HudRenderer.getEditorPosY(coreSettings, corners);
                ElementAnchor[] anchors = ElementAnchor.values();
                coreSettings.elementAnchor =
                        anchors[(coreSettings.elementAnchor.ordinal() + 1) % anchors.length];
                coreSettings.x += beforeX - HudRenderer.getEditorPosX(coreSettings, corners);
                coreSettings.y += beforeY - HudRenderer.getEditorPosY(coreSettings, corners);

                selectedElementX = coreSettings.x;
                selectedElementY = coreSettings.y;
            } else if (supportsMaxWidth(selectedElementIndex)
                    && (scancode == InputConstants.KEY_LBRACKET
                            || scancode == InputConstants.KEY_RBRACKET)) {
                // the same steps as the arrow keys: Ctrl = x5, Shift = x10
                int step = scancode == InputConstants.KEY_LBRACKET ? -1 : 1;
                if (event.hasShiftDown()) step *= 10;
                if (event.hasControlDown()) step *= 5;
                setMaxWidth(coreSettings, getEffectiveMaxWidth(coreSettingsElement) + step);
            } else if (supportsMaxWidth(selectedElementIndex)
                    && (scancode == InputConstants.KEY_BACKSPACE
                            || scancode == InputConstants.KEY_DELETE)) {
                clearMaxWidth(coreSettings);
            } else {
                int xDirection = 0;
                int yDirection = 0;

                switch (scancode) {
                    case InputConstants.KEY_UP:
                        yDirection = 1;
                        break;
                    case InputConstants.KEY_DOWN:
                        yDirection = -1;
                        break;
                    case InputConstants.KEY_LEFT:
                        xDirection = 1;
                        break;
                    case InputConstants.KEY_RIGHT:
                        xDirection = -1;
                        break;
                }

                if (xDirection == 0 && yDirection == 0) {
                    return super.keyPressed(event);
                }

                // shift moves 10 times as far, control 5 times (both: 50)
                if (event.hasShiftDown()) {
                    xDirection *= 10;
                    yDirection *= 10;
                }
                if (event.hasControlDown()) {
                    xDirection *= 5;
                    yDirection *= 5;
                }

                selectedElementX = coreSettings.x;
                selectedElementY = coreSettings.y;

                updateElementPosition(xDirection, yDirection);
            }
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        highlightedElementIndex = mouseInElement(mouseX, mouseY);

        TextList lines = new TextList();
        lines.addHeader(title);
        lines.add("Click to select, hold click and drag to move");
        lines.add("Arrow keys for precise movement, Ctrl = x5, Shift = x10");
        lines.add("Space to cycle alignment");
        lines.add(
                "Drag the orange bar or press [ and ] to set the max width, Backspace to reset it");
        lines.add("Escape to close without saving");
        lines.addEmpty();

        if (highlightedElementIndex != INVALID_ID
                && highlightedElementIndex == selectedElementIndex) {
            ElementCorners corners = CORE_SETTINGS_ELEMENTS.get(highlightedElementIndex).corners;
            context.fill(corners.left, corners.top, corners.right, corners.bottom, 0x7fffffff);
        } else {
            if (highlightedElementIndex != INVALID_ID) {
                ElementCorners corners =
                        CORE_SETTINGS_ELEMENTS.get(highlightedElementIndex).corners;
                context.fill(corners.left, corners.top, corners.right, corners.bottom, 0x7fffffff);
            }

            if (selectedElementIndex != INVALID_ID) {
                ElementCorners corners = CORE_SETTINGS_ELEMENTS.get(selectedElementIndex).corners;
                context.fill(corners.left, corners.top, corners.right, corners.bottom, 0x7fffffff);
            }
        }

        if (hasMaxWidthHandle(selectedElementIndex)) {
            CoreSettingsElement element = CORE_SETTINGS_ELEMENTS.get(selectedElementIndex);
            ElementCorners corners = element.corners;
            int handleX = getMaxWidthHandleX(element);
            context.fill(handleX - 1, corners.top, handleX + 1, corners.bottom, HANDLE_COLOUR);
        }

        if (highlightedElementIndex != INVALID_ID) {
            CoreSettingsElement coreSettingsElement =
                    CORE_SETTINGS_ELEMENTS.get(highlightedElementIndex);
            CoreSettings coreSettings = coreSettingsElement.coreSettings;

            lines.add(coreSettings.elementName);
            lines.add("X: %d Y: %d".formatted(coreSettings.x, coreSettings.y));
            lines.add("Anchor: %s".formatted(coreSettings.elementAnchor.toString()));
            if (supportsMaxWidth(highlightedElementIndex)) {
                lines.add(describeMaxWidth(coreSettings));
            }

            if (highlightedElementIndex == selectedElementIndex) {
                lines.add("Highlighted & Selected");
            } else {
                lines.add("Highlighted");
            }
        } else if (selectedElementIndex != INVALID_ID) {
            CoreSettings coreSettings =
                    CORE_SETTINGS_ELEMENTS.get(selectedElementIndex).coreSettings;

            lines.add(coreSettings.elementName);
            lines.add("X: %d Y: %d".formatted(coreSettings.x, coreSettings.y));
            lines.add("Anchor: %s".formatted(coreSettings.elementAnchor.toString()));
            if (supportsMaxWidth(selectedElementIndex)) {
                lines.add(describeMaxWidth(coreSettings));
            }
            lines.add("Selected");
        }

        for (int i = 0; i < lines.size(); i++) {
            context.centeredText(
                    font,
                    lines.get(i),
                    width / 2,
                    10 + (font.lineHeight + 2) * i,
                    0xFF_00_00_00 + getConfig().textColour);
        }

        super.extractRenderState(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != InputConstants.MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        double mouseX = event.x();
        double mouseY = event.y();

        // the selected element's max width handle may lie outside the element, so it is checked
        // before the click selects another element
        resizingMaxWidth = mouseOnMaxWidthHandle(mouseX, mouseY);
        if (resizingMaxWidth) return true;

        selectedElementIndex = mouseInElement(mouseX, mouseY);

        if (selectedElementIndex != INVALID_ID) {
            CoreSettings coreSettings =
                    CORE_SETTINGS_ELEMENTS.get(selectedElementIndex).coreSettings;

            selectedElementX = coreSettings.x;
            selectedElementY = coreSettings.y;

            selectedMouseX = mouseX;
            selectedMouseY = mouseY;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT
                && selectedElementIndex != INVALID_ID) {
            double mouseX = event.x();
            double mouseY = event.y();
            if (resizingMaxWidth) {
                CoreSettingsElement element = CORE_SETTINGS_ELEMENTS.get(selectedElementIndex);
                setMaxWidth(element.coreSettings, getMaxWidthAt(element, mouseX));
            } else {
                updateElementPosition(
                        (int) (selectedMouseX - mouseX), (int) (selectedMouseY - mouseY));
            }
        }

        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT) resizingMaxWidth = false;

        return super.mouseReleased(event);
    }
}
