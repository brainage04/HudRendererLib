package io.github.brainage04.hudrendererlib.neoforge;

import io.github.brainage04.hudrendererlib.HudRendererLib;
import io.github.brainage04.hudrendererlib.config.core.HudRendererLibConfig;
import me.shedaniel.autoconfig.AutoConfigClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = HudRendererLib.MOD_ID, dist = Dist.CLIENT)
public final class HudRendererLibNeoForge {
	public HudRendererLibNeoForge(IEventBus modBus, ModContainer container) {
		HudRendererLib.initialize(new NeoForgeHudRendererPlatform(modBus));
		container.registerExtensionPoint(
				IConfigScreenFactory.class,
				(modContainer, parent) -> AutoConfigClient.getConfigScreen(HudRendererLibConfig.class, parent).get());
	}
}
