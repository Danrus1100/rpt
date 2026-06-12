# AGENTS.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Overview

RPT (`[RPT] Resource Packs Tweaks`) is a **client-only Minecraft Fabric mod** that adds resource-pack–driven item model features: variable/template/regex item models, conditional and select model properties, custom item transforms, texture (equipment) swappers, first-person animations, and per-pack settings/metadata. It is built on top of **RPF** (`com.danrus:rpf`, "Resource Pack Framework"), an upstream library that supplies the event bus, model-baking delegation, and `SignedItemModel` infrastructure RPT hooks into.

`com.danrus.rpt` is the root package. Group id is `com.danrus`, artifact `rpt`.

## Build system

This is a **multi-version** project built with two stacked Gradle plugins:

- **Stonecutter** (`dev.kikugie.stonecutter`) — manages building the same source against multiple Minecraft versions. Configured in `settings.gradle.kts`. Supported versions: **1.21.8, 1.21.10, 1.21.11, 26.1**. The *active* version (what the root project currently represents) is set by `stonecutter active "<ver>"` in `stonecutter.gradle.kts` (default/VCS version is `1.21.8`). Each version's dev runtime lives under `versions/<ver>/run/`.
- **modstitch** (`dev.isxander.modstitch.base`) — wraps Fabric Loom, mod metadata, mixin registration, and the final jar. Configured in `build.gradle.kts`.

Per-version dependency values (`deps.mc`, `deps.fapi`, `deps.parchment`, `deps.rpf`, `mod.mcdep`, etc.) are injected by Stonecutter per version node — they are **not** all in the root `gradle.properties` (which only holds version-independent props like `mod.id`, `mod.version`, `deps.rpf`). Mod metadata is expanded into `fabric.mod.json` from `src/main/templates/fabric.mod.json` via `processResources`.

### Commands

```bash
# Build the currently active version
./gradlew build

# Launch the dev Minecraft client for the active version
# (modstitch run named "rpt" -> client run)
./gradlew runRpt

# Switch the active version (then build/run as above)
./gradlew "Set active project to 1.21.11"
# ...or edit stonecutter.gradle.kts: stonecutter active "1.21.11"

# Build all versions at once
./gradlew chiseledBuild

# Publish (Modrinth/CurseForge/Discord) — requires *-token props
./gradlew publishMods
```

There is **no test suite** in this repo. Validation is done by running the client (`runRpt`) against the test resource packs found in `versions/<ver>/run/resourcepacks/` (e.g. `test_rp`, `six-seven-rp`).

## Cross-version source: read this before editing `.java`

Because one source tree compiles against several Minecraft versions with incompatible APIs, version differences are handled **two** ways. When editing, preserve both mechanisms exactly — they are parsed as comments and rewritten at build time:

1. **Stonecutter preprocessor comments** inside source files. These look like comments but are processed by version predicate. Examples (see `core/OwnerHolder.java`):
   ```java
   //? if <=1.21.8 {
   private final @Nullable LivingEntity owner;
    //? } else {
   /*private final @Nullable net.minecraft.world.entity.ItemOwner owner;
   *///? }

   return get()
   //? >=1.21.10
   //.asLivingEntity()
   ;
   ```
   The active branch is live code; inactive branches are kept inside `/* */`. Do not "clean up" these comments.

2. **String replacements** declared in `build.gradle.kts` under `stonecutter { replacements { ... } }`. These rewrite class names / imports across versions (e.g. `ResourceLocation`→`Identifier`, `PlayerRenderState`→`AvatarRenderState`, `ArmorStandModel` import moves) for `>=1.21.11` / `>=1.21.10` / `>=26.1`. Write source in the **base (lowest-version) form**; add a replacement rule here when a newer MC version renames an API rather than hand-editing per version.

## Architecture

### Entry point and RPF integration
- `Rpt` (`Rpt.java`) is the `ClientModInitializer`. It holds the singleton managers (`TemplatesManager`, `TextureSwappersManager`, `FirstPersonAnimManager`), registers reload listeners, and calls `RptHooks.register()`.
- `RptHooks` is the bridge to RPF. Each `@RptHook` static method takes one RPF event subtype; `register()` reflectively wires them onto `Rpf.getEventBus()`. This is where RPT participates in RPF's model lifecycle: model discovery, pre/post bake, per-stack model param updates, and select-property delegation. To add behavior in the model pipeline, add a `@RptHook` method here rather than inventing a new registration path.

### Package layout (`com.danrus.rpt.*`)
- **`core`** — version-agnostic logic and the managers: `template/` (template models), `textures/` (equipment swappers), `selection/` (nested selector engine + selector types), `expression/` (EvalEx-backed numeric/vector expressions, dependency `com.ezylang:EvalEx`), `arm/` & `fpa/` (custom item transforms / first-person anims), `meta/` (pack metadata + YACL-backed per-pack settings), `item/` (RPT field & model-update data).
- **`impl`** — concrete model/property implementations registered into the game: `model/` (e.g. `TemplateItemModel`, `RegexItemModel`, `VariableItemModelWrapper`, `RptSelectItemModel`), `select/` (select properties like biome/weather/difficulty/constants), `conditional/` (boolean properties like `HasFlagProperty`, `InFluidProperty`, `MatchCustomNameRegexProperty`).
- **`duck`** — duck-typing interfaces (`Rpt*Holder`, `RptBakingContext`, `RptClientItem`, etc.) implemented onto vanilla classes via mixins. Code casts a vanilla object to one of these interfaces (`RptFieldHolder.class.cast(stack)`) to read/write injected RPT state.
- **`mixin`** — Mixins, organized by area (`load/`, `regs/`, `render/entity/`, `render/fpa/`, `meta/`, `rpf/`, `accessor/`).
- **`plugin`** — `Rpt12111MixinPlugin`, the mixin config plugin gating the 1.21.11+ mixin set at load time.

### The two mixin configs (important)
There are **two** mixin JSONs, both registered in `build.gradle.kts` (`mixin { configs.register("rpt"); configs.register("rpt.12111") }`):
- `rpt.mixins.json` — the main set, applied on all versions.
- `rpt.12111.mixins.json` — mixins that only apply on **1.21.11 / 26.1+**, gated at runtime by `Rpt12111MixinPlugin.shouldApplyMixin` (parses the raw MC version: applies when major≥26, or 1.21.patch≥11). Put a mixin here when it targets classes/APIs that only exist on the newer versions.

The access widener is `rpt.accesswidener` (the `modstitch.ct` file), exposing internal `ModelBakery` / `ModelManager` / `SelectItemModel` members needed by the load mixins.

## Pack-facing format
The resource-pack JSON format RPT consumes (the `rpt` field on item models, and the `rpt:template` / `rpt:variable` / `rpt:regex` model types, plus conditional/select properties). Public docs: https://danrus1100.github.io/rtp-rpf-docs/

## git and GitHub
Never try or make commits/push by yourself, only if user says do this