package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.LayerInfo;
import io.github.brainage04.hudrendererlib.util.VanillaHudLayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface CoreHudElement<T extends ICoreSettingsContainer> {
    T getElementConfig();

    default LayerInfo getLayerInfo() {
        return new LayerInfo(VanillaHudLayer.CHAT, true);
    }

    /**
     * Whether the element wraps its text at its max width ({@link
     * io.github.brainage04.hudrendererlib.HudRendererLib#getMaxWidth(CoreHudElement)}); the HUD
     * Element Editor only offers its max width controls for elements that do. Text elements do; a
     * custom element that reads the max width overrides this.
     */
    default boolean supportsMaxWidth() {
        return false;
    }

    /**
     * The max width in GUI pixels the element wraps at while neither its own Max Width override nor
     * the global Max Element Width sets one; {@code 0}, the default, means none. See {@link
     * io.github.brainage04.hudrendererlib.HudRendererLib#getMaxWidth(CoreHudElement)}.
     */
    default int getDefaultMaxWidth() {
        return 0;
    }

    default void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        HudRenderer.render(graphics, deltaTracker, this);
    }

    default void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {}
}
