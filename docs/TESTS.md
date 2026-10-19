# Test review

## Frozen rubric

- Keep: runs production behaviour and can fail when a player-visible result or a library contract breaks.
- Redundant: another named test exercises the same production path and checks the same result at least as strongly.
- Tautological: checks a fixture, copied implementation, mock echo, registration wiring or constant without a production behaviour result.
- A small test is not redundant just because a larger test runs nearby code.

## Classification before pruning

| Test or assertion | Classification | Reason |
|---|---|---|
| `CoreSettingsIdAssignerTest.assignsIdsGloballyAcrossConfigClasses` | Keep | Separate mod configs must not share editor IDs. |
| `VanillaHudLayerTest.resolvesFabricAndNeoForgeIdsToTheSameLayer` | Keep | Resolves public identifiers from both loaders. |
| `VanillaHudLayerTest.rejectsUnknownAndNonVanillaIdsNamingTheId` | Keep | Rejects bad public input and names it in the error. |
| Original Fabric `assertRegistrations` | Tautological | Counts fixture registrations and compares the fixtures' fixed `before` values, without observing rendering. |
| Original Fabric before-chat/after-chat demo scenes | Redundant | `HudClientScenarios.run` now checks actual late rendering, both sides of chat and same-side ordering on both loaders; the demo scenes only wait. |
| Shared late-registration and order assertions | Keep | Observe extraction callbacks from the real loader HUD. |
| Shared enable/disable and F1 assertions | Keep | Observe stopped and resumed rendering, not the settings alone. |
| Shared custom-bounds and editor-drag assertions | Keep | Use real bounds and mouse input to check selected ID and moved coordinates. |
| NeoForge client runner | Keep | Required loader entry point for the shared scenarios, not an independent assertion. |

Removed the original Fabric `assertRegistrations` helper and its two demo scenes, demo-only settings and rendering fixture. No unit test was removed.

## Running the suites

```bash
heavy status
heavy --label hudrendererlib-wave -- ./gradlew --no-daemon -Pfabricmoddingconventions.devClientOptions=/nonexistent build runAllGameTests
```

`runAllGameTests` includes the Fabric production client suite and the NeoForge integrated-world client suite. Dedicated-server GameTests are disabled because this library is client-only. Both clients use the same production HUD/editor assertions in `common/src/gametest`.
The NeoForge client prepares a separate, muted options file through the conventions task rather than copying the owner's settings.

## Coverage gaps filled

The original suites did not exercise command dispatch, key handling, config save/load, undo, keyboard editing, anchor cycling, text wrapping or status-effect offsets. The shared client suite now checks both commands and both default keys, a real custom-element drag, Ctrl+Shift movement, anchor-position preservation, save/reload/undo with fresh config objects, text wrapping with padding, and global/per-mod status-effect hiding and offsets on both loaders.

Remaining gaps: pixel-level styling, all vanilla layer/game-mode combinations, optional Mod Menu and NeoForge Mods-list navigation, and rendering across resource reloads. The server has no commands, gamerules or lifecycle behaviour in this client-only library. The tests check extraction order and bounds, not screenshot pixels or sound.

## Code-review regressions

- Custom elements moved by their padding when Space cycled alignment. Anchor cycling now uses the renderer's offsets, accounting separately for text bounds; the client suite checks custom alignment and a complete nine-anchor cycle for padded text.
- Replacing an editor screen without closing it left menu blur disabled. Blur now belongs to the displayed screen lifecycle and restores on removal, including editor-to-editor replacement.
- Disabled elements and text elements that became empty left selectable stale bounds. Hit testing skips disabled settings and empty text clears its old rectangle; both cases have client assertions.
- Inherited config fields were not assigned IDs after deserialization, and static config fields were treated as instance elements. Unit regressions cover inherited IDs, stable existing IDs and ignored static fields.

No public API signatures changed. English-only editor labels and overlapping-element selection order remain judgement calls rather than changed behaviour.
