# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Minecraft server plugin called "Survisland" built using Paper API 26.2 (alpha) and Java 25. The plugin uses the Bukkit/Spigot framework and follows standard Minecraft plugin development patterns.

## Build System

- **Build Tool**: Gradle
- **Java Version**: 25 (required)
- **Minecraft Version**: 26.2 (now on Paper's stable channel)
- **Paper API**: 26.2.build.116-stable (via paperweight-userdev dev bundle)

## Common Commands

```bash
# Build the plugin
./gradlew build

# Run a local Minecraft server with the plugin (via run-paper plugin)
./gradlew runServer

# Clean build artifacts
./gradlew clean

# Build and run in one command
./gradlew build runServer
```

## Architecture

- **Modules**: `api` (reusable command framework + menu utility, no plugin-specific logic) and
  `plugin` (the actual Survisland plugin, depends on `api`)
- **Main Plugin Class**: `com.lwidev.survisland.Survisland` - extends JavaPlugin, entry point for the plugin
- **Plugin Configuration**: `plugin/src/main/resources/plugin.yml` - only metadata (name, version, main class,
  api-version); commands and permissions are **not** declared there anymore (see Commands below)
- **Package Structure**: `com.lwidev.survisland` (plugin) / `com.lwidev.survisland.api` (api module)
- **Build Configuration**: Uses xyz.jpenilla.run-paper plugin for local testing with a Minecraft server

## Commands (Brigadier + Survisland CommandAPI)

Commands are registered via Paper's Brigadier command API, not `plugin.yml`. Each command extends
`com.lwidev.survisland.api.command.SurvislandCommand` (in the `api` module), which:
- Auto-derives its permission node as `survisland.<name>` (and `survisland.<name>.<subcommand>` for
  subcommands built via `subcommand(name)`) — no permission string to declare by hand.
- Auto-registers those permission nodes with Bukkit at startup (see
  `com.lwidev.survisland.api.command.SurvislandCommands.register(...)`, called once from
  `Survisland.registerCommands()`).
- Enforces player-only access when constructed with `playerOnly = true`.

Target-selector arguments (`@a`, `@e`, `@p`, `@r`, `@s`, or a plain player name) use Paper's native
`ArgumentTypes`:
- `.player()`/`.players()` (via `resolvePlayer`/`resolvePlayers`) resolve **online players only** —
  purely local, no network call. Use these when offline targets don't make sense.
- `.playerProfiles()` (via `resolvePlayerProfiles`) additionally resolves **offline** players by name
  — see `SkinCommand` (`/skin force`/`/skin restore` work on a disconnected player, applying/queuing
  the change for their next join). For a plain name, this checks the server's local profile cache
  (`usercache.json`, i.e. anyone who has ever joined) first and only falls back to a **blocking Mojang
  HTTP call on the main thread** for a name never seen on this server — acceptable in practice, but
  worth knowing if a command ever needs to target a brand-new/never-joined pseudo.

Whichever type is used, note that `CommandChain` (the shared plumbing behind `argument(...)` chains)
must finish configuring each argument node (its own `executes()`/`then()`) *before* attaching it to
its parent via `.then()` — Brigadier's `ArgumentBuilder#then()` builds an immutable snapshot of the
child immediately, so mutating it afterward is silently a no-op. If a multi-argument subcommand ever
parses fine but always reports a generic "Unknown or incomplete command", check this ordering first.

## Development Notes

- UTF-8 encoding is enforced for all source files
- The project uses Paper API (not vanilla Bukkit) which provides additional features and performance improvements
- Plugin lifecycle is managed through `onEnable()` and `onDisable()` methods in the main class