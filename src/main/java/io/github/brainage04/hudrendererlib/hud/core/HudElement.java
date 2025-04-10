package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;

public interface HudElement<T extends ICoreSettingsContainer> {
    T getElementConfig();
}
