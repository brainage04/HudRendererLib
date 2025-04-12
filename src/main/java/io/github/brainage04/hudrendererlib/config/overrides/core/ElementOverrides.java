package io.github.brainage04.hudrendererlib.config.overrides.core;

import io.github.brainage04.hudrendererlib.config.overrides.custom.ColourOverride;
import io.github.brainage04.hudrendererlib.config.overrides.custom.OpacityOverride;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class ElementOverrides {
    // todo: create generic override class with custom gui handlers
    @ConfigEntry.Gui.CollapsibleObject
    public IntegerOverride padding = new IntegerOverride(false, 2);
    @ConfigEntry.Gui.CollapsibleObject
    public BooleanOverride textShadows = new BooleanOverride(false, true);
    @ConfigEntry.Gui.CollapsibleObject
    public ColourOverride textColour = new ColourOverride(false, 0xffffff);
    @ConfigEntry.Gui.CollapsibleObject
    public OpacityOverride backdropOpacity = new OpacityOverride(false, 100);
}
