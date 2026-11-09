package io.github.brainage04.hudrendererlib.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.brainage04.hudrendererlib.config.core.ElementAnchor;
import io.github.brainage04.hudrendererlib.hud.core.HudRenderer;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import org.junit.jupiter.api.Test;

/**
 * Wrapping is measured here in characters (one unit per character), so the expected lines can be
 * counted by hand; in game the same code measures GUI pixels with the font.
 */
class TextWrapperTest {
    private static final ToIntFunction<FormattedText> CHARACTERS =
            text -> text.getString().length();

    private static final List<Component> NAMES =
            List.of(
                    Component.literal("Annihilation"),
                    Component.literal("Bane of Illagers"),
                    Component.literal("Bane of the Deep"),
                    Component.literal("Duelist"),
                    Component.literal("Sharpness"));

    @Test
    void wrappingOffJoinsEveryPartOnOneLine() {
        List<Component> lines =
                TextWrapper.wrapParts(Component.literal("Missing: "), NAMES, ", ", 0, CHARACTERS);

        assertEquals(
                List.of(
                        "Missing: Annihilation, Bane of Illagers, Bane of the Deep, Duelist,"
                                + " Sharpness"),
                strings(lines));
    }

    @Test
    void wrapsOnlyAtSeparatorsAndKeepsTheSeparatorAtTheLineEnd() {
        List<Component> lines =
                TextWrapper.wrapParts(Component.empty(), NAMES, " / ", 36, CHARACTERS);

        // "Bane of Illagers" is never split at its spaces; each wrapped line ends in " /"
        assertEquals(
                List.of(
                        "Annihilation / Bane of Illagers /",
                        "Bane of the Deep / Duelist /",
                        "Sharpness"),
                strings(lines));
        for (String line : strings(lines)) assertTrue(line.length() <= 36, line);
    }

    @Test
    void commaSeparatorLeavesACommaAtTheLineEnd() {
        List<Component> lines =
                TextWrapper.wrapParts(Component.literal("Missing: "), NAMES, ", ", 40, CHARACTERS);

        assertEquals(
                List.of(
                        "Missing: Annihilation, Bane of Illagers,",
                        "Bane of the Deep, Duelist, Sharpness"),
                strings(lines));
    }

    @Test
    void theLineEndSeparatorNeverPushesALinePastTheMaximum() {
        // "Ab / Cd" is 7 wide, but with " /" after it, as Ef follows, it would be 9
        List<Component> parts =
                List.of(Component.literal("Ab"), Component.literal("Cd"), Component.literal("Ef"));
        List<Component> lines =
                TextWrapper.wrapParts(Component.empty(), parts, " / ", 8, CHARACTERS);

        assertEquals(List.of("Ab /", "Cd / Ef"), strings(lines));
    }

    @Test
    void aPartWiderThanTheMaximumStaysWholeOnItsOwnLine() {
        List<Component> parts =
                List.of(
                        Component.literal("Fire Aspect"),
                        Component.literal("Curse of Vanishing"),
                        Component.literal("Looting"));
        List<Component> lines =
                TextWrapper.wrapParts(Component.empty(), parts, ", ", 10, CHARACTERS);

        assertEquals(List.of("Fire Aspect,", "Curse of Vanishing,", "Looting"), strings(lines));
    }

    @Test
    void wordsWrapAtSpacesAndALongWordIsNeverSplit() {
        Component line = Component.literal("A long HUD line with Supercalifragilistic words");

        assertEquals(
                List.of("A long HUD", "line with", "Supercalifragilistic", "words"),
                strings(TextWrapper.wrapWords(line, 10, CHARACTERS)));
    }

    @Test
    void wordWrappingOffOrALineThatFitsReturnsTheSameLine() {
        Component line = Component.literal("Day 12, 06:30");

        assertSame(line, TextWrapper.wrapWords(line, 0, CHARACTERS).getFirst());
        assertSame(line, TextWrapper.wrapWords(line, 13, CHARACTERS).getFirst());
    }

    @Test
    void wordWrappingKeepsEachWordsStyle() {
        Component line =
                Component.empty()
                        .append(Component.literal("Map: ").withStyle(ChatFormatting.BOLD))
                        .append(Component.literal("Tide break").withStyle(ChatFormatting.YELLOW));

        List<Component> lines = TextWrapper.wrapWords(line, 10, CHARACTERS);

        assertEquals(List.of("Map: Tide", "break"), strings(lines));
        assertEquals(
                List.of(Style.EMPTY.withBold(true), Style.EMPTY.withColor(ChatFormatting.YELLOW)),
                styles(lines.getFirst()));
        assertEquals(List.of(Style.EMPTY.withColor(ChatFormatting.YELLOW)), styles(lines.get(1)));
    }

    @Test
    void wrappedLinesKeepTheirAnchorsEdge() {
        int screenWidth = 400;
        int padding = 2;
        int wide = 120;
        int narrow = 45;

        // right anchors: every line ends at the same x, so the element hugs its right edge
        for (ElementAnchor anchor :
                List.of(ElementAnchor.TOP_RIGHT, ElementAnchor.RIGHT, ElementAnchor.BOTTOM_RIGHT)) {
            assertEquals(
                    HudRenderer.getXOffset(anchor, screenWidth, wide, padding) + wide,
                    HudRenderer.getXOffset(anchor, screenWidth, narrow, padding) + narrow,
                    anchor.toString());
            assertEquals(
                    screenWidth - padding * 2,
                    HudRenderer.getXOffset(anchor, screenWidth, wide, padding) + wide);
        }
        // centre anchors: every line is centred on the screen's centre
        for (ElementAnchor anchor :
                List.of(ElementAnchor.TOP, ElementAnchor.CENTER, ElementAnchor.BOTTOM)) {
            assertEquals(
                    screenWidth / 2,
                    HudRenderer.getXOffset(anchor, screenWidth, wide, padding) + wide / 2);
            assertEquals(
                    screenWidth / 2,
                    HudRenderer.getXOffset(anchor, screenWidth, narrow, padding) + narrow / 2 + 1);
        }
        // left anchors: every line starts at the same x
        for (ElementAnchor anchor :
                List.of(ElementAnchor.TOP_LEFT, ElementAnchor.LEFT, ElementAnchor.BOTTOM_LEFT)) {
            assertEquals(
                    HudRenderer.getXOffset(anchor, screenWidth, wide, padding),
                    HudRenderer.getXOffset(anchor, screenWidth, narrow, padding));
        }
    }

    private static List<String> strings(List<Component> lines) {
        return lines.stream().map(Component::getString).toList();
    }

    /** The styles of the line's text runs that are not only spaces, in order. */
    private static List<Style> styles(Component line) {
        List<Style> styles = new java.util.ArrayList<>();
        line.visit(
                (style, text) -> {
                    if (!text.isBlank()) styles.add(style);
                    return java.util.Optional.empty();
                },
                Style.EMPTY);
        return styles;
    }
}
