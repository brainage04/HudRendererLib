package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.LayerInfo;
import net.fabricmc.fabric.api.client.rendering.v1.IdentifiedLayer;

public interface HudElement<T extends ICoreSettingsContainer> {
    T getElementConfig();
    default LayerInfo getLayerInfo() {
        return new LayerInfo(IdentifiedLayer.CHAT, true);
    }
}
