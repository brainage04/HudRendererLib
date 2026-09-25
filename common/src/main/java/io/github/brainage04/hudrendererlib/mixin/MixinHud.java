package io.github.brainage04.hudrendererlib.mixin;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hides the status effect icons the game draws in the top right of the HUD when Show Vanilla Status
 * Effects is off or a mod has asked for them to be hidden (see
 * {@link HudRendererLib#registerVanillaStatusEffectsHider}). The inventory screen's effect list is
 * drawn elsewhere and is left alone.
 */
@Mixin(Hud.class)
public class MixinHud {
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void hudrendererlib$hideVanillaStatusEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (HudRendererLib.shouldHideVanillaStatusEffects()) ci.cancel();
    }
}
