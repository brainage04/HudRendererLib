package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.LayerInfo;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface CoreHudElement<T extends ICoreSettingsContainer> extends HudElement {
    T getElementConfig();
    default LayerInfo getLayerInfo() {
        return new LayerInfo(VanillaHudElements.CHAT, true);
    }

    @Override
    default void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        HudRenderer.render(graphics, deltaTracker, this);
    }

    default void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    }
}
