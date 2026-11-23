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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.ContainerEventHandler;

/**
 * Checks that the HUD editor's two buttons sit on one row: one width, one y, one gap, and the pair centred on the
 * screen. When {@value #DIRECTORY_ENV} names a directory, it also writes the geometry and a screenshot there, so a
 * guide-line overlay can be drawn from the layout the game computed.
 */
@SuppressWarnings("UnstableApiUsage")
public final class GridlinesClientGameTest implements FabricClientGameTest {
    public static final String DIRECTORY_ENV = "HUDRENDERERLIB_GRIDLINES_DIR";

    private static final int BUTTON_GAP = 20;

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
                    assertButtonsShareOneRow(context);
                    int[] original = context.computeOnClient(
                            client -> new int[] {
                                client.getWindow().getWidth(), client.getWindow().getHeight()
                            });
                    context.getInput().resizeWindow(640, 480);
                    context.waitTicks(5);
                    assertButtonsShareOneRow(context);
                    context.getInput().resizeWindow(original[0], original[1]);
                    context.waitTicks(5);
                });
    }

    private static void assertButtonsShareOneRow(ClientGameTestContext context) {
        context.runOnClient(
                client -> {
                    List<AbstractWidget> widgets = new ArrayList<>();
                    collect(client.gui.screen(), widgets);
                    List<Button> buttons = widgets.stream()
                            .filter(Button.class::isInstance)
                            .map(Button.class::cast)
                            .toList();
                    if (buttons.size() != 2) {
                        throw new AssertionError("Expected 2 buttons, found " + buttons.size());
                    }
                    Button left = buttons.getFirst();
                    Button right = buttons.getLast();
                    if (left.getWidth() != right.getWidth()) {
                        throw new AssertionError("'" + left.getMessage().getString() + "' is "
                                + left.getWidth() + " px wide, '" + right.getMessage().getString()
                                + "' is " + right.getWidth());
                    }
                    if (left.getY() != right.getY()) {
                        throw new AssertionError("The buttons sit at y " + left.getY() + " and "
                                + right.getY());
                    }
                    int gap = right.getX() - (left.getX() + left.getWidth());
                    if (gap != BUTTON_GAP) {
                        throw new AssertionError("The gap between the buttons is " + gap
                                + " px, expected " + BUTTON_GAP);
                    }
                    int pairCentre = left.getX() + (right.getX() + right.getWidth() - left.getX()) / 2;
                    int screenCentre = client.gui.screen().width / 2;
                    if (Math.abs(pairCentre - screenCentre) > 1) {
                        throw new AssertionError("The buttons are centred at " + pairCentre
                                + " px, the screen at " + screenCentre);
                    }
                    if (left.getX() < 0 || right.getX() + right.getWidth() > client.gui.screen().width) {
                        throw new AssertionError("The buttons span " + left.getX() + " to "
                                + (right.getX() + right.getWidth()) + " px, the screen is "
                                + client.gui.screen().width + " wide");
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
