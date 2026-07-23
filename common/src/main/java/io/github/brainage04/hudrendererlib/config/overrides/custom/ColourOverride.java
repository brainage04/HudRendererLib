package io.github.brainage04.hudrendererlib.config.overrides.custom;

import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ColourOverride {
    public boolean enabled;
    @ConfigEntry.ColorPicker
    public int value;

    public ColourOverride(boolean enabled, int value) {
        this.enabled = enabled;
        this.value = value;
    }
}
