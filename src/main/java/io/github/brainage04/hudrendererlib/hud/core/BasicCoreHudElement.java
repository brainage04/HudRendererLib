package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import io.github.brainage04.hudrendererlib.util.TextList;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public interface BasicCoreHudElement<T extends ICoreSettingsContainer> extends CoreHudElement<T> {
    TextList getLines();
    default void render(DrawContext context, RenderTickCounter tickCounter) {
        HudRenderer.render(context, tickCounter, this);
    }
}
