package io.github.brainage04.hudrendererlib.fabric;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import net.fabricmc.api.ModInitializer;

public final class HudRendererLibFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		HudRendererLib.initialize(new FabricHudRendererPlatform());
	}
}
