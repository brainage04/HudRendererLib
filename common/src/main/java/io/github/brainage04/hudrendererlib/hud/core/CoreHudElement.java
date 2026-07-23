package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.LayerInfo;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public interface CoreHudElement<T extends ICoreSettingsContainer> {
    T getElementConfig();
    default LayerInfo getLayerInfo() {
        return new LayerInfo(Identifier.fromNamespaceAndPath("minecraft", "chat"), true);
    }

    default void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        HudRenderer.render(graphics, deltaTracker, this);
    }

    default void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
    }
}
