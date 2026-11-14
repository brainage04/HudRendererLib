package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface BasicCoreHudElement<T extends ICoreSettingsContainer> extends CoreHudElement<T> {
    TextList getLines();

    /**
     * Whether {@link #getLines()} already fits the element's max width ({@link
     * io.github.brainage04.hudrendererlib.HudRendererLib#getMaxWidth(CoreHudElement)}), e.g.
     * because the element wraps at its own separators with {@link
     * io.github.brainage04.hudrendererlib.util.TextWrapper}. When {@code false} (the default) the
     * renderer wraps each line at its spaces.
     */
    default boolean wrapsOwnLines() {
        return false;
    }

    @Override
    default boolean supportsMaxWidth() {
        return true;
    }

    default void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        HudRenderer.render(context, tickCounter, this);
    }
}
