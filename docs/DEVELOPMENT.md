# Developing with HudRendererLib

## Setup

HudRendererLib 1.1.1 targets Minecraft 26.3 on both loaders. The 1.0.x line is for Minecraft 26.2 and the 1.1.x line for Minecraft 26.3; see [Upgrading from 1.0.x](#upgrading-from-10x-minecraft-262) when moving a mod across.

### Fabric Loom

HudRendererLib is published on Maven Central. Version 1.1.1 gets there with its tagged release; until then, build it from source as shown below. Add it with:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    modImplementation "io.github.brainage04:hudrendererlib:1.1.1"
}
```

### NeoForge with Architectury Loom

The NeoForge artifact is also published on Maven Central:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.brainage04:hudrendererlib-neoforge:1.1.1"
}
```

To build from source instead of using a Maven Central release, publish both loader artifacts from your clone of this repository into its `build/local-repo` directory:

```bash
./gradlew :fabric:publishAllPublicationsToLocalRepository :neoforge:publishAllPublicationsToLocalRepository
```

Then add that directory to your mod's repositories, next to `mavenCentral()`. The path below assumes the clone sits next to your mod's directory:

```groovy
repositories {
    mavenCentral()
    maven { url = uri("../HudRendererLib/build/local-repo") }
}
```

The version you depend on is the `mod_version` in the clone's `gradle.properties`.

The library version and supported Minecraft version are tracked separately in `gradle.properties`, `fabric.mod.json`, and `neoforge.mods.toml`. Check the loader-specific release metadata before using HudRendererLib with another Minecraft version.

For Fabric, register your config class in `onInitializeClient`; on NeoForge, make the same call from your client-only mod initialization path:

```java
@Override
public void onInitializeClient() {
    // ...

    HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);

    // ...
}
```

Where `ModConfig` is the name of your mod's config class.

This will automatically register the config class with both AutoConfig and HudRendererLib
and ensure the HUD Element Editor works properly after saving/loading.

See the [AutoConfig wiki](https://shedaniel.gitbook.io/cloth-config/auto-config/creating-a-config-class) for details on how to create a config class.

To ensure that the config screen works properly, make sure to annotate all `CoreSettings` fields
in classes that implement `ICoreSettingsContainer` with `@ConfigEntry.Gui.CollapsibleObject`,
otherwise it will not appear in the config GUI! Here is an example of how to do this:
```java
public class ExampleHudConfig implements ICoreSettingsContainer {
    @ConfigEntry.Gui.CollapsibleObject
    public CoreSettings coreSettings;

    public ExampleHudConfig() {
        this.coreSettings = new CoreSettings("Example HUD", true, 5, 5, ElementAnchor.TOP_LEFT);
    }

    @Override
    public CoreSettings getCoreSettings() {
        return coreSettings;
    }
}
```


Once you have registered a config object, you can register a basic (text-only) HUD element like so:
```java
public class ExampleHud implements BasicCoreHudElement<ExampleHudConfig> {
    @Override
    public TextList getLines() {
        TextList lines = new TextList();

        // add stuff to lines here

        return lines;
    }

    @Override
    public ExampleHudConfig getElementConfig() {
        return getConfig().exampleHudConfig;
    }
}
```

or a custom HUD element like so:
```java
public class ExampleHud implements CoreHudElement<ExampleHudConfig> {
    @Override
    public void render(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
        CoreSettings settings = getElementConfig().getCoreSettings();
        int width = 100;
        int height = 24;
        int x = HudRenderer.getPosX(settings, width);
        int y = HudRenderer.getPosY(settings, height);
        HudRenderer.setElementBounds(settings, x, y, x + width, y + height);
        graphics.fill(x, y, x + width, y + height, 0x80000000);
        graphics.text(Minecraft.getInstance().font, "Example HUD", x + 4, y + 4, 0xFFFFFFFF);
    }

    @Override
    public ExampleHudConfig getElementConfig() {
        return getConfig().exampleHudConfig;
    }
}
```

Call `HudRenderer.setElementBounds(...)` on every rendered frame with the element's current screen-space bounds, including any padding.
The HUD Element Editor uses those bounds to select and drag custom elements; text-only elements set them automatically.
For padded custom layouts, pass the `ElementCorners` returned by `HudRenderer.getCornersWithPadding(...)` directly to `setElementBounds(...)`; it retains the padding information the editor needs when cycling alignment.

Every element has an optional max width (`HudRendererLib.getMaxWidth(element)`): the element's Style Overrides > Max Width, else the global Max Element Width, else the element's `getDefaultMaxWidth()`; `0`, the default of all three, means none.
An element that would otherwise run into its neighbours at common GUI sizes, such as a long list, overrides `getDefaultMaxWidth()` with a width in GUI pixels.
Users set it in the config editor or in the HUD Element Editor, where a selected element that wraps shows an orange handle on its free edge (`[`/`]` adjust it, Backspace resets it).
A text element's lines wrap at it between words (`TextWrapper.wrapWords`), never inside one; a word wider than the max width stays whole on its own line.
Every line keeps the element's alignment, so a right-anchored element still ends at its right edge.

An element whose lines have their own break points (lists, `name: value` pairs) wraps them itself with `TextWrapper.wrapParts(prefix, parts, separator, maxWidth, TextWrapper.widthOf(font))`, which breaks only between parts and leaves the separator's visible part at the end of a wrapped line (`", "` leaves `","`, `" / "` leaves `" /"`), and says so:
```java
    @Override
    public boolean wrapsOwnLines() {
        return true;
    }
```
A custom element that reads the max width overrides `supportsMaxWidth()` to return `true`, so that the editor offers its handle.

If you wish to change the layer that a given element (basic or custom) is rendered with:
```java
    @Override
    public LayerInfo getLayerInfo() {
        return new LayerInfo(VanillaHudLayer.CHAT, true);
    }
```

The first argument is the vanilla HUD layer to draw next to, and the second argument controls
whether the element renders before or after that layer. `VanillaHudLayer` names every vanilla layer
the same way on both loaders; HudRendererLib maps it to Fabric's `VanillaHudElements` or NeoForge's
`VanillaGuiLayers`. `new LayerInfo(Identifier, boolean)` also accepts either loader's identifier
(e.g. `minecraft:scoreboard` or `minecraft:scoreboard_sidebar`); an identifier that names no vanilla
layer fails at registration with an error naming the element and the identifier.

An element is drawn under the same conditions as its layer on both loaders: F1 hides it with the rest
of the HUD (except next to `SLEEP` and `SUBTITLES`, which the game keeps drawing), and it only
appears when the game draws the layer, e.g. `HEALTH_BAR` only in survival/adventure mode and
`HOTBAR` only outside spectator mode (`SPECTATOR_MENU` is the spectator hotbar).

Once you have created a config and HUD class for your element, register the element in your `onInitializeClient` method like so:

```java
@Override
public void onInitializeClient() {
    // ...

    // register your config class first
    HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);

    // THEN register your HUD elements
    HudRendererLib.registerHudElement(new ExampleHud());

    // ...
}
```

Once you have done this, you may want to add keybinds and/or commands that open the config GUI.
You can use the built-in methods like so AFTER registering your main config class:
```java
@Override
public void onInitializeClient() {
    // ...

    // register your config class first
    HudRendererLib.register(ModConfig.class, GsonConfigSerializer::new);
    
    // THEN register your commands/keys
    // HUD elements can be registered before/after commands/keys as long as they are registered after the main config class
    HudRendererLib.registerConfigCommand(ModConfig.class, MOD_ID);
    HudRendererLib.registerConfigKey(ModConfig.class, SDLScancode.SDL_SCANCODE_KP_MINUS, MOD_ID, MOD_NAME);

    // ...
}
```

Where `MOD_ID` is the ID of your mod, `MOD_NAME` is its display name, and the SDL3 scancode (`org.lwjgl.sdl.SDLScancode`, the key codes Minecraft uses since 26.3; GLFW key codes no longer work) selects the default key.
The key is listed in the controls screen under your mod's own category, `HudRendererLib.getKeyCategory(MOD_ID)`; use the same category for any other keys your mod registers so they appear together.
Provide the translations `key.<MOD_ID>.openConfig` for the key and `key.category.<MOD_ID>.keys` for the category title.

The HUD Element Editor can be accessed with `/hudelementeditor`, or the Numpad Plus key.
The HudRendererLib Config Editor can be accessed with `/hudrendererlibconfig`, or the Numpad Enter key.

For more examples, please see my mods that use this library:
- [BrainageHUD](https://github.com/brainage04/BrainageHUD/tree/master/src/main/java/com/github/brainage04/brainagehud)
- [TwitchPlaysMinecraft](https://github.com/brainage04/TwitchPlaysMinecraft/tree/master/src/main/java/io/github/brainage04/twitchplaysminecraft)

## Vanilla status effect icons

Top-right elements move down while the game's status effect icons are showing, so the two don't overlap.
Show Vanilla Status Effects in HudRendererLib's config editor hides the icons for every mod, and top-right elements then stay where they are.

A mod that draws its own status effect display can hide the vanilla icons only while that display is on:

```java
HudRendererLib.registerVanillaStatusEffectsHider(() -> getConfig().statusEffectHudConfig.coreSettings.enabled);
```

The icons are hidden while any registered supplier returns true, and top-right elements are not moved down for them.

## Upgrading from 1.0.x (Minecraft 26.2)

| HudRendererLib | Minecraft |
|---|---|
| 1.0.x | 26.2 |
| 1.1.x | 26.3 |

1.1.0 changes the public API in two places:

- `registerConfigKey(Class, int, String, String)` keeps its signature, but the `int` is now an SDL3 scancode (`org.lwjgl.sdl.SDLScancode`) instead of a GLFW key code, because Minecraft 26.3 reads keys through SDL3. Calls using `GLFW.GLFW_KEY_*` constants stop compiling, since GLFW is no longer on the classpath. Integer literals and values copied from a 26.2 build still compile but bind a different key (GLFW 66 is `B`, SDL scancode 66 is F9), so convert them too, e.g. `GLFW.GLFW_KEY_KP_SUBTRACT` becomes `SDLScancode.SDL_SCANCODE_KP_MINUS`.
- `io.github.brainage04.hudrendererlib.util.MathUtils` (`getBit`, `isBitOn`) was removed. Copy the two methods into your mod if you used them.

`VanillaHudLayer` gained `INFO_BAR_FOREGROUND`, the contextual info bar drawn over `INFO_BAR`'s background.

## Building and running tests

```bash
heavy status
heavy --mem 6G --label hudrendererlib-tests -- ./gradlew --no-daemon build runAllGameTests
```

`runAllGameTests` includes the Fabric production client suite and the NeoForge integrated-world client suite.
Dedicated-server GameTests are disabled because this library is client-only.
The NeoForge client prepares a separate, muted options file through the conventions task rather than copying the owner's settings.

`HudLayoutTest` checks `HudLayout`, the anchoring arithmetic of `HudRenderer` and `HudElementEditor`, with jqwik properties on GUI-scaled screens of every size and GUI scale: anchored edges, offsets, line alignment, anchor cycling, the editor's drag clamp and the effect icon rows. jqwik finds no properties inside the Fabric loader, so `:fabric:propertyTest` runs the `hud.core` tests without it and `:fabric:test` runs the rest; `check` runs both. `:fabric:pitest` runs mutation tests on `HudLayout`; it is not part of `check`, and the report is written to `fabric/build/reports/pitest`.
