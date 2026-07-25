# Changelog

All notable changes to Shulkaero are documented here.
Versions are `<mod>+mc<target>`; each release ships jars for all supported targets
(1.21.1, 1.21.4, 1.21.5, 1.21.8, 1.21.11 as Fabric/NeoForge pairs; 26.1 and 26.2 as
universal jars for both loaders).

## 1.1.0 — 2026-07-25

### Added
- **Pickup-aware waypoint removal:** after breaking a tracked box, the waypoint now
  stays at the break position while the dropped box item lies nearby, and is only
  removed once the box is picked up, despawns or burns. Creative-mode breaking (no
  drop) removes it immediately. Disable via `waitForPickup` for the old behavior.
- **Config file** `config/shulkaero/config.json`: `enabled`, `onlyNamedBoxes`,
  `waitForPickup`, `pickupSearchRadius`, `checkIntervalTicks`, `feedback`,
  `useOwnWaypointSet`, `waypointSetName`, `colorFromNameCodes`, `namePrefix`.
- **Actionbar/chat feedback** for created/kept/removed waypoints, localized in
  English and German (`feedback` option, default actionbar).
- **`/shulkaero` client command** with `list`, `clear` (forget tracking, keep
  waypoints), `toggle` and `reload`. On NeoForge always available; on Fabric it
  needs Fabric API (optional dependency — everything else works without it).
- Optional dedicated waypoint set for shulker waypoints (`useOwnWaypointSet`).
- Optional waypoint color from a literal `§` color code in the box name
  (`colorFromNameCodes`).
- Unit tests for the pure logic (initials, color mapping, store migration,
  `§`-code parsing).

### Changed
- Waypoints are now created ~0.5 s after placement, once the placement is
  confirmed client-side — protection-plugin rollbacks no longer leave ghost
  waypoints.
- Tracking store upgraded to format v2 (state machine + per-world last-visit
  timestamp). v1 files from Shulkaero 1.0.0 are migrated automatically.
- Worlds not visited for 90 days are pruned from the tracking store on load.
- Add/remove waypoint log messages moved from INFO to DEBUG.

## 1.0.0 — 2026-07-25

Initial release.

- Placing a shulker box creates a Xaero's Minimap / World Map waypoint with the
  box's (custom) name and matching color (16 dye colors + purple for undyed).
- Breaking the box removes the waypoint again, unless the player already deleted
  or renamed it (matched by exact position and name).
- Tracked boxes persist across relogs, keyed by Xaero's per-world path.
- Targets: MC 1.21–1.21.11 (separate Fabric and NeoForge jars per version family)
  and MC 26.1/26.2+ (one universal jar for both loaders, since Minecraft is
  unobfuscated from 26.1 on).
