package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.TextList;

public interface BasicHudElement<T extends ICoreSettingsContainer> extends HudElement<T> {
    TextList getLines();
}
