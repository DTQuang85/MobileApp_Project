# 🚀 Space English - Ứng dụng Học Tiếng Anh Cho Trẻ Em

<p align="center">
  <img src="app/src/main/res/drawable/logo.png" width="120" alt="Space English Logo"/>
</p>

<p align="center">
  <strong>Phiêu lưu vũ trụ - Học tiếng Anh vui vẻ!</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android"/>
  <img src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=for-the-badge&logo=firebase&logoColor=black" alt="Firebase"/>
  <img src="https://img.shields.io/badge/SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite"/>
</p>

---

## 📖 Mục Lục

- [Giới thiệu](#-giới-thiệu)
- [Tính năng](#-tính-năng)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cài đặt](#-cài-đặt)
- [Hướng dẫn sử dụng](#-hướng-dẫn-sử-dụng)
- [Kiến trúc ứng dụng](#-kiến-trúc-ứng-dụng)
- [Cơ sở dữ liệu](#-cơ-sở-dữ-liệu)
- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Đóng góp](#-đóng-góp)
- [Giấy phép](#-giấy-phép)

---

## 🌟 Giới thiệu

**Space English** là ứng dụng học tiếng Anh dành cho trẻ em từ **5-12 tuổi**, được thiết kế với chủ đề **du hành vũ trụ** hấp dẫn. Trẻ em sẽ lái tàu vũ trụ khám phá 9 hành tinh, mỗi hành tinh là một chủ đề học tập khác nhau với từ vựng, ngữ pháp và các mini-game tương tác.

### 🎯 Mục tiêu giáo dục

- **Từ vựng**: 200+ từ vựng phân theo chủ đề
- **Ngữ pháp**: Các cấu trúc câu cơ bản phù hợp lứa tuổi
- **Kỹ năng**: Nghe, Nói, Đọc, Viết
- **Phát âm**: Hỗ trợ Text-to-Speech phát âm chuẩn

---

## ✨ Tính năng

### 🪐 9 Hành tinh học tập

| # | Hành tinh | Chủ đề | Kỹ năng |
|---|-----------|--------|---------|
| 1 | **Coloria Prime** 🌈 | Màu sắc & Hình khối | Tính từ (big/small) |
| 2 | **Toytopia Orbit** 🎮 | Đồ chơi & Vị trí | Giới từ (in/on/under) |
| 3 | **Animania Wild** 🦁 | Động vật & Khả năng | can/can't |
| 4 | **Citytron Nova** 🏙️ | Thành phố & Chỉ đường | there is/are |
| 5 | **Foodora Station** 🍕 | Ẩm thực & Mua sắm | How many/How much |
| 6 | **Weatheron Sky** ⛅ | Thời tiết & Trang phục | because |
| 7 | **RoboLab Command** 🤖 | Robot & Chuỗi lệnh | first/then/next/finally |
| 8 | **TimeLapse Base** ⏰ | Thời gian & Thói quen | Present Simple |
| 9 | **Storyverse Galaxy** 📚 | Kể chuyện | Past Simple |

### 🎮 5 Scene mỗi hành tinh

1. **🚀 Landing Zone** - Học 8-12 từ mới với hình ảnh và phát âm
2. **🔮 Explore Area** - Thu thập Word Crystals bằng cách chạm vào vật phẩm
3. **💬 Dialogue Dock** - Hội thoại chọn đáp án với NPC
4. **🧩 Puzzle Zone** - Xếp câu, ghép từ, giải đố
5. **👾 Boss Gate** - Nghe và chọn từ đúng để đánh bại Boss

### 🏠 Spaceship Hub (Tàu Mẹ)

- **Galaxy Map**: Bản đồ 9 hành tinh với hệ thống mở khóa
- **Word Lab**: Phòng ôn tập từ vựng đã học
- **Buddy Room**: Chọn và tương tác với buddy đồng hành
- **Profile**: Hồ sơ cá nhân với avatar và thống kê

### 🎁 Hệ thống thưởng

- ⭐ **Stars**: Thu thập sao từ các mini-game
- 🔋 **Fuel Cells**: Mở khóa hành tinh mới
- 💎 **Crystals**: Thu thập từ vựng
- 🏅 **Badges**: Huy hiệu thành tích

### 🐾 Buddy đồng hành

| Buddy | Tên | Tính cách |
|-------|-----|-----------|
| 🤖 | Robo-Buddy | Robot thông minh, logic |
| 👽 | Alien-Friend | Người ngoài hành tinh vui vẻ |
| 🐱 | Kitty-Pal | Mèo dễ thương |
| 🦊 | Foxy-Guide | Cáo thông minh |
| 🐲 | Dragon | *Mở khóa sau 3 hành tinh* |
| 🦄 | Unicorn | *Mở khóa sau 5 hành tinh* |
| 🐼 | Panda | *Mở khóa sau 7 hành tinh* |
| 🦁 | Lion | *Mở khóa sau 9 hành tinh* |

---

## 📁 Cấu trúc dự án

```
MobileApp_Project/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/engapp/
│   │   │   │   ├── adapter/          # RecyclerView Adapters
│   │   │   │   ├── data/             # Data providers
│   │   │   │   ├── database/         # SQLite Database Helper
│   │   │   │   ├── model/            # Data models
│   │   │   │   │
│   │   │   │   ├── SplashActivity.java       # Màn hình khởi động
│   │   │   │   ├── IntroActivity.java        # Intro slides
│   │   │   │   ├── LoginActivity.java        # Đăng nhập
│   │   │   │   ├── SpaceshipHubActivity.java # Màn hình chính
│   │   │   │   ├── PlanetActivity.java       # Chi tiết hành tinh
│   │   │   │   │
│   │   │   │   ├── LearnWordsActivity.java   # Scene: Học từ vựng
│   │   │   │   ├── ExploreActivity.java      # Scene: Thu thập crystals
│   │   │   │   ├── DialogueActivity.java     # Scene: Hội thoại
│   │   │   │   ├── PuzzleGameActivity.java   # Scene: Xếp câu
│   │   │   │   ├── BossGateActivity.java     # Scene: Boss battle
│   │   │   │   │
│   │   │   │   ├── WordLabActivity.java      # Phòng từ vựng
│   │   │   │   ├── BuddyRoomActivity.java    # Phòng buddy
│   │   │   │   ├── ProfileActivity.java      # Hồ sơ cá nhân
│   │   │   │   ├── BadgesActivity.java       # Huy hiệu
│   │   │   │   │
│   │   │   │   └── SpaceDialog.java          # Custom dialog theo theme
│   │   │   │
│   │   │   ├── res/
│   │   │   │   ├── layout/           # XML layouts
│   │   │   │   ├── drawable/         # Drawables & backgrounds
│   │   │   │   ├── anim/             # Animations
│   │   │   │   ├── values/           # Colors, strings, styles
│   │   │   │   └── mipmap-*/         # App icons
│   │   │   │
│   │   │   └── AndroidManifest.xml
│   │   │
│   │   └── test/                     # Unit tests
│   │
│   ├── build.gradle.kts              # App-level Gradle config
│   └── google-services.json          # Firebase config
│
├── gradle/
│   └── libs.versions.toml            # Version catalog
│
├── build.gradle.kts                  # Project-level Gradle config
├── settings.gradle.kts
└── README.md
```

---

## 💻 Yêu cầu hệ thống

### Phát triển
- **Android Studio**: Hedgehog (2023.1.1) trở lên
- **JDK**: 11 hoặc cao hơn
- **Gradle**: 8.0+
- **Android SDK**: API 24 (Android 7.0) - API 34 (Android 14)

### Thiết bị
- **Android**: 7.0 (Nougat) trở lên
- **RAM**: 2GB+
- **Bộ nhớ**: 100MB+ dung lượng trống

---

## 🔧 Cài đặt

### 1. Clone repository

```bash
git clone https://github.com/your-username/space-english.git
cd space-english
```

### 2. Mở project trong Android Studio

```
File → Open → Chọn thư mục project
```

### 3. Cấu hình Firebase

1. Tạo project trên [Firebase Console](https://console.firebase.google.com/)
2. Thêm ứng dụng Android với package name: `com.example.engapp`
3. Tải file `google-services.json` và đặt vào thư mục `app/`
4. Enable các dịch vụ:
   - Authentication (Email/Password + Google Sign-In)
   - Firestore Database

### 4. Build và chạy

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Cài đặt lên thiết bị/emulator
./gradlew installDebug
```

---

## 📱 Hướng dẫn sử dụng

### Khởi động ứng dụng

1. **Splash Screen**: Màn hình khởi động với animation tên lửa
2. **Intro Slides**: Xem giới thiệu (lần đầu tiên)
3. **Đăng nhập**: Email/Password hoặc Google Sign-In
4. **Spaceship Hub**: Màn hình chính

### Luồng chơi

```
🚀 Spaceship Hub
    │
    ├─→ 🌍 Galaxy Map (Chọn hành tinh)
    │       │
    │       └─→ 🪐 Planet (Chọn scene)
    │               │
    │               ├─→ 📚 Landing Zone (Học từ)
    │               ├─→ 🔮 Explore Area (Thu thập)
    │               ├─→ 💬 Dialogue Dock (Hội thoại)
    │               ├─→ 🧩 Puzzle Zone (Xếp câu)
    │               └─→ 👾 Boss Gate (Thử thách)
    │
    ├─→ 📖 Word Lab (Ôn tập từ vựng)
    │
    ├─→ 🐾 Buddy Room (Chọn buddy)
    │
    ├─→ 🏅 Badges (Xem huy hiệu)
    │
    └─→ 👤 Profile (Hồ sơ cá nhân)
```

### Điều khiển

- **Chạm**: Chọn, tương tác
- **Vuốt**: Cuộn danh sách
- **Nút Back**: Quay lại màn hình trước

---

## 🏗 Kiến trúc ứng dụng

### Pattern: MVC (Model-View-Controller)

```
┌─────────────────────────────────────────────────────┐
│                      VIEW                           │
│  (Activities, Layouts, Adapters)                    │
├─────────────────────────────────────────────────────┤
│                   CONTROLLER                        │
│  (Activity Logic, Event Handlers)                   │
├─────────────────────────────────────────────────────┤
│                     MODEL                           │
│  (Database, Data Classes, Firebase)                 │
└─────────────────────────────────────────────────────┘
```

### Luồng dữ liệu

```
┌──────────┐     ┌──────────────┐     ┌─────────────┐
│   UI     │ ←→  │   Activity   │ ←→  │  Database   │
│ (Layout) │     │  (Logic)     │     │  (SQLite)   │
└──────────┘     └──────────────┘     └─────────────┘
                        ↕
                ┌──────────────┐
                │   Firebase   │
                │ (Auth, Sync) │
                └──────────────┘
```

---

## 💾 Cơ sở dữ liệu

### SQLite Tables

| Table | Mô tả |
|-------|-------|
| `planets` | Thông tin 9 hành tinh |
| `scenes` | 5 scene mỗi hành tinh |
| `words` | Từ vựng (200+ từ) |
| `sentences` | Câu mẫu và hội thoại |
| `minigames` | Cấu hình mini-game |
| `user_progress` | Tiến độ người chơi |
| `collected_items` | Vật phẩm đã thu thập |
| `badges` | Huy hiệu thành tích |

### Entity Relationship

```
planets (1) ──── (n) scenes
    │                  │
    │                  │
    └───── (n) words ──┘
    │                  │
    └── (n) sentences ─┘

user_progress ──── collected_items
              └─── badges
```

---

## 🛠 Công nghệ sử dụng

### Core
| Công nghệ | Phiên bản | Mục đích |
|-----------|-----------|----------|
| Java | 11 | Ngôn ngữ chính |
| Android SDK | 24-34 | Nền tảng |
| Gradle | 8.0+ | Build system |

### Firebase
| Dịch vụ | Mục đích |
|---------|----------|
| Authentication | Đăng nhập/Đăng ký |
| Firestore | Đồng bộ dữ liệu |
| Analytics | Phân tích |

### UI Libraries
| Thư viện | Mục đích |
|----------|----------|
| Material Design | UI components |
| RecyclerView | Danh sách |
| CardView | Card layout |
| ViewPager2 | Intro slides |
| Glide | Load hình ảnh |
| CircleImageView | Avatar tròn |

### Media
| Thư viện | Mục đích |
|----------|----------|
| Media3 ExoPlayer | Video player |
| TextToSpeech | Phát âm |

---

## 🎨 Theme & Styling

### Color Palette

```
Primary:     #2563EB (Blue)
Secondary:   #7C3AED (Purple)
Accent:      #F97316 (Orange)
Success:     #22C55E (Green)
Error:       #EF4444 (Red)
Star Gold:   #FFD93D (Yellow)

Space Dark:  #0F0F23
Space Purple:#1A1A3E
Space Blue:  #2D3561
```

### Typography

- **Headings**: Bold, 20-36sp
- **Body**: Regular, 14-18sp
- **Emoji**: Native system emoji

---

## 🧪 Testing

### Chạy Unit Tests

```bash
./gradlew test
```

### Chạy Instrumented Tests

```bash
./gradlew connectedAndroidTest
```

---

## 📦 Build APK

### Debug APK

```bash
./gradlew assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release APK

```bash
./gradlew assembleRelease
# Output: app/build/outputs/apk/release/app-release.apk
```

> ⚠️ Release build cần keystore. Xem [Signing guide](https://developer.android.com/studio/publish/app-signing)

---

## 🤝 Đóng góp

1. Fork repository
2. Tạo branch mới: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Mở Pull Request

### Coding Standards

- Đặt tên biến: camelCase
- Đặt tên class: PascalCase
- Comment bằng tiếng Anh
- Format code trước khi commit

---

## 📝 Changelog

### v1.0.0 (2024-12-18)
- 🎉 Release đầu tiên
- ✅ 9 hành tinh với nội dung đầy đủ
- ✅ 5 loại mini-game
- ✅ Hệ thống buddy và avatar
- ✅ SQLite database
- ✅ Firebase Authentication

---

## 📄 Giấy phép

Dự án này được cấp phép theo [MIT License](LICENSE).

```
MIT License

Copyright (c) 2024 Space English Team

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction...
```

---

## 👥 Tác giả

- **Developer**: [Your Name]
- **Email**: your.email@example.com
- **GitHub**: [@your-username](https://github.com/your-username)

---

## 🙏 Cảm ơn

- [Google Firebase](https://firebase.google.com/) - Backend services
- [Material Design](https://material.io/) - UI guidelines
- [Android Developers](https://developer.android.com/) - Documentation
- Emoji graphics by [Twemoji](https://twemoji.twitter.com/)

---

<p align="center">
  Made with ❤️ for Kids Learning English
</p>

<p align="center">
  🚀 <strong>Space English</strong> - Fly to Learn! 🌟
</p>

