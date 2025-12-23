# 🎨 Space English - Navigation & Progression Redesign

## 📐 NEW NAVIGATION FLOW

```
┌─────────────────────────────────────────────────────────┐
│              GALAXY MAP (Entry Point)                   │
│  Shows: 3 Galaxies (Beginner, Explorer, Advanced)       │
│  Progress: Global stars, Next unlock preview            │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│              PLANET MAP (Galaxy → Planets)              │
│  Shows: 4 Planets in selected galaxy                    │
│  Progress: Planet completion rings, Next node highlight │
│  Breadcrumb: 🌌 Galaxy Name                             │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│         LEARNING PATH (Planet → Nodes)                  │
│  Shows: 5 Nodes (Learn → Explore → Dialogue → Puzzle → Boss) │
│  Progress: Node completion, Stars needed for next       │
│  Breadcrumb: 🌌 Galaxy › 🪐 Planet                      │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│         ACTIVITY SCREEN (Node → Game)                   │
│  Shows: Learning activity or game                        │
│  Progress: Activity progress, Stars earned               │
│  Breadcrumb: 🌌 Galaxy › 🪐 Planet › 📚 Activity        │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│         BATTLE (Contextual, Auto-triggered)             │
│  Shows: Battle with planet-themed enemy                  │
│  Context: Planet icon, Word theme, Enemy name            │
│  Breadcrumb: 🌌 Galaxy › 🪐 Planet › ⚔️ Battle          │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│         REWARD + AUTO-UNLOCK                            │
│  Shows: Stars earned, Progress update, Next node unlock │
│  Action: Auto-navigate to next node (if unlocked)       │
│  No manual "back" needed                                 │
└─────────────────────────────────────────────────────────┘
```

## 🎯 KEY PRINCIPLES

### 1. One Journey, One Path
- No dead-end screens
- Continuous flow forward
- Auto-navigation after completion

### 2. One Unlock Rule
- **ONLY STARS** for progression
- Remove fuel cells completely
- Stars unlock: nodes → planets → galaxies

### 3. Always Show "What's Next"
- Global HUD: Total stars, next unlock requirement
- Planet card: Progress ring, next node highlight
- Predictive unlock: Greyed planet with "Unlock at ⭐ X"

## 📊 UNIFIED UNLOCK SYSTEM

### Stars Earned From:
- ✅ Learning nodes (LearnWordsActivity)
- ✅ Battle victories (BattleActivity)
- ✅ Daily missions
- ✅ Puzzle completions
- ✅ Dialogue completions

### Stars Unlock:
- ✅ Next node in planet
- ✅ Next planet in galaxy
- ✅ Next galaxy

### Unlock Requirements:
```
Galaxy 1 (Beginner):
  - Planet 1 (Coloria): 0 stars (always unlocked)
  - Planet 2 (Toytopia): 20 stars
  - Planet 3 (Animania): 50 stars
  - Planet 4 (Numberia): 100 stars

Galaxy 2 (Explorer):
  - Planet 5 (Citytron): 150 stars
  - Planet 6 (Foodora): 200 stars
  - Planet 7 (Weatheron): 280 stars
  - Planet 8 (Familia): 360 stars

Galaxy 3 (Advanced):
  - Planet 9 (RoboLab): 450 stars
  - Planet 10 (TimeLapse): 550 stars
  - Planet 11 (Storyverse): 660 stars
  - Planet 12 (Natura): 780 stars
```

## 🧭 BREADCRUMB SYSTEM

### Design Spec:
- **Location**: Top bar, always visible
- **Max Depth**: 3 levels
- **Format**: Icon + Short Text
- **Clickable**: Each segment navigates back
- **Style**: Lightweight, non-intrusive

### Examples:
```
🌌 Beginner Galaxy
🌌 Beginner Galaxy › 🪐 Coloria Prime
🌌 Beginner Galaxy › 🪐 Coloria Prime › ⚔️ Battle
🌌 Beginner Galaxy › 🪐 Coloria Prime › 📚 Learn Words
```

## 📈 PROGRESS INDICATORS

### A) Global HUD (Top Bar)
```
┌─────────────────────────────────────────┐
│ ⭐ 42 / 50 → Unlock Toytopia            │
│ [████████░░] 84%                        │
└─────────────────────────────────────────┘
```

### B) Planet Progress Card
```
┌─────────────────────────────────────────┐
│ 🪐 Coloria Prime                        │
│ Progress: 3 / 5 battles completed       │
│ [███████░░░] 60%                        │
│ Next: 🧩 Puzzle Zone (needs 10 ⭐)      │
└─────────────────────────────────────────┘
```

### C) Predictive Unlock
```
┌─────────────────────────────────────────┐
│ 🪐 Toytopia (Locked)                    │
│ Unlock at ⭐ 20                          │
│ [████████░░] 80% (16/20 stars)         │
│ "Almost there! 4 more stars!"           │
└─────────────────────────────────────────┘
```

## ⚔️ BATTLE INTEGRATION

### Battle Triggers:
1. **Boss Node**: Auto-triggered when reaching boss node
2. **End of Word List**: Optional battle after learning words
3. **Planet Completion**: Final battle before next planet

### Battle Context:
- **Planet Icon**: Shows current planet
- **Word Theme**: Displays planet's vocabulary theme
- **Enemy**: Planet-themed enemy (e.g., "Color Beast" for Coloria)

### Auto-Unlock After Battle:
1. Stars awarded immediately
2. Progress bar updates
3. Next node unlocks (if stars sufficient)
4. Auto-navigate to next node
5. Celebration animation if planet/galaxy unlocked

## 🎨 UX COPY

### Vietnamese (VI)
- Progress: "Sắp mở hành tinh mới!"
- Battle Context: "Chiến đấu tại Coloria Prime"
- Unlock: "Hành tinh mới đã mở!"
- Next Node: "Hoàn thành để mở khóa nút tiếp theo"
- Stars Needed: "Cần thêm X ⭐ để mở khóa"

### English (EN)
- Progress: "Almost unlocked!"
- Battle Context: "Battle on Coloria Prime"
- Unlock: "New planet unlocked!"
- Next Node: "Complete to unlock next node"
- Stars Needed: "Need X more ⭐ to unlock"

## 🚀 IMPLEMENTATION PRIORITY

1. ✅ Breadcrumb Component
2. ✅ Unified ProgressionManager (Stars only)
3. ✅ Progress Indicator Components
4. ✅ Battle Context Integration
5. ✅ Auto-Unlock Logic
6. ✅ Simplified Navigation

