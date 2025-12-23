# 🚀 Implementation Plan - Navigation & Progression Redesign

## Phase 1: Core Components ✅

### 1.1 Breadcrumb Component ✅
- ✅ Created `BreadcrumbView.java`
- ✅ Max 3 levels, clickable segments
- ⏳ Add to layouts

### 1.2 Progress HUD Component ✅
- ✅ Created `ProgressHUDView.java`
- ✅ Shows stars, next unlock, progress bar
- ⏳ Add to layouts

## Phase 2: ProgressionManager Updates

### 2.1 Fix Unlock Bug
**File**: `ProgressionManager.java`
**Issue**: `isPlanetUnlocked()` uses `contains()` - false positives
**Fix**: Use proper string splitting and exact matching

### 2.2 Unify Unlock System
**Remove**: Fuel cells from unlock logic
**Keep**: Only stars for progression
**Update**: All unlock checks to use stars only

### 2.3 Add Helper Methods
- `getNextUnlockTarget()` - Returns next planet/galaxy to unlock
- `getUnlockProgress()` - Returns progress toward next unlock
- `getGalaxyNameForPlanet()` - Helper for breadcrumbs

## Phase 3: BattleActivity Integration

### 3.1 Add Breadcrumb
- Get planet name from planetId
- Get galaxy name from planetId
- Display: Galaxy › Planet › Battle

### 3.2 Integrate ProgressionManager
- Replace `dbHelper.addStars()` with `ProgressionManager.addStars()`
- Auto-check unlocks after battle
- Show unlock notifications

### 3.3 Add Battle Context
- Display planet icon/name
- Show word theme
- Planet-themed enemy

### 3.4 Auto-Navigation
- After victory, check if next node unlocked
- Auto-navigate to next node (if available)
- Show celebration if planet/galaxy unlocked

## Phase 4: Layout Updates

### 4.1 BattleActivity Layout
- Add BreadcrumbView at top
- Add ProgressHUDView (optional, can be in parent)
- Add planet context display

### 4.2 PlanetMapActivity Layout
- Add BreadcrumbView
- Add ProgressHUDView
- Show next unlock requirement

### 4.3 Other Activity Layouts
- Add BreadcrumbView to LearnWordsActivity
- Add BreadcrumbView to other learning activities

## Phase 5: Navigation Simplification

### 5.1 Remove Redundant Screens
- Keep: Galaxy Map → Planet Map → Learning Path
- Remove: Separate battle menu (integrate into path)
- Simplify: Reward screens (inline animations)

### 5.2 Auto-Navigation Logic
- After activity completion → check next node
- If unlocked → auto-navigate
- If locked → show requirement

## Phase 6: UX Copy

### 6.1 Vietnamese
- Progress: "Sắp mở hành tinh mới!"
- Battle: "Chiến đấu tại {Planet}"
- Unlock: "Hành tinh mới đã mở!"

### 6.2 English
- Progress: "Almost unlocked!"
- Battle: "Battle on {Planet}"
- Unlock: "New planet unlocked!"

## Implementation Order

1. ✅ Create components (BreadcrumbView, ProgressHUDView)
2. ⏳ Fix ProgressionManager unlock bug
3. ⏳ Update BattleActivity with ProgressionManager
4. ⏳ Add breadcrumb to BattleActivity
5. ⏳ Add layouts
6. ⏳ Test unlock flow
7. ⏳ Add auto-navigation
8. ⏳ Update other activities

