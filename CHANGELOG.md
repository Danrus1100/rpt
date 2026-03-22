# This massive update was made possible with the support of KSEPSP

## Breaking Changes
– In `rpt:arm_transform`, the game variable `arm` has been renamed to `holdArm`

## New Features:
- **Patch System**: Allows you to separate vanilla Resource Packs from RPT.
- **Swapper System**: Texture replacement based on player and item parameters for armor, spyglass, and carved pumpkins.
- **First-Person Transformation System**: Define custom positions and even animations for the first-person view.

## Additions
- New game variables added to `rpt:arm_transform`:
    1. `swing` — a value from 0 to 1 representing the hit animation progress.
    2. `swingArm` — 1 if the attacking arm is the right one, -1 if it's the left.
    3. `useArm` — 1 if the item is being used in the main hand, -1 if in the offhand.
    4. `useTick` — the remaining ticks of item use.
    5. `delta` — a value between 0 and 1 representing the frame progress (partial ticks).
- Added a new field `attack` to `rpt:arm_transform` (defaults to `true`). If set to `false`, the vanilla attack animation will not play in third-person view.
- New global game variables added:
    1. `delta` — a value between 0 and 1 representing the frame progress.
    2. `motionX`, `motionZ`, `speed`, `horizontalSpeed` — holder's movement and speed data.
    3. `onGround`, `isSprinting`, `isCrouching`, `isInWater`, `isInLava`, `isSwimming`, `isFallFlying` — entity status flags.
    4. `attackCooldown`, `attackProgress`, `usageProgress` — data for hit and usage animations.
- Added the `opposite` field for `rpt:arm_transform` — accepts the same structure as `transform` but applies transformations to the opposite arm.

## Fixes:
- `rpt:arm_transform` now works correctly with variables.
- Many other minor bug fixes and improvements.