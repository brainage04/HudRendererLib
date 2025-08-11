package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.LayerInfo;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;

public interface CoreHudElement<T extends ICoreSettingsContainer> extends HudElement {
    T getElementConfig();
    default LayerInfo getLayerInfo() {
        return new LayerInfo(VanillaHudElements.CHAT, true);
    }
}
