package io.github.brainage04.hudrendererlib.command;

import io.github.brainage04.hudrendererlib.util.ScreenUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.Minecraft;

public class ModCommands {
    public static void initialize() {
        ClientCommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommands.literal("hudelementeditor")
                        .executes(context -> {
                            Minecraft.getInstance().schedule(() -> ScreenUtils.openElementEditor(context.getSource().getClient()));

                            return 1;
                        })
                )
        ));
    }
}
