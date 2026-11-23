package io.github.brainage04.hudrendererlib.gametest;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.brainage04.hudrendererlib.hud.core.HudElementEditor;
import io.github.brainage04.minecraftmodconventions.ClientGameTestServers;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

/**
 * Checks that the HUD editor's two buttons sit on one row: one width, one y, one gap, and the pair centred on the
 * screen. When {@value #DIRECTORY_ENV} names a directory, it also writes the geometry and a screenshot there, so a
 * guide-line overlay can be drawn from the layout the game computed.
 */
@SuppressWarnings("UnstableApiUsage")
public final class GridlinesClientGameTest implements FabricClientGameTest {
    public static final String DIRECTORY_ENV = "HUDRENDERERLIB_GRIDLINES_DIR";

    @Override
    public void runTest(ClientGameTestContext context) {
        String directory = System.getenv(DIRECTORY_ENV);
        String name = directory == null ? "" : directory.strip();
        Path output = name.isEmpty() ? null : Path.of(name);
        ClientGameTestServers.withDedicatedServer(
                context,
                ClientGameTestServers.flatServerProperties(),
                "HudRendererLib gridlines GameTest",
                server -> {
                    ClientGameTestServers.assertClientWorldAndPlayerAvailable(context);
                    context.waitTicks(20);
                    context.runOnClient(client -> client.setScreenAndShow(new HudElementEditor()));
                    context.waitForScreen(HudElementEditor.class);
                    context.waitTicks(10);
                    if (output != null) {
                        try {
                            Files.createDirectories(output);
                            Files.writeString(
                                    output.resolve("geometry.json"),
                                    geometry(context),
                                    StandardCharsets.UTF_8);
                        } catch (IOException exception) {
                            throw new AssertionError("Could not write the gridlines geometry", exception);
                        }
                        context.takeScreenshot(
                                TestScreenshotOptions.of("hud-editor-screen")
                                        .disableCounterPrefix()
                                        .withDestinationDir(output));
                    }
                });
    }

    private static void collect(ContainerEventHandler parent, List<AbstractWidget> out) {
        for (var listener : parent.children()) {
            if (listener instanceof AbstractWidget widget) {
                out.add(widget);
                if (widget instanceof ContainerEventHandler nested) {
                    collect(nested, out);
                }
            }
        }
    }

    private static String geometry(ClientGameTestContext context) {
        return context.computeOnClient(
                client -> {
                    JsonObject root = new JsonObject();
                    root.addProperty("guiWidth", client.gui.screen().width);
                    root.addProperty("guiHeight", client.gui.screen().height);
                    root.addProperty("guiScale", client.getWindow().getGuiScale());
                    root.addProperty("windowWidth", client.getWindow().getWidth());
                    root.addProperty("windowHeight", client.getWindow().getHeight());
                    List<AbstractWidget> widgets = new ArrayList<>();
                    collect(client.gui.screen(), widgets);
                    JsonArray array = new JsonArray();
                    for (AbstractWidget widget : widgets) {
                        JsonObject entry = new JsonObject();
                        entry.addProperty("type", widget.getClass().getSimpleName());
                        entry.addProperty("x", widget.getX());
                        entry.addProperty("y", widget.getY());
                        entry.addProperty("width", widget.getWidth());
                        entry.addProperty("height", widget.getHeight());
                        entry.addProperty("message", widget.getMessage().getString());
                        array.add(entry);
                    }
                    root.add("widgets", array);
                    return new GsonBuilder().setPrettyPrinting().create().toJson(root);
                });
    }
}
