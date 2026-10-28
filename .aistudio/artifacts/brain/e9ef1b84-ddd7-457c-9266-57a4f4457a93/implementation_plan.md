# KhetKhata (शेतखाते) — UI Performance & Responsiveness Fix

This plan applies only the requested performance optimizations to eliminate UI lag and jank without changing any app features.

---

## Exact Performance Changes

1. **Instant Press State (`scale 0.97`) & `<= 150ms` Transform/Opacity Only**:
   - Update `BounceableCard` to animate press scale to **`0.97f`** using a fast **`80ms` tween** (`<= 150ms`) applied inside `Modifier.graphicsLayer` (GPU transform only, zero layout pass).
   - Keep `AnimatedVisibility` transitions at **`120ms`** using only `fadeIn`/`fadeOut` and `scaleIn`/`scaleOut` (transform + opacity only).
   - Reduce post-save celebration delay in `KhetKhataViewModel` so saving feels instantaneous (`<= 150ms`).

2. **Remove All Infinite Animations**:
   - Remove `rememberInfiniteTransition` from `AnimatedTeachingHand` (render a static `👆` indicator with zero per-frame layout invalidation).
   - Remove `rememberInfiniteTransition` from the microphone button in `KhetKhataTopBar` (which was running continuously every frame whenever online).

3. **Remove Heavy Box-Shadows & Blur Effects**:
   - Remove `.shadow(...)` modifiers from `BounceableCard`, `KhetKhataTopBar`, `LanguageScreen`, `HomeScreen`, `CategoryScreen`, `AmountScreen`, `ProfitScreen`, and dialogs, relying on crisp solid borders (`border(...)`) and high-contrast surfaces.

4. **Update Only What Changed (No Whole-Screen Re-renders on Tap)**:
   - Isolate the dynamic `AmountDisplayBox` and `SaveEntryButton` composables on `AmountScreen` and stabilize keypad/chip callbacks with `remember`, so tapping digits updates only the display box instead of re-rendering all 12 keypad buttons, 4 chips, and header pill.

5. **Zero Layout Shifts**:
   - Keep all tile grids, keypad buttons, and action buttons at fixed heights with no layout-changing animations.
