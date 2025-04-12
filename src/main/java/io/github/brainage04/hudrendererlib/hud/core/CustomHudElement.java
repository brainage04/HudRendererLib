package io.github.brainage04.hudrendererlib.hud.core;

import io.github.brainage04.hudrendererlib.config.core.ICoreSettingsContainer;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public interface CustomHudElement<T extends ICoreSettingsContainer> extends HudElement<T> {
    void render(TextRenderer renderer, DrawContext drawContext);
}
