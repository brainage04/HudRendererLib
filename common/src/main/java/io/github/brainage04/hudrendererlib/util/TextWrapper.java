package io.github.brainage04.hudrendererlib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.ToIntFunction;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

/**
 * Wraps HUD text at a maximum width without ever splitting a part: a line breaks only at the
 * separators between parts, and a part wider than the maximum stays whole on a line of its own.
 *
 * <p>When a line breaks, the separator it broke at keeps its visible part at the end of the line
 * ({@code ", "} leaves {@code ","} and {@code " / "} leaves {@code " /"}), so a reader can tell
 * which separator joined the two lines. A maximum width of {@code 0} or less turns wrapping off:
 * every call then returns exactly one line.
 */
public final class TextWrapper {
    private TextWrapper() {}

    /** Measures text in GUI pixels, the unit of the max width settings. */
    public static ToIntFunction<FormattedText> widthOf(Font font) {
        return font::width;
    }

    /**
     * Joins {@code parts} with {@code separator} after {@code prefix}, wrapping between parts so
     * that no line is wider than {@code maxWidth} unless a single part (with the prefix, on the
     * first line) is wider on its own. A part that starts a line keeps the separator's line end
     * after it when the line breaks there, so a line holding one part within the line end's width
     * of {@code maxWidth} can overshoot it by that much.
     *
     * @param width measures text in the same unit as {@code maxWidth}
     */
    public static List<Component> wrapParts(
            Component prefix,
            List<? extends Component> parts,
            String separator,
            int maxWidth,
            ToIntFunction<FormattedText> width) {
        List<Component> lines = new ArrayList<>();
        MutableComponent line = Component.empty().append(prefix);

        if (maxWidth <= 0) {
            for (int i = 0; i < parts.size(); i++) {
                if (i > 0) line.append(separator);
                line.append(parts.get(i));
            }
            lines.add(line);
            return lines;
        }

        String lineEnd = separator.stripTrailing();
        int separatorWidth = width.applyAsInt(Component.literal(separator));
        int lineEndWidth = width.applyAsInt(Component.literal(lineEnd));
        int lineWidth = width.applyAsInt(prefix);
        boolean lineHasPart = false;

        for (int i = 0; i < parts.size(); i++) {
            Component part = parts.get(i);
            int partWidth = width.applyAsInt(part);
            // a part that is not the last may still have to carry the line end after it
            int reserve = i < parts.size() - 1 ? lineEndWidth : 0;

            if (lineHasPart) {
                if (lineWidth + separatorWidth + partWidth + reserve <= maxWidth) {
                    line.append(separator);
                    lineWidth += separatorWidth;
                } else {
                    if (!lineEnd.isEmpty()) line.append(lineEnd);
                    lines.add(line);
                    line = Component.empty();
                    lineWidth = 0;
                }
            }

            line.append(part);
            lineWidth += partWidth;
            lineHasPart = true;
        }

        lines.add(line);
        return lines;
    }

    /**
     * Wraps {@code line} at its spaces, keeping each word's style; a word wider than {@code
     * maxWidth} stays whole on a line of its own. With wrapping off the line is returned as it is.
     */
    public static List<Component> wrapWords(
            Component line, int maxWidth, ToIntFunction<FormattedText> width) {
        if (maxWidth <= 0 || width.applyAsInt(line) <= maxWidth) return List.of(line);

        return wrapParts(Component.empty(), splitWords(line), " ", maxWidth, width);
    }

    /** The words of {@code line} between its spaces, each with the styles of its characters. */
    static List<Component> splitWords(Component line) {
        List<Component> words = new ArrayList<>();
        MutableComponent[] word = {Component.empty()};

        line.<Void>visit(
                (style, text) -> {
                    int start = 0;
                    for (int i = 0; i < text.length(); i++) {
                        if (text.charAt(i) != ' ') continue;
                        if (i > start) {
                            word[0].append(
                                    Component.literal(text.substring(start, i)).withStyle(style));
                        }
                        words.add(word[0]);
                        word[0] = Component.empty();
                        start = i + 1;
                    }
                    if (start < text.length()) {
                        word[0].append(Component.literal(text.substring(start)).withStyle(style));
                    }
                    return Optional.empty();
                },
                Style.EMPTY);
        words.add(word[0]);

        return words;
    }
}
