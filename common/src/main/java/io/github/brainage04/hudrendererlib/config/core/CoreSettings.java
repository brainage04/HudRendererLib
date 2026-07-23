package io.github.brainage04.hudrendererlib.config.core;

import io.github.brainage04.hudrendererlib.config.overrides.core.ElementOverrides;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@SuppressWarnings("CanBeFinal")
public class CoreSettings {
    @ConfigEntry.Gui.Excluded public transient int elementId;
    public final String elementName;

    public boolean enabled;
    public int x;
    public int y;
    public ElementAnchor elementAnchor;

    @ConfigEntry.Gui.CollapsibleObject public ElementOverrides elementOverrides;

    public CoreSettings(String elementName, boolean enabled, int x, int y, ElementAnchor elementAnchor) {
        this.elementId = CoreSettingsIdAssigner.assignIdIfUnset(this.elementId);
        this.elementName = elementName;

        this.enabled = enabled;
        this.x = x;
        this.y = y;
        this.elementAnchor = elementAnchor;

        this.elementOverrides = new ElementOverrides();
    }
}
