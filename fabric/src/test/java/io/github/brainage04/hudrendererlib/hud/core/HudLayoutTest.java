package io.github.brainage04.hudrendererlib.hud.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.From;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;

/**
 * The anchoring arithmetic of {@link HudLayout}, put together as {@link HudRenderer#renderElement}
 * and {@link HudElementEditor} put it together, on the GUI-scaled screens the game makes.
 */
class HudLayoutTest {
    /** A GUI-scaled screen, as Window.setGuiScale makes it (26.3 Window.java:704-708). */
    record Screen(int width, int height) {}

    /** A text element's content: its widest line and how many lines it has. */
    record Content(int width, int lines, int lineHeight, int padding) {
        int height() {
            return (lineHeight + padding) * lines + padding;
        }
    }

    record Bounds(int left, int top, int right, int bottom) {
        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }
    }

    /**
     * A framebuffer of the window's minimum size (Window.java:610-611) or larger, at a GUI scale.
     */
    @Provide
    Arbitrary<Screen> screens() {
        return Combinators.combine(
                        Arbitraries.integers().between(320, 7680),
                        Arbitraries.integers().between(240, 4320),
                        Arbitraries.integers().between(0, 8))
                .as(
                        (framebufferWidth, framebufferHeight, option) -> {
                            int autoScale = calculateScale(framebufferWidth, framebufferHeight);
                            int scale = option == 0 ? autoScale : Math.min(option, autoScale);
                            return new Screen(
                                    (int) Math.ceil((double) framebufferWidth / scale),
                                    (int) Math.ceil((double) framebufferHeight / scale));
                        });
    }

    /** Window.calculateScale with no maximum and no forced Unicode font (Window.java:616-633). */
    private static int calculateScale(int framebufferWidth, int framebufferHeight) {
        int scale = 1;
        while (scale < framebufferWidth
                && scale < framebufferHeight
                && framebufferWidth / (scale + 1) >= 320
                && framebufferHeight / (scale + 1) >= 240) {
            scale++;
        }
        return scale;
    }

    @Provide
    Arbitrary<Content> contents() {
        return Combinators.combine(
                        Arbitraries.integers().between(0, 280),
                        Arbitraries.integers().between(1, 8),
                        Arbitraries.integers().between(1, 20),
                        Arbitraries.integers().between(0, 10))
                .as(Content::new);
    }

    /** The shift of top-right elements below the effect icons, which only they get. */
    private static int shift(ElementAnchor anchor, int effectIconOffset) {
        return anchor == ElementAnchor.TOP_RIGHT ? effectIconOffset : 0;
    }

    /** The padded bounds renderElement gives a text element at x, y. */
    private static Bounds render(
            ElementAnchor anchor, int x, int y, Screen screen, Content content, int iconOffset) {
        int padding = content.padding();
        int posX = x + HudLayout.xOffset(anchor, screen.width(), content.width(), padding);
        int posY =
                y
                        + HudLayout.yOffset(anchor, screen.height(), content.height(), padding)
                        + shift(anchor, iconOffset);
        return new Bounds(
                posX - padding * 2,
                posY - padding * 2,
                posX + content.width() + padding * 2,
                posY + content.height());
    }

    /** HudRenderer.getEditorPosX for padded bounds. */
    private static int editorPosX(ElementAnchor anchor, int x, Screen screen, Bounds bounds) {
        return x + HudLayout.xOffsetNoPadding(anchor, screen.width(), bounds.width());
    }

    /** HudRenderer.getEditorPosY for padded bounds. */
    private static int editorPosY(
            ElementAnchor anchor,
            int y,
            Screen screen,
            Bounds bounds,
            int padding,
            int iconOffset) {
        return y
                + HudLayout.yOffset(anchor, screen.height(), bounds.height(), padding)
                + shift(anchor, iconOffset)
                - HudLayout.editorPaddingY(anchor, padding);
    }

    private static boolean fits(Screen screen, Content content, int margin) {
        return content.width() + content.padding() * 4 <= screen.width() - margin * 2
                && content.height() + content.padding() * 2 <= screen.height() - margin * 2;
    }

    private static boolean isLeft(ElementAnchor anchor) {
        return List.of(ElementAnchor.TOP_LEFT, ElementAnchor.LEFT, ElementAnchor.BOTTOM_LEFT)
                .contains(anchor);
    }

    private static boolean isRight(ElementAnchor anchor) {
        return List.of(ElementAnchor.TOP_RIGHT, ElementAnchor.RIGHT, ElementAnchor.BOTTOM_RIGHT)
                .contains(anchor);
    }

    private static boolean isTop(ElementAnchor anchor) {
        return List.of(ElementAnchor.TOP_LEFT, ElementAnchor.TOP, ElementAnchor.TOP_RIGHT)
                .contains(anchor);
    }

    private static boolean isBottom(ElementAnchor anchor) {
        return List.of(ElementAnchor.BOTTOM_LEFT, ElementAnchor.BOTTOM, ElementAnchor.BOTTOM_RIGHT)
                .contains(anchor);
    }

    @Property
    void anElementAtZeroTouchesTheEdgesOfItsAnchor(
            @ForAll ElementAnchor anchor,
            @ForAll("screens") Screen screen,
            @ForAll("contents") Content content,
            @ForAll @IntRange(min = 0, max = 80) int iconOffset) {
        Assume.that(fits(screen, content, 0));
        Bounds bounds = render(anchor, 0, 0, screen, content, iconOffset);
        assertEquals(content.width() + content.padding() * 4, bounds.width());
        assertEquals(content.height() + content.padding() * 2, bounds.height());

        int leftGap = bounds.left();
        int rightGap = screen.width() - bounds.right();
        if (isLeft(anchor)) {
            assertEquals(0, leftGap, "left gap");
        } else if (isRight(anchor)) {
            assertEquals(0, rightGap, "right gap");
        } else {
            assertTrue(
                    rightGap - leftGap == 0 || rightGap - leftGap == 1, bounds + " on " + screen);
        }

        int topGap = bounds.top();
        int bottomGap = screen.height() - bounds.bottom();
        if (isTop(anchor)) {
            assertEquals(shift(anchor, iconOffset), topGap, "top gap");
        } else if (isBottom(anchor)) {
            assertEquals(0, bottomGap, "bottom gap");
        } else {
            assertTrue(
                    bottomGap - topGap == 0 || bottomGap - topGap == 1, bounds + " on " + screen);
        }
    }

    /** getXOffsetNoPadding and getYOffsetNoPadding place bounds of their own size at the edges. */
    @Property
    void boundsWithoutPaddingTouchTheEdgesOfTheirAnchor(
            @ForAll ElementAnchor anchor,
            @ForAll("screens") Screen screen,
            @ForAll @IntRange(min = 0, max = 320) int width,
            @ForAll @IntRange(min = 0, max = 240) int height) {
        int left = HudLayout.xOffsetNoPadding(anchor, screen.width(), width);
        int top = HudLayout.yOffsetNoPadding(anchor, screen.height(), height);
        int rightGap = screen.width() - (left + width);
        int bottomGap = screen.height() - (top + height);
        if (isLeft(anchor)) {
            assertEquals(0, left);
        } else if (isRight(anchor)) {
            assertEquals(0, rightGap);
        } else {
            assertTrue(rightGap - left == 0 || rightGap - left == 1, left + " vs " + rightGap);
        }
        if (isTop(anchor)) {
            assertEquals(0, top);
        } else if (isBottom(anchor)) {
            assertEquals(0, bottomGap);
        } else {
            assertTrue(bottomGap - top == 0 || bottomGap - top == 1, top + " vs " + bottomGap);
        }
    }

    @Property
    void theOffsetMovesTheElementByExactlyThatMuch(
            @ForAll ElementAnchor anchor,
            @ForAll("screens") Screen screen,
            @ForAll("contents") Content content,
            @ForAll @IntRange(min = -2000, max = 2000) int x,
            @ForAll @IntRange(min = -2000, max = 2000) int y,
            @ForAll @IntRange(min = 0, max = 80) int iconOffset) {
        Bounds origin = render(anchor, 0, 0, screen, content, iconOffset);
        Bounds moved = render(anchor, x, y, screen, content, iconOffset);
        assertEquals(
                new Bounds(
                        origin.left() + x,
                        origin.top() + y,
                        origin.right() + x,
                        origin.bottom() + y),
                moved);
    }

    @Property
    void everyLineKeepsTheEdgeOfItsAnchorInsideTheElement(
            @ForAll ElementAnchor anchor,
            @ForAll("screens") Screen screen,
            @ForAll("contents") Content content,
            @ForAll @IntRange(min = 0, max = 400) int lineWidth,
            @ForAll @IntRange(min = -500, max = 500) int x) {
        Assume.that(lineWidth <= content.width());
        int padding = content.padding();
        int elementX = x + HudLayout.xOffset(anchor, screen.width(), content.width(), padding);
        int lineX = x + HudLayout.xOffset(anchor, screen.width(), lineWidth, padding);
        int aligned = HudLayout.alignContentX(anchor, elementX, content.width(), lineWidth);
        int leftGap = lineX - elementX;
        int rightGap = elementX + content.width() - (lineX + lineWidth);
        assertTrue(leftGap >= 0 && rightGap >= 0, "line outside the element");
        if (isLeft(anchor)) {
            assertEquals(0, leftGap);
            assertEquals(lineX, aligned);
        } else if (isRight(anchor)) {
            assertEquals(0, rightGap);
            assertEquals(lineX, aligned);
        } else {
            assertTrue(Math.abs(leftGap - rightGap) <= 1, leftGap + " vs " + rightGap);
            assertTrue(Math.abs(aligned - lineX) <= 1, aligned + " vs " + lineX);
        }
    }

    @Property
    void cyclingTheAnchorLeavesTheElementWhereItIs(
            @ForAll ElementAnchor from,
            @ForAll ElementAnchor to,
            @ForAll("screens") Screen screen,
            @ForAll("contents") Content content,
            @ForAll @IntRange(min = -500, max = 500) int x,
            @ForAll @IntRange(min = -500, max = 500) int y,
            @ForAll @IntRange(min = 0, max = 80) int iconOffset) {
        Assume.that(fits(screen, content, 0));
        assertCyclingLeavesTheElementWhereItIs(from, to, screen, content, x, y, iconOffset);
    }

    /** Elements that end up to 40 px wider or taller than the screen once padded. */
    @Property
    void cyclingTheAnchorLeavesAnOversizedElementWhereItIs(
            @ForAll ElementAnchor from,
            @ForAll ElementAnchor to,
            @ForAll("screens") Screen screen,
            @ForAll @IntRange(min = 1, max = 20) int lineHeight,
            @ForAll @IntRange(min = 0, max = 10) int padding,
            @ForAll @IntRange(min = -40, max = 40) int overflowX,
            @ForAll @IntRange(min = -40, max = 40) int overflowY,
            @ForAll @IntRange(min = -500, max = 500) int x,
            @ForAll @IntRange(min = -500, max = 500) int y,
            @ForAll @IntRange(min = 0, max = 80) int iconOffset) {
        int width = screen.width() - padding * 4 + overflowX;
        int lines =
                Math.max(1, (screen.height() - padding * 3 + overflowY) / (lineHeight + padding));
        Content content = new Content(width, lines, lineHeight, padding);
        Assume.that(!fits(screen, content, 0));
        assertCyclingLeavesTheElementWhereItIs(from, to, screen, content, x, y, iconOffset);
    }

    private static void assertCyclingLeavesTheElementWhereItIs(
            ElementAnchor from,
            ElementAnchor to,
            Screen screen,
            Content content,
            int x,
            int y,
            int iconOffset) {
        int padding = content.padding();
        Bounds before = render(from, x, y, screen, content, iconOffset);
        // HudElementEditor.keyPressed on Space, with the bounds of the last frame.
        int newX = x + editorPosX(from, x, screen, before) - editorPosX(to, x, screen, before);
        int newY =
                y
                        + editorPosY(from, y, screen, before, padding, iconOffset)
                        - editorPosY(to, y, screen, before, padding, iconOffset);
        assertEquals(before, render(to, newX, newY, screen, content, iconOffset));
    }

    @Property
    void draggingKeepsTheElementInsideTheScreenMargin(
            @ForAll ElementAnchor anchor,
            @ForAll("screens") Screen screen,
            @ForAll("contents") Content content,
            @ForAll @IntRange(min = 0, max = 20) int margin,
            @ForAll @IntRange(min = -5000, max = 5000) int wantedX,
            @ForAll @IntRange(min = -5000, max = 5000) int wantedY) {
        Assume.that(fits(screen, content, margin));
        Bounds last = render(anchor, 0, 0, screen, content, 0);
        int x = HudLayout.editorX(anchor, wantedX, margin, screen.width(), last.width());
        int y = HudLayout.editorY(anchor, wantedY, margin, screen.height(), last.height());
        Bounds dragged = render(anchor, x, y, screen, content, 0);
        assertTrue(dragged.left() >= margin, dragged + " on " + screen);
        assertTrue(dragged.right() <= screen.width() - margin, dragged + " on " + screen);
        assertTrue(dragged.top() >= margin, dragged + " on " + screen);
        assertTrue(dragged.bottom() <= screen.height() - margin, dragged + " on " + screen);
    }

    @Property
    void aDragInsideTheMarginGoesWhereTheMouseSays(
            @ForAll ElementAnchor anchor,
            @ForAll("screens") Screen screen,
            @ForAll("contents") Content content,
            @ForAll @IntRange(min = 0, max = 20) int margin,
            @ForAll @IntRange(min = -5000, max = 5000) int wantedX,
            @ForAll @IntRange(min = -5000, max = 5000) int wantedY) {
        Assume.that(fits(screen, content, margin));
        Bounds last = render(anchor, 0, 0, screen, content, 0);
        Bounds wanted = render(anchor, wantedX, wantedY, screen, content, 0);
        int x = HudLayout.editorX(anchor, wantedX, margin, screen.width(), last.width());
        int y = HudLayout.editorY(anchor, wantedY, margin, screen.height(), last.height());
        if (wanted.left() >= margin && wanted.right() <= screen.width() - margin) {
            assertEquals(wantedX, x);
        }
        if (wanted.top() >= margin && wanted.bottom() <= screen.height() - margin) {
            assertEquals(wantedY, y);
        }
    }

    /**
     * An element wider or taller than the screen minus the margin, by up to 40 px, has no room to
     * move along that side, so a drag leaves one of its edges on the margin.
     */
    @Property
    void draggingAnOversizedElementLeavesAnEdgeOnTheMargin(
            @ForAll ElementAnchor anchor,
            @ForAll("screens") Screen screen,
            @ForAll @IntRange(min = 1, max = 20) int lineHeight,
            @ForAll @IntRange(min = 0, max = 10) int padding,
            @ForAll @IntRange(min = 0, max = 20) int margin,
            @ForAll @IntRange(min = 1, max = 40) int overflowX,
            @ForAll @IntRange(min = 1, max = 40) int overflowY,
            @ForAll @IntRange(min = -5000, max = 5000) int wantedX,
            @ForAll @IntRange(min = -5000, max = 5000) int wantedY) {
        int width = screen.width() - margin * 2 - padding * 4 + overflowX;
        int lines =
                Math.ceilDiv(
                        screen.height() - margin * 2 - padding * 3 + overflowY,
                        lineHeight + padding);
        Content content = new Content(width, lines, lineHeight, padding);
        Bounds last = render(anchor, 0, 0, screen, content, 0);
        int x = HudLayout.editorX(anchor, wantedX, margin, screen.width(), last.width());
        int y = HudLayout.editorY(anchor, wantedY, margin, screen.height(), last.height());
        Bounds dragged = render(anchor, x, y, screen, content, 0);
        assertTrue(
                dragged.left() == margin || dragged.right() == screen.width() - margin,
                dragged + " on " + screen);
        assertTrue(
                dragged.top() == margin || dragged.bottom() == screen.height() - margin,
                dragged + " on " + screen);
    }

    /** One active effect: whether it shows an icon and whether it is beneficial. */
    record Effect(boolean showIcon, boolean beneficial) {}

    @Property
    void topRightElementsMoveDownByTheLowestIconRowTheGameDraws(
            @ForAll List<@From("effects") Effect> effects,
            @ForAll boolean demo,
            @ForAll @IntRange(min = 0, max = 100) int amount) {
        boolean anyIcon = false;
        boolean harmfulIcon = false;
        int lowestRowY = -1;
        for (Effect effect : effects) {
            if (!effect.showIcon()) continue;
            anyIcon = true;
            harmfulIcon |= !effect.beneficial();
            lowestRowY = Math.max(lowestRowY, vanillaIconY(effect.beneficial(), demo));
        }
        int offset = HudLayout.effectIconOffset(amount, anyIcon, harmfulIcon, demo);
        if (lowestRowY < 0) {
            assertEquals(0, offset);
        } else {
            // amount places an element below the first row of a game outside demo mode.
            assertEquals(lowestRowY - vanillaIconY(true, false), offset - amount);
        }
    }

    @Provide
    Arbitrary<Effect> effects() {
        return Combinators.combine(Arbitraries.of(true, false), Arbitraries.of(true, false))
                .as(Effect::new);
    }

    /** The y of an effect icon as Hud.extractEffects draws it (26.3 Hud.java:500-510). */
    private static int vanillaIconY(boolean beneficial, boolean demo) {
        int y = 1;
        if (demo) y += 15;
        if (!beneficial) y += 26;
        return y;
    }
}
