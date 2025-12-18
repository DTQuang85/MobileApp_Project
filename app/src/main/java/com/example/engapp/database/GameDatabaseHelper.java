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
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_PLANETS = "planets";
    public static final String TABLE_SCENES = "scenes";
    public static final String TABLE_WORDS = "words";
    public static final String TABLE_SENTENCES = "sentences";
    public static final String TABLE_MINIGAMES = "minigames";
    public static final String TABLE_USER_PROGRESS = "user_progress";
    public static final String TABLE_COLLECTED_ITEMS = "collected_items";
    public static final String TABLE_BADGES = "badges";

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
        // Create Planets table
        db.execSQL("CREATE TABLE " + TABLE_PLANETS + " (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
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
            "is_unlocked INTEGER DEFAULT 0" +
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

        // Insert initial data
        insertInitialData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COLLECTED_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BADGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROGRESS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MINIGAMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SENTENCES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WORDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCENES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PLANETS);
        onCreate(db);
    }

    private void insertInitialData(SQLiteDatabase db) {
        // Insert 9 planets
        insertPlanet(db, "coloria_prime", "Coloria Prime", "Hành tinh Sắc Màu",
            "Thành phố pha lê với cầu vồng và laser màu", "🌈", "#FF6B6B",
            "crystal_city", "Prism Shards", "💎",
            "Adjectives (big/small)", "Colors & Shapes", 0, 1, 1);

        insertPlanet(db, "toytopia_orbit", "Toytopia Orbit", "Quỹ đạo Đồ Chơi",
            "Công viên robot đồ chơi, tàu lửa mini, nhà bóng", "🎮", "#4ECDC4",
            "toy_park", "Sticker Toys", "🎨",
            "Prepositions", "Toys & Positions", 3, 2, 0);

        insertPlanet(db, "animania_wild", "Animania Wild", "Sở Thú Ngoài Hành Tinh",
            "Mái vòm rừng, savannah, hang đêm, băng tuyết", "🦁", "#45B7D1",
            "alien_zoo", "Animal Badges", "🏅",
            "Can/Can't", "Animals & Actions", 5, 3, 0);

        insertPlanet(db, "citytron_nova", "Citytron Nova", "Thành Phố Tương Lai",
            "Tàu điện không gian, biển neon, toà tháp", "🌆", "#96CEB4",
            "future_city", "Metro Tickets", "🎫",
            "There is/are", "Places & Directions", 8, 4, 0);

        insertPlanet(db, "foodora_station", "Foodora Station", "Trạm Ẩm Thực",
            "Chợ liên ngân hà, bếp tàu vũ trụ, nông trại sao", "🍕", "#FFEAA7",
            "space_kitchen", "Recipe Cards", "📜",
            "Countable/Uncountable", "Food & Shopping", 12, 5, 0);

        insertPlanet(db, "weatheron_sky", "Weatheron Sky", "Bầu Trời Thời Tiết",
            "Cảng mây, bão điện, thị trấn tuyết", "⛈️", "#74B9FF",
            "cloud_port", "Weather Orbs", "🔮",
            "Because/So", "Weather & Clothes", 15, 6, 0);

        insertPlanet(db, "robolab_command", "RoboLab Command", "Phòng Chỉ Huy Robot",
            "Nhà máy mạch điện, drone bay, đường hầm laser", "🤖", "#A29BFE",
            "robot_factory", "Circuit Parts", "⚡",
            "Imperatives", "Commands & Sequences", 18, 7, 0);

        insertPlanet(db, "timelapse_base", "TimeLapse Base", "Căn Cứ Thời Gian",
            "Tháp đồng hồ, cầu ngày-đêm, trạm lịch tuần", "⏰", "#FD79A8",
            "time_tower", "Time Crystals", "⌛",
            "Present Simple", "Time & Routines", 22, 8, 0);

        insertPlanet(db, "storyverse_galaxy", "Storyverse Galaxy", "Thiên Hà Truyện Kể",
            "Lâu đài sao, rừng phép, thư viện vũ trụ", "📚", "#E17055",
            "story_castle", "Story Pages", "📖",
            "Past Simple", "Storytelling", 25, 9, 0);

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

        // Insert words for Coloria Prime
        insertColoriaWords(db, planetId);

        // Insert sentences for Coloria Prime
        insertColoriaSentences(db, planetId);

        // Insert words for all other planets
        insertToytopiaWords(db, 2);
        insertAnimaniaWords(db, 3);
        insertCitytronWords(db, 4);
        insertFoodoraWords(db, 5);
        insertWeatheronWords(db, 6);
        insertRobolabWords(db, 7);
        insertTimelapseWords(db, 8);
        insertStoryverseWords(db, 9);

        // Insert default user progress
        ContentValues userValues = new ContentValues();
        userValues.put("user_id", "default");
        userValues.put("total_stars", 0);
        userValues.put("total_fuel_cells", 0);
        db.insert(TABLE_USER_PROGRESS, null, userValues);

        // Insert badges
        insertBadges(db);
    }

    private void insertPlanet(SQLiteDatabase db, String key, String name, String nameVi,
            String description, String emoji, String color, String bgImage,
            String collectible, String collectibleEmoji, String grammar, String skill,
            int requiredFuel, int order, int unlocked) {
        ContentValues values = new ContentValues();
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
    }

    // Planet 4: Citytron Nova - Places & Directions
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
    }

    // Planet 5: Foodora Station - Food & Shopping
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
    }

    // Planet 6: Weatheron Sky - Weather & Clothes
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
    }

    // Planet 7: RoboLab Command - Commands & Sequences
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
    }

    // Planet 8: TimeLapse Base - Time & Routines
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
    }

    // Planet 9: Storyverse Galaxy - Storytelling
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
        insertSentence(db, planetId, 1, "It's a blue triangle.",
            "Đây là một hình tam giác xanh.", "blue,triangle", "pattern");
        insertSentence(db, planetId, 1, "I can see three stars.",
            "Tôi có thể thấy ba ngôi sao.", "can,see,stars", "pattern");
        insertSentence(db, planetId, 1, "Which one is bigger?",
            "Cái nào lớn hơn?", "which,bigger", "question");
        insertSentence(db, planetId, 1, "The red circle is small.",
            "Hình tròn đỏ thì nhỏ.", "red,circle,small", "pattern");
        insertSentence(db, planetId, 1, "Choose the green square.",
            "Chọn hình vuông xanh lá.", "choose,green,square", "command");
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

    public List<SentenceData> getSentencesForPlanet(int planetId) {
        List<SentenceData> sentences = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_SENTENCES, null, "planet_id = ?",
            new String[]{String.valueOf(planetId)}, null, null, null);

        while (cursor.moveToNext()) {
            sentences.add(cursorToSentence(cursor));
        }
        cursor.close();
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

    public void markWordAsLearned(int wordId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_learned", 1);
        db.update(TABLE_WORDS, values, "id = ?", new String[]{String.valueOf(wordId)});
    }

    public void updateSceneProgress(int sceneId, int stars) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_completed", 1);
        values.put("stars_earned", stars);
        db.update(TABLE_SCENES, values, "id = ?", new String[]{String.valueOf(sceneId)});
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
}

