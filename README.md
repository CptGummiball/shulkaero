# Shulkaero

**Automatic Xaero waypoints for your shulker boxes.**

Shulkaero is a client-side addon for [Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap)
(and [Xaero's World Map](https://modrinth.com/mod/xaeros-worldmap)): whenever you place a
shulker box, a waypoint is created at its position — named after the box (custom anvil name,
or "Shulker Box") and colored to match the box's dye color. When the box is removed again
(mined, blown up, pushed...), the waypoint disappears automatically — unless you already
deleted or renamed it yourself.

*by CptGummiball*

## Features

- Waypoint on shulker box placement, with the box's **name** and **color**
  (all 16 dye colors map to matching Xaero waypoint colors; the undyed box is purple)
- Placement is confirmed for ~half a second before the waypoint appears, so
  server-side rollbacks (claims/protection plugins) never leave ghost waypoints
- When the box is broken, the **waypoint stays while the dropped box item lies
  nearby** — it is removed once the box is picked up (by anyone), despawns or burns.
  Creative-mode breaking (no drop) removes it right away. Configurable (`waitForPickup`)
- Also detects removal by explosions, pistons, other players etc. (checked while
  the chunk is loaded)
- Waypoints you deleted or renamed manually are left alone
- Actionbar/chat feedback (English + German), configurable or off
- `/shulkaero` client command: `list`, `clear`, `toggle`, `reload`
- Tracked boxes are persisted (`config/shulkaero/tracked_waypoints.json`), so breaking a
  box after a relog still cleans up its waypoint; worlds not visited for 90 days are
  pruned automatically
- Waypoints are saved through Xaero's own system, so they show up on the minimap **and**
  the world map and survive restarts
- 100 % client-side; Fabric API is optional (only needed for the command on Fabric);
  safe to install without Xaero (does nothing then)

## Configuration

`config/shulkaero/config.json` (created on first run, apply with `/shulkaero reload`):

| Option | Default | Meaning |
|---|---|---|
| `enabled` | `true` | Master switch (also `/shulkaero toggle`) |
| `onlyNamedBoxes` | `false` | Only track boxes with a custom (anvil) name |
| `waitForPickup` | `true` | Keep the waypoint until the dropped box is picked up |
| `pickupSearchRadius` | `8` | Search radius (blocks) for the dropped box item |
| `checkIntervalTicks` | `10` | How often tracked positions are checked |
| `feedback` | `"actionbar"` | `"actionbar"`, `"chat"` or `"none"` |
| `useOwnWaypointSet` | `false` | Put shulker waypoints into their own waypoint set |
| `waypointSetName` | `"Shulkaero"` | Name of that set |
| `colorFromNameCodes` | `false` | Waypoint color from a literal `§`-code in the box name |
| `namePrefix` | `""` | Prefix for every waypoint name, e.g. `"[Box] "` |

## Supported versions

| Build target | Covers Minecraft | Loaders | Jar |
|---|---|---|---|
| `1.21.1` | 1.21 – 1.21.1 | Fabric / NeoForge | separate jars |
| `1.21.4` | 1.21.2 – 1.21.4 | Fabric / NeoForge | separate jars |
| `1.21.5` | 1.21.5 | Fabric / NeoForge | separate jars |
| `1.21.8` | 1.21.6 – 1.21.8 | Fabric / NeoForge | separate jars |
| `1.21.11` | 1.21.9 – 1.21.11 | Fabric / NeoForge | separate jars |
| `26.1` | 26.1 – 26.1.2 | Fabric **and** NeoForge | **one universal jar** |
| `26.2` | 26.2+ | Fabric **and** NeoForge | **one universal jar** |

> **Why is there no universal jar for 1.21.x?** On 1.21.x, Fabric still runs the game with
> intermediary mappings while NeoForge uses official names, so one jar cannot serve both
> loaders. Starting with Minecraft 26.1 the game is no longer obfuscated, both loaders use
> the same class names, and Shulkaero ships as a single universal jar for both.

Requires **Xaero's Minimap** (any recent version; 26.4.x recommended — older versions
without the extended waypoint palette fall back to the closest classic color).

## Building

Requires JDK 21+ (JDK 25 recommended; the build auto-provisions what it needs) and an
internet connection.

```bash
# one target
./gradlew collectJars -Ptarget=1.21.1

# all targets (PowerShell)
foreach ($t in (Get-ChildItem versions -Name) -replace '\.properties$','') { ./gradlew collectJars -Ptarget=$t }
```

The finished jars land in `dist/<target>/`:

- `shulkaero-fabric-<version>+mc<target>.jar` — Fabric (1.21.x targets)
- `shulkaero-neoforge-<version>+mc<target>.jar` — NeoForge (1.21.x targets)
- `shulkaero-universal-<version>+mc<target>.jar` — Fabric **and** NeoForge (26.x targets)

Adding another target is just a new file in `versions/` (Minecraft, NeoForge, Xaero
versions and metadata ranges) — the Java code is shared and version-agnostic across
1.21 – 26.2.

## How it works

- A mixin into `BlockItem#place` captures placements by the local player client-side
  (the item stack is copied *before* vanilla shrinks it, so the custom name of the last
  box in a stack isn't lost). After a ~10-tick confirmation window the waypoint is
  created via Xaero's hud API (`BuiltInHudModules.MINIMAP` → current minimap world →
  current/configured waypoint set), followed by an immediate save through Xaero's world
  manager IO.
- A lightweight client-tick check (every 10 ticks, only tracked positions, only loaded
  chunks) notices when a tracked box is gone, then watches for the dropped box item
  nearby; once the item is gone, it removes the matching waypoint — matched by exact
  position **and** name, so player-edited waypoints survive.
- Tracked positions are bucketed by Xaero's own per-world path (server/world +
  dimension), so multiworld servers and dimension changes behave correctly.

## Notes & limitations

- Only boxes placed by **you** are tracked (the box's name isn't available client-side
  for other players' placements).
- If a tracked box is destroyed while the chunk is not loaded on your client, the
  waypoint is removed the next time you come near the position.
- Waypoints are added to the waypoint set that is currently active in Xaero.

## License

[MIT](LICENSE) © CptGummiball
