package io.github.brainage04.hudrendererlib.config.overrides.core;

@SuppressWarnings("CanBeFinal")
public class IntegerOverride {
    public boolean enabled;
    public int value;

    public IntegerOverride(boolean enabled, int value) {
        this.enabled = enabled;
        this.value = value;
    }
}
