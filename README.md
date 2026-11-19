<p align="center">
  <img src="images/logo.png" alt="Shulkaero" width="200">
</p>

<h1 align="center">Shulkaero</h1>

<p align="center"><b>Automatic Xaero waypoints for your shulker boxes.</b><br>
Client-side addon for Xaero's Minimap / World Map · Fabric &amp; NeoForge · MC 1.21 - 26.3</p>

<p align="center">
  <a href="#what-it-is">What it is</a> ·
  <a href="#features">Features</a> ·
  <a href="#configuration">Configuration</a> ·
  <a href="#supported-versions">Versions</a> ·
  <a href="#downloads">Downloads</a> ·
  <a href="#reporting-bugs">Bugs</a> ·
  <a href="#license">License</a>
</p>

---

<a id="what-it-is"></a>
![What it is](images/headers/what_is.png)

Whenever you place a shulker box, Shulkaero creates a waypoint at its position in
[Xaero's Minimap](https://modrinth.com/mod/xaeros-minimap) (and
[Xaero's World Map](https://modrinth.com/mod/xaeros-worldmap)) — named after the box
(custom anvil name, or "Shulker Box") and colored to match the box's dye color. When the
box is removed again, the waypoint disappears automatically — unless you already deleted
or renamed it yourself.

100 % client-side: nothing is installed on the server, and the mod is safe to keep
installed without Xaero (it simply does nothing then).

<a id="features"></a>
![Features](images/headers/features.png)

- Waypoint on shulker box placement, with the box's **name** and **color**
  (all 16 dye colors map to matching Xaero waypoint colors; the undyed box is purple)
- Placement is confirmed for ~half a second before the waypoint appears, so server-side
  rollbacks (claims/protection plugins) never leave ghost waypoints
- When the box is broken, the **waypoint stays while the dropped box item lies nearby** —
  it is removed once the box is picked up (by anyone), despawns or burns. Creative-mode
  breaking (no drop) removes it right away. Configurable (`waitForPickup`)
- Also detects removal by explosions, pistons, other players etc.
- Waypoints you deleted or renamed manually are left alone
- Actionbar/chat feedback (English + German), configurable or off
- `/shulkaero` client command: `list`, `clear`, `toggle`, `reload`
- Tracked boxes survive relogs; waypoints are saved through Xaero's own system, so they
  show on the minimap **and** the world map and survive restarts
- Multiworld servers and dimensions are kept apart (bucketed by Xaero's own world path)

<a id="configuration"></a>
![Configuration](images/headers/configuration.png)

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

More in the [wiki](../../wiki).

<a id="supported-versions"></a>
![Supported versions](images/headers/versions.png)

| Minecraft | Loaders | Jar |
|---|---|---|
| 1.21 - 1.21.1 | Fabric / NeoForge | `...+mc1.21-1.21.1` (one per loader) |
| 1.21.2 - 1.21.11 | Fabric / NeoForge | `...+mc1.21.2-1.21.11` (one per loader) |
| 26.1 - 26.3 | Fabric **and** NeoForge | **one universal jar** `...+mc26.1-26.3` |

From Minecraft 26.1 on the game is no longer obfuscated and both loaders use the same
class names, so Shulkaero ships as a single universal jar there. Details and test status:
[Supported versions](../../wiki/Supported-Versions) in the wiki.

**Requirements:** Xaero's Minimap (any recent version; 26.4.x recommended — older versions
without the extended waypoint palette fall back to the closest classic color). On Fabric,
Fabric API is optional and only needed for the `/shulkaero` command.

<a id="downloads"></a>
![Downloads](images/headers/downloads.png)

Jars are published on **Modrinth** and **CurseForge** only:

- Modrinth: https://modrinth.com/mod/shulkaero
- CurseForge: https://www.curseforge.com/minecraft/mc-mods/shulkaero

This repository holds the documentation, wiki and issue tracker. Pick the jar whose name
contains your Minecraft version, for your loader.

<a id="reporting-bugs"></a>
![Reporting bugs](images/headers/bugs.png)

Please use the [issue forms](../../issues/new/choose) and include your exact jar name,
loader version and `logs/latest.log`. Check the [FAQ](../../wiki/FAQ) first.

<a id="license"></a>
![License](images/headers/license.png)

Proprietary, © 2026 CptGummiball — see [LICENSE](LICENSE). In short: play with it, show
it in videos and streams, put it **unmodified** in modpacks (free or paid); no
modification, decompiling or re-uploading; compile-only use for compatibility development
is allowed. For anything else, open a
[permission request](../../issues/new?template=permission_request.yml).
Versions 1.0.0 and 1.1.0 were published under MIT and remain MIT.

Shulkaero is an unofficial addon for Xaero's Minimap / World Map and is not affiliated
with their author, Mojang or Microsoft.
