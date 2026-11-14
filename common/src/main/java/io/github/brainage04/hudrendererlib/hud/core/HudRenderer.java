package io.github.brainage04.hudrendererlib.hud.core;

import static io.github.brainage04.hudrendererlib.util.ConfigUtils.getConfig;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.CoreSettings;
import io.github.brainage04.hudrendererlib.config.core.CoreSettingsElement;
import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.config.core.ElementCorners;
import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.TextList;
import io.github.brainage04.hudrendererlib.util.TextWrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.effect.MobEffectInstance;

public class HudRenderer {
    public static final List<CoreHudElement<? extends ICoreSettingsContainer>> REGISTERED_ELEMENTS =
            new ArrayList<>();

    /** The registered element with {@code elementId}, or {@code null}. */
    public static CoreHudElement<? extends ICoreSettingsContainer> findElement(int elementId) {
        for (CoreHudElement<? extends ICoreSettingsContainer> element : REGISTERED_ELEMENTS) {
            if (element.getElementConfig().getCoreSettings().elementId == elementId) {
                return element;
            }
        }
        return null;
    }

    /**
     * Draws {@code lines} as a text element wrapped at the max width of the element these settings
     * belong to ({@link HudRendererLib#getMaxWidth(CoreSettings)}), its own default included.
     */
    public static void renderElement(
            Font renderer,
            GuiGraphicsExtractor drawContext,
            TextList lines,
            CoreSettings coreSettings) {
        renderElement(
                renderer,
                drawContext,
                lines,
                coreSettings,
                HudRendererLib.getMaxWidth(coreSettings));
    }

    /**
     * Draws {@code lines} as a text element, each line wrapped at its spaces at {@code maxWidth}
     * GUI pixels with {@link TextWrapper#wrapWords}, never inside a word; {@code 0} or less draws
     * the lines as they are. Every line is aligned on its own to the element's anchor: left,
     * centred or right.
     */
    public static void renderElement(
            Font renderer,
            GuiGraphicsExtractor drawContext,
            TextList lines,
            CoreSettings coreSettings,
            int maxWidth) {
        if (lines.isEmpty()) {
            CoreSettingsElement element =
                    HudElementEditor.CORE_SETTINGS_ELEMENTS.get(coreSettings.elementId);
            if (element != null) {
                element.corners.left = -1;
                element.corners.top = -1;
                element.corners.right = -1;
                element.corners.bottom = -1;
            }
            return;
        }

        int elementWidth = 0;

        int elementPadding = HudRendererLib.getPadding(coreSettings);
        List<WrappedLine> wrappedLines = wrapLines(renderer, lines, maxWidth);

        int lineHeight = renderer.lineHeight + elementPadding;
        int elementHeight = lineHeight * wrappedLines.size() + elementPadding;

        int posY = getPosY(coreSettings, elementHeight);

        for (WrappedLine line : wrappedLines) {
            elementWidth = Math.max(elementWidth, line.width());
        }

        int posX = getPosX(coreSettings, elementWidth);

        ElementCorners corners =
                setElementBounds(
                        coreSettings,
                        getCornersWithPadding(
                                posX,
                                posY,
                                posX + elementWidth,
                                posY + elementHeight,
                                coreSettings));
        renderBackdrop(drawContext, corners, coreSettings);

        for (int i = 0; i < wrappedLines.size(); i++) {
            WrappedLine line = wrappedLines.get(i);
            int linePosX = getPosX(coreSettings, line.width());

            drawContext.text(
                    renderer,
                    line.text(),
                    linePosX,
                    posY + (lineHeight * i),
                    HudRendererLib.getTextColour(coreSettings),
                    HudRendererLib.getTextShadows(coreSettings));
        }
    }

    private static List<WrappedLine> wrapLines(Font renderer, TextList lines, int maxWidth) {
        List<WrappedLine> wrappedLines = new ArrayList<>();

        for (Component line : lines) {
            for (Component wrapped :
                    TextWrapper.wrapWords(line, maxWidth, TextWrapper.widthOf(renderer))) {
                FormattedCharSequence text = wrapped.getVisualOrderText();
                wrappedLines.add(new WrappedLine(text, renderer.width(text)));
            }
        }

        return wrappedLines;
    }

    public static int getXOffset(CoreSettings coreSettings, int elementWidth) {
        return getXOffset(
                coreSettings.elementAnchor,
                getScaledWidth(),
                elementWidth,
                HudRendererLib.getPadding(coreSettings));
    }

    /**
     * The x offset of content {@code width} wide from an element anchored at {@code anchor} on a
     * screen {@code screenWidth} wide: right anchors keep its right edge, centre anchors its
     * centre, left anchors its left edge.
     */
    public static int getXOffset(ElementAnchor anchor, int screenWidth, int width, int padding) {
        return HudLayout.xOffset(anchor, screenWidth, width, padding);
    }

    public static int getXOffsetNoPadding(ElementAnchor elementAnchor, int elementWidth) {
        return HudLayout.xOffsetNoPadding(elementAnchor, getScaledWidth(), elementWidth);
    }

    public static int getPosX(CoreSettings coreSettings, int elementWidth) {
        return coreSettings.x + getXOffset(coreSettings, elementWidth);
    }

    public static int getYOffset(CoreSettings coreSettings, int elementHeight) {
        return HudLayout.yOffset(
                coreSettings.elementAnchor,
                getScaledHeight(),
                elementHeight,
                HudRendererLib.getPadding(coreSettings));
    }

    public static int getYOffsetNoPadding(ElementAnchor elementAnchor, int elementHeight) {
        return HudLayout.yOffsetNoPadding(elementAnchor, getScaledHeight(), elementHeight);
    }

    public static int getPosY(CoreSettings coreSettings, int elementHeight) {
        return coreSettings.y
                + getYOffset(coreSettings, elementHeight)
                + getEffectIconShift(coreSettings.elementAnchor);
    }

    /** How far elements anchored at {@code anchor} move down below the vanilla effect icons. */
    static int getEffectIconShift(ElementAnchor anchor) {
        if (anchor == ElementAnchor.TOP_RIGHT
                && !HudRendererLib.shouldHideVanillaStatusEffects()
                && getConfig().adjustTopRightElementsWithStatusEffects) {
            return getEffectIconOffset(getConfig().adjustTopRightElementsWithStatusEffectsAmount);
        }
        return 0;
    }

    /**
     * How far top-right elements move down for the vanilla status effect icons: {@code amount}, the
     * second row's 26 px more while that row is drawn, and 15 px more in demo mode; none while no
     * icon is drawn, also while a screen such as the inventory lists the effects instead.
     */
    private static int getEffectIconOffset(int amount) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return 0;
        Screen screen = minecraft.gui.screen();
        if (screen != null && screen.showsActiveEffects()) return 0;

        boolean anyIcon = false;
        boolean harmfulIcon = false;
        for (MobEffectInstance effect : player.getActiveEffects()) {
            if (!effect.showIcon()) continue;
            anyIcon = true;
            if (!effect.getEffect().value().isBeneficial()) harmfulIcon = true;
        }
        return HudLayout.effectIconOffset(amount, anyIcon, harmfulIcon, minecraft.isDemo());
    }

    public static void render(
            GuiGraphicsExtractor drawContext,
            DeltaTracker tickCounter,
            CoreHudElement<? extends ICoreSettingsContainer> coreHudElement) {
        if (!coreHudElement.getElementConfig().getCoreSettings().enabled) return;

        if (coreHudElement instanceof BasicCoreHudElement<?> basicHudElement) {
            renderElement(
                    Minecraft.getInstance().font,
                    drawContext,
                    basicHudElement.getLines(),
                    coreHudElement.getElementConfig().getCoreSettings(),
                    basicHudElement.wrapsOwnLines()
                            ? 0
                            : HudRendererLib.getMaxWidth(basicHudElement));
        } else coreHudElement.render(drawContext, tickCounter);
    }

    public static int alignContentX(
            CoreSettings coreSettings, int elementX, int elementWidth, int contentWidth) {
        return HudLayout.alignContentX(
                coreSettings.elementAnchor, elementX, elementWidth, contentWidth);
    }

    public static ElementCorners setElementBounds(
            CoreSettings coreSettings, int left, int top, int right, int bottom) {
        return setElementBounds(coreSettings, new ElementCorners(left, top, right, bottom));
    }

    public static ElementCorners setElementBounds(
            CoreSettings coreSettings, ElementCorners corners) {
        CoreSettingsElement coreSettingsElement =
                HudElementEditor.CORE_SETTINGS_ELEMENTS.get(coreSettings.elementId);
        if (coreSettingsElement == null) {
            HudRendererLib.LOGGER.error(
                    "Core settings element with element ID {} in HudElementEditor.CORE_SETTINGS_ELEMENTS does not exist - this shouldn't happen!",
                    coreSettings.elementId);
        } else {
            coreSettingsElement.corners = corners;
        }
        return corners;
    }

    public static void renderBackdrop(
            GuiGraphicsExtractor drawContext, ElementCorners corners, CoreSettings coreSettings) {
        int backdropOpacity = HudRendererLib.getOpacity(coreSettings);
        if (backdropOpacity > 0) {
            drawContext.fill(
                    corners.left,
                    corners.top,
                    corners.right,
                    corners.bottom,
                    backdropOpacity << 24);
        }
    }

    public static ElementCorners getCornersWithPadding(
            int left, int top, int right, int bottom, CoreSettings coreSettings) {
        int elementPadding = HudRendererLib.getPadding(coreSettings);

        return new PaddedElementCorners(
                left - elementPadding * 2,
                top - elementPadding * 2,
                right + elementPadding * 2,
                bottom);
    }

    /**
     * The width of an element's content inside its editor bounds: padded bounds (from {@link
     * #getCornersWithPadding}) lose their padding on both sides, other bounds are all content.
     */
    static int getContentWidth(CoreSettings settings, ElementCorners corners) {
        int width = corners.right - corners.left;
        return corners instanceof PaddedElementCorners
                ? width - HudRendererLib.getPadding(settings) * 4
                : width;
    }

    static int getEditorPosX(CoreSettings settings, ElementCorners corners) {
        int width = corners.right - corners.left;
        return corners instanceof PaddedElementCorners
                ? settings.x + getXOffsetNoPadding(settings.elementAnchor, width)
                : getPosX(settings, width);
    }

    static int getEditorPosY(CoreSettings settings, ElementCorners corners) {
        int y = getPosY(settings, corners.bottom - corners.top);
        if (corners instanceof PaddedElementCorners) {
            y -=
                    HudLayout.editorPaddingY(
                            settings.elementAnchor, HudRendererLib.getPadding(settings));
        }
        return y;
    }

    // Keep the padding contract with the bounds, not the element interface: custom renderers can
    // use the same padded bounds as BasicCoreHudElement, and can extend them before registration.
    private static final class PaddedElementCorners extends ElementCorners {
        private PaddedElementCorners(int left, int top, int right, int bottom) {
            super(left, top, right, bottom);
        }
    }

    public static int getScaledWidth() {
        return Minecraft.getInstance().getWindow().getGuiScaledWidth();
    }

    public static int getScaledHeight() {
        return Minecraft.getInstance().getWindow().getGuiScaledHeight();
    }

    private record WrappedLine(FormattedCharSequence text, int width) {}
}
