package io.github.brainage04.hudrendererlib.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;

public class TextList extends ArrayList<Text> {
    @SuppressWarnings("UnusedReturnValue")
    public boolean add(String string) {
        return add(Text.literal(string));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addEmpty() {
        return add(Text.empty());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addHeader(Text text) {
        return add(Text.empty().append(text).formatted(Formatting.BOLD));
    }

    public boolean addHeader(MutableText text) {
        return add(text.formatted(Formatting.BOLD));
    }

    @SuppressWarnings("unused")
    public boolean addHeader(String string) {
        return addHeader(Text.literal(string));
    }
}
