package io.github.brainage04.hudrendererlib.util;

import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class TextList extends ArrayList<Component> {
    @SuppressWarnings("UnusedReturnValue")
    public boolean add(String string) {
        return add(Component.literal(string));
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addEmpty() {
        return add(Component.empty());
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean addHeader(Component text) {
        return add(Component.empty().append(text).withStyle(ChatFormatting.BOLD));
    }

    public boolean addHeader(MutableComponent text) {
        return add(text.withStyle(ChatFormatting.BOLD));
    }

    @SuppressWarnings("unused")
    public boolean addHeader(String string) {
        return addHeader(Component.literal(string));
    }
}
