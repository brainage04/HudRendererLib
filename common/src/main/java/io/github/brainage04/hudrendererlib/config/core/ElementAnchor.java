package io.github.brainage04.hudrendererlib.config.core;

public enum ElementAnchor {
    TOP_LEFT("Top Left"),
    TOP("Top"),
    TOP_RIGHT("Top Right"),
    LEFT("Left"),
    CENTER("Center"),
    RIGHT("Right"),
    BOTTOM_LEFT("Bottom Left"),
    BOTTOM("Bottom"),
    BOTTOM_RIGHT("Bottom Right");

    private final String name;

    ElementAnchor(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
