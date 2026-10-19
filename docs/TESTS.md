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
