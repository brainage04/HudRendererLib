package io.github.brainage04.hudrendererlib.config.core;

public class CoreSettingsElement {
    public ElementCorners corners;
    public final CoreSettings coreSettings;

    public CoreSettingsElement(ElementCorners corners, CoreSettings coreSettings) {
        this.corners = corners;
        this.coreSettings = coreSettings;
    }
}
