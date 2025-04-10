package io.github.brainage04.hudrendererlib.command;

import io.github.brainage04.hudrendererlib.util.ScreenUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.MinecraftClient;

public class ModCommands {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("hudelementeditor")
                        .executes(context -> {
                            MinecraftClient.getInstance().send(() -> ScreenUtils.openElementEditor(context.getSource().getClient()));

                            return 1;
                        })
                )
        ));
    }
}
