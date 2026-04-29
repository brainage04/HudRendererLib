package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public interface BasicCoreHudElement<T extends ICoreSettingsContainer> extends CoreHudElement<T> {
    TextList getLines();
    default void render(GuiGraphicsExtractor context, DeltaTracker tickCounter) {
        HudRenderer.render(context, tickCounter, this);
    }
}
