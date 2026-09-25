package io.github.brainage04.hudrendererlib.mixin;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static io.github.brainage04.hudrendererlib.util.ConfigUtils.getConfig;

/**
 * Hides the status effect icons the game draws in the top right of the HUD when Show Vanilla Status
 * Effects is off. The inventory screen's effect list is drawn elsewhere and is left alone.
 */
@Mixin(Hud.class)
public class MixinHud {
    @Inject(method = "extractEffects", at = @At("HEAD"), cancellable = true)
    private void hudrendererlib$hideVanillaStatusEffects(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!getConfig().showVanillaStatusEffects) ci.cancel();
    }
}
