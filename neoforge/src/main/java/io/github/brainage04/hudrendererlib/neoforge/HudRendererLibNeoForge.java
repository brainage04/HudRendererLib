package io.github.brainage04.hudrendererlib.neoforge;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(value = HudRendererLib.MOD_ID, dist = Dist.CLIENT)
public final class HudRendererLibNeoForge {
	public HudRendererLibNeoForge(IEventBus modBus) {
		HudRendererLib.initialize(new NeoForgeHudRendererPlatform(modBus));
	}
}
