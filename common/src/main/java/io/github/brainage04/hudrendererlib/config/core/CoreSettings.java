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

    @ConfigEntry.Gui.CollapsibleObject
    public ElementOverrides elementOverrides = new ElementOverrides();

    public CoreSettings(
            String elementName, boolean enabled, int x, int y, ElementAnchor elementAnchor) {
        this.elementId = CoreSettingsIdAssigner.assignIdIfUnset(this.elementId);
        this.elementName = elementName;

        this.enabled = enabled;
        this.x = x;
        this.y = y;
        this.elementAnchor = elementAnchor;
    }

    /**
     * Without it Gson allocates through Unsafe and skips the field initialisers above (Gson 2.14.0
     * {@code ConstructorConstructor#newUnsafeAllocator}), so a config file that leaves {@code
     * elementOverrides} out would load it null. The name is only ever read from the file.
     */
    private CoreSettings() {
        this.elementName = null;
    }
}
