package com.example.engapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.engapp.model.*;
import java.util.ArrayList;
import java.util.List;

public class GameDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "space_english_game.db";
    private static final int DATABASE_VERSION = 6;

    // Table names
    public static final String TABLE_GALAXIES = "galaxies";
    public static final String TABLE_PLANETS = "planets";
    public static final String TABLE_SCENES = "scenes";
    public static final String TABLE_WORDS = "words";
    public static final String TABLE_SENTENCES = "sentences";
    public static final String TABLE_MINIGAMES = "minigames";
    public static final String TABLE_USER_PROGRESS = "user_progress";
    public static final String TABLE_COLLECTED_ITEMS = "collected_items";
    public static final String TABLE_BADGES = "badges";
    public static final String TABLE_BUDDIES = "buddies";
    public static final String TABLE_BUDDY_SKILLS = "buddy_skills";
    public static final String TABLE_BATTLES = "battles";
    public static final String TABLE_DAILY_MISSIONS = "daily_missions";
    public static final String TABLE_INVENTORY = "inventory";

    private static GameDatabaseHelper instance;
    private Context context;

    public static synchronized GameDatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GameDatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private GameDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create Galaxies table (Phase 2)
        db.execSQL("CREATE TABLE " + TABLE_GALAXIES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "galaxy_key TEXT UNIQUE," +
            "name TEXT," +
            "name_vi TEXT," +
            "description TEXT," +
            "emoji TEXT," +
            "theme_color TEXT," +
            "background_image TEXT," +
            "required_stars INTEGER DEFAULT 0," +
            "order_index INTEGER," +
            "is_unlocked INTEGER DEFAULT 0" +
        ")");

        // Create Planets table (updated with galaxy_id)
        db.execSQL("CREATE TABLE " + TABLE_PLANETS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "galaxy_id INTEGER DEFAULT 1," +
            "planet_key TEXT UNIQUE," +
            "name TEXT," +
            "name_vi TEXT," +
            "description TEXT," +
            "emoji TEXT," +
            "theme_color TEXT," +
            "background_image TEXT," +
            "atmosphere TEXT," +
            "collectible_name TEXT," +
            "collectible_emoji TEXT," +
            "grammar_focus TEXT," +
            "skill_focus TEXT," +
            "required_fuel_cells INTEGER DEFAULT 0," +
            "order_index INTEGER," +
            "is_unlocked INTEGER DEFAULT 0," +
            "FOREIGN KEY(galaxy_id) REFERENCES galaxies(id)" +
        ")");

        // Create Scenes table (5 scenes per planet)
        db.execSQL("CREATE TABLE " + TABLE_SCENES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "planet_id INTEGER," +
            "scene_key TEXT," +
            "scene_type TEXT," +  // landing_zone, explore_area, dialogue_dock, puzzle_zone, boss_gate
            "name TEXT," +
            "name_vi TEXT," +
            "description TEXT," +
            "emoji TEXT," +
            "order_index INTEGER," +
            "is_completed INTEGER DEFAULT 0," +
            "stars_earned INTEGER DEFAULT 0," +
            "FOREIGN KEY(planet_id) REFERENCES planets(id)" +
        ")");

        // Create Words table
        db.execSQL("CREATE TABLE " + TABLE_WORDS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "planet_id INTEGER," +
            "scene_id INTEGER," +
            "english TEXT," +
            "vietnamese TEXT," +
            "pronunciation TEXT," +
            "emoji TEXT," +
            "image_url TEXT," +
            "audio_url TEXT," +
            "category TEXT," +
            "difficulty INTEGER DEFAULT 1," +
            "example_sentence TEXT," +
            "example_translation TEXT," +
            "is_learned INTEGER DEFAULT 0," +
            "times_correct INTEGER DEFAULT 0," +
            "times_wrong INTEGER DEFAULT 0," +
            "FOREIGN KEY(planet_id) REFERENCES planets(id)," +
            "FOREIGN KEY(scene_id) REFERENCES scenes(id)" +
        ")");

        // Create Sentences table
        db.execSQL("CREATE TABLE " + TABLE_SENTENCES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "planet_id INTEGER," +
            "scene_id INTEGER," +
            "english TEXT," +
            "vietnamese TEXT," +
            "audio_url TEXT," +
            "keywords TEXT," +  // comma-separated
            "sentence_type TEXT," +  // pattern, dialogue, command, question
            "difficulty INTEGER DEFAULT 1," +
            "is_learned INTEGER DEFAULT 0," +
            "FOREIGN KEY(planet_id) REFERENCES planets(id)," +
            "FOREIGN KEY(scene_id) REFERENCES scenes(id)" +
        ")");

        // Create MiniGames table
        db.execSQL("CREATE TABLE " + TABLE_MINIGAMES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "scene_id INTEGER," +
            "game_type TEXT," +  // listen_choose, sentence_scramble, command_chain, etc.
            "title TEXT," +
            "title_vi TEXT," +
            "description TEXT," +
            "max_score INTEGER DEFAULT 100," +
            "time_limit INTEGER DEFAULT 0," +  // 0 = no limit
            "difficulty INTEGER DEFAULT 1," +
            "game_data TEXT," +  // JSON data for game content
            "FOREIGN KEY(scene_id) REFERENCES scenes(id)" +
        ")");

        // Create User Progress table
        db.execSQL("CREATE TABLE " + TABLE_USER_PROGRESS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id TEXT DEFAULT 'default'," +
            "total_stars INTEGER DEFAULT 0," +
            "total_fuel_cells INTEGER DEFAULT 0," +
            "total_crystals INTEGER DEFAULT 0," +
            "current_planet_id INTEGER DEFAULT 1," +
            "current_level INTEGER DEFAULT 1," +
            "words_learned INTEGER DEFAULT 0," +
            "games_completed INTEGER DEFAULT 0," +
            "streak_days INTEGER DEFAULT 0," +
            "last_played_date TEXT," +
            "avatar_id INTEGER DEFAULT 1," +
            "buddy_id INTEGER DEFAULT 1," +
            "experience_points INTEGER DEFAULT 0" +
        ")");

        // Create Collected Items table
        db.execSQL("CREATE TABLE " + TABLE_COLLECTED_ITEMS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id TEXT DEFAULT 'default'," +
            "planet_id INTEGER," +
            "item_type TEXT," +
            "item_name TEXT," +
            "item_emoji TEXT," +
            "collected_at TEXT," +
            "FOREIGN KEY(planet_id) REFERENCES planets(id)" +
        ")");

        // Create Badges table
        db.execSQL("CREATE TABLE " + TABLE_BADGES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "badge_key TEXT UNIQUE," +
            "name TEXT," +
            "name_vi TEXT," +
            "description TEXT," +
            "emoji TEXT," +
            "requirement_type TEXT," +
            "requirement_value INTEGER," +
            "is_earned INTEGER DEFAULT 0," +
            "earned_date TEXT" +
        ")");

        // Create Buddies table (Phase 6)
        db.execSQL("CREATE TABLE " + TABLE_BUDDIES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "buddy_key TEXT UNIQUE," +
            "name TEXT," +
            "name_vi TEXT," +
            "emoji TEXT," +
            "description TEXT," +
            "level INTEGER DEFAULT 1," +
            "experience INTEGER DEFAULT 0," +
            "is_active INTEGER DEFAULT 0," +
            "is_unlocked INTEGER DEFAULT 0" +
        ")");

        // Create Buddy Skills table (Phase 6)
        db.execSQL("CREATE TABLE " + TABLE_BUDDY_SKILLS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "buddy_id INTEGER," +
            "skill_key TEXT," +
            "skill_name TEXT," +
            "skill_type TEXT," +  // hint, shield, reward_boost
            "cooldown_seconds INTEGER DEFAULT 0," +
            "is_unlocked INTEGER DEFAULT 0," +
            "FOREIGN KEY(buddy_id) REFERENCES buddies(id)" +
        ")");

        // Create Battles table (Phase 5)
        db.execSQL("CREATE TABLE " + TABLE_BATTLES + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "planet_id INTEGER," +
            "battle_key TEXT," +
            "name TEXT," +
            "name_vi TEXT," +
            "difficulty INTEGER DEFAULT 1," +
            "max_errors INTEGER DEFAULT 3," +
            "questions_count INTEGER DEFAULT 5," +
            "reward_stars INTEGER DEFAULT 3," +
            "reward_crystals INTEGER DEFAULT 10," +
            "is_completed INTEGER DEFAULT 0," +
            "best_score INTEGER DEFAULT 0," +
            "FOREIGN KEY(planet_id) REFERENCES planets(id)" +
        ")");

        // Create Daily Missions table (Phase 8)
        db.execSQL("CREATE TABLE " + TABLE_DAILY_MISSIONS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "mission_key TEXT," +
            "title TEXT," +
            "description TEXT," +
            "target_value INTEGER," +
            "current_value INTEGER DEFAULT 0," +
            "reward_stars INTEGER," +
            "reward_crystals INTEGER," +
            "mission_date TEXT," +
            "is_completed INTEGER DEFAULT 0" +
        ")");

        // Create Inventory table (Phase 7)
        db.execSQL("CREATE TABLE " + TABLE_INVENTORY + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id TEXT DEFAULT 'default'," +
            "item_type TEXT," +  // star, fuel, crystal
            "amount INTEGER DEFAULT 0," +
            "last_updated TEXT" +
        ")");

        // Insert initial data
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            // Version 6: Add 7 new planets (13-19)
            // Check if new planets already exist
            Cursor checkCursor = db.query(TABLE_PLANETS, new String[]{"id"}, 
                "planet_key IN (?,?,?,?,?,?,?)",
                new String[]{"artopia_planet", "playground_park", "school_academy", 
                    "body_parts_planet", "sports_arena", "birthday_party", "ocean_deep"},
                null, null, null);
            
            boolean hasNewPlanets = checkCursor.getCount() > 0;
            checkCursor.close();
            
            if (!hasNewPlanets) {
                // Insert 7 new planets
                insertPlanet(db, 1, "artopia_planet", "Artopia Planet", "Hành Tinh Nghệ Thuật",
                    "Thế giới đầy màu sắc với bảo tàng, studio vẽ, phòng nhạc và sân khấu", "🎨", "#FF6B9D",
                    "art_museum", "Art Supplies", "🖌️",
                    "Like/Don't like", "Art & Creativity", 0, 13, 0);

                insertPlanet(db, 1, "playground_park", "Playground Park", "Công Viên Vui Chơi",
                    "Công viên giải trí với cầu trượt, xích đu, bập bênh và khu vui chơi", "🎠", "#FFD93D",
                    "playground", "Play Tokens", "🎫",
                    "Let's...", "Playground & Activities", 0, 14, 0);

                insertPlanet(db, 1, "school_academy", "School Academy", "Học Viện Trường Học",
                    "Ngôi trường với lớp học, thư viện, sân chơi và phòng thí nghiệm", "🏫", "#6C5CE7",
                    "school_building", "Star Stickers", "⭐",
                    "Have/Has", "School & Learning", 0, 15, 0);

                insertPlanet(db, 2, "body_parts_planet", "Body Parts Planet", "Hành Tinh Cơ Thể",
                    "Bệnh viện không gian với phòng khám, phòng tập thể dục và khu vui chơi", "👶", "#FF7675",
                    "hospital_space", "Health Badges", "💊",
                    "My/Your/His/Her", "Body Parts & Health", 0, 16, 0);

                insertPlanet(db, 2, "sports_arena", "Sports Arena", "Đấu Trường Thể Thao",
                    "Sân vận động với sân bóng, bể bơi, sân tennis và đường chạy", "⚽", "#00B894",
                    "stadium", "Trophy Medals", "🏆",
                    "I can/I can't", "Sports & Actions", 0, 17, 0);

                insertPlanet(db, 2, "birthday_party", "Birthday Party", "Bữa Tiệc Sinh Nhật",
                    "Phòng tiệc với bánh kem, bóng bay, quà tặng và âm nhạc", "🎂", "#FDCB6E",
                    "party_room", "Party Hats", "🎩",
                    "How old are you?", "Numbers & Celebrations", 0, 18, 0);

                insertPlanet(db, 2, "ocean_deep", "Ocean Deep", "Đại Dương Sâu Thẳm",
                    "Đại dương với san hô, cá, sao biển và kho báu dưới đáy biển", "🌊", "#0984E3",
                    "ocean_floor", "Sea Shells", "🐚",
                    "There is/There are", "Sea Creatures & Ocean", 0, 19, 0);

                // Insert scenes for new planets
                insertScene(db, 13, "landing_zone", "Art Landing", "Bãi Đáp Nghệ Thuật",
                    "Học từ vựng về màu sắc, dụng cụ vẽ, nhạc cụ", "🎨", 1);
                insertScene(db, 13, "explore_area", "Art Hunt", "Săn Nghệ Thuật",
                    "Thu thập Word Crystals về nghệ thuật", "🔍", 2);
                insertScene(db, 13, "dialogue_dock", "Art Talk", "Nói Về Nghệ Thuật",
                    "Hội thoại về sở thích nghệ thuật", "💬", 3);
                insertScene(db, 13, "puzzle_zone", "Art Puzzle", "Ghép Hình Nghệ Thuật",
                    "Xếp câu về hoạt động nghệ thuật", "🧩", 4);
                insertScene(db, 13, "boss_gate", "Artist Boss", "Boss Nghệ Sĩ",
                    "Đánh bại boss bằng cách nói về sở thích nghệ thuật", "👾", 5);

                insertScene(db, 14, "landing_zone", "Play Landing", "Bãi Đáp Vui Chơi",
                    "Học từ vựng về các trò chơi", "🎠", 1);
                insertScene(db, 14, "explore_area", "Play Hunt", "Săn Trò Chơi",
                    "Thu thập Word Crystals về các trò chơi", "🔍", 2);
                insertScene(db, 14, "dialogue_dock", "Play Talk", "Nói Về Vui Chơi",
                    "Luyện cách rủ bạn chơi cùng", "💬", 3);
                insertScene(db, 14, "puzzle_zone", "Play Puzzle", "Ghép Hình Vui Chơi",
                    "Xếp câu về hoạt động vui chơi", "🧩", 4);
                insertScene(db, 14, "boss_gate", "Playground Boss", "Boss Công Viên",
                    "Đánh bại boss bằng cách nói về các trò chơi", "👾", 5);

                insertScene(db, 15, "landing_zone", "School Landing", "Bãi Đáp Trường Học",
                    "Học từ vựng về trường học và đồ dùng học tập", "🏫", 1);
                insertScene(db, 15, "explore_area", "School Hunt", "Săn Đồ Dùng Học Tập",
                    "Thu thập Word Crystals về đồ dùng học tập", "🔍", 2);
                insertScene(db, 15, "dialogue_dock", "School Talk", "Nói Về Trường Học",
                    "Luyện cách nói về đồ dùng học tập", "💬", 3);
                insertScene(db, 15, "puzzle_zone", "School Puzzle", "Ghép Hình Trường Học",
                    "Xếp câu về đồ dùng và màu sắc", "🧩", 4);
                insertScene(db, 15, "boss_gate", "Teacher Boss", "Boss Giáo Viên",
                    "Đánh bại boss bằng cách nói về trường học", "👾", 5);

                insertScene(db, 16, "landing_zone", "Body Landing", "Bãi Đáp Cơ Thể",
                    "Học từ vựng về các bộ phận cơ thể", "👶", 1);
                insertScene(db, 16, "explore_area", "Body Hunt", "Săn Bộ Phận Cơ Thể",
                    "Thu thập Word Crystals về các bộ phận cơ thể", "🔍", 2);
                insertScene(db, 16, "dialogue_dock", "Body Talk", "Nói Về Cơ Thể",
                    "Luyện cách mô tả cơ thể", "💬", 3);
                insertScene(db, 16, "puzzle_zone", "Body Puzzle", "Ghép Hình Cơ Thể",
                    "Xếp câu về bộ phận cơ thể", "🧩", 4);
                insertScene(db, 16, "boss_gate", "Doctor Boss", "Boss Bác Sĩ",
                    "Đánh bại boss bằng cách nói về cơ thể", "👾", 5);

                insertScene(db, 17, "landing_zone", "Sports Landing", "Bãi Đáp Thể Thao",
                    "Học từ vựng về môn thể thao", "⚽", 1);
                insertScene(db, 17, "explore_area", "Sports Hunt", "Săn Dụng Cụ Thể Thao",
                    "Thu thập Word Crystals về môn thể thao", "🔍", 2);
                insertScene(db, 17, "dialogue_dock", "Sports Talk", "Nói Về Thể Thao",
                    "Luyện cách nói về khả năng thể thao", "💬", 3);
                insertScene(db, 17, "puzzle_zone", "Sports Puzzle", "Ghép Hình Thể Thao",
                    "Xếp câu về môn thể thao và hành động", "🧩", 4);
                insertScene(db, 17, "boss_gate", "Champion Boss", "Boss Vô Địch",
                    "Đánh bại boss bằng cách nói về thể thao", "👾", 5);

                insertScene(db, 18, "landing_zone", "Party Landing", "Bãi Đáp Tiệc",
                    "Học từ vựng về đồ tiệc và sinh nhật", "🎂", 1);
                insertScene(db, 18, "explore_area", "Party Hunt", "Săn Đồ Tiệc",
                    "Thu thập Word Crystals về đồ tiệc", "🔍", 2);
                insertScene(db, 18, "dialogue_dock", "Party Talk", "Nói Về Tiệc",
                    "Luyện cách hỏi và trả lời về tuổi", "💬", 3);
                insertScene(db, 18, "puzzle_zone", "Party Puzzle", "Ghép Hình Tiệc",
                    "Xếp câu về số tuổi và đồ tiệc", "🧩", 4);
                insertScene(db, 18, "boss_gate", "Birthday Boss", "Boss Sinh Nhật",
                    "Đánh bại boss bằng cách nói về tuổi và tiệc", "👾", 5);

                insertScene(db, 19, "landing_zone", "Ocean Landing", "Bãi Đáp Đại Dương",
                    "Học từ vựng về sinh vật biển", "🌊", 1);
                insertScene(db, 19, "explore_area", "Ocean Hunt", "Săn Sinh Vật Biển",
                    "Thu thập Word Crystals về sinh vật biển", "🔍", 2);
                insertScene(db, 19, "dialogue_dock", "Ocean Talk", "Nói Về Đại Dương",
                    "Luyện cách mô tả sinh vật biển", "💬", 3);
                insertScene(db, 19, "puzzle_zone", "Ocean Puzzle", "Ghép Hình Đại Dương",
                    "Xếp câu về sinh vật và đại dương", "🧩", 4);
                insertScene(db, 19, "boss_gate", "Sea Boss", "Boss Biển Cả",
                    "Đánh bại boss bằng cách nói về đại dương", "👾", 5);

                // Insert words and sentences for new planets
                insertArtopiaWords(db, 13);
                insertArtopiaSentences(db, 13);
                insertPlaygroundWords(db, 14);
                insertPlaygroundSentences(db, 14);
                insertSchoolAcademyWords(db, 15);
                insertSchoolAcademySentences(db, 15);
                insertBodyPartsWords(db, 16);
                insertBodyPartsSentences(db, 16);
                insertSportsArenaWords(db, 17);
                insertSportsArenaSentences(db, 17);
                insertBirthdayPartyWords(db, 18);
                insertBirthdayPartySentences(db, 18);
                insertOceanDeepWords(db, 19);
                insertOceanDeepSentences(db, 19);
            }
        }
        
        // For other upgrades, use the old method (drop and recreate)
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_INVENTORY);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAILY_MISSIONS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BATTLES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDDY_SKILLS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDDIES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECTED_ITEMS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BADGES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROGRESS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MINIGAMES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENTENCES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCENES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANETS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_GALAXIES);
            onCreate(db);
        }
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        ensurePlanetsSeeded(db);
    }

    public void ensurePlanetsSeededNow() {
        ensurePlanetsSeeded(getWritableDatabase());
    }

    public int ensureMinimumPlanets(int minCount) {
        SQLiteDatabase db = getWritableDatabase();
        ensurePlanetsSeeded(db);
        int count = getPlanetsCount(db);
        if (count < minCount) {
            insertBasePlanets(db);
            count = getPlanetsCount(db);
        }
        return count;
    }

    private int getPlanetsCount(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PLANETS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    private void insertBasePlanets(SQLiteDatabase db) {
        ensurePlanet(db, 1, "coloria_prime", "Coloria Prime", "Coloria Prime",
            "", "", "#FF6B6B", "crystal_city", "Prism Shards", "",
            "Adjectives", "Colors & Shapes", 0, 1, 1);

        ensurePlanet(db, 1, "toytopia_orbit", "Toytopia Orbit", "Toytopia Orbit",
            "", "", "#4ECDC4", "toy_park", "Sticker Toys", "",
            "Prepositions", "Toys & Positions", 3, 2, 0);

        ensurePlanet(db, 1, "animania_wild", "Animania Wild", "Animania Wild",
            "", "", "#45B7D1", "alien_zoo", "Animal Badges", "",
            "Can/Can't", "Animals & Actions", 5, 3, 0);

        ensurePlanet(db, 1, "numberia_station", "Numberia Station", "Numberia Station",
            "", "", "#F59E0B", "math_space", "Number Gems", "",
            "How many?", "Numbers & Counting", 7, 4, 0);
    }

    private void ensurePlanetsSeeded(SQLiteDatabase db) {
        // Galaxy 1
        ensurePlanet(db, 1, "coloria_prime", "Coloria Prime", "Coloria Prime",
            "", "", "#FF6B6B", "crystal_city", "Prism Shards", "",
            "Adjectives", "Colors & Shapes", 0, 1, 1);

        ensurePlanet(db, 1, "toytopia_orbit", "Toytopia Orbit", "Toytopia Orbit",
            "", "", "#4ECDC4", "toy_park", "Sticker Toys", "",
            "Prepositions", "Toys & Positions", 3, 2, 0);

        ensurePlanet(db, 1, "animania_wild", "Animania Wild", "Animania Wild",
            "", "", "#45B7D1", "alien_zoo", "Animal Badges", "",
            "Can/Can't", "Animals & Actions", 5, 3, 0);

        ensurePlanet(db, 1, "numberia_station", "Numberia Station", "Numberia Station",
            "", "", "#F59E0B", "math_space", "Number Gems", "",
            "How many?", "Numbers & Counting", 7, 4, 0);

        ensurePlanet(db, 1, "artopia_planet", "Artopia Planet", "Artopia Planet",
            "", "", "#FF6B9D", "art_museum", "Art Supplies", "",
            "Like/Don't like", "Art & Creativity", 0, 13, 0);

        ensurePlanet(db, 1, "playground_park", "Playground Park", "Playground Park",
            "", "", "#FFD93D", "playground", "Play Tokens", "",
            "Let's...", "Playground & Activities", 0, 14, 0);

        ensurePlanet(db, 1, "school_academy", "School Academy", "School Academy",
            "", "", "#6C5CE7", "school_building", "Star Stickers", "",
            "Have/Has", "School & Learning", 0, 15, 0);

        // Galaxy 2
        ensurePlanet(db, 2, "citytron_nova", "Citytron Nova", "Citytron Nova",
            "", "", "#96CEB4", "future_city", "Metro Tickets", "",
            "There is/are", "Places & Directions", 8, 5, 0);

        ensurePlanet(db, 2, "foodora_station", "Foodora Station", "Foodora Station",
            "", "", "#FFEAA7", "space_kitchen", "Recipe Cards", "",
            "Countable/Uncountable", "Food & Shopping", 12, 6, 0);

        ensurePlanet(db, 2, "weatheron_sky", "Weatheron Sky", "Weatheron Sky",
            "", "", "#74B9FF", "cloud_port", "Weather Orbs", "",
            "Because/So", "Weather & Clothes", 15, 7, 0);

        ensurePlanet(db, 2, "familia_home", "Familia Home", "Familia Home",
            "", "", "#10B981", "cozy_house", "Family Photos", "",
            "Possessive", "Family & Home", 18, 8, 0);

        ensurePlanet(db, 2, "body_parts_planet", "Body Parts Planet", "Body Parts Planet",
            "", "", "#FF7675", "hospital_space", "Health Badges", "",
            "My/Your/His/Her", "Body Parts & Health", 0, 16, 0);

        ensurePlanet(db, 2, "sports_arena", "Sports Arena", "Sports Arena",
            "", "", "#00B894", "stadium", "Trophy Medals", "",
            "I can/I can't", "Sports & Actions", 0, 17, 0);

        ensurePlanet(db, 2, "birthday_party", "Birthday Party", "Birthday Party",
            "", "", "#FDCB6E", "party_room", "Party Hats", "",
            "How old are you?", "Numbers & Celebrations", 0, 18, 0);

        ensurePlanet(db, 2, "ocean_deep", "Ocean Deep", "Ocean Deep",
            "", "", "#0984E3", "ocean_floor", "Sea Shells", "",
            "There is/There are", "Sea Creatures & Ocean", 0, 19, 0);

        // Galaxy 3
        ensurePlanet(db, 3, "robolab_command", "RoboLab Command", "RoboLab Command",
            "", "", "#A29BFE", "robot_factory", "Circuit Parts", "",
            "Imperatives", "Commands & Sequences", 18, 9, 0);

        ensurePlanet(db, 3, "timelapse_base", "TimeLapse Base", "TimeLapse Base",
            "", "", "#FD79A8", "time_tower", "Time Crystals", "",
            "Present Simple", "Time & Routines", 22, 10, 0);

        ensurePlanet(db, 3, "storyverse_galaxy", "Storyverse Galaxy", "Storyverse Galaxy",
            "", "", "#E17055", "story_castle", "Story Pages", "",
            "Past Simple", "Storytelling", 25, 11, 0);

        ensurePlanet(db, 3, "natura_wilderness", "Natura Wilderness", "Natura Wilderness",
            "", "", "#059669", "nature_forest", "Leaf Tokens", "",
            "Comparatives", "Nature & Environment", 28, 12, 0);
    }

    private void ensurePlanet(SQLiteDatabase db, int galaxyId, String key, String name, String nameVi,
            String description, String emoji, String color, String bgImage,
            String collectible, String collectibleEmoji, String grammar, String skill,
            int requiredFuel, int order, int unlocked) {
        if (!planetExists(db, key)) {
            insertPlanet(db, galaxyId, key, name, nameVi, description, emoji, color, bgImage,
                collectible, collectibleEmoji, grammar, skill, requiredFuel, order, unlocked);
        }
        long planetId = getPlanetIdByKey(db, key);
        if (planetId != -1) {
            ensureScene(db, planetId, "landing_zone", "Landing Zone", "Landing Zone", "", "", 1);
            ensureScene(db, planetId, "explore_area", "Explore Area", "Explore Area", "", "", 2);
            ensureScene(db, planetId, "dialogue_dock", "Dialogue Dock", "Dialogue Dock", "", "", 3);
            ensureScene(db, planetId, "puzzle_zone", "Puzzle Zone", "Puzzle Zone", "", "", 4);
            ensureScene(db, planetId, "boss_gate", "Boss Gate", "Boss Gate", "", "", 5);
        }
    }

    private boolean planetExists(SQLiteDatabase db, String key) {
        Cursor cursor = db.query(TABLE_PLANETS, new String[]{"id"}, "planet_key = ?",
            new String[]{key}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    private long getPlanetIdByKey(SQLiteDatabase db, String key) {
        Cursor cursor = db.query(TABLE_PLANETS, new String[]{"id"}, "planet_key = ?",
            new String[]{key}, null, null, null);
        long id = -1;
        if (cursor.moveToFirst()) {
            id = cursor.getLong(cursor.getColumnIndexOrThrow("id"));
        }
        cursor.close();
        return id;
    }

    private void ensureScene(SQLiteDatabase db, long planetId, String sceneKey, String name,
            String nameVi, String description, String emoji, int orderIndex) {
        Cursor cursor = db.query(TABLE_SCENES, new String[]{"id"}, "planet_id = ? AND scene_key = ?",
            new String[]{String.valueOf(planetId), sceneKey}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        if (!exists) {
            insertScene(db, planetId, sceneKey, name, nameVi, description, emoji, orderIndex);
        }
    }


    private void insertInitialData(SQLiteDatabase db) {
        // Insert 3 Galaxies (Phase 2)
        insertGalaxy(db, "milky_way", "Milky Way", "Dải Ngân Hà",
            "Your home galaxy - start your adventure here!", "🌌", "#4A90D9", 0, 1, 1);
        insertGalaxy(db, "andromeda", "Andromeda", "Thiên Hà Tiên Nữ",
            "A beautiful spiral galaxy with advanced civilizations", "🌀", "#9B59B6", 15, 2, 0);
        insertGalaxy(db, "nebula_prime", "Nebula Prime", "Tinh Vân Nguyên Thủy",
            "Ancient mysteries await in this colorful nebula", "✨", "#E74C3C", 30, 3, 0);

        // Insert Buddies (Phase 6)
        insertBuddy(db, "cosmo", "Cosmo", "Cosmo", "🤖",
            "A friendly robot companion who loves learning!", 1, 1);
        insertBuddy(db, "luna", "Luna", "Luna", "🐱",
            "A curious space cat who collects word crystals!", 0, 0);
        insertBuddy(db, "nova", "Nova", "Nova", "🦊",
            "A clever fox with special hint abilities!", 0, 0);

        // Insert 9 planets (Galaxy 1: Milky Way - planets 1-3)
        insertPlanet(db, 1, "coloria_prime", "Coloria Prime", "Hành tinh Sắc Màu",
            "Thành phố pha lê với cầu vồng và laser màu", "🌈", "#FF6B6B",
            "crystal_city", "Prism Shards", "💎",
            "Adjectives (big/small)", "Colors & Shapes", 0, 1, 1);

        insertPlanet(db, 1, "toytopia_orbit", "Toytopia Orbit", "Quỹ đạo Đồ Chơi",
            "Công viên robot đồ chơi, tàu lửa mini, nhà bóng", "🎮", "#4ECDC4",
            "toy_park", "Sticker Toys", "🎨",
            "Prepositions", "Toys & Positions", 3, 2, 0);

        insertPlanet(db, 1, "animania_wild", "Animania Wild", "Sở Thú Ngoài Hành Tinh",
            "Mái vòm rừng, savannah, hang đêm, băng tuyết", "🦁", "#45B7D1",
            "alien_zoo", "Animal Badges", "🏅",
            "Can/Can't", "Animals & Actions", 5, 3, 0);

        // NEW: Galaxy 1 - Planet 4
        insertPlanet(db, 1, "numberia_station", "Numberia Station", "Trạm Số Học",
            "Vũ trụ số học với vòng đếm, tháp cộng trừ", "🔢", "#F59E0B",
            "math_space", "Number Gems", "💎",
            "How many?", "Numbers & Counting", 7, 4, 0);

        // Galaxy 2: Andromeda - planets 5-8
        insertPlanet(db, 2, "citytron_nova", "Citytron Nova", "Thành Phố Tương Lai",
            "Tàu điện không gian, biển neon, toà tháp", "🌆", "#96CEB4",
            "future_city", "Metro Tickets", "🎫",
            "There is/are", "Places & Directions", 8, 5, 0);

        insertPlanet(db, 2, "foodora_station", "Foodora Station", "Trạm Ẩm Thực",
            "Chợ liên ngân hà, bếp tàu vũ trụ, nông trại sao", "🍕", "#FFEAA7",
            "space_kitchen", "Recipe Cards", "📜",
            "Countable/Uncountable", "Food & Shopping", 12, 6, 0);

        insertPlanet(db, 2, "weatheron_sky", "Weatheron Sky", "Bầu Trời Thời Tiết",
            "Cảng mây, bão điện, thị trấn tuyết", "⛈️", "#74B9FF",
            "cloud_port", "Weather Orbs", "🔮",
            "Because/So", "Weather & Clothes", 15, 7, 0);

        // NEW: Galaxy 2 - Planet 8
        insertPlanet(db, 2, "familia_home", "Familia Home", "Nhà Gia Đình",
            "Ngôi nhà ấm cúng với phòng khách, bếp, vườn", "🏠", "#10B981",
            "cozy_house", "Family Photos", "📸",
            "Possessive", "Family & Home", 18, 8, 0);

        // Galaxy 3: Nebula Prime - planets 9-12
        insertPlanet(db, 3, "robolab_command", "RoboLab Command", "Phòng Chỉ Huy Robot",
            "Nhà máy mạch điện, drone bay, đường hầm laser", "🤖", "#A29BFE",
            "robot_factory", "Circuit Parts", "⚡",
            "Imperatives", "Commands & Sequences", 18, 9, 0);

        insertPlanet(db, 3, "timelapse_base", "TimeLapse Base", "Căn Cứ Thời Gian",
            "Tháp đồng hồ, cầu ngày-đêm, trạm lịch tuần", "⏰", "#FD79A8",
            "time_tower", "Time Crystals", "⌛",
            "Present Simple", "Time & Routines", 22, 10, 0);

        insertPlanet(db, 3, "storyverse_galaxy", "Storyverse Galaxy", "Thiên Hà Truyện Kể",
            "Lâu đài sao, rừng phép, thư viện vũ trụ", "📚", "#E17055",
            "story_castle", "Story Pages", "📖",
            "Past Simple", "Storytelling", 25, 11, 0);

        // NEW: Galaxy 3 - Planet 12
        insertPlanet(db, 3, "natura_wilderness", "Natura Wilderness", "Vùng Hoang Dã Thiên Nhiên",
            "Rừng xanh, sông hồ, núi non và sa mạc", "🌳", "#059669",
            "nature_forest", "Leaf Tokens", "🍃",
            "Comparatives", "Nature & Environment", 28, 12, 0);

        // NEW PLANETS from NEW_PLANETS_IDEA.md - Galaxy 1 continuation
        insertPlanet(db, 1, "artopia_planet", "Artopia Planet", "Hành Tinh Nghệ Thuật",
            "Thế giới đầy màu sắc với bảo tàng, studio vẽ, phòng nhạc và sân khấu", "🎨", "#FF6B9D",
            "art_museum", "Art Supplies", "🖌️",
            "Like/Don't like", "Art & Creativity", 0, 13, 0);

        insertPlanet(db, 1, "playground_park", "Playground Park", "Công Viên Vui Chơi",
            "Công viên giải trí với cầu trượt, xích đu, bập bênh và khu vui chơi", "🎠", "#FFD93D",
            "playground", "Play Tokens", "🎫",
            "Let's...", "Playground & Activities", 0, 14, 0);

        insertPlanet(db, 1, "school_academy", "School Academy", "Học Viện Trường Học",
            "Ngôi trường với lớp học, thư viện, sân chơi và phòng thí nghiệm", "🏫", "#6C5CE7",
            "school_building", "Star Stickers", "⭐",
            "Have/Has", "School & Learning", 0, 15, 0);

        insertPlanet(db, 2, "body_parts_planet", "Body Parts Planet", "Hành Tinh Cơ Thể",
            "Bệnh viện không gian với phòng khám, phòng tập thể dục và khu vui chơi", "👶", "#FF7675",
            "hospital_space", "Health Badges", "💊",
            "My/Your/His/Her", "Body Parts & Health", 0, 16, 0);

        insertPlanet(db, 2, "sports_arena", "Sports Arena", "Đấu Trường Thể Thao",
            "Sân vận động với sân bóng, bể bơi, sân tennis và đường chạy", "⚽", "#00B894",
            "stadium", "Trophy Medals", "🏆",
            "I can/I can't", "Sports & Actions", 0, 17, 0);

        insertPlanet(db, 2, "birthday_party", "Birthday Party", "Bữa Tiệc Sinh Nhật",
            "Phòng tiệc với bánh kem, bóng bay, quà tặng và âm nhạc", "🎂", "#FDCB6E",
            "party_room", "Party Hats", "🎩",
            "How old are you?", "Numbers & Celebrations", 0, 18, 0);

        insertPlanet(db, 2, "ocean_deep", "Ocean Deep", "Đại Dương Sâu Thẳm",
            "Đại dương với san hô, cá, sao biển và kho báu dưới đáy biển", "🌊", "#0984E3",
            "ocean_floor", "Sea Shells", "🐚",
            "There is/There are", "Sea Creatures & Ocean", 0, 19, 0);

        // Insert scenes for first planet (Coloria Prime)
        long planetId = 1;
        insertScene(db, planetId, "landing_zone", "Landing Zone", "Vùng Đổ Bộ",
            "Học từ vựng về màu sắc và hình khối", "🚀", 1);
        insertScene(db, planetId, "explore_area", "Explore Area", "Vùng Khám Phá",
            "Thu thập Word Crystals", "🔍", 2);
        insertScene(db, planetId, "dialogue_dock", "Dialogue Dock", "Bến Giao Tiếp",
            "Luyện hội thoại về màu sắc", "💬", 3);
        insertScene(db, planetId, "puzzle_zone", "Puzzle Zone", "Vùng Giải Đố",
            "Mini-game ghép màu và hình", "🧩", 4);
        insertScene(db, planetId, "boss_gate", "Boss Gate", "Cửa Ải Boss",
            "Nghe và chọn đúng vật thể", "👾", 5);

        // Planet 2: Toytopia Orbit
        insertScene(db, 2, "landing_zone", "Toy Landing", "Bãi Đáp Đồ Chơi",
            "Học từ vựng về đồ chơi", "🧸", 1);
        insertScene(db, 2, "explore_area", "Toy Hunt", "Săn Đồ Chơi",
            "Tìm đồ chơi ẩn giấu", "🔎", 2);
        insertScene(db, 2, "dialogue_dock", "Toy Talk", "Nói Chuyện Đồ Chơi",
            "Hỏi đáp về vị trí đồ chơi", "💬", 3);
        insertScene(db, 2, "puzzle_zone", "Toy Puzzle", "Ghép Hình Đồ Chơi",
            "Xếp câu với giới từ", "🧩", 4);
        insertScene(db, 2, "boss_gate", "Teddy Boss", "Boss Gấu Bông",
            "Cứu Teddy bị lạc", "🧸", 5);

        // Planet 3: Animania Wild
        insertScene(db, 3, "landing_zone", "Safari Start", "Bắt Đầu Safari",
            "Học tên các con vật", "🦁", 1);
        insertScene(db, 3, "explore_area", "Animal Hunt", "Tìm Thú",
            "Tìm động vật trong rừng", "🔍", 2);
        insertScene(db, 3, "dialogue_dock", "Zoo Guide", "Hướng Dẫn Viên",
            "Hỏi đáp về khả năng động vật", "💬", 3);
        insertScene(db, 3, "puzzle_zone", "Animal Match", "Ghép Thú",
            "Ghép động vật với hành động", "🧩", 4);
        insertScene(db, 3, "boss_gate", "Wild Boss", "Boss Hoang Dã",
            "Thuần phục thú hoang", "🐉", 5);

        // Planet 4: Citytron Nova
        insertScene(db, 4, "landing_zone", "City Tour", "Tham Quan Thành Phố",
            "Học tên địa điểm", "🏙️", 1);
        insertScene(db, 4, "explore_area", "City Hunt", "Khám Phá Phố",
            "Tìm các địa điểm", "🔍", 2);
        insertScene(db, 4, "dialogue_dock", "Ask Direction", "Hỏi Đường",
            "Luyện hỏi và chỉ đường", "💬", 3);
        insertScene(db, 4, "puzzle_zone", "Map Puzzle", "Ghép Bản Đồ",
            "Xếp câu chỉ đường", "🧩", 4);
        insertScene(db, 4, "boss_gate", "Traffic Boss", "Boss Giao Thông",
            "Vượt qua mê cung đường", "🚦", 5);

        // Planet 5: Foodora Station
        insertScene(db, 5, "landing_zone", "Menu Learn", "Học Menu",
            "Học tên đồ ăn thức uống", "🍕", 1);
        insertScene(db, 5, "explore_area", "Food Hunt", "Tìm Đồ Ăn",
            "Thu thập nguyên liệu", "🔍", 2);
        insertScene(db, 5, "dialogue_dock", "Order Food", "Gọi Món",
            "Luyện gọi món ăn", "💬", 3);
        insertScene(db, 5, "puzzle_zone", "Recipe Puzzle", "Ghép Công Thức",
            "Xếp câu nấu ăn", "🧩", 4);
        insertScene(db, 5, "boss_gate", "Chef Boss", "Boss Đầu Bếp",
            "Hoàn thành món ăn", "👨‍🍳", 5);

        // Planet 6: Weatheron Sky
        insertScene(db, 6, "landing_zone", "Weather Watch", "Xem Thời Tiết",
            "Học từ vựng thời tiết", "⛅", 1);
        insertScene(db, 6, "explore_area", "Cloud Hunt", "Săn Mây",
            "Thu thập các loại mây", "🔍", 2);
        insertScene(db, 6, "dialogue_dock", "Weather Talk", "Nói Về Thời Tiết",
            "Hỏi đáp thời tiết và trang phục", "💬", 3);
        insertScene(db, 6, "puzzle_zone", "Dress Up", "Mặc Đồ",
            "Chọn trang phục phù hợp", "🧩", 4);
        insertScene(db, 6, "boss_gate", "Storm Boss", "Boss Bão Táp",
            "Vượt qua bão", "🌪️", 5);

        // Planet 7: RoboLab Command
        insertScene(db, 7, "landing_zone", "Command Learn", "Học Lệnh",
            "Học các từ chỉ lệnh", "🤖", 1);
        insertScene(db, 7, "explore_area", "Part Hunt", "Tìm Linh Kiện",
            "Thu thập linh kiện robot", "🔍", 2);
        insertScene(db, 7, "dialogue_dock", "Robot Talk", "Nói Với Robot",
            "Ra lệnh cho robot", "💬", 3);
        insertScene(db, 7, "puzzle_zone", "Command Chain", "Chuỗi Lệnh",
            "Xếp thứ tự các lệnh", "🧩", 4);
        insertScene(db, 7, "boss_gate", "Mech Boss", "Boss Cơ Khí",
            "Lập trình đánh boss", "🦾", 5);

        // Planet 8: TimeLapse Base
        insertScene(db, 8, "landing_zone", "Time Learn", "Học Thời Gian",
            "Học giờ và ngày", "⏰", 1);
        insertScene(db, 8, "explore_area", "Schedule Hunt", "Tìm Lịch Trình",
            "Thu thập các hoạt động", "🔍", 2);
        insertScene(db, 8, "dialogue_dock", "Daily Talk", "Nói Về Ngày",
            "Hỏi đáp thói quen hàng ngày", "💬", 3);
        insertScene(db, 8, "puzzle_zone", "Schedule Fix", "Sửa Lịch",
            "Xếp thời gian biểu", "🧩", 4);
        insertScene(db, 8, "boss_gate", "Time Boss", "Boss Thời Gian",
            "Hoàn thành đúng giờ", "⌛", 5);

        // Planet 9: Storyverse Galaxy
        insertScene(db, 9, "landing_zone", "Story Start", "Bắt Đầu Truyện",
            "Học từ kể chuyện", "📚", 1);
        insertScene(db, 9, "explore_area", "Page Hunt", "Tìm Trang Truyện",
            "Thu thập các trang truyện", "🔍", 2);
        insertScene(db, 9, "dialogue_dock", "Story Talk", "Kể Chuyện",
            "Luyện kể câu chuyện", "💬", 3);
        insertScene(db, 9, "puzzle_zone", "Story Order", "Xếp Truyện",
            "Xếp thứ tự câu chuyện", "🧩", 4);
        insertScene(db, 9, "boss_gate", "Dragon Boss", "Boss Rồng",
            "Đánh bại rồng bằng từ", "🐲", 5);

        // Planet 4: Numberia Station
        insertScene(db, 4, "landing_zone", "Number Learn", "Học Số",
            "Học đếm và số", "🔢", 1);
        insertScene(db, 4, "explore_area", "Gem Hunt", "Săn Đá Quý",
            "Thu thập các số", "🔍", 2);
        insertScene(db, 4, "dialogue_dock", "Count Talk", "Nói Về Số",
            "Hỏi đáp về số lượng", "💬", 3);
        insertScene(db, 4, "puzzle_zone", "Math Puzzle", "Giải Toán",
            "Ghép số với số lượng", "🧩", 4);
        insertScene(db, 4, "boss_gate", "Calculator Boss", "Boss Máy Tính",
            "Đếm đúng để thắng", "🤖", 5);

        // Planet 8: Familia Home
        insertScene(db, 8, "landing_zone", "Family Meet", "Gặp Gia Đình",
            "Học về thành viên gia đình", "👨‍👩‍👧", 1);
        insertScene(db, 8, "explore_area", "Photo Hunt", "Tìm Ảnh",
            "Thu thập ảnh gia đình", "🔍", 2);
        insertScene(db, 8, "dialogue_dock", "Family Talk", "Nói Về Gia Đình",
            "Giới thiệu gia đình", "💬", 3);
        insertScene(db, 8, "puzzle_zone", "Family Tree", "Cây Gia Đình",
            "Xếp cây gia đình", "🧩", 4);
        insertScene(db, 8, "boss_gate", "Reunion Boss", "Boss Đoàn Tụ",
            "Tìm đúng thành viên", "👪", 5);

        // Planet 12: Natura Wilderness
        insertScene(db, 12, "landing_zone", "Nature Start", "Bắt Đầu Thiên Nhiên",
            "Học về thiên nhiên", "🌳", 1);
        insertScene(db, 12, "explore_area", "Leaf Hunt", "Săn Lá",
            "Thu thập lá cây", "🔍", 2);
        insertScene(db, 12, "dialogue_dock", "Nature Talk", "Nói Về Thiên Nhiên",
            "So sánh cây cối, động vật", "💬", 3);
        insertScene(db, 12, "puzzle_zone", "Ecosystem", "Hệ Sinh Thái",
            "Xếp chuỗi thức ăn", "🧩", 4);
        insertScene(db, 12, "boss_gate", "Forest Boss", "Boss Rừng",
            "Bảo vệ rừng", "🐻", 5);

        // Planet 13: Artopia Planet
        insertScene(db, 13, "landing_zone", "Art Landing", "Bãi Đáp Nghệ Thuật",
            "Học từ vựng về màu sắc, dụng cụ vẽ, nhạc cụ", "🎨", 1);
        insertScene(db, 13, "explore_area", "Art Hunt", "Săn Nghệ Thuật",
            "Thu thập Word Crystals về nghệ thuật", "🔍", 2);
        insertScene(db, 13, "dialogue_dock", "Art Talk", "Nói Về Nghệ Thuật",
            "Hội thoại về sở thích nghệ thuật", "💬", 3);
        insertScene(db, 13, "puzzle_zone", "Art Puzzle", "Ghép Hình Nghệ Thuật",
            "Xếp câu về hoạt động nghệ thuật", "🧩", 4);
        insertScene(db, 13, "boss_gate", "Artist Boss", "Boss Nghệ Sĩ",
            "Đánh bại boss bằng cách nói về sở thích nghệ thuật", "👾", 5);

        // Planet 14: Playground Park
        insertScene(db, 14, "landing_zone", "Play Landing", "Bãi Đáp Vui Chơi",
            "Học từ vựng về các trò chơi", "🎠", 1);
        insertScene(db, 14, "explore_area", "Play Hunt", "Săn Trò Chơi",
            "Thu thập Word Crystals về các trò chơi", "🔍", 2);
        insertScene(db, 14, "dialogue_dock", "Play Talk", "Nói Về Vui Chơi",
            "Luyện cách rủ bạn chơi cùng", "💬", 3);
        insertScene(db, 14, "puzzle_zone", "Play Puzzle", "Ghép Hình Vui Chơi",
            "Xếp câu về hoạt động vui chơi", "🧩", 4);
        insertScene(db, 14, "boss_gate", "Playground Boss", "Boss Công Viên",
            "Đánh bại boss bằng cách nói về các trò chơi", "👾", 5);

        // Planet 15: School Academy
        insertScene(db, 15, "landing_zone", "School Landing", "Bãi Đáp Trường Học",
            "Học từ vựng về trường học và đồ dùng học tập", "🏫", 1);
        insertScene(db, 15, "explore_area", "School Hunt", "Săn Đồ Dùng Học Tập",
            "Thu thập Word Crystals về đồ dùng học tập", "🔍", 2);
        insertScene(db, 15, "dialogue_dock", "School Talk", "Nói Về Trường Học",
            "Luyện cách nói về đồ dùng học tập", "💬", 3);
        insertScene(db, 15, "puzzle_zone", "School Puzzle", "Ghép Hình Trường Học",
            "Xếp câu về đồ dùng và màu sắc", "🧩", 4);
        insertScene(db, 15, "boss_gate", "Teacher Boss", "Boss Giáo Viên",
            "Đánh bại boss bằng cách nói về trường học", "👾", 5);

        // Planet 16: Body Parts Planet
        insertScene(db, 16, "landing_zone", "Body Landing", "Bãi Đáp Cơ Thể",
            "Học từ vựng về các bộ phận cơ thể", "👶", 1);
        insertScene(db, 16, "explore_area", "Body Hunt", "Săn Bộ Phận Cơ Thể",
            "Thu thập Word Crystals về các bộ phận cơ thể", "🔍", 2);
        insertScene(db, 16, "dialogue_dock", "Body Talk", "Nói Về Cơ Thể",
            "Luyện cách mô tả cơ thể", "💬", 3);
        insertScene(db, 16, "puzzle_zone", "Body Puzzle", "Ghép Hình Cơ Thể",
            "Xếp câu về bộ phận cơ thể", "🧩", 4);
        insertScene(db, 16, "boss_gate", "Doctor Boss", "Boss Bác Sĩ",
            "Đánh bại boss bằng cách nói về cơ thể", "👾", 5);

        // Planet 17: Sports Arena
        insertScene(db, 17, "landing_zone", "Sports Landing", "Bãi Đáp Thể Thao",
            "Học từ vựng về môn thể thao", "⚽", 1);
        insertScene(db, 17, "explore_area", "Sports Hunt", "Săn Dụng Cụ Thể Thao",
            "Thu thập Word Crystals về môn thể thao", "🔍", 2);
        insertScene(db, 17, "dialogue_dock", "Sports Talk", "Nói Về Thể Thao",
            "Luyện cách nói về khả năng thể thao", "💬", 3);
        insertScene(db, 17, "puzzle_zone", "Sports Puzzle", "Ghép Hình Thể Thao",
            "Xếp câu về môn thể thao và hành động", "🧩", 4);
        insertScene(db, 17, "boss_gate", "Champion Boss", "Boss Vô Địch",
            "Đánh bại boss bằng cách nói về thể thao", "👾", 5);

        // Planet 18: Birthday Party
        insertScene(db, 18, "landing_zone", "Party Landing", "Bãi Đáp Tiệc",
            "Học từ vựng về đồ tiệc và sinh nhật", "🎂", 1);
        insertScene(db, 18, "explore_area", "Party Hunt", "Săn Đồ Tiệc",
            "Thu thập Word Crystals về đồ tiệc", "🔍", 2);
        insertScene(db, 18, "dialogue_dock", "Party Talk", "Nói Về Tiệc",
            "Luyện cách hỏi và trả lời về tuổi", "💬", 3);
        insertScene(db, 18, "puzzle_zone", "Party Puzzle", "Ghép Hình Tiệc",
            "Xếp câu về số tuổi và đồ tiệc", "🧩", 4);
        insertScene(db, 18, "boss_gate", "Birthday Boss", "Boss Sinh Nhật",
            "Đánh bại boss bằng cách nói về tuổi và tiệc", "👾", 5);

        // Planet 19: Ocean Deep
        insertScene(db, 19, "landing_zone", "Ocean Landing", "Bãi Đáp Đại Dương",
            "Học từ vựng về sinh vật biển", "🌊", 1);
        insertScene(db, 19, "explore_area", "Ocean Hunt", "Săn Sinh Vật Biển",
            "Thu thập Word Crystals về sinh vật biển", "🔍", 2);
        insertScene(db, 19, "dialogue_dock", "Ocean Talk", "Nói Về Đại Dương",
            "Luyện cách mô tả sinh vật biển", "💬", 3);
        insertScene(db, 19, "puzzle_zone", "Ocean Puzzle", "Ghép Hình Đại Dương",
            "Xếp câu về sinh vật và đại dương", "🧩", 4);
        insertScene(db, 19, "boss_gate", "Sea Boss", "Boss Biển Cả",
            "Đánh bại boss bằng cách nói về đại dương", "👾", 5);

        // Insert words for Coloria Prime
        insertColoriaWords(db, planetId);

        // Insert sentences for Coloria Prime
        insertColoriaSentences(db, planetId);

        // Insert words for all other planets
        insertToytopiaWords(db, 2);
        
        // Insert sentences for Toytopia Orbit
        insertToytopiaSentences(db, 2);
        insertAnimaniaWords(db, 3);
        insertAnimaniaSentences(db, 3);
        insertNumberiaWords(db, 4);
        insertNumberiaSentences(db, 4);
        insertCitytronWords(db, 5);
        insertCitytronSentences(db, 5);
        insertFoodoraWords(db, 6);
        insertFoodoraSentences(db, 6);
        insertWeatheronWords(db, 7);
        insertWeatheronSentences(db, 7);
        insertFamiliaWords(db, 8);
        insertFamiliaSentences(db, 8);
        insertRobolabWords(db, 9);
        insertRobolabSentences(db, 9);
        insertTimelapseWords(db, 10);
        insertTimelapseSentences(db, 10);
        insertStoryverseWords(db, 11);
        insertStoryverseSentences(db, 11);
        insertNaturaWords(db, 12);
        insertNaturaSentences(db, 12);

        // Insert words and sentences for new planets (13-19)
        insertArtopiaWords(db, 13);
        insertArtopiaSentences(db, 13);
        insertPlaygroundWords(db, 14);
        insertPlaygroundSentences(db, 14);
        insertSchoolAcademyWords(db, 15);
        insertSchoolAcademySentences(db, 15);
        insertBodyPartsWords(db, 16);
        insertBodyPartsSentences(db, 16);
        insertSportsArenaWords(db, 17);
        insertSportsArenaSentences(db, 17);
        insertBirthdayPartyWords(db, 18);
        insertBirthdayPartySentences(db, 18);
        insertOceanDeepWords(db, 19);
        insertOceanDeepSentences(db, 19);

        // Insert sentences for puzzle zones
        insertPuzzleZoneSentences(db);

        // Insert default user progress
        ContentValues userValues = new ContentValues();
        userValues.put("user_id", "default");
        userValues.put("total_stars", 0);
        userValues.put("total_fuel_cells", 0);
        db.insert(TABLE_USER_PROGRESS, null, userValues);

        // Insert badges
        insertBadges(db);
    }

    private void insertGalaxy(SQLiteDatabase db, String key, String name, String nameVi,
            String description, String emoji, String color, int requiredStars, int order, int unlocked) {
        ContentValues values = new ContentValues();
        values.put("galaxy_key", key);
        values.put("name", name);
        values.put("name_vi", nameVi);
        values.put("description", description);
        values.put("emoji", emoji);
        values.put("theme_color", color);
        values.put("required_stars", requiredStars);
        values.put("order_index", order);
        values.put("is_unlocked", unlocked);
        db.insert(TABLE_GALAXIES, null, values);
    }

    private void insertBuddy(SQLiteDatabase db, String key, String name, String nameVi,
            String emoji, String description, int isActive, int isUnlocked) {
        ContentValues values = new ContentValues();
        values.put("buddy_key", key);
        values.put("name", name);
        values.put("name_vi", nameVi);
        values.put("emoji", emoji);
        values.put("description", description);
        values.put("level", 1);
        values.put("experience", 0);
        values.put("is_active", isActive);
        values.put("is_unlocked", isUnlocked);
        db.insert(TABLE_BUDDIES, null, values);
    }

    private void insertPlanet(SQLiteDatabase db, int galaxyId, String key, String name, String nameVi,
            String description, String emoji, String color, String bgImage,
            String collectible, String collectibleEmoji, String grammar, String skill,
            int requiredFuel, int order, int unlocked) {
        ContentValues values = new ContentValues();
        values.put("galaxy_id", galaxyId);
        values.put("planet_key", key);
        values.put("name", name);
        values.put("name_vi", nameVi);
        values.put("description", description);
        values.put("emoji", emoji);
        values.put("theme_color", color);
        values.put("background_image", bgImage);
        values.put("collectible_name", collectible);
        values.put("collectible_emoji", collectibleEmoji);
        values.put("grammar_focus", grammar);
        values.put("skill_focus", skill);
        values.put("required_fuel_cells", requiredFuel);
        values.put("order_index", order);
        values.put("is_unlocked", unlocked);
        db.insert(TABLE_PLANETS, null, values);
    }

    private void insertScene(SQLiteDatabase db, long planetId, String sceneKey,
            String name, String nameVi, String description, String emoji, int order) {
        ContentValues values = new ContentValues();
        values.put("planet_id", planetId);
        values.put("scene_key", sceneKey);
        values.put("scene_type", sceneKey);
        values.put("name", name);
        values.put("name_vi", nameVi);
        values.put("description", description);
        values.put("emoji", emoji);
        values.put("order_index", order);
        db.insert(TABLE_SCENES, null, values);
    }

    private void insertColoriaWords(SQLiteDatabase db, long planetId) {
        // Colors
        insertWord(db, planetId, 1, "red", "màu đỏ", "/red/", "🔴", "color", 1,
            "The apple is red.", "Quả táo màu đỏ.");
        insertWord(db, planetId, 1, "blue", "màu xanh dương", "/bluː/", "🔵", "color", 1,
            "The sky is blue.", "Bầu trời màu xanh.");
        insertWord(db, planetId, 1, "green", "màu xanh lá", "/ɡriːn/", "🟢", "color", 1,
            "The grass is green.", "Cỏ màu xanh lá.");
        insertWord(db, planetId, 1, "yellow", "màu vàng", "/ˈjeloʊ/", "🟡", "color", 1,
            "The sun is yellow.", "Mặt trời màu vàng.");
        insertWord(db, planetId, 1, "orange", "màu cam", "/ˈɔːrɪndʒ/", "🟠", "color", 1,
            "The orange is orange.", "Quả cam màu cam.");
        insertWord(db, planetId, 1, "purple", "màu tím", "/ˈpɜːrpl/", "🟣", "color", 1,
            "The grape is purple.", "Quả nho màu tím.");
        insertWord(db, planetId, 1, "pink", "màu hồng", "/pɪŋk/", "💗", "color", 1,
            "The flower is pink.", "Bông hoa màu hồng.");
        insertWord(db, planetId, 1, "black", "màu đen", "/blæk/", "⚫", "color", 1,
            "The cat is black.", "Con mèo màu đen.");
        insertWord(db, planetId, 1, "white", "màu trắng", "/waɪt/", "⚪", "color", 1,
            "The cloud is white.", "Đám mây màu trắng.");
        insertWord(db, planetId, 1, "brown", "màu nâu", "/braʊn/", "🟤", "color", 1,
            "The dog is brown.", "Con chó màu nâu.");

        // Shapes
        insertWord(db, planetId, 1, "circle", "hình tròn", "/ˈsɜːrkl/", "⭕", "shape", 1,
            "Draw a circle.", "Vẽ một hình tròn.");
        insertWord(db, planetId, 1, "square", "hình vuông", "/skwer/", "🔲", "shape", 1,
            "This is a square.", "Đây là hình vuông.");
        insertWord(db, planetId, 1, "triangle", "hình tam giác", "/ˈtraɪæŋɡl/", "🔺", "shape", 1,
            "A triangle has three sides.", "Hình tam giác có ba cạnh.");
        insertWord(db, planetId, 1, "star", "ngôi sao", "/stɑːr/", "⭐", "shape", 1,
            "I can see a star.", "Tôi thấy một ngôi sao.");
        insertWord(db, planetId, 1, "heart", "hình trái tim", "/hɑːrt/", "❤️", "shape", 1,
            "I love this heart.", "Tôi thích hình trái tim này.");

        // Adjectives
        insertWord(db, planetId, 1, "big", "to, lớn", "/bɪɡ/", "🐘", "adjective", 1,
            "The elephant is big.", "Con voi rất to.");
        insertWord(db, planetId, 1, "small", "nhỏ, bé", "/smɔːl/", "🐜", "adjective", 1,
            "The ant is small.", "Con kiến rất nhỏ.");
        insertWord(db, planetId, 1, "bright", "sáng", "/braɪt/", "☀️", "adjective", 1,
            "The sun is bright.", "Mặt trời rất sáng.");
        insertWord(db, planetId, 1, "dark", "tối", "/dɑːrk/", "🌑", "adjective", 1,
            "The room is dark.", "Căn phòng rất tối.");
    }

    // Planet 2: Toytopia Orbit - Toys & Positions
    private void insertToytopiaWords(SQLiteDatabase db, long planetId) {
        // Toys
        insertWord(db, planetId, 1, "ball", "quả bóng", "/bɔːl/", "⚽", "toy", 1,
            "I play with the ball.", "Tôi chơi với quả bóng.");
        insertWord(db, planetId, 1, "doll", "búp bê", "/dɒl/", "🎎", "toy", 1,
            "She has a doll.", "Cô ấy có một con búp bê.");
        insertWord(db, planetId, 1, "car", "ô tô", "/kɑːr/", "🚗", "toy", 1,
            "The car is fast.", "Chiếc ô tô rất nhanh.");
        insertWord(db, planetId, 1, "robot", "người máy", "/ˈroʊbɒt/", "🤖", "toy", 1,
            "The robot can walk.", "Người máy có thể đi.");
        insertWord(db, planetId, 1, "teddy", "gấu bông", "/ˈtedi/", "🧸", "toy", 1,
            "I love my teddy.", "Tôi yêu gấu bông của tôi.");
        insertWord(db, planetId, 1, "train", "tàu hỏa", "/treɪn/", "🚂", "toy", 1,
            "The train is long.", "Tàu hỏa rất dài.");
        insertWord(db, planetId, 1, "kite", "con diều", "/kaɪt/", "🪁", "toy", 1,
            "The kite flies high.", "Con diều bay cao.");
        insertWord(db, planetId, 1, "puzzle", "xếp hình", "/ˈpʌzl/", "🧩", "toy", 1,
            "I do the puzzle.", "Tôi xếp hình.");

        // Prepositions
        insertWord(db, planetId, 1, "in", "trong", "/ɪn/", "📦", "preposition", 1,
            "The ball is in the box.", "Quả bóng ở trong hộp.");
        insertWord(db, planetId, 1, "on", "trên", "/ɒn/", "📚", "preposition", 1,
            "The book is on the table.", "Quyển sách ở trên bàn.");
        insertWord(db, planetId, 1, "under", "dưới", "/ˈʌndər/", "🛋️", "preposition", 1,
            "The cat is under the sofa.", "Con mèo ở dưới ghế sofa.");
        insertWord(db, planetId, 1, "behind", "phía sau", "/bɪˈhaɪnd/", "🚪", "preposition", 1,
            "The dog is behind the door.", "Con chó ở phía sau cửa.");
        insertWord(db, planetId, 1, "next to", "bên cạnh", "/nekst tuː/", "🪑", "preposition", 1,
            "The chair is next to the desk.", "Cái ghế ở bên cạnh bàn.");
        insertWord(db, planetId, 1, "between", "ở giữa", "/bɪˈtwiːn/", "🌳", "preposition", 1,
            "The house is between two trees.", "Ngôi nhà ở giữa hai cây.");
        insertWord(db, planetId, 1, "toy car", "xe đồ chơi", "/tɔɪ kɑːr/", "🚙", "toy", 1,
            "I have a toy car.", "Tôi có một chiếc xe đồ chơi.");
        insertWord(db, planetId, 1, "blocks", "khối gỗ", "/blɒks/", "🧱", "toy", 1,
            "I build with blocks.", "Tôi xây bằng khối gỗ.");
        insertWord(db, planetId, 1, "above", "phía trên", "/əˈbʌv/", "⬆️", "preposition", 1,
            "The bird is above the tree.", "Con chim ở phía trên cây.");
        insertWord(db, planetId, 1, "below", "phía dưới", "/bɪˈloʊ/", "⬇️", "preposition", 1,
            "The fish is below the water.", "Con cá ở phía dưới nước.");
        insertWord(db, planetId, 1, "inside", "bên trong", "/ɪnˈsaɪd/", "📦", "preposition", 1,
            "The toy is inside the box.", "Đồ chơi ở bên trong hộp.");
    }

    // Planet 3: Animania Wild - Animals & Abilities
    private void insertAnimaniaWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "dog", "con chó", "/dɒɡ/", "🐕", "animal", 1,
            "The dog can run fast.", "Con chó có thể chạy nhanh.");
        insertWord(db, planetId, 1, "cat", "con mèo", "/kæt/", "🐱", "animal", 1,
            "The cat can climb trees.", "Con mèo có thể leo cây.");
        insertWord(db, planetId, 1, "bird", "con chim", "/bɜːrd/", "🐦", "animal", 1,
            "The bird can fly.", "Con chim có thể bay.");
        insertWord(db, planetId, 1, "fish", "con cá", "/fɪʃ/", "🐟", "animal", 1,
            "The fish can swim.", "Con cá có thể bơi.");
        insertWord(db, planetId, 1, "lion", "sư tử", "/ˈlaɪən/", "🦁", "animal", 1,
            "The lion is the king.", "Sư tử là vua.");
        insertWord(db, planetId, 1, "elephant", "con voi", "/ˈelɪfənt/", "🐘", "animal", 1,
            "The elephant is big.", "Con voi rất to.");
        insertWord(db, planetId, 1, "monkey", "con khỉ", "/ˈmʌŋki/", "🐒", "animal", 1,
            "The monkey can jump.", "Con khỉ có thể nhảy.");
        insertWord(db, planetId, 1, "penguin", "chim cánh cụt", "/ˈpeŋɡwɪn/", "🐧", "animal", 1,
            "The penguin can swim.", "Chim cánh cụt có thể bơi.");
        insertWord(db, planetId, 1, "rabbit", "con thỏ", "/ˈræbɪt/", "🐰", "animal", 1,
            "The rabbit can hop.", "Con thỏ có thể nhảy lò cò.");
        insertWord(db, planetId, 1, "snake", "con rắn", "/sneɪk/", "🐍", "animal", 1,
            "The snake can't walk.", "Con rắn không thể đi.");

        // Actions
        insertWord(db, planetId, 1, "run", "chạy", "/rʌn/", "🏃", "action", 1,
            "I can run fast.", "Tôi có thể chạy nhanh.");
        insertWord(db, planetId, 1, "jump", "nhảy", "/dʒʌmp/", "🦘", "action", 1,
            "Kangaroos can jump high.", "Kangaroo có thể nhảy cao.");
        insertWord(db, planetId, 1, "fly", "bay", "/flaɪ/", "🦅", "action", 1,
            "Birds can fly.", "Chim có thể bay.");
        insertWord(db, planetId, 1, "swim", "bơi", "/swɪm/", "🏊", "action", 1,
            "Fish can swim.", "Cá có thể bơi.");
        insertWord(db, planetId, 1, "tiger", "con hổ", "/ˈtaɪɡər/", "🐅", "animal", 1,
            "The tiger is strong.", "Con hổ rất mạnh.");
        insertWord(db, planetId, 1, "bear", "con gấu", "/ber/", "🐻", "animal", 1,
            "The bear is big.", "Con gấu rất to.");
        insertWord(db, planetId, 1, "giraffe", "con hươu cao cổ", "/dʒɪˈræf/", "🦒", "animal", 1,
            "The giraffe is tall.", "Hươu cao cổ rất cao.");
        insertWord(db, planetId, 1, "zebra", "con ngựa vằn", "/ˈziːbrə/", "🦓", "animal", 1,
            "The zebra has stripes.", "Ngựa vằn có sọc.");
        insertWord(db, planetId, 1, "climb", "leo", "/klaɪm/", "🧗", "action", 1,
            "Monkeys can climb.", "Khỉ có thể leo.");
        insertWord(db, planetId, 1, "walk", "đi bộ", "/wɔːk/", "🚶", "action", 1,
            "I can walk.", "Tôi có thể đi bộ.");
    }

    // Planet 4: Numberia Station - Numbers & Counting
    private void insertNumberiaWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "one", "một", "/wʌn/", "1️⃣", "number", 1,
            "I have one apple.", "Tôi có một quả táo.");
        insertWord(db, planetId, 1, "two", "hai", "/tuː/", "2️⃣", "number", 1,
            "I have two eyes.", "Tôi có hai mắt.");
        insertWord(db, planetId, 1, "three", "ba", "/θriː/", "3️⃣", "number", 1,
            "I have three books.", "Tôi có ba quyển sách.");
        insertWord(db, planetId, 1, "four", "bốn", "/fɔːr/", "4️⃣", "number", 1,
            "I have four pencils.", "Tôi có bốn cây bút chì.");
        insertWord(db, planetId, 1, "five", "năm", "/faɪv/", "5️⃣", "number", 1,
            "I have five fingers.", "Tôi có năm ngón tay.");
        insertWord(db, planetId, 1, "six", "sáu", "/sɪks/", "6️⃣", "number", 1,
            "I have six cookies.", "Tôi có sáu cái bánh quy.");
        insertWord(db, planetId, 1, "seven", "bảy", "/ˈsevn/", "7️⃣", "number", 1,
            "I have seven days.", "Tôi có bảy ngày.");
        insertWord(db, planetId, 1, "eight", "tám", "/eɪt/", "8️⃣", "number", 1,
            "I have eight legs.", "Tôi có tám chân.");
        insertWord(db, planetId, 1, "nine", "chín", "/naɪn/", "9️⃣", "number", 1,
            "I have nine balloons.", "Tôi có chín quả bóng bay.");
        insertWord(db, planetId, 1, "ten", "mười", "/ten/", "🔟", "number", 1,
            "I have ten toys.", "Tôi có mười đồ chơi.");
        insertWord(db, planetId, 1, "eleven", "mười một", "/ɪˈlevn/", "1️⃣1️⃣", "number", 1,
            "I am eleven years old.", "Tôi mười một tuổi.");
        insertWord(db, planetId, 1, "twelve", "mười hai", "/twelv/", "1️⃣2️⃣", "number", 1,
            "I have twelve months.", "Tôi có mười hai tháng.");
        insertWord(db, planetId, 1, "count", "đếm", "/kaʊnt/", "🔢", "action", 1,
            "I can count to ten.", "Tôi có thể đếm đến mười.");
        insertWord(db, planetId, 1, "how many", "bao nhiêu", "/haʊ ˈmeni/", "❓", "question", 1,
            "How many apples?", "Bao nhiêu quả táo?");
        insertWord(db, planetId, 1, "many", "nhiều", "/ˈmeni/", "📊", "quantity", 1,
            "I have many toys.", "Tôi có nhiều đồ chơi.");
        insertWord(db, planetId, 1, "few", "ít", "/fjuː/", "📉", "quantity", 1,
            "I have few books.", "Tôi có ít sách.");
        insertWord(db, planetId, 1, "more", "nhiều hơn", "/mɔːr/", "➕", "quantity", 1,
            "I want more cookies.", "Tôi muốn nhiều bánh quy hơn.");
        insertWord(db, planetId, 1, "less", "ít hơn", "/les/", "➖", "quantity", 1,
            "I have less candy.", "Tôi có ít kẹo hơn.");
        insertWord(db, planetId, 1, "zero", "không", "/ˈzɪroʊ/", "0️⃣", "number", 1,
            "I have zero apples.", "Tôi có không quả táo nào.");
    }

    // Planet 5: Citytron Nova - Places & Directions
    private void insertCitytronWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "school", "trường học", "/skuːl/", "🏫", "place", 1,
            "I go to school.", "Tôi đi đến trường.");
        insertWord(db, planetId, 1, "hospital", "bệnh viện", "/ˈhɒspɪtl/", "🏥", "place", 1,
            "The hospital is big.", "Bệnh viện rất lớn.");
        insertWord(db, planetId, 1, "park", "công viên", "/pɑːrk/", "🏞️", "place", 1,
            "I play in the park.", "Tôi chơi ở công viên.");
        insertWord(db, planetId, 1, "supermarket", "siêu thị", "/ˈsuːpərmɑːrkɪt/", "🏪", "place", 1,
            "Mom shops at the supermarket.", "Mẹ mua sắm ở siêu thị.");
        insertWord(db, planetId, 1, "library", "thư viện", "/ˈlaɪbrəri/", "📚", "place", 1,
            "I read at the library.", "Tôi đọc sách ở thư viện.");
        insertWord(db, planetId, 1, "restaurant", "nhà hàng", "/ˈrestrɒnt/", "🍽️", "place", 1,
            "We eat at the restaurant.", "Chúng tôi ăn ở nhà hàng.");

        // Directions
        insertWord(db, planetId, 1, "left", "trái", "/left/", "⬅️", "direction", 1,
            "Turn left.", "Rẽ trái.");
        insertWord(db, planetId, 1, "right", "phải", "/raɪt/", "➡️", "direction", 1,
            "Turn right.", "Rẽ phải.");
        insertWord(db, planetId, 1, "straight", "thẳng", "/streɪt/", "⬆️", "direction", 1,
            "Go straight.", "Đi thẳng.");
        insertWord(db, planetId, 1, "near", "gần", "/nɪr/", "📍", "direction", 1,
            "The park is near.", "Công viên ở gần.");
        insertWord(db, planetId, 1, "far", "xa", "/fɑːr/", "🗺️", "direction", 1,
            "The beach is far.", "Bãi biển ở xa.");
        insertWord(db, planetId, 1, "zoo", "sở thú", "/zuː/", "🦁", "place", 1,
            "I go to the zoo.", "Tôi đi đến sở thú.");
        insertWord(db, planetId, 1, "museum", "bảo tàng", "/mjuˈziːəm/", "🏛️", "place", 1,
            "I visit the museum.", "Tôi thăm bảo tàng.");
        insertWord(db, planetId, 1, "bank", "ngân hàng", "/bæŋk/", "🏦", "place", 1,
            "I go to the bank.", "Tôi đi đến ngân hàng.");
        insertWord(db, planetId, 1, "post office", "bưu điện", "/poʊst ˈɒfɪs/", "📮", "place", 1,
            "I send a letter at the post office.", "Tôi gửi thư ở bưu điện.");
        insertWord(db, planetId, 1, "cinema", "rạp chiếu phim", "/ˈsɪnəmə/", "🎬", "place", 1,
            "I watch a movie at the cinema.", "Tôi xem phim ở rạp chiếu phim.");
        insertWord(db, planetId, 1, "behind", "phía sau", "/bɪˈhaɪnd/", "⬅️", "direction", 1,
            "The car is behind the house.", "Xe ở phía sau nhà.");
        insertWord(db, planetId, 1, "in front of", "phía trước", "/ɪn frʌnt ʌv/", "➡️", "direction", 1,
            "The tree is in front of the house.", "Cây ở phía trước nhà.");
        insertWord(db, planetId, 1, "across", "bên kia", "/əˈkrɒs/", "↔️", "direction", 1,
            "The shop is across the street.", "Cửa hàng ở bên kia đường.");
    }

    // Planet 6: Foodora Station - Food & Shopping
    private void insertFoodoraWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "apple", "quả táo", "/ˈæpl/", "🍎", "food", 1,
            "I eat an apple.", "Tôi ăn một quả táo.");
        insertWord(db, planetId, 1, "banana", "quả chuối", "/bəˈnænə/", "🍌", "food", 1,
            "Monkeys like bananas.", "Khỉ thích chuối.");
        insertWord(db, planetId, 1, "bread", "bánh mì", "/bred/", "🍞", "food", 1,
            "I eat bread for breakfast.", "Tôi ăn bánh mì cho bữa sáng.");
        insertWord(db, planetId, 1, "rice", "cơm", "/raɪs/", "🍚", "food", 1,
            "We eat rice every day.", "Chúng tôi ăn cơm mỗi ngày.");
        insertWord(db, planetId, 1, "chicken", "thịt gà", "/ˈtʃɪkɪn/", "🍗", "food", 1,
            "I like chicken.", "Tôi thích thịt gà.");
        insertWord(db, planetId, 1, "pizza", "bánh pizza", "/ˈpiːtsə/", "🍕", "food", 1,
            "Pizza is delicious.", "Pizza rất ngon.");
        insertWord(db, planetId, 1, "milk", "sữa", "/mɪlk/", "🥛", "drink", 1,
            "I drink milk.", "Tôi uống sữa.");
        insertWord(db, planetId, 1, "juice", "nước ép", "/dʒuːs/", "🧃", "drink", 1,
            "I like orange juice.", "Tôi thích nước cam.");
        insertWord(db, planetId, 1, "water", "nước", "/ˈwɔːtər/", "💧", "drink", 1,
            "Drink more water.", "Uống nhiều nước hơn.");
        insertWord(db, planetId, 1, "ice cream", "kem", "/ˈaɪs kriːm/", "🍦", "food", 1,
            "I love ice cream.", "Tôi yêu kem.");
        insertWord(db, planetId, 1, "orange", "quả cam", "/ˈɔːrɪndʒ/", "🍊", "food", 1,
            "I eat an orange.", "Tôi ăn một quả cam.");
        insertWord(db, planetId, 1, "grapes", "quả nho", "/ɡreɪps/", "🍇", "food", 1,
            "I like grapes.", "Tôi thích nho.");
        insertWord(db, planetId, 1, "strawberry", "dâu tây", "/ˈstrɔːberi/", "🍓", "food", 1,
            "Strawberries are sweet.", "Dâu tây ngọt.");
        insertWord(db, planetId, 1, "cookie", "bánh quy", "/ˈkʊki/", "🍪", "food", 1,
            "I eat a cookie.", "Tôi ăn bánh quy.");
        insertWord(db, planetId, 1, "sandwich", "bánh mì kẹp", "/ˈsænwɪtʃ/", "🥪", "food", 1,
            "I make a sandwich.", "Tôi làm bánh mì kẹp.");
        insertWord(db, planetId, 1, "soup", "súp", "/suːp/", "🍲", "food", 1,
            "I eat soup.", "Tôi ăn súp.");
        insertWord(db, planetId, 1, "cheese", "phô mai", "/tʃiːz/", "🧀", "food", 1,
            "I like cheese.", "Tôi thích phô mai.");
        insertWord(db, planetId, 1, "egg", "trứng", "/eɡ/", "🥚", "food", 1,
            "I eat an egg.", "Tôi ăn một quả trứng.");
        insertWord(db, planetId, 1, "buy", "mua", "/baɪ/", "🛒", "shopping", 1,
            "I buy food.", "Tôi mua thức ăn.");
    }

    // Planet 7: Weatheron Sky - Weather & Clothes
    private void insertWeatheronWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "sunny", "nắng", "/ˈsʌni/", "☀️", "weather", 1,
            "It is sunny today.", "Hôm nay trời nắng.");
        insertWord(db, planetId, 1, "rainy", "mưa", "/ˈreɪni/", "🌧️", "weather", 1,
            "It is rainy.", "Trời đang mưa.");
        insertWord(db, planetId, 1, "cloudy", "nhiều mây", "/ˈklaʊdi/", "☁️", "weather", 1,
            "It is cloudy.", "Trời nhiều mây.");
        insertWord(db, planetId, 1, "windy", "có gió", "/ˈwɪndi/", "💨", "weather", 1,
            "It is windy.", "Trời có gió.");
        insertWord(db, planetId, 1, "snowy", "có tuyết", "/ˈsnoʊi/", "❄️", "weather", 1,
            "It is snowy in winter.", "Trời có tuyết vào mùa đông.");
        insertWord(db, planetId, 1, "hot", "nóng", "/hɒt/", "🥵", "weather", 1,
            "It is hot today.", "Hôm nay trời nóng.");
        insertWord(db, planetId, 1, "cold", "lạnh", "/koʊld/", "🥶", "weather", 1,
            "It is cold outside.", "Bên ngoài trời lạnh.");

        // Clothes
        insertWord(db, planetId, 1, "jacket", "áo khoác", "/ˈdʒækɪt/", "🧥", "clothes", 1,
            "Wear a jacket.", "Mặc áo khoác.");
        insertWord(db, planetId, 1, "hat", "mũ", "/hæt/", "🧢", "clothes", 1,
            "Wear a hat.", "Đội mũ.");
        insertWord(db, planetId, 1, "umbrella", "ô, dù", "/ʌmˈbrelə/", "☂️", "clothes", 1,
            "Take an umbrella.", "Mang theo ô.");
        insertWord(db, planetId, 1, "boots", "giày ống", "/buːts/", "👢", "clothes", 1,
            "I wear boots.", "Tôi mang giày ống.");
        insertWord(db, planetId, 1, "sunglasses", "kính mát", "/ˈsʌnɡlæsɪz/", "🕶️", "clothes", 1,
            "I wear sunglasses.", "Tôi đeo kính mát.");
        insertWord(db, planetId, 1, "shirt", "áo sơ mi", "/ʃɜːrt/", "👕", "clothes", 1,
            "I wear a shirt.", "Tôi mặc áo sơ mi.");
        insertWord(db, planetId, 1, "pants", "quần", "/pænts/", "👖", "clothes", 1,
            "I wear pants.", "Tôi mặc quần.");
        insertWord(db, planetId, 1, "shoes", "giày", "/ʃuːz/", "👟", "clothes", 1,
            "I wear shoes.", "Tôi mang giày.");
        insertWord(db, planetId, 1, "dress", "váy", "/dres/", "👗", "clothes", 1,
            "She wears a dress.", "Cô ấy mặc váy.");
        insertWord(db, planetId, 1, "stormy", "có bão", "/ˈstɔːrmi/", "⛈️", "weather", 1,
            "It is stormy.", "Trời có bão.");
        insertWord(db, planetId, 1, "foggy", "có sương mù", "/ˈfɒɡi/", "🌫️", "weather", 1,
            "It is foggy.", "Trời có sương mù.");
        insertWord(db, planetId, 1, "warm", "ấm", "/wɔːrm/", "🌡️", "weather", 1,
            "It is warm today.", "Hôm nay trời ấm.");
    }

    // Planet 8: Familia Home - Family & Home
    private void insertFamiliaWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "father", "bố", "/ˈfɑːðər/", "👨", "family", 1,
            "My father is tall.", "Bố tôi cao.");
        insertWord(db, planetId, 1, "mother", "mẹ", "/ˈmʌðər/", "👩", "family", 1,
            "My mother is kind.", "Mẹ tôi tốt bụng.");
        insertWord(db, planetId, 1, "brother", "anh/em trai", "/ˈbrʌðər/", "👦", "family", 1,
            "My brother plays football.", "Anh trai tôi chơi bóng đá.");
        insertWord(db, planetId, 1, "sister", "chị/em gái", "/ˈsɪstər/", "👧", "family", 1,
            "My sister likes reading.", "Chị gái tôi thích đọc sách.");
        insertWord(db, planetId, 1, "grandfather", "ông", "/ˈɡrænfɑːðər/", "👴", "family", 1,
            "My grandfather tells stories.", "Ông tôi kể chuyện.");
        insertWord(db, planetId, 1, "grandmother", "bà", "/ˈɡrænmʌðər/", "👵", "family", 1,
            "My grandmother bakes cookies.", "Bà tôi nướng bánh quy.");
        insertWord(db, planetId, 1, "baby", "em bé", "/ˈbeɪbi/", "👶", "family", 1,
            "The baby is sleeping.", "Em bé đang ngủ.");
        insertWord(db, planetId, 1, "uncle", "chú/bác", "/ˈʌŋkl/", "👨", "family", 1,
            "My uncle visits us.", "Chú tôi đến thăm chúng tôi.");
        insertWord(db, planetId, 1, "aunt", "cô/dì", "/ænt/", "👩", "family", 1,
            "My aunt is nice.", "Cô tôi tốt bụng.");
        insertWord(db, planetId, 1, "cousin", "anh/chị/em họ", "/ˈkʌzn/", "👫", "family", 1,
            "I play with my cousin.", "Tôi chơi với anh họ.");
        insertWord(db, planetId, 1, "room", "phòng", "/ruːm/", "🚪", "home", 1,
            "I clean my room.", "Tôi dọn phòng.");
        insertWord(db, planetId, 1, "kitchen", "bếp", "/ˈkɪtʃɪn/", "🍳", "home", 1,
            "I cook in the kitchen.", "Tôi nấu ăn trong bếp.");
        insertWord(db, planetId, 1, "bedroom", "phòng ngủ", "/ˈbedruːm/", "🛏️", "home", 1,
            "I sleep in my bedroom.", "Tôi ngủ trong phòng ngủ.");
        insertWord(db, planetId, 1, "bathroom", "phòng tắm", "/ˈbæθruːm/", "🚿", "home", 1,
            "I wash in the bathroom.", "Tôi tắm trong phòng tắm.");
        insertWord(db, planetId, 1, "living room", "phòng khách", "/ˈlɪvɪŋ ruːm/", "🛋️", "home", 1,
            "We watch TV in the living room.", "Chúng tôi xem TV trong phòng khách.");
        insertWord(db, planetId, 1, "garden", "vườn", "/ˈɡɑːrdn/", "🌳", "home", 1,
            "I play in the garden.", "Tôi chơi trong vườn.");
        insertWord(db, planetId, 1, "door", "cửa", "/dɔːr/", "🚪", "home", 1,
            "I open the door.", "Tôi mở cửa.");
        insertWord(db, planetId, 1, "window", "cửa sổ", "/ˈwɪndoʊ/", "🪟", "home", 1,
            "I look out the window.", "Tôi nhìn ra cửa sổ.");
        insertWord(db, planetId, 1, "table", "bàn", "/ˈteɪbl/", "🪑", "home", 1,
            "I eat at the table.", "Tôi ăn ở bàn.");
        insertWord(db, planetId, 1, "bed", "giường", "/bed/", "🛏️", "home", 1,
            "I sleep on my bed.", "Tôi ngủ trên giường.");
    }

    // Planet 9: RoboLab Command - Commands & Sequences
    private void insertRobolabWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "open", "mở", "/ˈoʊpən/", "📂", "command", 1,
            "Open the door.", "Mở cửa.");
        insertWord(db, planetId, 1, "close", "đóng", "/kloʊz/", "📁", "command", 1,
            "Close the window.", "Đóng cửa sổ.");
        insertWord(db, planetId, 1, "press", "nhấn", "/pres/", "🔘", "command", 1,
            "Press the button.", "Nhấn nút.");
        insertWord(db, planetId, 1, "turn", "xoay, rẽ", "/tɜːrn/", "🔄", "command", 1,
            "Turn around.", "Xoay người.");
        insertWord(db, planetId, 1, "stop", "dừng", "/stɒp/", "🛑", "command", 1,
            "Stop right there!", "Dừng lại ngay!");
        insertWord(db, planetId, 1, "go", "đi", "/ɡoʊ/", "▶️", "command", 1,
            "Go forward.", "Đi về phía trước.");
        insertWord(db, planetId, 1, "wait", "chờ", "/weɪt/", "⏳", "command", 1,
            "Wait here.", "Chờ ở đây.");

        // Sequence words
        insertWord(db, planetId, 1, "first", "đầu tiên", "/fɜːrst/", "1️⃣", "sequence", 1,
            "First, open the door.", "Đầu tiên, mở cửa.");
        insertWord(db, planetId, 1, "then", "sau đó", "/ðen/", "2️⃣", "sequence", 1,
            "Then, go inside.", "Sau đó, đi vào trong.");
        insertWord(db, planetId, 1, "next", "tiếp theo", "/nekst/", "3️⃣", "sequence", 1,
            "Next, turn left.", "Tiếp theo, rẽ trái.");
        insertWord(db, planetId, 1, "finally", "cuối cùng", "/ˈfaɪnəli/", "🏁", "sequence", 1,
            "Finally, press the button.", "Cuối cùng, nhấn nút.");
        insertWord(db, planetId, 1, "start", "bắt đầu", "/stɑːrt/", "▶️", "command", 1,
            "Start the robot.", "Bắt đầu robot.");
        insertWord(db, planetId, 1, "move", "di chuyển", "/muːv/", "↔️", "command", 1,
            "Move forward.", "Di chuyển về phía trước.");
        insertWord(db, planetId, 1, "pick up", "nhặt lên", "/pɪk ʌp/", "🤖", "command", 1,
            "Pick up the box.", "Nhặt hộp lên.");
        insertWord(db, planetId, 1, "put down", "đặt xuống", "/pʊt daʊn/", "📦", "command", 1,
            "Put down the box.", "Đặt hộp xuống.");
        insertWord(db, planetId, 1, "second", "thứ hai", "/ˈsekənd/", "2️⃣", "sequence", 1,
            "Second, turn right.", "Thứ hai, rẽ phải.");
        insertWord(db, planetId, 1, "third", "thứ ba", "/θɜːrd/", "3️⃣", "sequence", 1,
            "Third, go straight.", "Thứ ba, đi thẳng.");
        insertWord(db, planetId, 1, "last", "cuối cùng", "/læst/", "🏁", "sequence", 1,
            "Last, stop here.", "Cuối cùng, dừng ở đây.");
        insertWord(db, planetId, 1, "repeat", "lặp lại", "/rɪˈpiːt/", "🔁", "command", 1,
            "Repeat the action.", "Lặp lại hành động.");
    }

    // Planet 10: TimeLapse Base - Time & Routines
    private void insertTimelapseWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "morning", "buổi sáng", "/ˈmɔːrnɪŋ/", "🌅", "time", 1,
            "Good morning!", "Chào buổi sáng!");
        insertWord(db, planetId, 1, "afternoon", "buổi chiều", "/ˌæftərˈnuːn/", "🌤️", "time", 1,
            "Good afternoon!", "Chào buổi chiều!");
        insertWord(db, planetId, 1, "evening", "buổi tối", "/ˈiːvnɪŋ/", "🌆", "time", 1,
            "Good evening!", "Chào buổi tối!");
        insertWord(db, planetId, 1, "night", "đêm", "/naɪt/", "🌙", "time", 1,
            "Good night!", "Chúc ngủ ngon!");

        // Days
        insertWord(db, planetId, 1, "Monday", "Thứ Hai", "/ˈmʌndeɪ/", "📅", "day", 1,
            "Today is Monday.", "Hôm nay là Thứ Hai.");
        insertWord(db, planetId, 1, "Tuesday", "Thứ Ba", "/ˈtuːzdeɪ/", "📅", "day", 1,
            "I have English on Tuesday.", "Tôi có tiếng Anh vào Thứ Ba.");
        insertWord(db, planetId, 1, "Wednesday", "Thứ Tư", "/ˈwenzdeɪ/", "📅", "day", 1,
            "Wednesday is fun.", "Thứ Tư vui lắm.");

        // Routines
        insertWord(db, planetId, 1, "wake up", "thức dậy", "/weɪk ʌp/", "⏰", "routine", 1,
            "I wake up at 7.", "Tôi thức dậy lúc 7 giờ.");
        insertWord(db, planetId, 1, "brush teeth", "đánh răng", "/brʌʃ tiːθ/", "🪥", "routine", 1,
            "I brush my teeth.", "Tôi đánh răng.");
        insertWord(db, planetId, 1, "eat breakfast", "ăn sáng", "/iːt ˈbrekfəst/", "🍳", "routine", 1,
            "I eat breakfast at 7:30.", "Tôi ăn sáng lúc 7:30.");
        insertWord(db, planetId, 1, "go to school", "đi học", "/ɡoʊ tuː skuːl/", "🎒", "routine", 1,
            "I go to school at 8.", "Tôi đi học lúc 8 giờ.");
        insertWord(db, planetId, 1, "do homework", "làm bài tập", "/duː ˈhoʊmwɜːrk/", "📝", "routine", 1,
            "I do my homework.", "Tôi làm bài tập về nhà.");
        insertWord(db, planetId, 1, "Thursday", "Thứ Năm", "/ˈθɜːrzdeɪ/", "📅", "day", 1,
            "Thursday is fun.", "Thứ Năm vui.");
        insertWord(db, planetId, 1, "Friday", "Thứ Sáu", "/ˈfraɪdeɪ/", "📅", "day", 1,
            "Friday is the last day.", "Thứ Sáu là ngày cuối.");
        insertWord(db, planetId, 1, "Saturday", "Thứ Bảy", "/ˈsætərdeɪ/", "📅", "day", 1,
            "Saturday is weekend.", "Thứ Bảy là cuối tuần.");
        insertWord(db, planetId, 1, "Sunday", "Chủ Nhật", "/ˈsʌndeɪ/", "📅", "day", 1,
            "Sunday is rest day.", "Chủ Nhật là ngày nghỉ.");
        insertWord(db, planetId, 1, "eat lunch", "ăn trưa", "/iːt lʌntʃ/", "🍽️", "routine", 1,
            "I eat lunch at 12.", "Tôi ăn trưa lúc 12 giờ.");
        insertWord(db, planetId, 1, "eat dinner", "ăn tối", "/iːt ˈdɪnər/", "🍽️", "routine", 1,
            "I eat dinner at 7.", "Tôi ăn tối lúc 7 giờ.");
        insertWord(db, planetId, 1, "go to bed", "đi ngủ", "/ɡoʊ tuː bed/", "🛏️", "routine", 1,
            "I go to bed at 9.", "Tôi đi ngủ lúc 9 giờ.");
        insertWord(db, planetId, 1, "watch TV", "xem TV", "/wɒtʃ tiː viː/", "📺", "routine", 1,
            "I watch TV in the evening.", "Tôi xem TV vào buổi tối.");
        insertWord(db, planetId, 1, "play games", "chơi game", "/pleɪ ɡeɪmz/", "🎮", "routine", 1,
            "I play games after school.", "Tôi chơi game sau giờ học.");
    }

    // Planet 11: Storyverse Galaxy - Storytelling
    private void insertStoryverseWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "once", "ngày xưa", "/wʌns/", "📖", "story", 1,
            "Once upon a time...", "Ngày xửa ngày xưa...");
        insertWord(db, planetId, 1, "forest", "khu rừng", "/ˈfɒrɪst/", "🌲", "story", 1,
            "I went to the forest.", "Tôi đã đi vào rừng.");
        insertWord(db, planetId, 1, "castle", "lâu đài", "/ˈkɑːsl/", "🏰", "story", 1,
            "The princess lives in a castle.", "Công chúa sống trong lâu đài.");
        insertWord(db, planetId, 1, "dragon", "con rồng", "/ˈdræɡən/", "🐉", "story", 1,
            "I saw a dragon.", "Tôi thấy một con rồng.");
        insertWord(db, planetId, 1, "magic", "phép màu", "/ˈmædʒɪk/", "✨", "story", 1,
            "Magic is real.", "Phép màu có thật.");
        insertWord(db, planetId, 1, "brave", "dũng cảm", "/breɪv/", "🦸", "story", 1,
            "The hero is brave.", "Người hùng rất dũng cảm.");
        insertWord(db, planetId, 1, "happy", "vui vẻ", "/ˈhæpi/", "😊", "story", 1,
            "They lived happily.", "Họ sống vui vẻ.");
        insertWord(db, planetId, 1, "scared", "sợ hãi", "/skerd/", "😨", "story", 1,
            "I was scared.", "Tôi đã sợ hãi.");

        // Connectors
        insertWord(db, planetId, 1, "and", "và", "/ænd/", "➕", "connector", 1,
            "I saw a bird and a cat.", "Tôi thấy một con chim và một con mèo.");
        insertWord(db, planetId, 1, "but", "nhưng", "/bʌt/", "↔️", "connector", 1,
            "I was scared, but I was brave.", "Tôi sợ, nhưng tôi dũng cảm.");
        insertWord(db, planetId, 1, "so", "vì vậy", "/soʊ/", "➡️", "connector", 1,
            "It was cold, so I wore a jacket.", "Trời lạnh, vì vậy tôi mặc áo khoác.");
        insertWord(db, planetId, 1, "because", "bởi vì", "/bɪˈkɒz/", "💡", "connector", 1,
            "I'm happy because I won.", "Tôi vui vì tôi thắng.");
        insertWord(db, planetId, 1, "prince", "hoàng tử", "/prɪns/", "🤴", "story", 1,
            "The prince is brave.", "Hoàng tử rất dũng cảm.");
        insertWord(db, planetId, 1, "princess", "công chúa", "/prɪnˈses/", "👸", "story", 1,
            "The princess is beautiful.", "Công chúa rất xinh đẹp.");
        insertWord(db, planetId, 1, "knight", "hiệp sĩ", "/naɪt/", "⚔️", "story", 1,
            "The knight saves the day.", "Hiệp sĩ cứu ngày.");
        insertWord(db, planetId, 1, "sword", "thanh kiếm", "/sɔːrd/", "🗡️", "story", 1,
            "The knight has a sword.", "Hiệp sĩ có thanh kiếm.");
        insertWord(db, planetId, 1, "treasure", "kho báu", "/ˈtreʒər/", "💎", "story", 1,
            "I found the treasure.", "Tôi tìm thấy kho báu.");
        insertWord(db, planetId, 1, "adventure", "cuộc phiêu lưu", "/ədˈventʃər/", "🗺️", "story", 1,
            "I go on an adventure.", "Tôi đi phiêu lưu.");
        insertWord(db, planetId, 1, "journey", "hành trình", "/ˈdʒɜːrni/", "🚶", "story", 1,
            "The journey is long.", "Hành trình rất dài.");
        insertWord(db, planetId, 1, "end", "kết thúc", "/end/", "🏁", "story", 1,
            "The story has a happy end.", "Câu chuyện có kết thúc vui.");
    }

    // Planet 12: Natura Wilderness - Nature & Environment
    private void insertNaturaWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "tree", "cây", "/triː/", "🌳", "nature", 1,
            "The tree is tall.", "Cây rất cao.");
        insertWord(db, planetId, 1, "flower", "hoa", "/ˈflaʊər/", "🌸", "nature", 1,
            "The flower is beautiful.", "Hoa rất đẹp.");
        insertWord(db, planetId, 1, "grass", "cỏ", "/ɡræs/", "🌱", "nature", 1,
            "The grass is green.", "Cỏ màu xanh.");
        insertWord(db, planetId, 1, "mountain", "núi", "/ˈmaʊntən/", "⛰️", "nature", 1,
            "The mountain is high.", "Núi rất cao.");
        insertWord(db, planetId, 1, "river", "sông", "/ˈrɪvər/", "🌊", "nature", 1,
            "The river flows.", "Sông chảy.");
        insertWord(db, planetId, 1, "lake", "hồ", "/leɪk/", "🏞️", "nature", 1,
            "I swim in the lake.", "Tôi bơi trong hồ.");
        insertWord(db, planetId, 1, "forest", "rừng", "/ˈfɒrɪst/", "🌲", "nature", 1,
            "I walk in the forest.", "Tôi đi bộ trong rừng.");
        insertWord(db, planetId, 1, "sun", "mặt trời", "/sʌn/", "☀️", "nature", 1,
            "The sun is bright.", "Mặt trời rất sáng.");
        insertWord(db, planetId, 1, "moon", "mặt trăng", "/muːn/", "🌙", "nature", 1,
            "The moon is round.", "Mặt trăng tròn.");
        insertWord(db, planetId, 1, "star", "ngôi sao", "/stɑːr/", "⭐", "nature", 1,
            "I see many stars.", "Tôi thấy nhiều sao.");
        insertWord(db, planetId, 1, "cloud", "mây", "/klaʊd/", "☁️", "nature", 1,
            "The cloud is white.", "Mây màu trắng.");
        insertWord(db, planetId, 1, "rain", "mưa", "/reɪn/", "🌧️", "nature", 1,
            "It is raining.", "Trời đang mưa.");
        insertWord(db, planetId, 1, "wind", "gió", "/wɪnd/", "💨", "nature", 1,
            "The wind is strong.", "Gió rất mạnh.");
        insertWord(db, planetId, 1, "snow", "tuyết", "/snoʊ/", "❄️", "nature", 1,
            "I play in the snow.", "Tôi chơi trong tuyết.");
        insertWord(db, planetId, 1, "rock", "đá", "/rɒk/", "🪨", "nature", 1,
            "I sit on a rock.", "Tôi ngồi trên đá.");
        insertWord(db, planetId, 1, "leaf", "lá", "/liːf/", "🍃", "nature", 1,
            "The leaf is green.", "Lá màu xanh.");
        insertWord(db, planetId, 1, "bird", "con chim", "/bɜːrd/", "🐦", "nature", 1,
            "The bird sings.", "Con chim hót.");
        insertWord(db, planetId, 1, "butterfly", "bướm", "/ˈbʌtərflaɪ/", "🦋", "nature", 1,
            "The butterfly is colorful.", "Bướm nhiều màu.");
        insertWord(db, planetId, 1, "bigger", "to hơn", "/ˈbɪɡər/", "📈", "comparative", 1,
            "The elephant is bigger.", "Con voi to hơn.");
        insertWord(db, planetId, 1, "smaller", "nhỏ hơn", "/ˈsmɔːlər/", "📉", "comparative", 1,
            "The mouse is smaller.", "Con chuột nhỏ hơn.");
    }

    // Planet 13: Artopia Planet - Art & Creativity
    private void insertArtopiaWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "paint", "sơn, vẽ", "/peɪnt/", "🎨", "art", 1,
            "I like to paint.", "Tôi thích vẽ.");
        insertWord(db, planetId, 1, "brush", "cọ vẽ", "/brʌʃ/", "🖌️", "art", 1,
            "I use a brush to paint.", "Tôi dùng cọ để vẽ.");
        insertWord(db, planetId, 1, "crayon", "bút màu", "/ˈkreɪən/", "🖍️", "art", 1,
            "I draw with crayons.", "Tôi vẽ bằng bút màu.");
        insertWord(db, planetId, 1, "piano", "đàn piano", "/piˈænoʊ/", "🎹", "music", 1,
            "I play the piano.", "Tôi chơi đàn piano.");
        insertWord(db, planetId, 1, "guitar", "đàn ghi-ta", "/ɡɪˈtɑːr/", "🎸", "music", 1,
            "I like the guitar.", "Tôi thích đàn ghi-ta.");
        insertWord(db, planetId, 1, "draw", "vẽ", "/drɔː/", "✏️", "art", 1,
            "I can draw pictures.", "Tôi có thể vẽ tranh.");
        insertWord(db, planetId, 1, "sing", "hát", "/sɪŋ/", "🎤", "music", 1,
            "I like to sing.", "Tôi thích hát.");
        insertWord(db, planetId, 1, "dance", "nhảy múa", "/dæns/", "💃", "art", 1,
            "I can dance.", "Tôi có thể nhảy múa.");
        insertWord(db, planetId, 1, "drum", "trống", "/drʌm/", "🥁", "music", 1,
            "I play the drum.", "Tôi chơi trống.");
        insertWord(db, planetId, 1, "violin", "đàn vi-ô-lông", "/ˌvaɪəˈlɪn/", "🎻", "music", 1,
            "I play the violin.", "Tôi chơi đàn vi-ô-lông.");
        insertWord(db, planetId, 1, "picture", "bức tranh", "/ˈpɪktʃər/", "🖼️", "art", 1,
            "I draw a picture.", "Tôi vẽ một bức tranh.");
        insertWord(db, planetId, 1, "color", "màu sắc", "/ˈkʌlər/", "🌈", "art", 1,
            "I use many colors.", "Tôi dùng nhiều màu.");
        insertWord(db, planetId, 1, "paper", "giấy", "/ˈpeɪpər/", "📄", "art", 1,
            "I draw on paper.", "Tôi vẽ trên giấy.");
        insertWord(db, planetId, 1, "scissors", "kéo", "/ˈsɪzərz/", "✂️", "art", 1,
            "I cut with scissors.", "Tôi cắt bằng kéo.");
        insertWord(db, planetId, 1, "glue", "keo dán", "/ɡluː/", "🩹", "art", 1,
            "I use glue to stick.", "Tôi dùng keo để dán.");
        insertWord(db, planetId, 1, "clay", "đất sét", "/kleɪ/", "🧱", "art", 1,
            "I make shapes with clay.", "Tôi tạo hình bằng đất sét.");
        insertWord(db, planetId, 1, "sculpture", "tượng điêu khắc", "/ˈskʌlptʃər/", "🗿", "art", 1,
            "I make a sculpture.", "Tôi làm một bức tượng.");
        insertWord(db, planetId, 1, "music", "âm nhạc", "/ˈmjuːzɪk/", "🎵", "music", 1,
            "I love music.", "Tôi yêu âm nhạc.");
        insertWord(db, planetId, 1, "song", "bài hát", "/sɒŋ/", "🎶", "music", 1,
            "I sing a song.", "Tôi hát một bài hát.");
    }

    private void insertArtopiaSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "I like painting.",
            "Tôi thích vẽ tranh.", "like,painting", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I don't like singing.",
            "Tôi không thích hát.", "don't,like,singing", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Do you like to draw?",
            "Bạn có thích vẽ không?", "do,you,like,draw", "question");
        insertSentence(db, planetId, dialogueSceneId, "I like to draw pictures.",
            "Tôi thích vẽ tranh.", "like,draw,pictures", "pattern");
    }

    // Planet 14: Playground Park - Playground & Activities
    private void insertPlaygroundWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "swing", "xích đu", "/swɪŋ/", "🪑", "playground", 1,
            "I play on the swing.", "Tôi chơi xích đu.");
        insertWord(db, planetId, 1, "slide", "cầu trượt", "/slaɪd/", "🛝", "playground", 1,
            "The slide is fun.", "Cầu trượt rất vui.");
        insertWord(db, planetId, 1, "seesaw", "bập bênh", "/ˈsiːsɔː/", "⚖️", "playground", 1,
            "I play on the seesaw.", "Tôi chơi bập bênh.");
        insertWord(db, planetId, 1, "sandbox", "hộp cát", "/ˈsændbɒks/", "🏖️", "playground", 1,
            "I play in the sandbox.", "Tôi chơi trong hộp cát.");
        insertWord(db, planetId, 1, "playground", "sân chơi", "/ˈpleɪɡraʊnd/", "🎠", "playground", 1,
            "Let's go to the playground.", "Hãy đi đến sân chơi.");
        insertWord(db, planetId, 1, "run", "chạy", "/rʌn/", "🏃", "action", 1,
            "I can run fast.", "Tôi có thể chạy nhanh.");
        insertWord(db, planetId, 1, "jump", "nhảy", "/dʒʌmp/", "🤸", "action", 1,
            "I can jump high.", "Tôi có thể nhảy cao.");
        insertWord(db, planetId, 1, "play", "chơi", "/pleɪ/", "🎮", "action", 1,
            "Let's play together.", "Hãy chơi cùng nhau.");
        insertWord(db, planetId, 1, "climb", "leo", "/klaɪm/", "🧗", "action", 1,
            "I climb the ladder.", "Tôi leo thang.");
        insertWord(db, planetId, 1, "balance", "cân bằng", "/ˈbæləns/", "⚖️", "action", 1,
            "I balance on the beam.", "Tôi giữ thăng bằng trên xà.");
        insertWord(db, planetId, 1, "hide", "trốn", "/haɪd/", "🙈", "action", 1,
            "Let's play hide and seek.", "Hãy chơi trốn tìm.");
        insertWord(db, planetId, 1, "seek", "tìm", "/siːk/", "🔍", "action", 1,
            "I seek my friends.", "Tôi tìm bạn.");
        insertWord(db, planetId, 1, "tag", "đuổi bắt", "/tæɡ/", "🏃", "action", 1,
            "Let's play tag.", "Hãy chơi đuổi bắt.");
        insertWord(db, planetId, 1, "hopscotch", "nhảy lò cò", "/ˈhɒpskɒtʃ/", "🦘", "action", 1,
            "I play hopscotch.", "Tôi chơi nhảy lò cò.");
        insertWord(db, planetId, 1, "merry-go-round", "vòng quay", "/ˈmeri ɡoʊ raʊnd/", "🎠", "playground", 1,
            "I ride the merry-go-round.", "Tôi ngồi vòng quay.");
        insertWord(db, planetId, 1, "tunnel", "đường hầm", "/ˈtʌnl/", "🚇", "playground", 1,
            "I crawl through the tunnel.", "Tôi bò qua đường hầm.");
        insertWord(db, planetId, 1, "fence", "hàng rào", "/fens/", "🚧", "playground", 1,
            "The playground has a fence.", "Sân chơi có hàng rào.");
        insertWord(db, planetId, 1, "bench", "ghế dài", "/bentʃ/", "🪑", "playground", 1,
            "I sit on the bench.", "Tôi ngồi trên ghế dài.");
        insertWord(db, planetId, 1, "tree", "cây", "/triː/", "🌳", "playground", 1,
            "I climb the tree.", "Tôi leo cây.");
    }

    private void insertPlaygroundSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "Let's play on the swing.",
            "Hãy chơi xích đu.", "let's,play,swing", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Let's go to the slide.",
            "Hãy đi đến cầu trượt.", "let's,go,slide", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Let's play on the playground.",
            "Hãy chơi ở sân chơi.", "let's,play,playground", "pattern");
    }

    // Planet 15: School Academy - School & Learning
    private void insertSchoolAcademyWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "classroom", "lớp học", "/ˈklæsruːm/", "🏫", "school", 1,
            "I go to the classroom.", "Tôi đi đến lớp học.");
        insertWord(db, planetId, 1, "teacher", "giáo viên", "/ˈtiːtʃər/", "👨‍🏫", "school", 1,
            "The teacher is kind.", "Giáo viên tốt bụng.");
        insertWord(db, planetId, 1, "student", "học sinh", "/ˈstuːdənt/", "👨‍🎓", "school", 1,
            "I am a student.", "Tôi là học sinh.");
        insertWord(db, planetId, 1, "book", "sách", "/bʊk/", "📚", "school", 1,
            "I read a book.", "Tôi đọc sách.");
        insertWord(db, planetId, 1, "pencil", "bút chì", "/ˈpensl/", "✏️", "school", 1,
            "I write with a pencil.", "Tôi viết bằng bút chì.");
        insertWord(db, planetId, 1, "desk", "bàn học", "/desk/", "🪑", "school", 1,
            "I sit at my desk.", "Tôi ngồi ở bàn học.");
        insertWord(db, planetId, 1, "chair", "ghế", "/tʃer/", "🪑", "school", 1,
            "The chair is blue.", "Ghế màu xanh.");
        insertWord(db, planetId, 1, "school", "trường học", "/skuːl/", "🏫", "school", 1,
            "I go to school.", "Tôi đi học.");
        insertWord(db, planetId, 1, "notebook", "vở", "/ˈnoʊtbʊk/", "📓", "school", 1,
            "I write in my notebook.", "Tôi viết vào vở.");
        insertWord(db, planetId, 1, "eraser", "cục tẩy", "/ɪˈreɪsər/", "🧹", "school", 1,
            "I use an eraser.", "Tôi dùng cục tẩy.");
        insertWord(db, planetId, 1, "ruler", "thước kẻ", "/ˈruːlər/", "📏", "school", 1,
            "I measure with a ruler.", "Tôi đo bằng thước kẻ.");
        insertWord(db, planetId, 1, "backpack", "ba lô", "/ˈbækpæk/", "🎒", "school", 1,
            "I carry my backpack.", "Tôi mang ba lô.");
        insertWord(db, planetId, 1, "homework", "bài tập về nhà", "/ˈhoʊmwɜːrk/", "📝", "school", 1,
            "I do my homework.", "Tôi làm bài tập về nhà.");
        insertWord(db, planetId, 1, "test", "bài kiểm tra", "/test/", "📋", "school", 1,
            "I take a test.", "Tôi làm bài kiểm tra.");
        insertWord(db, planetId, 1, "grade", "điểm", "/ɡreɪd/", "⭐", "school", 1,
            "I get a good grade.", "Tôi được điểm tốt.");
        insertWord(db, planetId, 1, "lesson", "bài học", "/ˈlesn/", "📖", "school", 1,
            "I learn a lesson.", "Tôi học một bài.");
        insertWord(db, planetId, 1, "friend", "bạn", "/frend/", "👫", "school", 1,
            "I play with my friend.", "Tôi chơi với bạn.");
        insertWord(db, planetId, 1, "recess", "giờ ra chơi", "/ˈriːses/", "⏰", "school", 1,
            "I play during recess.", "Tôi chơi trong giờ ra chơi.");
        insertWord(db, planetId, 1, "lunch break", "giờ nghỉ trưa", "/lʌntʃ breɪk/", "🍽️", "school", 1,
            "I eat during lunch break.", "Tôi ăn trong giờ nghỉ trưa.");
    }

    private void insertSchoolAcademySentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "I have a book.",
            "Tôi có một quyển sách.", "have,book", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "She has a pencil.",
            "Cô ấy có một cây bút chì.", "has,pencil", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I have a red pencil.",
            "Tôi có một cây bút chì đỏ.", "have,red,pencil", "pattern");
    }

    // Planet 16: Body Parts Planet - Body Parts & Health
    private void insertBodyPartsWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "head", "đầu", "/hed/", "👤", "body", 1,
            "I nod my head.", "Tôi gật đầu.");
        insertWord(db, planetId, 1, "eyes", "mắt", "/aɪz/", "👁️", "body", 1,
            "I have two eyes.", "Tôi có hai mắt.");
        insertWord(db, planetId, 1, "nose", "mũi", "/noʊz/", "👃", "body", 1,
            "I smell with my nose.", "Tôi ngửi bằng mũi.");
        insertWord(db, planetId, 1, "mouth", "miệng", "/maʊθ/", "👄", "body", 1,
            "I eat with my mouth.", "Tôi ăn bằng miệng.");
        insertWord(db, planetId, 1, "hands", "tay", "/hændz/", "✋", "body", 1,
            "I wave my hands.", "Tôi vẫy tay.");
        insertWord(db, planetId, 1, "feet", "bàn chân", "/fiːt/", "🦶", "body", 1,
            "I have two feet.", "Tôi có hai bàn chân.");
        insertWord(db, planetId, 1, "body", "cơ thể", "/ˈbɒdi/", "👤", "body", 1,
            "My body is healthy.", "Cơ thể tôi khỏe mạnh.");
        insertWord(db, planetId, 1, "face", "khuôn mặt", "/feɪs/", "😊", "body", 1,
            "I wash my face.", "Tôi rửa mặt.");
        insertWord(db, planetId, 1, "ears", "tai", "/ɪrz/", "👂", "body", 1,
            "I have two ears.", "Tôi có hai tai.");
        insertWord(db, planetId, 1, "hair", "tóc", "/her/", "💇", "body", 1,
            "I brush my hair.", "Tôi chải tóc.");
        insertWord(db, planetId, 1, "teeth", "răng", "/tiːθ/", "🦷", "body", 1,
            "I brush my teeth.", "Tôi đánh răng.");
        insertWord(db, planetId, 1, "tongue", "lưỡi", "/tʌŋ/", "👅", "body", 1,
            "I taste with my tongue.", "Tôi nếm bằng lưỡi.");
        insertWord(db, planetId, 1, "shoulders", "vai", "/ˈʃoʊldərz/", "💪", "body", 1,
            "I shrug my shoulders.", "Tôi nhún vai.");
        insertWord(db, planetId, 1, "knees", "đầu gối", "/niːz/", "🦵", "body", 1,
            "I bend my knees.", "Tôi gập đầu gối.");
        insertWord(db, planetId, 1, "elbows", "khuỷu tay", "/ˈelboʊz/", "🦾", "body", 1,
            "I bend my elbows.", "Tôi gập khuỷu tay.");
        insertWord(db, planetId, 1, "fingers", "ngón tay", "/ˈfɪŋɡərz/", "👆", "body", 1,
            "I have ten fingers.", "Tôi có mười ngón tay.");
        insertWord(db, planetId, 1, "toes", "ngón chân", "/toʊz/", "🦶", "body", 1,
            "I have ten toes.", "Tôi có mười ngón chân.");
        insertWord(db, planetId, 1, "legs", "chân", "/leɡz/", "🦵", "body", 1,
            "I have two legs.", "Tôi có hai chân.");
        insertWord(db, planetId, 1, "arms", "cánh tay", "/ɑːrmz/", "💪", "body", 1,
            "I have two arms.", "Tôi có hai cánh tay.");
    }

    private void insertBodyPartsSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "My head is big.",
            "Đầu tôi to.", "my,head,big", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Her eyes are blue.",
            "Mắt cô ấy màu xanh.", "her,eyes,blue", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I have two hands and two feet.",
            "Tôi có hai tay và hai chân.", "have,hands,feet", "pattern");
    }

    // Planet 17: Sports Arena - Sports & Actions
    private void insertSportsArenaWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "football", "bóng đá", "/ˈfʊtbɔːl/", "⚽", "sport", 1,
            "I play football.", "Tôi chơi bóng đá.");
        insertWord(db, planetId, 1, "basketball", "bóng rổ", "/ˈbæskɪtbɔːl/", "🏀", "sport", 1,
            "Basketball is fun.", "Bóng rổ vui.");
        insertWord(db, planetId, 1, "swimming", "bơi lội", "/ˈswɪmɪŋ/", "🏊", "sport", 1,
            "I like swimming.", "Tôi thích bơi.");
        insertWord(db, planetId, 1, "running", "chạy bộ", "/ˈrʌnɪŋ/", "🏃", "sport", 1,
            "Running is good.", "Chạy bộ tốt.");
        insertWord(db, planetId, 1, "jumping", "nhảy", "/ˈdʒʌmpɪŋ/", "🤸", "sport", 1,
            "I like jumping.", "Tôi thích nhảy.");
        insertWord(db, planetId, 1, "ball", "quả bóng", "/bɔːl/", "⚽", "sport", 1,
            "I kick the ball.", "Tôi đá quả bóng.");
        insertWord(db, planetId, 1, "goal", "khung thành", "/ɡoʊl/", "🥅", "sport", 1,
            "I score a goal.", "Tôi ghi bàn.");
        insertWord(db, planetId, 1, "team", "đội", "/tiːm/", "👥", "sport", 1,
            "I play with my team.", "Tôi chơi với đội của tôi.");
        insertWord(db, planetId, 1, "tennis", "quần vợt", "/ˈtenɪs/", "🎾", "sport", 1,
            "I play tennis.", "Tôi chơi quần vợt.");
        insertWord(db, planetId, 1, "badminton", "cầu lông", "/ˈbædmɪntən/", "🏸", "sport", 1,
            "I play badminton.", "Tôi chơi cầu lông.");
        insertWord(db, planetId, 1, "volleyball", "bóng chuyền", "/ˈvɒlibɔːl/", "🏐", "sport", 1,
            "I play volleyball.", "Tôi chơi bóng chuyền.");
        insertWord(db, planetId, 1, "cycling", "đạp xe", "/ˈsaɪklɪŋ/", "🚴", "sport", 1,
            "I like cycling.", "Tôi thích đạp xe.");
        insertWord(db, planetId, 1, "skating", "trượt băng", "/ˈskeɪtɪŋ/", "⛸️", "sport", 1,
            "I go skating.", "Tôi đi trượt băng.");
        insertWord(db, planetId, 1, "win", "thắng", "/wɪn/", "🏆", "sport", 1,
            "I win the game.", "Tôi thắng trận đấu.");
        insertWord(db, planetId, 1, "lose", "thua", "/luːz/", "😢", "sport", 1,
            "I lose the game.", "Tôi thua trận đấu.");
        insertWord(db, planetId, 1, "practice", "luyện tập", "/ˈpræktɪs/", "🏃", "sport", 1,
            "I practice every day.", "Tôi luyện tập mỗi ngày.");
        insertWord(db, planetId, 1, "champion", "nhà vô địch", "/ˈtʃæmpiən/", "🥇", "sport", 1,
            "I am a champion.", "Tôi là nhà vô địch.");
        insertWord(db, planetId, 1, "medal", "huy chương", "/ˈmedl/", "🏅", "sport", 1,
            "I win a medal.", "Tôi thắng huy chương.");
        insertWord(db, planetId, 1, "trophy", "cúp", "/ˈtroʊfi/", "🏆", "sport", 1,
            "I get a trophy.", "Tôi nhận cúp.");
    }

    private void insertSportsArenaSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "I can play football.",
            "Tôi có thể chơi bóng đá.", "can,play,football", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I can't swim.",
            "Tôi không thể bơi.", "can't,swim", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I can run fast.",
            "Tôi có thể chạy nhanh.", "can,run,fast", "pattern");
    }

    // Planet 18: Birthday Party - Numbers & Celebrations
    private void insertBirthdayPartyWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "cake", "bánh kem", "/keɪk/", "🎂", "party", 1,
            "I eat birthday cake.", "Tôi ăn bánh sinh nhật.");
        insertWord(db, planetId, 1, "candle", "nến", "/ˈkændl/", "🕯️", "party", 1,
            "I blow out the candles.", "Tôi thổi nến.");
        insertWord(db, planetId, 1, "present", "quà tặng", "/ˈprezənt/", "🎁", "party", 1,
            "I get a present.", "Tôi nhận quà.");
        insertWord(db, planetId, 1, "balloon", "bóng bay", "/bəˈluːn/", "🎈", "party", 1,
            "I see balloons.", "Tôi thấy bóng bay.");
        insertWord(db, planetId, 1, "party", "bữa tiệc", "/ˈpɑːrti/", "🎉", "party", 1,
            "I go to a party.", "Tôi đi dự tiệc.");
        insertWord(db, planetId, 1, "happy", "vui", "/ˈhæpi/", "😊", "feeling", 1,
            "I am happy.", "Tôi vui.");
        insertWord(db, planetId, 1, "birthday", "sinh nhật", "/ˈbɜːθdeɪ/", "🎂", "party", 1,
            "Happy birthday!", "Chúc mừng sinh nhật!");
        insertWord(db, planetId, 1, "age", "tuổi", "/eɪdʒ/", "🎂", "party", 1,
            "How old are you?", "Bạn bao nhiêu tuổi?");
        insertWord(db, planetId, 1, "gift", "quà", "/ɡɪft/", "🎁", "party", 1,
            "I give a gift.", "Tôi tặng quà.");
        insertWord(db, planetId, 1, "card", "thiệp", "/kɑːrd/", "💌", "party", 1,
            "I write a birthday card.", "Tôi viết thiệp sinh nhật.");
        insertWord(db, planetId, 1, "decoration", "trang trí", "/ˌdekəˈreɪʃn/", "🎊", "party", 1,
            "I put up decorations.", "Tôi treo đồ trang trí.");
        insertWord(db, planetId, 1, "invitation", "lời mời", "/ˌɪnvɪˈteɪʃn/", "📧", "party", 1,
            "I send an invitation.", "Tôi gửi lời mời.");
        insertWord(db, planetId, 1, "guest", "khách", "/ɡest/", "👥", "party", 1,
            "I invite guests.", "Tôi mời khách.");
        insertWord(db, planetId, 1, "celebrate", "ăn mừng", "/ˈselɪbreɪt/", "🎉", "party", 1,
            "We celebrate together.", "Chúng tôi ăn mừng cùng nhau.");
        insertWord(db, planetId, 1, "wish", "ước", "/wɪʃ/", "✨", "party", 1,
            "I make a wish.", "Tôi ước một điều ước.");
        insertWord(db, planetId, 1, "surprise", "bất ngờ", "/sərˈpraɪz/", "🎁", "party", 1,
            "It's a surprise!", "Đó là một bất ngờ!");
        insertWord(db, planetId, 1, "fun", "vui", "/fʌn/", "😄", "party", 1,
            "The party is fun.", "Bữa tiệc rất vui.");
        insertWord(db, planetId, 1, "music", "nhạc", "/ˈmjuːzɪk/", "🎵", "party", 1,
            "We play music.", "Chúng tôi bật nhạc.");
        insertWord(db, planetId, 1, "dance", "nhảy", "/dæns/", "💃", "party", 1,
            "We dance at the party.", "Chúng tôi nhảy ở bữa tiệc.");
    }

    private void insertBirthdayPartySentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "How old are you?",
            "Bạn bao nhiêu tuổi?", "how,old,are,you", "question");
        insertSentence(db, planetId, dialogueSceneId, "I'm 5 years old.",
            "Tôi 5 tuổi.", "years,old", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I am 6 years old today.",
            "Hôm nay tôi 6 tuổi.", "am,years,old,today", "pattern");
    }

    // Planet 19: Ocean Deep - Sea Creatures & Ocean
    private void insertOceanDeepWords(SQLiteDatabase db, long planetId) {
        insertWord(db, planetId, 1, "fish", "con cá", "/fɪʃ/", "🐟", "ocean", 1,
            "I see a fish.", "Tôi thấy một con cá.");
        insertWord(db, planetId, 1, "shark", "cá mập", "/ʃɑːrk/", "🦈", "ocean", 1,
            "The shark is big.", "Cá mập to.");
        insertWord(db, planetId, 1, "dolphin", "cá heo", "/ˈdɒlfɪn/", "🐬", "ocean", 1,
            "The dolphin is smart.", "Cá heo thông minh.");
        insertWord(db, planetId, 1, "starfish", "sao biển", "/ˈstɑːrfɪʃ/", "⭐", "ocean", 1,
            "I see a starfish.", "Tôi thấy sao biển.");
        insertWord(db, planetId, 1, "coral", "san hô", "/ˈkɒrəl/", "🪸", "ocean", 1,
            "The coral is colorful.", "San hô nhiều màu.");
        insertWord(db, planetId, 1, "sea", "biển", "/siː/", "🌊", "ocean", 1,
            "I swim in the sea.", "Tôi bơi trong biển.");
        insertWord(db, planetId, 1, "ocean", "đại dương", "/ˈoʊʃən/", "🌊", "ocean", 1,
            "The ocean is big.", "Đại dương rất lớn.");
        insertWord(db, planetId, 1, "water", "nước", "/ˈwɔːtər/", "💧", "ocean", 1,
            "The water is blue.", "Nước màu xanh.");
        insertWord(db, planetId, 1, "whale", "cá voi", "/weɪl/", "🐋", "ocean", 1,
            "The whale is huge.", "Cá voi rất lớn.");
        insertWord(db, planetId, 1, "octopus", "bạch tuộc", "/ˈɒktəpəs/", "🐙", "ocean", 1,
            "The octopus has eight arms.", "Bạch tuộc có tám cánh tay.");
        insertWord(db, planetId, 1, "jellyfish", "sứa", "/ˈdʒelifɪʃ/", "🎐", "ocean", 1,
            "The jellyfish is transparent.", "Sứa trong suốt.");
        insertWord(db, planetId, 1, "crab", "cua", "/kræb/", "🦀", "ocean", 1,
            "The crab walks sideways.", "Con cua đi ngang.");
        insertWord(db, planetId, 1, "lobster", "tôm hùm", "/ˈlɒbstər/", "🦞", "ocean", 1,
            "The lobster is red.", "Tôm hùm màu đỏ.");
        insertWord(db, planetId, 1, "seahorse", "cá ngựa", "/ˈsiːhɔːrs/", "🐴", "ocean", 1,
            "The seahorse is small.", "Cá ngựa nhỏ.");
        insertWord(db, planetId, 1, "turtle", "rùa biển", "/ˈtɜːrtl/", "🐢", "ocean", 1,
            "The turtle swims slowly.", "Rùa biển bơi chậm.");
        insertWord(db, planetId, 1, "shell", "vỏ sò", "/ʃel/", "🐚", "ocean", 1,
            "I collect shells.", "Tôi thu thập vỏ sò.");
        insertWord(db, planetId, 1, "beach", "bãi biển", "/biːtʃ/", "🏖️", "ocean", 1,
            "I play on the beach.", "Tôi chơi trên bãi biển.");
        insertWord(db, planetId, 1, "wave", "sóng", "/weɪv/", "🌊", "ocean", 1,
            "The wave is big.", "Sóng rất lớn.");
        insertWord(db, planetId, 1, "sand", "cát", "/sænd/", "🏖️", "ocean", 1,
            "I play in the sand.", "Tôi chơi trong cát.");
    }

    private void insertOceanDeepSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "There is a fish.",
            "Có một con cá.", "there,is,fish", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "There are many dolphins.",
            "Có nhiều cá heo.", "there,are,many,dolphins", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "There are many fish in the ocean.",
            "Có nhiều cá trong đại dương.", "there,are,many,fish,ocean", "pattern");
    }

    // Planet 3: Animania Wild - Animals & Abilities
    private void insertAnimaniaSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "The dog can run.",
            "Con chó có thể chạy.", "dog,can,run", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "The cat can jump.",
            "Con mèo có thể nhảy.", "cat,can,jump", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Can the bird fly?",
            "Con chim có thể bay không?", "can,bird,fly", "question");
        insertSentence(db, planetId, dialogueSceneId, "The fish can swim.",
            "Con cá có thể bơi.", "fish,can,swim", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I can see a lion.",
            "Tôi có thể thấy một con sư tử.", "can,see,lion", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "The elephant can't fly.",
            "Con voi không thể bay.", "elephant,can't,fly", "pattern");
    }

    // Planet 4: Numberia Station - Numbers & Counting
    private void insertNumberiaSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "How many apples are there?",
            "Có bao nhiêu quả táo?", "how,many,apples", "question");
        insertSentence(db, planetId, dialogueSceneId, "I have three books.",
            "Tôi có ba quyển sách.", "have,three,books", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "There are five stars.",
            "Có năm ngôi sao.", "there,are,five,stars", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Count the numbers.",
            "Đếm các số.", "count,numbers", "command");
        insertSentence(db, planetId, dialogueSceneId, "I see ten fingers.",
            "Tôi thấy mười ngón tay.", "see,ten,fingers", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "How many do you have?",
            "Bạn có bao nhiêu?", "how,many,have", "question");
    }

    // Planet 5: Citytron Nova - City & Directions
    private void insertCitytronSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "Where is the school?",
            "Trường học ở đâu?", "where,school", "question");
        insertSentence(db, planetId, dialogueSceneId, "There is a park near here.",
            "Có một công viên gần đây.", "there,is,park,near", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Go straight and turn left.",
            "Đi thẳng và rẽ trái.", "go,straight,turn,left", "command");
        insertSentence(db, planetId, dialogueSceneId, "The hospital is next to the school.",
            "Bệnh viện ở bên cạnh trường học.", "hospital,next,school", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "There are many buildings.",
            "Có nhiều tòa nhà.", "there,are,many,buildings", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Can you show me the way?",
            "Bạn có thể chỉ đường cho tôi không?", "can,show,way", "question");
    }

    // Planet 6: Foodora Station - Food & Shopping
    private void insertFoodoraSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "I would like an apple.",
            "Tôi muốn một quả táo.", "would,like,apple", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "How much is the bread?",
            "Bánh mì bao nhiêu tiền?", "how,much,bread", "question");
        insertSentence(db, planetId, dialogueSceneId, "I want some milk.",
            "Tôi muốn một ít sữa.", "want,some,milk", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Can I have a sandwich?",
            "Tôi có thể có một cái bánh sandwich không?", "can,have,sandwich", "question");
        insertSentence(db, planetId, dialogueSceneId, "I like pizza.",
            "Tôi thích pizza.", "like,pizza", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "How many eggs do you need?",
            "Bạn cần bao nhiêu quả trứng?", "how,many,eggs,need", "question");
    }

    // Planet 7: Weatheron Sky - Weather & Clothes
    private void insertWeatheronSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "It's sunny today.",
            "Hôm nay trời nắng.", "sunny,today", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I wear a coat because it's cold.",
            "Tôi mặc áo khoác vì trời lạnh.", "wear,coat,because,cold", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "It's raining, so I use an umbrella.",
            "Trời đang mưa, nên tôi dùng ô.", "raining,so,umbrella", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "What's the weather like?",
            "Thời tiết như thế nào?", "what,weather,like", "question");
        insertSentence(db, planetId, dialogueSceneId, "I wear shorts when it's hot.",
            "Tôi mặc quần đùi khi trời nóng.", "wear,shorts,when,hot", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Put on your jacket.",
            "Mặc áo khoác vào.", "put,jacket", "command");
    }

    // Planet 8: Familia Home - Family & Home
    private void insertFamiliaSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "This is my family.",
            "Đây là gia đình tôi.", "this,my,family", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "My father is tall.",
            "Bố tôi cao.", "my,father,tall", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Her mother is kind.",
            "Mẹ cô ấy tốt bụng.", "her,mother,kind", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I love my sister.",
            "Tôi yêu em gái tôi.", "love,my,sister", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Where is your brother?",
            "Anh trai bạn ở đâu?", "where,your,brother", "question");
        insertSentence(db, planetId, dialogueSceneId, "This is our house.",
            "Đây là nhà của chúng tôi.", "this,our,house", "pattern");
    }

    // Planet 9: RoboLab Command - Robot & Commands
    private void insertRobolabSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "Walk forward.",
            "Đi về phía trước.", "walk,forward", "command");
        insertSentence(db, planetId, dialogueSceneId, "Turn right and stop.",
            "Rẽ phải và dừng lại.", "turn,right,stop", "command");
        insertSentence(db, planetId, dialogueSceneId, "Pick up the box.",
            "Nhặt hộp lên.", "pick,up,box", "command");
        insertSentence(db, planetId, dialogueSceneId, "Jump three times.",
            "Nhảy ba lần.", "jump,three,times", "command");
        insertSentence(db, planetId, dialogueSceneId, "Follow the line.",
            "Đi theo đường thẳng.", "follow,line", "command");
        insertSentence(db, planetId, dialogueSceneId, "Do not touch the wall.",
            "Không được chạm vào tường.", "do,not,touch,wall", "command");
    }

    // Planet 10: TimeLapse Base - Time & Routines
    private void insertTimelapseSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "I wake up at seven o'clock.",
            "Tôi thức dậy lúc bảy giờ.", "wake,up,seven,o'clock", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I brush my teeth every morning.",
            "Tôi đánh răng mỗi buổi sáng.", "brush,teeth,every,morning", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "What time do you eat breakfast?",
            "Bạn ăn sáng lúc mấy giờ?", "what,time,eat,breakfast", "question");
        insertSentence(db, planetId, dialogueSceneId, "I go to school at eight.",
            "Tôi đi học lúc tám giờ.", "go,school,eight", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I play after school.",
            "Tôi chơi sau giờ học.", "play,after,school", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I sleep at nine o'clock.",
            "Tôi ngủ lúc chín giờ.", "sleep,nine,o'clock", "pattern");
    }

    // Planet 11: Storyverse Galaxy - Storytelling
    private void insertStoryverseSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "Once upon a time, there was a princess.",
            "Ngày xửa ngày xưa, có một công chúa.", "once,upon,time,princess", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "The prince went to the castle.",
            "Hoàng tử đi đến lâu đài.", "prince,went,castle", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "She found a magic key.",
            "Cô ấy tìm thấy một chìa khóa phép thuật.", "found,magic,key", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "What happened next?",
            "Chuyện gì xảy ra tiếp theo?", "what,happened,next", "question");
        insertSentence(db, planetId, dialogueSceneId, "They lived happily ever after.",
            "Họ sống hạnh phúc mãi mãi về sau.", "lived,happily,ever,after", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "The dragon flew away.",
            "Con rồng bay đi.", "dragon,flew,away", "pattern");
    }

    // Planet 12: Natura Wilderness - Nature & Environment
    private void insertNaturaSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        long dialogueSceneId = 3;
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        insertSentence(db, planetId, dialogueSceneId, "The tree is taller than the flower.",
            "Cây cao hơn bông hoa.", "tree,taller,than,flower", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Mountains are bigger than hills.",
            "Núi lớn hơn đồi.", "mountains,bigger,than,hills", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Which is longer, the river or the stream?",
            "Cái nào dài hơn, sông hay suối?", "which,longer,river,stream", "question");
        insertSentence(db, planetId, dialogueSceneId, "The ocean is deeper than the lake.",
            "Đại dương sâu hơn hồ.", "ocean,deeper,than,lake", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Protect the forest.",
            "Bảo vệ rừng.", "protect,forest", "command");
        insertSentence(db, planetId, dialogueSceneId, "The sun is brighter than the moon.",
            "Mặt trời sáng hơn mặt trăng.", "sun,brighter,than,moon", "pattern");
    }

    private void insertWord(SQLiteDatabase db, long planetId, long sceneId,
            String english, String vietnamese, String pronunciation, String emoji,
            String category, int difficulty, String example, String exampleVi) {
        ContentValues values = new ContentValues();
        values.put("planet_id", planetId);
        values.put("scene_id", sceneId);
        values.put("english", english);
        values.put("vietnamese", vietnamese);
        values.put("pronunciation", pronunciation);
        values.put("emoji", emoji);
        values.put("category", category);
        values.put("difficulty", difficulty);
        values.put("example_sentence", example);
        values.put("example_translation", exampleVi);
        db.insert(TABLE_WORDS, null, values);
    }

    private void insertColoriaSentences(SQLiteDatabase db, long planetId) {
        // Get scene IDs for Coloria Prime
        // Scene 1: landing_zone, Scene 2: explore_area, Scene 3: dialogue_dock
        // We need to insert sentences for dialogue_dock (scene 3)
        // Since scenes are inserted in order, dialogue_dock should be the 3rd scene
        // We'll use a query to find the dialogue_dock scene
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        
        long dialogueSceneId = 3; // Default fallback
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        // Insert sentences for dialogue_dock scene
        insertSentence(db, planetId, dialogueSceneId, "It's a blue triangle.",
            "Đây là một hình tam giác xanh.", "blue,triangle", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "I can see three stars.",
            "Tôi có thể thấy ba ngôi sao.", "can,see,stars", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Which one is bigger?",
            "Cái nào lớn hơn?", "which,bigger", "question");
        insertSentence(db, planetId, dialogueSceneId, "The red circle is small.",
            "Hình tròn đỏ thì nhỏ.", "red,circle,small", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Choose the green square.",
            "Chọn hình vuông xanh lá.", "choose,green,square", "command");
    }

    private void insertToytopiaSentences(SQLiteDatabase db, long planetId) {
        // Get scene ID for Toytopia Orbit dialogue_dock scene
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "dialogue_dock"}, null, null, null);
        
        long dialogueSceneId = 3; // Default fallback
        if (sceneCursor.moveToFirst()) {
            dialogueSceneId = sceneCursor.getLong(0);
        }
        sceneCursor.close();
        
        // Insert sentences for dialogue_dock scene - Toys & Prepositions
        insertSentence(db, planetId, dialogueSceneId, "The ball is in the box.",
            "Quả bóng ở trong hộp.", "ball,in,box", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "The doll is on the table.",
            "Búp bê ở trên bàn.", "doll,on,table", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "The car is under the bed.",
            "Ô tô ở dưới giường.", "car,under,bed", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Where is the teddy?",
            "Gấu bông ở đâu?", "where,teddy", "question");
        insertSentence(db, planetId, dialogueSceneId, "The robot is behind the chair.",
            "Người máy ở phía sau ghế.", "robot,behind,chair", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "Put the train on the track.",
            "Đặt tàu hỏa lên đường ray.", "put,train,on,track", "command");
        insertSentence(db, planetId, dialogueSceneId, "I can see the kite in the sky.",
            "Tôi có thể thấy con diều trên trời.", "can,see,kite,sky", "pattern");
        insertSentence(db, planetId, dialogueSceneId, "The puzzle is on the floor.",
            "Xếp hình ở trên sàn.", "puzzle,on,floor", "pattern");
    }

    // Insert sentences for puzzle zones
    private void insertPuzzleZoneSentences(SQLiteDatabase db) {
        // Planet 1: Coloria Prime - Puzzle Zone
        insertPuzzleSentencesForPlanet(db, 1, "puzzle_zone");
        
        // Planet 2: Toytopia Orbit - Toy Puzzle
        insertPuzzleSentencesForPlanet(db, 2, "puzzle_zone");
        
        // Planet 3: Animania Wild - Animal Match
        insertPuzzleSentencesForPlanet(db, 3, "puzzle_zone");
        
        // Planet 4: Citytron Nova - Map Puzzle
        insertPuzzleSentencesForPlanet(db, 4, "puzzle_zone");
        
        // Planet 5: Foodora Station - Recipe Puzzle
        insertPuzzleSentencesForPlanet(db, 5, "puzzle_zone");
        
        // Planet 6: Weatheron Sky - Dress Up
        insertPuzzleSentencesForPlanet(db, 6, "puzzle_zone");
        
        // Planet 7: RoboLab Command - Command Chain
        insertPuzzleSentencesForPlanet(db, 7, "puzzle_zone");
        
        // Planet 8: TimeLapse Base - Schedule Fix
        insertPuzzleSentencesForPlanet(db, 8, "puzzle_zone");
        
        // Planet 9: Storyverse Galaxy - Story Order
        insertPuzzleSentencesForPlanet(db, 9, "puzzle_zone");
        
        // Planet 10: TimeLapse Base - Schedule Fix
        insertPuzzleSentencesForPlanet(db, 10, "puzzle_zone");
        
        // Planet 11: Storyverse Galaxy - Story Order
        insertPuzzleSentencesForPlanet(db, 11, "puzzle_zone");
        
        // Planet 12: Natura Wilderness - Ecosystem
        insertPuzzleSentencesForPlanet(db, 12, "puzzle_zone");
        
        // Planet 13: Artopia Planet - Art Puzzle
        insertArtPuzzleSentences(db, 13);
        
        // Planet 14: Playground Park - Play Puzzle
        insertPlayPuzzleSentences(db, 14);
        
        // Planet 15: School Academy - School Puzzle
        insertSchoolPuzzleSentences(db, 15);
        
        // Planet 16: Body Parts Planet - Body Puzzle
        insertBodyPuzzleSentences(db, 16);
        
        // Planet 17: Sports Arena - Sports Puzzle
        insertSportsPuzzleSentences(db, 17);
        
        // Planet 18: Birthday Party - Party Puzzle
        insertPartyPuzzleSentences(db, 18);
        
        // Planet 19: Ocean Deep - Ocean Puzzle
        insertOceanPuzzleSentences(db, 19);
    }

    private void insertPuzzleSentencesForPlanet(SQLiteDatabase db, long planetId, String sceneKey) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), sceneKey}, null, null, null);
        
        if (!sceneCursor.moveToFirst()) {
            sceneCursor.close();
            return;
        }
        
        long puzzleSceneId = sceneCursor.getLong(0);
        sceneCursor.close();
        
        // Get dialogue sentences for this planet and reuse them for puzzle
        Cursor dialogueCursor = db.query(TABLE_SENTENCES, null,
            "planet_id = ?", new String[]{String.valueOf(planetId)}, null, null, null);
        
        int count = 0;
        while (dialogueCursor.moveToNext() && count < 7) {
            String english = dialogueCursor.getString(dialogueCursor.getColumnIndexOrThrow("english"));
            String vietnamese = dialogueCursor.getString(dialogueCursor.getColumnIndexOrThrow("vietnamese"));
            String keywords = dialogueCursor.getString(dialogueCursor.getColumnIndexOrThrow("keywords"));
            String type = dialogueCursor.getString(dialogueCursor.getColumnIndexOrThrow("sentence_type"));
            
            insertSentence(db, planetId, puzzleSceneId, english, vietnamese, keywords, type);
            count++;
        }
        dialogueCursor.close();
    }

    private void insertArtPuzzleSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "puzzle_zone"}, null, null, null);
        
        if (!sceneCursor.moveToFirst()) {
            sceneCursor.close();
            return;
        }
        
        long puzzleSceneId = sceneCursor.getLong(0);
        sceneCursor.close();
        
        insertSentence(db, planetId, puzzleSceneId, "I like to draw pictures.",
            "Tôi thích vẽ tranh.", "like,draw,pictures", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I like painting.",
            "Tôi thích vẽ tranh.", "like,painting", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I don't like singing.",
            "Tôi không thích hát.", "don't,like,singing", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "Do you like to draw?",
            "Bạn có thích vẽ không?", "do,you,like,draw", "question");
        insertSentence(db, planetId, puzzleSceneId, "I can play the piano.",
            "Tôi có thể chơi đàn piano.", "can,play,piano", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I like to color.",
            "Tôi thích tô màu.", "like,color", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "Let's make art together.",
            "Hãy làm nghệ thuật cùng nhau.", "let's,make,art,together", "pattern");
    }

    private void insertPlayPuzzleSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "puzzle_zone"}, null, null, null);
        
        if (!sceneCursor.moveToFirst()) {
            sceneCursor.close();
            return;
        }
        
        long puzzleSceneId = sceneCursor.getLong(0);
        sceneCursor.close();
        
        insertSentence(db, planetId, puzzleSceneId, "Let's play on the playground.",
            "Hãy chơi ở sân chơi.", "let's,play,playground", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "Let's play on the swing.",
            "Hãy chơi xích đu.", "let's,play,swing", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "Let's go to the slide.",
            "Hãy đi đến cầu trượt.", "let's,go,slide", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I can run fast.",
            "Tôi có thể chạy nhanh.", "can,run,fast", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I can jump high.",
            "Tôi có thể nhảy cao.", "can,jump,high", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "Let's play together.",
            "Hãy chơi cùng nhau.", "let's,play,together", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I play in the sandbox.",
            "Tôi chơi trong hộp cát.", "play,sandbox", "pattern");
    }

    private void insertSchoolPuzzleSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "puzzle_zone"}, null, null, null);
        
        if (!sceneCursor.moveToFirst()) {
            sceneCursor.close();
            return;
        }
        
        long puzzleSceneId = sceneCursor.getLong(0);
        sceneCursor.close();
        
        insertSentence(db, planetId, puzzleSceneId, "I have a red pencil.",
            "Tôi có một cây bút chì đỏ.", "have,red,pencil", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I have a book.",
            "Tôi có một quyển sách.", "have,book", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "She has a pencil.",
            "Cô ấy có một cây bút chì.", "has,pencil", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I write with a pencil.",
            "Tôi viết bằng bút chì.", "write,pencil", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I read a book.",
            "Tôi đọc sách.", "read,book", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I go to school.",
            "Tôi đi học.", "go,school", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I sit at my desk.",
            "Tôi ngồi ở bàn học.", "sit,desk", "pattern");
    }

    private void insertBodyPuzzleSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "puzzle_zone"}, null, null, null);
        
        if (!sceneCursor.moveToFirst()) {
            sceneCursor.close();
            return;
        }
        
        long puzzleSceneId = sceneCursor.getLong(0);
        sceneCursor.close();
        
        insertSentence(db, planetId, puzzleSceneId, "I have two hands and two feet.",
            "Tôi có hai tay và hai chân.", "have,hands,feet", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "My head is big.",
            "Đầu tôi to.", "my,head,big", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "Her eyes are blue.",
            "Mắt cô ấy màu xanh.", "her,eyes,blue", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I have two eyes.",
            "Tôi có hai mắt.", "have,eyes", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I have two ears.",
            "Tôi có hai tai.", "have,ears", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I brush my teeth.",
            "Tôi đánh răng.", "brush,teeth", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I wash my face.",
            "Tôi rửa mặt.", "wash,face", "pattern");
    }

    private void insertSportsPuzzleSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "puzzle_zone"}, null, null, null);
        
        if (!sceneCursor.moveToFirst()) {
            sceneCursor.close();
            return;
        }
        
        long puzzleSceneId = sceneCursor.getLong(0);
        sceneCursor.close();
        
        insertSentence(db, planetId, puzzleSceneId, "I can run fast.",
            "Tôi có thể chạy nhanh.", "can,run,fast", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I can play football.",
            "Tôi có thể chơi bóng đá.", "can,play,football", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I can't swim.",
            "Tôi không thể bơi.", "can't,swim", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I play basketball.",
            "Tôi chơi bóng rổ.", "play,basketball", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I kick the ball.",
            "Tôi đá quả bóng.", "kick,ball", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I score a goal.",
            "Tôi ghi bàn.", "score,goal", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I practice every day.",
            "Tôi luyện tập mỗi ngày.", "practice,every,day", "pattern");
    }

    private void insertPartyPuzzleSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "puzzle_zone"}, null, null, null);
        
        if (!sceneCursor.moveToFirst()) {
            sceneCursor.close();
            return;
        }
        
        long puzzleSceneId = sceneCursor.getLong(0);
        sceneCursor.close();
        
        insertSentence(db, planetId, puzzleSceneId, "I am 6 years old today.",
            "Hôm nay tôi 6 tuổi.", "am,years,old,today", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I'm 5 years old.",
            "Tôi 5 tuổi.", "years,old", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "How old are you?",
            "Bạn bao nhiêu tuổi?", "how,old,are,you", "question");
        insertSentence(db, planetId, puzzleSceneId, "I eat birthday cake.",
            "Tôi ăn bánh sinh nhật.", "eat,birthday,cake", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I blow out the candles.",
            "Tôi thổi nến.", "blow,candles", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I get a present.",
            "Tôi nhận quà.", "get,present", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "Happy birthday!",
            "Chúc mừng sinh nhật!", "happy,birthday", "pattern");
    }

    private void insertOceanPuzzleSentences(SQLiteDatabase db, long planetId) {
        Cursor sceneCursor = db.query(TABLE_SCENES, new String[]{"id"}, 
            "planet_id = ? AND scene_key = ?", 
            new String[]{String.valueOf(planetId), "puzzle_zone"}, null, null, null);
        
        if (!sceneCursor.moveToFirst()) {
            sceneCursor.close();
            return;
        }
        
        long puzzleSceneId = sceneCursor.getLong(0);
        sceneCursor.close();
        
        insertSentence(db, planetId, puzzleSceneId, "There are many fish in the ocean.",
            "Có nhiều cá trong đại dương.", "there,are,many,fish,ocean", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "There is a fish.",
            "Có một con cá.", "there,is,fish", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "There are many dolphins.",
            "Có nhiều cá heo.", "there,are,many,dolphins", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I see a fish.",
            "Tôi thấy một con cá.", "see,fish", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "The shark is big.",
            "Cá mập to.", "shark,big", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I swim in the sea.",
            "Tôi bơi trong biển.", "swim,sea", "pattern");
        insertSentence(db, planetId, puzzleSceneId, "I play on the beach.",
            "Tôi chơi trên bãi biển.", "play,beach", "pattern");
    }

    private void insertSentence(SQLiteDatabase db, long planetId, long sceneId,
            String english, String vietnamese, String keywords, String type) {
        ContentValues values = new ContentValues();
        values.put("planet_id", planetId);
        values.put("scene_id", sceneId);
        values.put("english", english);
        values.put("vietnamese", vietnamese);
        values.put("keywords", keywords);
        values.put("sentence_type", type);
        db.insert(TABLE_SENTENCES, null, values);
    }

    private void insertBadges(SQLiteDatabase db) {
        insertBadge(db, "first_star", "Ngôi Sao Đầu Tiên", "⭐", "Đạt được 1 sao đầu tiên", "stars", 1);
        insertBadge(db, "explorer", "Nhà Thám Hiểm", "🚀", "Hoàn thành 1 hành tinh", "planets", 1);
        insertBadge(db, "word_collector", "Nhà Sưu Tập Từ", "📚", "Học 50 từ vựng", "words", 50);
        insertBadge(db, "star_hunter", "Thợ Săn Sao", "🌟", "Đạt 100 sao", "stars", 100);
        insertBadge(db, "boss_slayer", "Chiến Binh Boss", "👾", "Đánh bại 5 Boss", "bosses", 5);
        insertBadge(db, "streak_master", "Bền Bỉ", "🔥", "Chơi 7 ngày liên tiếp", "streak", 7);
        insertBadge(db, "crystal_master", "Vua Pha Lê", "💎", "Thu thập 100 crystals", "crystals", 100);
        insertBadge(db, "perfect_score", "Hoàn Hảo", "🏆", "Đạt 100% một màn", "perfect", 1);
        insertBadge(db, "galaxy_hero", "Anh Hùng Ngân Hà", "🦸", "Hoàn thành 9 hành tinh", "planets", 9);
        insertBadge(db, "vocabulary_master", "Bậc Thầy Từ Vựng", "🎓", "Học 200 từ", "words", 200);
    }

    private void insertBadge(SQLiteDatabase db, String key, String name, String emoji,
            String description, String reqType, int reqValue) {
        ContentValues values = new ContentValues();
        values.put("badge_key", key);
        values.put("name", name);
        values.put("name_vi", name);
        values.put("emoji", emoji);
        values.put("description", description);
        values.put("requirement_type", reqType);
        values.put("requirement_value", reqValue);
        db.insert(TABLE_BADGES, null, values);
    }

    // ============ QUERY METHODS ============

    public List<PlanetData> getAllPlanets() {
        ensurePlanetsSeeded(getWritableDatabase());
        List<PlanetData> planets = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANETS, null, null, null, null, null, "order_index ASC");

        while (cursor.moveToNext()) {
            planets.add(cursorToPlanet(cursor));
        }
        cursor.close();
        return planets;
    }

    public PlanetData getPlanetById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANETS, null, "id = ?",
            new String[]{String.valueOf(id)}, null, null, null);

        PlanetData planet = null;
        if (cursor.moveToFirst()) {
            planet = cursorToPlanet(cursor);
        }
        cursor.close();
        return planet;
    }

    public PlanetData getPlanetByKey(String planetKey) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANETS, null, "planet_key = ?",
            new String[]{planetKey}, null, null, null);

        PlanetData planet = null;
        if (cursor.moveToFirst()) {
            planet = cursorToPlanet(cursor);
        }
        cursor.close();
        return planet;
    }

    public List<SceneData> getScenesForPlanet(int planetId) {
        List<SceneData> scenes = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SCENES, null, "planet_id = ?",
            new String[]{String.valueOf(planetId)}, null, null, "order_index ASC");

        while (cursor.moveToNext()) {
            scenes.add(cursorToScene(cursor));
        }
        cursor.close();
        return scenes;
    }

    public List<WordData> getWordsForPlanet(int planetId) {
        List<WordData> words = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_WORDS, null, "planet_id = ?",
            new String[]{String.valueOf(planetId)}, null, null, null);

        while (cursor.moveToNext()) {
            words.add(cursorToWord(cursor));
        }
        cursor.close();
        return words;
    }

    public List<WordData> getLearnedWords() {
        List<WordData> words = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_WORDS, null, "is_learned = ?",
            new String[]{"1"}, null, null, null);

        while (cursor.moveToNext()) {
            words.add(cursorToWord(cursor));
        }
        cursor.close();
        return words;
    }

    public List<SentenceData> getSentencesForPlanet(int planetId) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"GameDatabaseHelper.getSentencesForPlanet:1061\",\"message\":\"Query entry\",\"data\":{\"planetId\":" + planetId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        List<SentenceData> sentences = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SENTENCES, null, "planet_id = ?",
            new String[]{String.valueOf(planetId)}, null, null, null);
        
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"GameDatabaseHelper.getSentencesForPlanet:1065\",\"message\":\"Cursor result\",\"data\":{\"planetId\":" + planetId + ",\"count\":" + cursor.getCount() + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion

        while (cursor.moveToNext()) {
            sentences.add(cursorToSentence(cursor));
        }
        cursor.close();
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"GameDatabaseHelper.getSentencesForPlanet:1071\",\"message\":\"Query exit\",\"data\":{\"planetId\":" + planetId + ",\"resultCount\":" + sentences.size() + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        return sentences;
    }
    
    public List<SentenceData> getSentencesForScene(int sceneId) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"GameDatabaseHelper.getSentencesForScene:1074\",\"message\":\"Query entry\",\"data\":{\"sceneId\":" + sceneId + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        List<SentenceData> sentences = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SENTENCES, null, "scene_id = ?",
            new String[]{String.valueOf(sceneId)}, null, null, null);
        
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"GameDatabaseHelper.getSentencesForScene:1078\",\"message\":\"Cursor result\",\"data\":{\"sceneId\":" + sceneId + ",\"count\":" + cursor.getCount() + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion

        while (cursor.moveToNext()) {
            sentences.add(cursorToSentence(cursor));
        }
        cursor.close();
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"GameDatabaseHelper.getSentencesForScene:1084\",\"message\":\"Query exit\",\"data\":{\"sceneId\":" + sceneId + ",\"resultCount\":" + sentences.size() + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        return sentences;
    }

    public UserProgressData getUserProgress() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER_PROGRESS, null, "user_id = ?",
            new String[]{"default"}, null, null, null);

        UserProgressData progress = null;
        if (cursor.moveToFirst()) {
            progress = cursorToUserProgress(cursor);
        }
        cursor.close();
        return progress;
    }

    public void updateUserProgress(int stars, int fuelCells, int crystals, int wordsLearned) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("total_stars", stars);
        values.put("total_fuel_cells", fuelCells);
        values.put("total_crystals", crystals);
        values.put("words_learned", wordsLearned);
        db.update(TABLE_USER_PROGRESS, values, "user_id = ?", new String[]{"default"});
    }

    public void addStars(int starsToAdd) {
        UserProgressData progress = getUserProgress();
        if (progress != null) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("total_stars", progress.totalStars + starsToAdd);
            db.update(TABLE_USER_PROGRESS, values, "user_id = ?", new String[]{"default"});
        }
    }

    public void addFuelCells(int cells) {
        UserProgressData progress = getUserProgress();
        if (progress != null) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("total_fuel_cells", progress.totalFuelCells + cells);
            db.update(TABLE_USER_PROGRESS, values, "user_id = ?", new String[]{"default"});
        }
    }

    public void addCrystals(int crystals) {
        UserProgressData progress = getUserProgress();
        if (progress != null) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put("total_crystals", progress.totalCrystals + crystals);
            db.update(TABLE_USER_PROGRESS, values, "user_id = ?", new String[]{"default"});
        }
    }

    public void addExperience(int xp) {
        UserProgressData progress = getUserProgress();
        if (progress != null) {
            SQLiteDatabase db = getWritableDatabase();
            ContentValues values = new ContentValues();
            int newXp = progress.experiencePoints + xp;
            int newLevel = newXp / 100 + 1; // Level up every 100 XP
            values.put("experience_points", newXp);
            values.put("current_level", newLevel);
            db.update(TABLE_USER_PROGRESS, values, "user_id = ?", new String[]{"default"});
        }
    }

    public void markWordAsLearned(int wordId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_learned", 1);
        db.update(TABLE_WORDS, values, "id = ?", new String[]{String.valueOf(wordId)});
    }

    public void updateSceneProgress(int sceneId, int stars) {
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"GameDatabaseHelper.updateSceneProgress:1238\",\"message\":\"updateSceneProgress entry\",\"data\":{\"sceneId\":" + sceneId + ",\"stars\":" + stars + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_completed", 1);
        values.put("stars_earned", stars);
        int rowsUpdated = db.update(TABLE_SCENES, values, "id = ?", new String[]{String.valueOf(sceneId)});
        // #region agent log
        try {
            java.io.FileWriter fw = new java.io.FileWriter("c:\\Users\\ADMIN\\Downloads\\MobileApp_Project-main (2)\\MobileApp_Project-main\\.cursor\\debug.log", true);
            fw.write("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"GameDatabaseHelper.updateSceneProgress:1244\",\"message\":\"updateSceneProgress result\",\"data\":{\"sceneId\":" + sceneId + ",\"stars\":" + stars + ",\"rowsUpdated\":" + rowsUpdated + "},\"timestamp\":" + System.currentTimeMillis() + "}\n");
            fw.close();
        } catch (Exception e) {}
        // #endregion
    }

    public void updateSceneUnlockStatus(int sceneId, boolean isUnlocked) {
        // Note: Scenes table doesn't have is_unlocked column
        // Unlock status is managed by LessonUnlockManager via SharedPreferences
        // This method is kept for API compatibility
    }

    public void unlockPlanet(int planetId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_unlocked", 1);
        db.update(TABLE_PLANETS, values, "id = ?", new String[]{String.valueOf(planetId)});
    }

    public List<BadgeData> getAllBadges() {
        List<BadgeData> badges = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_BADGES, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            badges.add(cursorToBadge(cursor));
        }
        cursor.close();
        return badges;
    }

    // ============ CURSOR CONVERTERS ============

    private PlanetData cursorToPlanet(Cursor c) {
        PlanetData p = new PlanetData();
        p.id = c.getInt(c.getColumnIndexOrThrow("id"));
        p.galaxyId = c.getInt(c.getColumnIndexOrThrow("galaxy_id"));
        p.planetKey = c.getString(c.getColumnIndexOrThrow("planet_key"));
        p.name = c.getString(c.getColumnIndexOrThrow("name"));
        p.nameVi = c.getString(c.getColumnIndexOrThrow("name_vi"));
        p.description = c.getString(c.getColumnIndexOrThrow("description"));
        p.emoji = c.getString(c.getColumnIndexOrThrow("emoji"));
        p.themeColor = c.getString(c.getColumnIndexOrThrow("theme_color"));
        p.collectibleName = c.getString(c.getColumnIndexOrThrow("collectible_name"));
        p.collectibleEmoji = c.getString(c.getColumnIndexOrThrow("collectible_emoji"));
        p.grammarFocus = c.getString(c.getColumnIndexOrThrow("grammar_focus"));
        p.skillFocus = c.getString(c.getColumnIndexOrThrow("skill_focus"));
        p.requiredFuelCells = c.getInt(c.getColumnIndexOrThrow("required_fuel_cells"));
        p.orderIndex = c.getInt(c.getColumnIndexOrThrow("order_index"));
        p.isUnlocked = c.getInt(c.getColumnIndexOrThrow("is_unlocked")) == 1;
        return p;
    }

    private SceneData cursorToScene(Cursor c) {
        SceneData s = new SceneData();
        s.id = c.getInt(c.getColumnIndexOrThrow("id"));
        s.planetId = c.getInt(c.getColumnIndexOrThrow("planet_id"));
        s.sceneKey = c.getString(c.getColumnIndexOrThrow("scene_key"));
        s.sceneType = c.getString(c.getColumnIndexOrThrow("scene_type"));
        s.name = c.getString(c.getColumnIndexOrThrow("name"));
        s.nameVi = c.getString(c.getColumnIndexOrThrow("name_vi"));
        s.description = c.getString(c.getColumnIndexOrThrow("description"));
        s.emoji = c.getString(c.getColumnIndexOrThrow("emoji"));
        s.orderIndex = c.getInt(c.getColumnIndexOrThrow("order_index"));
        s.isCompleted = c.getInt(c.getColumnIndexOrThrow("is_completed")) == 1;
        s.starsEarned = c.getInt(c.getColumnIndexOrThrow("stars_earned"));
        return s;
    }

    private WordData cursorToWord(Cursor c) {
        WordData w = new WordData();
        w.id = c.getInt(c.getColumnIndexOrThrow("id"));
        w.planetId = c.getInt(c.getColumnIndexOrThrow("planet_id"));
        w.english = c.getString(c.getColumnIndexOrThrow("english"));
        w.vietnamese = c.getString(c.getColumnIndexOrThrow("vietnamese"));
        w.pronunciation = c.getString(c.getColumnIndexOrThrow("pronunciation"));
        w.emoji = c.getString(c.getColumnIndexOrThrow("emoji"));
        w.category = c.getString(c.getColumnIndexOrThrow("category"));
        w.exampleSentence = c.getString(c.getColumnIndexOrThrow("example_sentence"));
        w.exampleTranslation = c.getString(c.getColumnIndexOrThrow("example_translation"));
        w.isLearned = c.getInt(c.getColumnIndexOrThrow("is_learned")) == 1;
        return w;
    }

    private SentenceData cursorToSentence(Cursor c) {
        SentenceData s = new SentenceData();
        s.id = c.getInt(c.getColumnIndexOrThrow("id"));
        s.planetId = c.getInt(c.getColumnIndexOrThrow("planet_id"));
        s.english = c.getString(c.getColumnIndexOrThrow("english"));
        s.vietnamese = c.getString(c.getColumnIndexOrThrow("vietnamese"));
        s.keywords = c.getString(c.getColumnIndexOrThrow("keywords"));
        s.sentenceType = c.getString(c.getColumnIndexOrThrow("sentence_type"));
        return s;
    }

    private UserProgressData cursorToUserProgress(Cursor c) {
        UserProgressData p = new UserProgressData();
        p.id = c.getInt(c.getColumnIndexOrThrow("id"));
        p.totalStars = c.getInt(c.getColumnIndexOrThrow("total_stars"));
        p.totalFuelCells = c.getInt(c.getColumnIndexOrThrow("total_fuel_cells"));
        p.totalCrystals = c.getInt(c.getColumnIndexOrThrow("total_crystals"));
        p.currentPlanetId = c.getInt(c.getColumnIndexOrThrow("current_planet_id"));
        p.currentLevel = c.getInt(c.getColumnIndexOrThrow("current_level"));
        p.wordsLearned = c.getInt(c.getColumnIndexOrThrow("words_learned"));
        p.gamesCompleted = c.getInt(c.getColumnIndexOrThrow("games_completed"));
        p.streakDays = c.getInt(c.getColumnIndexOrThrow("streak_days"));
        p.avatarId = c.getInt(c.getColumnIndexOrThrow("avatar_id"));
        p.buddyId = c.getInt(c.getColumnIndexOrThrow("buddy_id"));
        p.experiencePoints = c.getInt(c.getColumnIndexOrThrow("experience_points"));
        return p;
    }

    private BadgeData cursorToBadge(Cursor c) {
        BadgeData b = new BadgeData();
        b.id = c.getInt(c.getColumnIndexOrThrow("id"));
        b.badgeKey = c.getString(c.getColumnIndexOrThrow("badge_key"));
        b.name = c.getString(c.getColumnIndexOrThrow("name"));
        b.emoji = c.getString(c.getColumnIndexOrThrow("emoji"));
        b.description = c.getString(c.getColumnIndexOrThrow("description"));
        b.requirementType = c.getString(c.getColumnIndexOrThrow("requirement_type"));
        b.requirementValue = c.getInt(c.getColumnIndexOrThrow("requirement_value"));
        b.isEarned = c.getInt(c.getColumnIndexOrThrow("is_earned")) == 1;
        return b;
    }

    // ============ DATA CLASSES ============

    public static class PlanetData {
        public int id;
        public int galaxyId;
        public String planetKey;
        public String name;
        public String nameVi;
        public String description;
        public String emoji;
        public String themeColor;
        public String collectibleName;
        public String collectibleEmoji;
        public String grammarFocus;
        public String skillFocus;
        public int requiredFuelCells;
        public int orderIndex;
        public boolean isUnlocked;
    }

    public static class SceneData {
        public int id;
        public int planetId;
        public String sceneKey;
        public String sceneType;
        public String name;
        public String nameVi;
        public String description;
        public String emoji;
        public int orderIndex;
        public boolean isCompleted;
        public int starsEarned;
    }

    public static class WordData {
        public int id;
        public int planetId;
        public String english;
        public String vietnamese;
        public String pronunciation;
        public String emoji;
        public String category;
        public String exampleSentence;
        public String exampleTranslation;
        public boolean isLearned;
    }

    public static class SentenceData {
        public int id;
        public int planetId;
        public String english;
        public String vietnamese;
        public String keywords;
        public String sentenceType;
    }

    public static class UserProgressData {
        public int id;
        public int totalStars;
        public int totalFuelCells;
        public int totalCrystals;
        public int currentPlanetId;
        public int currentLevel;
        public int wordsLearned;
        public int gamesCompleted;
        public int streakDays;
        public int avatarId;
        public int buddyId;
        public int experiencePoints;
    }

    public static class BadgeData {
        public int id;
        public String badgeKey;
        public String name;
        public String emoji;
        public String description;
        public String requirementType;
        public int requirementValue;
        public boolean isEarned;
    }

    public static class GalaxyData {
        public int id;
        public String galaxyKey;
        public String name;
        public String nameVi;
        public String description;
        public String emoji;
        public String themeColor;
        public String backgroundImage;
        public int requiredStars;
        public int orderIndex;
        public boolean isUnlocked;
    }

    public static class BuddyData {
        public int id;
        public String buddyKey;
        public String name;
        public String nameVi;
        public String emoji;
        public String description;
        public int level;
        public int experience;
        public boolean isActive;
        public boolean isUnlocked;
    }

    // ============ GALAXY QUERY METHODS ============

    public List<GalaxyData> getAllGalaxies() {
        List<GalaxyData> galaxies = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GALAXIES, null, null, null, null, null, "order_index ASC");

        while (cursor.moveToNext()) {
            galaxies.add(cursorToGalaxy(cursor));
        }
        cursor.close();
        return galaxies;
    }

    public GalaxyData getGalaxyById(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_GALAXIES, null, "id = ?",
            new String[]{String.valueOf(id)}, null, null, null);

        GalaxyData galaxy = null;
        if (cursor.moveToFirst()) {
            galaxy = cursorToGalaxy(cursor);
        }
        cursor.close();
        return galaxy;
    }

    public List<PlanetData> getPlanetsForGalaxy(int galaxyId) {
        ensurePlanetsSeeded(getWritableDatabase());
        List<PlanetData> planets = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_PLANETS, null, "galaxy_id = ?",
            new String[]{String.valueOf(galaxyId)}, null, null, "order_index ASC");

        while (cursor.moveToNext()) {
            planets.add(cursorToPlanet(cursor));
        }
        cursor.close();
        return planets;
    }

    public void unlockGalaxy(int galaxyId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_unlocked", 1);
        db.update(TABLE_GALAXIES, values, "id = ?", new String[]{String.valueOf(galaxyId)});
    }

    private GalaxyData cursorToGalaxy(Cursor c) {
        GalaxyData g = new GalaxyData();
        g.id = c.getInt(c.getColumnIndexOrThrow("id"));
        g.galaxyKey = c.getString(c.getColumnIndexOrThrow("galaxy_key"));
        g.name = c.getString(c.getColumnIndexOrThrow("name"));
        g.nameVi = c.getString(c.getColumnIndexOrThrow("name_vi"));
        g.description = c.getString(c.getColumnIndexOrThrow("description"));
        g.emoji = c.getString(c.getColumnIndexOrThrow("emoji"));
        g.themeColor = c.getString(c.getColumnIndexOrThrow("theme_color"));
        g.requiredStars = c.getInt(c.getColumnIndexOrThrow("required_stars"));
        g.orderIndex = c.getInt(c.getColumnIndexOrThrow("order_index"));
        g.isUnlocked = c.getInt(c.getColumnIndexOrThrow("is_unlocked")) == 1;
        return g;
    }

    // ============ BUDDY QUERY METHODS ============

    public List<BuddyData> getAllBuddies() {
        List<BuddyData> buddies = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_BUDDIES, null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            buddies.add(cursorToBuddy(cursor));
        }
        cursor.close();
        return buddies;
    }

    public BuddyData getActiveBuddy() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_BUDDIES, null, "is_active = 1",
            null, null, null, null);

        BuddyData buddy = null;
        if (cursor.moveToFirst()) {
            buddy = cursorToBuddy(cursor);
        }
        cursor.close();
        return buddy;
    }

    public void setActiveBuddy(int buddyId) {
        SQLiteDatabase db = getWritableDatabase();
        // First deactivate all buddies
        ContentValues deactivate = new ContentValues();
        deactivate.put("is_active", 0);
        db.update(TABLE_BUDDIES, deactivate, null, null);

        // Then activate the selected one
        ContentValues activate = new ContentValues();
        activate.put("is_active", 1);
        db.update(TABLE_BUDDIES, activate, "id = ?", new String[]{String.valueOf(buddyId)});
    }

    public void unlockBuddy(int buddyId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_unlocked", 1);
        db.update(TABLE_BUDDIES, values, "id = ?", new String[]{String.valueOf(buddyId)});
    }

    public void addBuddyExperience(int buddyId, int exp) {
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_BUDDIES, new String[]{"experience", "level"},
            "id = ?", new String[]{String.valueOf(buddyId)}, null, null, null);

        if (cursor.moveToFirst()) {
            int currentExp = cursor.getInt(0);
            int currentLevel = cursor.getInt(1);
            int newExp = currentExp + exp;
            int expNeeded = currentLevel * 100; // 100 exp per level

            ContentValues values = new ContentValues();
            if (newExp >= expNeeded) {
                values.put("level", currentLevel + 1);
                values.put("experience", newExp - expNeeded);
            } else {
                values.put("experience", newExp);
            }
            db.update(TABLE_BUDDIES, values, "id = ?", new String[]{String.valueOf(buddyId)});
        }
        cursor.close();
    }

    private BuddyData cursorToBuddy(Cursor c) {
        BuddyData b = new BuddyData();
        b.id = c.getInt(c.getColumnIndexOrThrow("id"));
        b.buddyKey = c.getString(c.getColumnIndexOrThrow("buddy_key"));
        b.name = c.getString(c.getColumnIndexOrThrow("name"));
        b.nameVi = c.getString(c.getColumnIndexOrThrow("name_vi"));
        b.emoji = c.getString(c.getColumnIndexOrThrow("emoji"));
        b.description = c.getString(c.getColumnIndexOrThrow("description"));
        b.level = c.getInt(c.getColumnIndexOrThrow("level"));
        b.experience = c.getInt(c.getColumnIndexOrThrow("experience"));
        b.isActive = c.getInt(c.getColumnIndexOrThrow("is_active")) == 1;
        b.isUnlocked = c.getInt(c.getColumnIndexOrThrow("is_unlocked")) == 1;
        return b;
    }
}

