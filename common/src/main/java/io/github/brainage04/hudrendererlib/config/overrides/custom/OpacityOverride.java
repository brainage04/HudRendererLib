package io.github.brainage04.hudrendererlib.config.overrides.custom;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class OpacityOverride {
    public boolean enabled;
    @ConfigEntry.BoundedDiscrete(max = 255)
    public int value;

    public OpacityOverride(boolean enabled, int value) {
        this.enabled = enabled;
        this.value = value;
    }
}
