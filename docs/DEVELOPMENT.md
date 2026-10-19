# Developing with HudRendererLib

## Setup

HudRendererLib 1.0.12 targets Minecraft 26.2 on both loaders.

### Fabric Loom

HudRendererLib 1.0.12 is published on Maven Central:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    modImplementation "io.github.brainage04:hudrendererlib:1.0.12"
}
```

### NeoForge with Architectury Loom

The NeoForge artifact is also published on Maven Central:

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation "io.github.brainage04:hudrendererlib-neoforge:1.0.12"
}
```

For local workspace development, publish both loader artifacts to the sibling Maven repository with:

```bash
heavy -- ./gradlew --no-daemon :fabric:publishAllPublicationsToLocalRepository :neoforge:publishAllPublicationsToLocalRepository
```

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
    HudRendererLib.registerConfigKey(ModConfig.class, GLFW.GLFW_KEY_KP_SUBTRACT, MOD_ID, MOD_NAME);

    // ...
}
```

Where `MOD_ID` is the ID of your mod, `MOD_NAME` is its display name, and the GLFW constant selects the default key.
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

## Building and running tests

```bash
heavy status
heavy --mem 6G --label hudrendererlib-tests -- ./gradlew --no-daemon -Pfabricmoddingconventions.devClientOptions=/nonexistent build runAllGameTests
```

`runAllGameTests` includes the Fabric production client suite and the NeoForge integrated-world client suite.
Dedicated-server GameTests are disabled because this library is client-only.
The NeoForge client prepares a separate, muted options file through the conventions task rather than copying the owner's settings.
