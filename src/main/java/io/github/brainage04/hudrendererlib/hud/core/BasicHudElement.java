package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;

import java.util.List;

public interface BasicHudElement<T extends ICoreSettingsContainer> extends HudElement<T> {
    List<String> getLines();
}
