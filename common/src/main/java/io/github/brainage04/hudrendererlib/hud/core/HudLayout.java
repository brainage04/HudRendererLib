package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;

/**
 * The anchoring arithmetic of {@link HudRenderer} and {@link HudElementEditor} on a screen of a
 * given GUI-scaled size, with no game or config state.
 */
final class HudLayout {
    private HudLayout() {}

    /** {@link HudRenderer#getXOffset(ElementAnchor, int, int, int)}. */
    static int xOffset(ElementAnchor anchor, int screenWidth, int width, int padding) {
        return switch (anchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> screenWidth - width - padding * 2;
            case TOP, CENTER, BOTTOM -> (screenWidth - width) / 2;
            default -> padding * 2;
        };
    }

    static int xOffsetNoPadding(ElementAnchor anchor, int screenWidth, int width) {
        return switch (anchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> screenWidth - width;
            case TOP, CENTER, BOTTOM -> (screenWidth - width) / 2;
            default -> 0;
        };
    }

    static int yOffset(ElementAnchor anchor, int screenHeight, int height, int padding) {
        return switch (anchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> screenHeight - height;
            case LEFT, CENTER, RIGHT -> (screenHeight - height) / 2 + padding;
            default -> padding * 2;
        };
    }

    static int yOffsetNoPadding(ElementAnchor anchor, int screenHeight, int height) {
        return switch (anchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> screenHeight - height;
            case LEFT, CENTER, RIGHT -> (screenHeight - height) / 2;
            default -> 0;
        };
    }

    /**
     * How far top-right elements move down for the vanilla status effect icons that are drawn:
     * {@code amount}, the second row's 26 px more while that row is drawn, and 15 px more in demo
     * mode; none without an icon.
     */
    static int effectIconOffset(int amount, boolean anyIcon, boolean harmfulIcon, boolean demo) {
        if (!anyIcon) return 0;

        // Hud.extractEffects (26.3 Hud.java:500-510) hardcodes the rows: demo mode moves both 15 px
        // lower, and every icon that is not beneficial is drawn 26 px below the first row.
        return amount + (harmfulIcon ? 26 : 0) + (demo ? 15 : 0);
    }

    static int alignContentX(
            ElementAnchor anchor, int elementX, int elementWidth, int contentWidth) {
        return switch (anchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> elementX + elementWidth - contentWidth;
            case TOP, CENTER, BOTTOM -> elementX + (elementWidth - contentWidth) / 2;
            default -> elementX;
        };
    }

    /** What the editor takes off the y of padded bounds measured with their padded height. */
    static int editorPaddingY(ElementAnchor anchor, int padding) {
        return switch (anchor) {
            case TOP_LEFT, TOP, TOP_RIGHT -> padding * 2;
            case LEFT, CENTER, RIGHT -> padding;
            default -> 0;
        };
    }

    /** The x the editor gives an element of bounds {@code width} wide dragged to {@code x}. */
    static int editorX(ElementAnchor anchor, double x, int margin, int screenWidth, int width) {
        int minX = editorMinX(anchor, margin, screenWidth, width);
        return (int) clamp(x, minX, minX + screenWidth - width - margin * 2);
    }

    /**
     * The y the editor gives an element of bounds {@code height} high dragged to {@code y}, where
     * the element is drawn {@code shift} lower than its y says, below the effect icons.
     */
    static int editorY(
            ElementAnchor anchor, double y, int margin, int screenHeight, int height, int shift) {
        int minY = editorMinY(anchor, margin, screenHeight, height);
        return (int) clamp(y, minY, minY + screenHeight - height - margin * 2 - shift);
    }

    private static int editorMinX(ElementAnchor anchor, int margin, int screenWidth, int width) {
        return switch (anchor) {
            case TOP_RIGHT, RIGHT, BOTTOM_RIGHT -> margin - (screenWidth - width);
            case TOP, CENTER, BOTTOM -> margin - (screenWidth - width) / 2;
            default -> margin;
        };
    }

    private static int editorMinY(ElementAnchor anchor, int margin, int screenHeight, int height) {
        return switch (anchor) {
            case BOTTOM_LEFT, BOTTOM, BOTTOM_RIGHT -> margin - (screenHeight - height);
            case LEFT, CENTER, RIGHT -> margin - (screenHeight - height) / 2;
            default -> margin;
        };
    }

    // Mth.clamp(double, double, double) (26.3 Mth.java:106-108), copied so that this class loads
    // without the game.
    private static double clamp(double value, double min, double max) {
        return value < min ? min : Math.min(value, max);
    }
}
