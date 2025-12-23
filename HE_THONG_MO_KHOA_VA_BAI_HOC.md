# 🎓 Hệ Thống Mở Khóa và Bài Học - Tài Liệu Kỹ Thuật

## 📋 Tổng Quan

Tài liệu này giải thích hệ thống mở khóa bài học (lesson/node) và hành tinh đã được xây dựng lại hoàn toàn để đảm bảo logic rõ ràng, nhất quán và dễ bảo trì.

## 🔍 Vấn Đề Của Hệ Thống Cũ

### 1. **Logic Mở Khóa Rời Rạc**
- Sử dụng SharedPreferences với string concatenation (`"animal,color,number"`) - dễ lỗi
- Không có logic rõ ràng cho việc mở khóa lesson/node
- Hai hệ thống riêng biệt: ProgressionManager (SharedPreferences) và GameDatabaseHelper (SQLite)
- Không đồng bộ giữa database và unlock status

### 2. **Thiếu Quy Tắc Mở Khóa Bài Học**
- Lesson đầu tiên luôn mở khóa (hardcoded)
- Không có logic để mở khóa lesson tiếp theo
- Không kiểm tra completion của lesson trước khi mở lesson sau
- Không có tracking rõ ràng về lesson nào đã hoàn thành

### 3. **Mở Khóa Hành Tinh Không Đầy Đủ**
- Chỉ kiểm tra số sao, không kiểm tra completion của planet trước
- Không có logic tuần tự: planet N chỉ mở khi planet N-1 hoàn thành
- Không tích hợp với lesson completion

## ✅ Giải Pháp: Hệ Thống Mới

### Kiến Trúc Tổng Quan

```
┌─────────────────────────────────────────────────────────┐
│              LessonUnlockManager                         │
│  - Quản lý unlock status (SharedPreferences)            │
│  - Logic mở khóa lesson dựa trên completion            │
│  - Logic mở khóa planet dựa trên stars + completion     │
└─────────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────────┐
│              ProgressionManager                          │
│  - Quản lý stars, badges, achievements                 │
│  - Tích hợp với LessonUnlockManager                     │
│  - Trigger unlock checks sau khi earn stars              │
└─────────────────────────────────────────────────────────┘
                        ↕
┌─────────────────────────────────────────────────────────┐
│              GameDatabaseHelper                          │
│  - Lưu trữ dữ liệu (planets, scenes, words)            │
│  - Tracking completion status (is_completed)            │
│  - Cung cấp data cho unlock logic                        │
└─────────────────────────────────────────────────────────┘
```

## 📚 Quy Tắc Mở Khóa

### 1. **Mở Khóa Bài Học (Lesson/Node)**

#### Quy Tắc:
1. **Lesson đầu tiên** của mỗi planet **luôn được mở khóa** khi planet được mở
2. **Lesson tiếp theo** được mở khóa khi:
   - Lesson trước đó đã **hoàn thành** (is_completed = 1)
   - VÀ đạt đủ số sao yêu cầu (nếu có)

#### Flow:
```
Planet Unlocked
    ↓
Lesson 1 Unlocked (automatic)
    ↓
User completes Lesson 1
    ↓
Lesson 2 Unlocked (automatic)
    ↓
User completes Lesson 2
    ↓
Lesson 3 Unlocked (automatic)
    ...
```

#### Implementation:
```java
// Trong LessonUnlockManager.java

public boolean completeLesson(int planetId, int sceneId, int starsEarned) {
    // 1. Đánh dấu lesson đã hoàn thành
    markLessonCompleted(planetId, sceneId);
    
    // 2. Mở khóa lesson tiếp theo
    unlockNextLesson(planetId, sceneId);
    
    // 3. Kiểm tra xem planet đã hoàn thành chưa
    checkPlanetCompletion(planetId);
    
    return newLessonUnlocked;
}
```

### 2. **Mở Khóa Hành Tinh (Planet)**

#### Quy Tắc:
1. **Planet đầu tiên** của galaxy đầu tiên **luôn được mở khóa**
2. **Planet tiếp theo** được mở khóa khi:
   - Đạt đủ **số sao yêu cầu** (từ ProgressionManager)
   - VÀ **tất cả lessons của planet trước đó đã hoàn thành** (nếu có)

#### Flow:
```
Galaxy 1, Planet 1: Unlocked (0 stars)
    ↓
User earns 20 stars + completes all lessons in Planet 1
    ↓
Galaxy 1, Planet 2: Unlocked (20 stars required)
    ↓
User earns 50 stars + completes all lessons in Planet 2
    ↓
Galaxy 1, Planet 3: Unlocked (50 stars required)
    ...
```

#### Implementation:
```java
// Trong LessonUnlockManager.java

public void checkAndUnlockPlanet(String planetKey, int requiredStars, int currentStars) {
    // 1. Kiểm tra đã unlock chưa
    if (isPlanetUnlocked(planetKey)) return;
    
    // 2. Kiểm tra đủ sao chưa
    if (currentStars < requiredStars) return;
    
    // 3. Kiểm tra planet trước đã hoàn thành chưa
    if (!isPreviousPlanetCompleted(planetKey)) return;
    
    // 4. Mở khóa planet
    unlockPlanet(planetKey);
    
    // 5. Tự động mở lesson đầu tiên của planet
    refreshPlanetLessons(planetId);
}
```

### 3. **Mở Khóa Thiên Hà (Galaxy)**

#### Quy Tắc:
1. **Galaxy đầu tiên** **luôn được mở khóa**
2. **Galaxy tiếp theo** được mở khóa khi:
   - Đạt đủ **số sao yêu cầu**

#### Flow:
```
Galaxy 1: Unlocked (0 stars)
    ↓
User earns 15 stars
    ↓
Galaxy 2: Unlocked (15 stars required)
    ↓
User earns 30 stars
    ↓
Galaxy 3: Unlocked (30 stars required)
```

## 🔧 Các Component Chính

### 1. **LessonUnlockManager**

**File**: `app/src/main/java/com/example/engapp/manager/LessonUnlockManager.java`

**Trách nhiệm**:
- Quản lý unlock status của lessons, planets, galaxies
- Lưu trữ trong SharedPreferences (JSON format)
- Logic mở khóa dựa trên completion và stars
- Đồng bộ với database khi cần

**Key Methods**:
```java
// Lesson methods
boolean isLessonUnlocked(int planetId, int sceneId)
boolean completeLesson(int planetId, int sceneId, int starsEarned)
void refreshPlanetLessons(int planetId)
boolean isLessonCompleted(int planetId, int sceneId)

// Planet methods
boolean isPlanetUnlocked(String planetKey)
void unlockPlanet(String planetKey)
void checkAndUnlockPlanet(String planetKey, int requiredStars, int currentStars)
boolean isPlanetCompleted(int planetId)

// Galaxy methods
boolean isGalaxyUnlocked(String galaxyKey)
void unlockGalaxy(String galaxyKey)
```

**Data Storage**:
- `unlocked_lessons`: Set<String> - Format: "planetId_sceneId"
- `completed_lessons`: Set<String> - Format: "planetId_sceneId"
- `unlocked_planets`: Set<String> - Planet keys
- `unlocked_galaxies`: Set<String> - Galaxy keys

### 2. **ProgressionManager (Updated)**

**File**: `app/src/main/java/com/example/engapp/manager/ProgressionManager.java`

**Thay Đổi**:
- Tích hợp với `LessonUnlockManager`
- Sử dụng `LessonUnlockManager` để kiểm tra unlock status
- Gọi `checkAndUnlockPlanet()` sau khi earn stars
- Method mới: `recordLessonCompleted()` để track lesson completion

**Key Changes**:
```java
// OLD: String concatenation
String unlockedPlanets = prefs.getString("unlocked_planets", "animal");
unlockedPlanets += "," + planetId;

// NEW: Use LessonUnlockManager
lessonUnlockManager.isPlanetUnlocked(planetKey);
lessonUnlockManager.checkAndUnlockPlanet(planetKey, required, currentStars);
```

### 3. **GameDatabaseHelper (Updated)**

**File**: `app/src/main/java/com/example/engapp/database/GameDatabaseHelper.java`

**Thay Đổi**:
- Thêm method `getPlanetByKey(String planetKey)`
- Thêm method `updateSceneUnlockStatus()` (placeholder, unlock status trong SharedPreferences)
- Thêm field `galaxyId` vào `PlanetData`

**Key Methods**:
```java
PlanetData getPlanetByKey(String planetKey)
void updateSceneUnlockStatus(int sceneId, boolean isUnlocked)
List<SceneData> getScenesForPlanet(int planetId)
```

## 🔄 Flow Hoàn Chỉnh

### Scenario: User Hoàn Thành Một Battle

```
1. User plays BattleActivity
   ↓
2. User wins battle, earns 6 stars (3 stars × 2)
   ↓
3. BattleActivity calls:
   progressionManager.recordGameCompleted("battle", 6);
   ↓
4. ProgressionManager:
   - Adds 6 stars to total
   - Calls checkForNewUnlocks()
   ↓
5. ProgressionManager.checkForNewUnlocks():
   - Checks each planet requirement
   - For each planet with enough stars:
     lessonUnlockManager.checkAndUnlockPlanet(planetKey, required, current);
   ↓
6. LessonUnlockManager.checkAndUnlockPlanet():
   - Checks if previous planet completed
   - If yes, unlocks planet
   - Unlocks first lesson of planet
   ↓
7. If sceneId provided, BattleActivity calls:
   progressionManager.recordLessonCompleted(planetId, sceneId, 6);
   ↓
8. ProgressionManager.recordLessonCompleted():
   - Calls lessonUnlockManager.completeLesson(planetId, sceneId, 6)
   ↓
9. LessonUnlockManager.completeLesson():
   - Marks lesson as completed
   - Unlocks next lesson in planet
   - Checks if planet completed
   ↓
10. If planet completed:
    - ProgressionManager.recordPlanetCompleted(planetKey)
    - Awards badge
    - Checks for next planet unlock
```

## 📊 Cấu Trúc Dữ Liệu

### SharedPreferences (LessonUnlockManager)

```json
{
  "unlocked_lessons": ["1_1", "1_2", "1_3", "2_1"],
  "completed_lessons": ["1_1", "1_2", "1_3"],
  "unlocked_planets": ["coloria_prime", "toytopia_orbit"],
  "unlocked_galaxies": ["milky_way", "andromeda"]
}
```

**Format**: `"planetId_sceneId"` cho lessons

### Database (GameDatabaseHelper)

**TABLE_SCENES**:
- `id`: Scene ID
- `planet_id`: Planet ID
- `order_index`: Thứ tự lesson (1, 2, 3, ...)
- `is_completed`: Đã hoàn thành chưa (0/1)
- `stars_earned`: Số sao đạt được

**TABLE_PLANETS**:
- `id`: Planet ID
- `galaxy_id`: Galaxy ID
- `planet_key`: Key duy nhất (ví dụ: "coloria_prime")
- `order_index`: Thứ tự trong galaxy
- `is_unlocked`: Đã mở khóa chưa (0/1) - **Note**: Status này được quản lý bởi LessonUnlockManager

## 🎯 Yêu Cầu Mở Khóa Mặc Định

### Galaxy 1: Milky Way (Beginner)
- **Planet 1** (Coloria Prime): 0 stars (always unlocked)
- **Planet 2** (Toytopia Orbit): 20 stars
- **Planet 3** (Animania Wild): 50 stars
- **Planet 4** (Numberia Station): 100 stars

### Galaxy 2: Andromeda (Explorer)
- **Planet 5** (Citytron Nova): 150 stars
- **Planet 6** (Foodora Station): 200 stars
- **Planet 7** (Weatheron Sky): 280 stars
- **Planet 8** (Familia Home): 360 stars

### Galaxy 3: Nebula Prime (Advanced)
- **Planet 9** (RoboLab): 450 stars
- **Planet 10** (TimeLapse): 550 stars
- **Planet 11** (Storyverse): 660 stars
- **Planet 12** (Natura): 780 stars

## 🔌 Integration Points

### 1. **BattleActivity**

```java
// After battle victory
if (isVictory && stars > 0) {
    int starsEarned = stars * 2;
    int sceneId = getIntent().getIntExtra("scene_id", -1);
    
    // Record game completion
    progressionManager.recordGameCompleted("battle", starsEarned);
    
    // Record lesson completion if sceneId provided
    if (sceneId > 0) {
        progressionManager.recordLessonCompleted(planetId, sceneId, starsEarned);
    }
    
    // Check for new unlocks
    progressionManager.checkForNewUnlocks();
}
```

### 2. **LearnWordsActivity** (Tương tự)

```java
// After learning session
progressionManager.recordLessonCompleted(planetId, sceneId, starsEarned);
progressionManager.checkForNewUnlocks();
```

### 3. **PlanetMapActivity**

```java
// Check lesson unlock status
LessonUnlockManager unlockManager = LessonUnlockManager.getInstance(this);
for (SceneData scene : scenes) {
    boolean isUnlocked = unlockManager.isLessonUnlocked(planetId, scene.id);
    // Update UI accordingly
}
```

## 🐛 Debugging & Testing

### Reset Progress
```java
LessonUnlockManager.getInstance(context).resetAllProgress();
```

### Check Unlock Status
```java
LessonUnlockManager unlockManager = LessonUnlockManager.getInstance(context);

// Check lesson
boolean lessonUnlocked = unlockManager.isLessonUnlocked(planetId, sceneId);
boolean lessonCompleted = unlockManager.isLessonCompleted(planetId, sceneId);

// Check planet
boolean planetUnlocked = unlockManager.isPlanetUnlocked("coloria_prime");
boolean planetCompleted = unlockManager.isPlanetCompleted(planetId);

// Check galaxy
boolean galaxyUnlocked = unlockManager.isGalaxyUnlocked("milky_way");
```

### Manual Unlock (for testing)
```java
// Unlock a lesson
unlockManager.unlockLesson(planetId, sceneId);

// Unlock a planet
unlockManager.unlockPlanet("coloria_prime");

// Complete a lesson
unlockManager.completeLesson(planetId, sceneId, 3);
```

## 📝 Best Practices

### 1. **Luôn Sử Dụng LessonUnlockManager**
- Không trực tiếp thao tác với SharedPreferences
- Không hardcode unlock logic trong Activities

### 2. **Kiểm Tra Unlock Status Trước Khi Hiển Thị**
```java
// GOOD
if (lessonUnlockManager.isLessonUnlocked(planetId, sceneId)) {
    // Show lesson
} else {
    // Show lock icon, requirement
}

// BAD
if (scene.orderIndex == 1) {
    // Always unlocked - WRONG! Use LessonUnlockManager
}
```

### 3. **Gọi checkForNewUnlocks() Sau Khi Earn Stars**
```java
// After earning stars
progressionManager.addStars(amount, source);
progressionManager.checkForNewUnlocks(); // Important!
```

### 4. **Record Lesson Completion Khi Hoàn Thành**
```java
// After completing a lesson/activity
progressionManager.recordLessonCompleted(planetId, sceneId, starsEarned);
```

## 🚀 Migration từ Hệ Thống Cũ

### Bước 1: Khởi Tạo
- `LessonUnlockManager` tự động khởi tạo lesson đầu tiên
- Planet đầu tiên được unlock mặc định

### Bước 2: Migrate Existing Data
```java
// Nếu có dữ liệu cũ trong SharedPreferences
String oldUnlocked = prefs.getString("unlocked_planets", "animal");
String[] planets = oldUnlocked.split(",");
for (String planetKey : planets) {
    lessonUnlockManager.unlockPlanet(planetKey.trim());
}
```

### Bước 3: Refresh All Planets
```java
// Refresh unlock status cho tất cả planets
List<PlanetData> planets = dbHelper.getAllPlanets();
for (PlanetData planet : planets) {
    lessonUnlockManager.refreshPlanetLessons(planet.id);
}
```

## 📌 Tóm Tắt

### Điểm Mạnh của Hệ Thống Mới:
1. ✅ **Logic rõ ràng**: Quy tắc mở khóa được định nghĩa rõ ràng
2. ✅ **Nhất quán**: Một nguồn sự thật duy nhất (LessonUnlockManager)
3. ✅ **Dễ bảo trì**: Code tập trung, dễ debug
4. ✅ **Mở rộng được**: Dễ thêm logic mới (ví dụ: unlock theo thời gian, special events)
5. ✅ **Tích hợp tốt**: ProgressionManager và Database đồng bộ

### So Sánh:

| Aspect | Hệ Thống Cũ | Hệ Thống Mới |
|--------|-------------|--------------|
| Unlock Logic | Rời rạc, hardcoded | Tập trung, có quy tắc |
| Data Storage | String concatenation | JSON Set trong SharedPreferences |
| Lesson Unlock | Không có logic | Dựa trên completion |
| Planet Unlock | Chỉ kiểm tra stars | Stars + completion |
| Integration | Rời rạc | Tích hợp tốt |
| Debugging | Khó | Dễ (có methods rõ ràng) |

## 🎓 Kết Luận

Hệ thống mới cung cấp một nền tảng vững chắc cho việc quản lý progression trong game. Logic mở khóa rõ ràng, dễ hiểu và dễ bảo trì, đảm bảo trải nghiệm người dùng mượt mà và công bằng.

---

**Tác giả**: AI Assistant  
**Ngày tạo**: 2024  
**Phiên bản**: 1.0

