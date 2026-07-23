package io.github.brainage04.hudrendererlib.config.core;

@SuppressWarnings("CanBeFinal")
public class ElementCorners {
    public int left;
    public int top;
    public int right;
    public int bottom;

    public ElementCorners() {
        this.left = -1;
        this.top = -1;
        this.right = -1;
        this.bottom = -1;
    }

    public ElementCorners(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }
}
