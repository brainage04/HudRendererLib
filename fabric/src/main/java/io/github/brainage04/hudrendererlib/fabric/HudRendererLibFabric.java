package io.github.brainage04.hudrendererlib.fabric;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import net.fabricmc.api.ClientModInitializer;

public final class HudRendererLibFabric implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		HudRendererLib.initialize(new FabricHudRendererPlatform());
	}
}
