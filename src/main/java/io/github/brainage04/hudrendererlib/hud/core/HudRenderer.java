package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.*;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;

import java.util.ArrayList;
import java.util.List;

import static io.github.brainage04.hudrendererlib.util.ConfigUtils.getConfig;

public class HudRenderer {
    public static final List<HudElement<? extends ICoreSettingsContainer>> REGISTERED_ELEMENTS = new ArrayList<>();

    public static void renderElement(TextRenderer renderer, DrawContext drawContext, TextList lines, CoreSettings coreSettings) {
        if (lines.isEmpty()) return;

        int elementWidth = 0;

        int elementPadding = HudRendererLib.getPadding(coreSettings);

        int lineHeight = renderer.fontHeight + elementPadding;
        int elementHeight = lineHeight * lines.size() + elementPadding;

        // vertical adjustments
        int posY = getPosY(coreSettings, elementHeight);

        for (int i = 0; i < lines.size(); i++) {
            int lineWidth = renderer.getWidth(lines.get(i));

            elementWidth = Math.max(elementWidth, lineWidth);

            // horizontal adjustments (for line)
            int posX = getPosX(coreSettings, lineWidth);

            drawContext.drawText(
                    renderer,
                    lines.get(i),
                    posX,
                    posY + (lineHeight * i),
                    HudRendererLib.getTextColour(coreSettings),
                    HudRendererLib.getTextShadows(coreSettings)
            );
        }

        // horizontal adjustments (for element)
        int posX = getPosX(coreSettings, elementWidth);

        // adjust for padding
        ElementCorners corners = getCornersWithPadding(posX, posY, posX + elementWidth, posY + elementHeight, coreSettings);

        CoreSettingsElement coreSettingsElement = HudElementEditor.CORE_SETTINGS_ELEMENTS.get(coreSettings.elementId);
        if (coreSettingsElement == null) {
            HudRendererLib.LOGGER.error("Core settings element with index {} in HudElementEditor.CORE_SETTINGS_ELEMENTS does not exist - this shouldn't happen!", coreSettings.elementId);
        } else {
            coreSettingsElement.corners = corners;
        }

        // render backdrop
        int backdropOpacity = HudRendererLib.getOpacity(coreSettings);

        if (backdropOpacity > 0) {
            drawContext.fill(
                    corners.left,
                    corners.top,
                    corners.right,
                    corners.bottom,
                    -1,
                    backdropOpacity << 24
            );
        }
    }

    public static int getXOffset(CoreSettings coreSettings, int elementWidth) {
        int elementPadding = HudRendererLib.getPadding(coreSettings);

        return switch (coreSettings.elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> getScaledWidth() - elementWidth - elementPadding * 2;
            case TOP, CENTER, BOTTOM -> (getScaledWidth() - elementWidth) / 2;
            default -> elementPadding * 2;
        };
    }

    public static int getXOffsetNoPadding(ElementAnchor elementAnchor, int elementWidth) {
        return switch (elementAnchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> getScaledWidth() - elementWidth;
            case TOP, CENTER, BOTTOM -> (getScaledWidth() - elementWidth) / 2;
            default -> 0;
        };
    }

    public static int getPosX(CoreSettings coreSettings, int elementWidth) {
        return coreSettings.x + getXOffset(coreSettings, elementWidth);
    }

    public static int getYOffset(CoreSettings coreSettings, int elementHeight) {
        int elementPadding = HudRendererLib.getPadding(coreSettings);

        return switch (coreSettings.elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> getScaledHeight() - elementHeight;
            case LEFT, CENTER, RIGHT -> (getScaledHeight() - elementHeight) / 2 + elementPadding;
            default -> elementPadding * 2;
        };
    }

    public static int getYOffsetNoPadding(ElementAnchor elementAnchor, int elementHeight) {
        return switch (elementAnchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> getScaledHeight() - elementHeight;
            case LEFT, CENTER, RIGHT -> (getScaledHeight() - elementHeight) / 2;
            default -> 0;
        };
    }

    public static int getPosY(CoreSettings coreSettings, int elementHeight) {
        int posY = coreSettings.y + getYOffset(coreSettings, elementHeight);

        if (coreSettings.elementAnchor == ElementAnchor.TOP_RIGHT) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && !player.getStatusEffects().isEmpty() && getConfig().adjustTopRightElementsWithStatusEffects) {
                posY += getConfig().adjustTopRightElementsWithStatusEffectsAmount;
            }
        }

        return posY;
    }

    public static void render(DrawContext drawContext, HudElement<? extends ICoreSettingsContainer> hudElement) {
        if (!hudElement.getElementConfig().getCoreSettings().enabled) return;

        TextRenderer renderer = MinecraftClient.getInstance().textRenderer;

        if (hudElement instanceof BasicHudElement<?> basicHudElement) {
            renderElement(renderer, drawContext, basicHudElement.getLines(), hudElement.getElementConfig().getCoreSettings());
        } else if (hudElement instanceof CustomHudElement<?> customHudElement) {
            customHudElement.render(renderer, drawContext);
        }
    }

    public static ElementCorners getCornersWithPadding(int left, int top, int right, int bottom, CoreSettings coreSettings) {
        int elementPadding = HudRendererLib.getPadding(coreSettings);

        return new ElementCorners(
                left - elementPadding * 2,
                top - elementPadding * 2,
                right + elementPadding * 2,
                bottom
        );
    }

    public static int getScaledWidth() {
        return MinecraftClient.getInstance().getWindow().getScaledWidth();
    }

    public static int getScaledHeight() {
        return MinecraftClient.getInstance().getWindow().getScaledHeight();
    }
}