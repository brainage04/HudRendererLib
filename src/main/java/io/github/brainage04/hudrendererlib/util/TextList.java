package io.github.brainage04.hudrendererlib.util;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class TextList extends ArrayList<Text> {
    private final List<Text> list = new ArrayList<>();

    public boolean add(String string) {
        return list.add(Text.literal(string));
    }

    public boolean addEmpty() {
        return list.add(Text.empty());
    }

    public boolean addHeader(Text text) {
        return list.add(Text.empty().append(text).formatted(Formatting.BOLD));
    }

    public boolean addHeader(MutableText text) {
        return list.add(text.formatted(Formatting.BOLD));
    }

    public boolean addHeader(String string) {
        return addHeader(Text.literal(string));
    }
}
