# Space English

Space English là ứng dụng học tiếng Anh theo chủ đề vũ trụ dành cho trẻ em.
Người chơi du hành qua các thiên hà và hành tinh, hoàn thành các scene học tập,
và theo dõi tiến độ qua XP, sao, huy hiệu. Ứng dụng kết hợp dữ liệu offline
trong SQLite với dịch vụ online để đăng nhập và luyện tập dạng câu hỏi hành vi
(behavioral).

## Mục lục
- Tổng quan
- Tính năng chính
- Luồng trải nghiệm
- Màn hình chính (Activity)
- Học tập và mini game
- Hệ thống tiến độ
- Ghi chú và nhắc nhở
- Dữ liệu và lưu trữ
- Firebase và dữ liệu behavioral
- Công nghệ sử dụng
- Cấu hình build
- Quyền truy cập
- Cài đặt và chạy
- Kiểm thử
- Ghi chú phát triển
- Xử lý lỗi thường gặp
- Tài khoản test

## Tổng quan
- Chủ đề vũ trụ, giao diện thân thiện cho trẻ 5-12 tuổi.
- Bản đồ nhiều tầng: Thiên hà -> Bản đồ sao -> Hành tinh -> Scene.
- Học từ vựng, câu, nghe và phản xạ qua nhiều hoạt động.
- Có hệ thống buddy đồng hành và hồ sơ tiến độ.
- Hỗ trợ ghi chú và nhắc nhở học tập offline.

## Tính năng chính
- Onboarding: Splash, Cutscene, Intro.
- Đăng nhập/đăng ký email bằng Firebase Auth.
- Bản đồ thiên hà và bản đồ sao tương tác, có animation di chuyển.
- Hành tinh với nhiều scene học tập và mini game.
- Học từ vựng theo thẻ, Word Lab, Word Review.
- Thử thách, boss gate, word battle.
- Hệ thống tiến độ: XP, level, sao, fuel cells, crystals, streak, huy hiệu.
- Buddy Room: chọn bạn đồng hành, nói chuyện, chơi, học.
- Profile: XP, số từ đã học, số game hoàn thành, % theo hành tinh.
- Ghi chú offline và nhắc nhở bằng thông báo hệ thống.

## Luồng trải nghiệm
1. SplashActivity -> CutsceneActivity -> IntroActivity
2. LoginActivity / SignUpActivity (Firebase Auth)
3. InteractiveGalaxyMapActivity -> InteractiveStarMapActivity
4. PlanetActivity -> Scene Activities
5. Các hub phụ trợ: SpaceshipHubActivity, WordLabActivity, BuddyRoomActivity,
   ProfileActivity

## Màn hình chính (Activity)
Onboarding và đăng nhập:
- SplashActivity: splash + animation.
- CutsceneActivity: giới thiệu cốt truyện.
- IntroActivity: hướng dẫn nhanh.
- LoginActivity / SignUpActivity: đăng nhập, đăng ký.

Bản đồ và di chuyển:
- InteractiveGalaxyMapActivity: chọn thiên hà.
- InteractiveStarMapActivity: chọn hành tinh trong thiên hà.
- InteractivePlanetMapActivity: bản đồ hành tinh (nút, node).
- SpaceTravelActivity: hoạt cảnh di chuyển.

Hành tinh và scene:
- PlanetActivity: danh sách scene theo hành tinh.
- PlanetMapActivity: danh sách node học tập theo planet.
- ZoneActivity: khu vực học trong planet.

Học tập và mini game:
- LearnWordsActivity: học từ vựng cơ bản.
- ExploreActivity: khám phá thu thập từ.
- DialogueActivity: luyện hội thoại.
- PuzzleGameActivity: puzzle từ/câu.
- SentenceActivity: xây dựng câu.
- GuessNameGameActivity: đoán từ.
- ListenChooseGameActivity: nghe và chọn đáp án.
- MatchGameActivity: nối cặp.
- BossGateActivity: đánh boss.
- SignalDecodeActivity: giải mã tín hiệu (typing).
- WordBattleActivity / BattleActivity: chiến đấu bằng từ vựng.
- WordReviewActivity: flashcard review.
- WordLabActivity: kho từ và TTS.

Hub và tiện ích:
- SpaceshipHubActivity: hub chính.
- BuddyRoomActivity: bạn đồng hành.
- ProfileActivity: hồ sơ và thống kê.
- NotesActivity: ghi chú offline.
- RemindersActivity: đặt nhắc nhở.
- BadgesActivity, DailyMissionsActivity, RewardActivity: thưởng và nhiệm vụ.
- CaptainsLogActivity, AdventureActivity: nội dung mở rộng.

Legacy/compat:
- SpaceMapActivity, MainActivity, VocabularyActivity và một số màn hình cũ
  được giữ để tương thích.

## Học tập và mini game
Nhóm học tập:
- LearnWordsActivity: thẻ từ vựng theo scene/planet.
- WordReviewActivity: lặp lại có đánh giá độ khó.
- WordLabActivity: tổng hợp từ vựng đã học, có TTS.
- DialogueActivity: luyện phản xạ hội thoại.
- SentenceActivity: luyện ghép câu và cấu trúc.

Nhóm mini game:
- GuessNameGameActivity: đoán từ theo gợi ý.
- ListenChooseGameActivity: nghe và chọn đáp án.
- MatchGameActivity: nối cặp từ và hình.
- PuzzleGameActivity: puzzle từ/câu.
- BossGateActivity: thử thách cuối scene.
- SignalDecodeActivity: mini game gõ chữ.
- WordBattleActivity/BattleActivity: đánh boss bằng từ vựng.

## Hệ thống tiến độ
- XP và level: cộng sau mỗi hoạt động.
- Sao và fuel cells: dùng để mở khóa hành tinh/thiên hà.
- Crystals, badges, collectibles: phần thưởng và sưu tầm.
- Streak ngày: theo dõi tính liên tục học tập.
- Profile: hiển thị XP, số từ đã học, số game hoàn thành, tiến độ theo planet.

## Ghi chú và nhắc nhở
- Ghi chú offline: NotesActivity, lưu trong SQLite.
- Nhắc nhở học tập: RemindersActivity, AlarmManager + NotificationCompat.
- Tự phục hồi nhắc nhở sau reboot (BOOT_COMPLETED).
- Channel thông báo: study_reminders.

## Dữ liệu và lưu trữ
SQLite:
- Database: space_english_game.db (DATABASE_VERSION = 7).
- Các bảng chính: galaxies, planets, scenes, words, sentences, minigames,
  user_progress, collected_items, badges, buddies, buddy_skills, battles,
  daily_missions, inventory, notes, reminders.
- Dữ liệu seed cho galaxy/planet/scene được tạo trong GameDatabaseHelper.

SharedPreferences:
- Lưu trạng thái xem cutscene/intro, avatar đã chọn, và một số cài đặt nhỏ.

## Firebase và dữ liệu behavioral
- Firebase Auth: đăng nhập/đăng ký email.
- Firebase Firestore:
  - behavioral_questions: danh sách câu hỏi.
  - user_behavioral_answers: lưu câu trả lời của người dùng.
- Dataset mẫu nằm ở behavioral_questions.json để seed Firestore.

## Công nghệ sử dụng
- Ngôn ngữ: Java (Android), ViewBinding.
- UI: Material Components, RecyclerView, ViewPager2, CardView, ConstraintLayout.
- Data: SQLite qua GameDatabaseHelper.
- Firebase: Auth, Firestore, Analytics.
- Media/Animation: Glide, Lottie, ExoPlayer.
- Speech: TextToSpeech và Speech Recognition.
- Notification: AlarmManager + NotificationCompat.

## Cấu hình build
- applicationId: com.example.engapp
- minSdk: 24
- targetSdk: 34
- compileSdk: 34
- Java: 11

## Quyền truy cập (AndroidManifest.xml)
- INTERNET, ACCESS_NETWORK_STATE
- MODIFY_AUDIO_SETTINGS
- READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE (legacy)
- WAKE_LOCK, FOREGROUND_SERVICE
- POST_NOTIFICATIONS (Android 13+)
- RECEIVE_BOOT_COMPLETED

## Cài đặt và chạy
Yêu cầu:
- Android Studio Flamingo hoặc mới hơn
- JDK 11
- Android SDK 34

Các bước:
1. Mở project trong Android Studio, chờ Gradle sync.
2. Đảm bảo app/google-services.json đã có và Firebase đã cấu hình.
3. Chạy app trên thiết bị thật hoặc emulator.

Build CLI:
```bash
./gradlew assembleDebug
```

Windows:
```bat
gradlew.bat assembleDebug
```

## Kiểm thử
- Unit tests: ./gradlew test
- Instrumented tests: ./gradlew connectedAndroidTest

## Ghi chú phát triển
- SplashActivity đang gọi LessonUnlockManager.unlockAllForTesting() để test.
  Cần bỏ khi release.
- Khi thay đổi schema SQLite, cần tăng DATABASE_VERSION.
- usesCleartextTraffic đang bật trong manifest, chỉ nên dùng khi cần.

## Xử lý lỗi thường gặp
- Không nhận thông báo: kiểm tra quyền POST_NOTIFICATIONS và channel.
- Behavioral không hiển thị: chưa seed behavioral_questions lên Firestore.
- Sau khi đổi schema DB: clear app data hoặc tăng DATABASE_VERSION.

## Tài khoản test
- Email: test@local.app
- Password: 123456
