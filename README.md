# HudRendererLib

HudRendererLib is a client-side only library for rendering Heads-Up Display (HUD) elements in Fabric and NeoForge mods for Minecraft 26.3.

This mod integrates with Cloth Config's AutoConfig system, and uses annotations from it.

## Features

- Text-only and custom HUD elements, with configurable positions and anchors
- A HUD Element Editor for selecting and dragging elements registered by other mods
- Shared padding, text colour, text shadows, width and backdrop settings, with per-element overrides
- Vanilla HUD layer anchors on both loaders
- Max Width wraps text at the element's own width, and the order is Max Width, then Max Element Width, then the element's own default
- An orange bar marks the selected element in the editor, `[` and `]` step through the elements, and `Backspace` resets the selected element
- Top-right elements move down by the second row's 26 px while a harmful effect icon is drawn, 15 px more in demo mode, and not at all while a screen lists the effects
- An element wider or taller than the screen keeps an edge on the margin when dragged and stays put when `Space` changes its anchor
- An empty config file, a null or misspelt element setting, or an element saved without a name loads as the defaults
- The editor drops its selection when another screen hands control back

Top-right elements move down while the game's status effect icons are showing, so the two don't overlap.

## Dependencies

- Fabric Loader >=0.19.5 w/ Fabric API or NeoForge >=26.3.0.99
- Cloth Config >=26.3.159 for your loader
- Java >=25

For mod developers, dependency setup and the public API examples are in [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## Commands

- `/hudelementeditor` or `Numpad Plus` - open the HUD Element Editor
- `/hudrendererlibconfig` or `Numpad Enter` - open the HudRendererLib Config Editor
- In the element editor: click to select, drag to move, arrow keys for precise movement, `Ctrl` for x5, `Shift` for x10, and `Space` to cycle alignment
- `Save & Close` saves positions - `Undo & Close` or `Escape` closes without saving

## Config

The config editor controls the screen margin, element padding, Max Element Width, text shadows, text colour and backdrop opacity.

Show Vanilla Status Effects controls the game's status effect icons - the top-right adjustment settings control whether and how far elements move down for them.

A mod that draws its own status effect display can hide the vanilla icons only while that display is on.

A negative shift moves top-right elements like zero, and a tall top-right element dragged below the effect icons stays inside the margin once they go.

## Compatibility

An element is drawn under the same conditions as its layer on both loaders: F1 hides it with the rest of the HUD (except next to `SLEEP` and `SUBTITLES`, which the game keeps drawing), and it only appears when the game draws the layer, e.g. `HEALTH_BAR` only in survival/adventure mode and `HOTBAR` only outside spectator mode (`SPECTATOR_MENU` is the spectator hotbar).

## Documentation

- [Development and API](docs/DEVELOPMENT.md) - dependency setup, config registration, text and custom elements, bounds for the editor, layer anchors and status effect integration
- [Publishing](docs/PUBLISHING.md) - publishing configuration
- [Release checklist](docs/RELEASE.md) - loader artifacts and release steps
- [Modrinth](docs/MODRINTH.md) - Modrinth publishing setup
