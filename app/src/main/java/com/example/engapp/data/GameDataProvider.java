package com.example.engapp.data;

import com.example.engapp.model.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Provides game data for all planets, zones, words, and sentences.
 * This class contains rich learning content for children aged 5-12.
 */
public class GameDataProvider {

    // Time Eras
    public static final String ERA_PREHISTORIC = "prehistoric";
    public static final String ERA_MEDIEVAL = "medieval";
    public static final String ERA_MODERN = "modern";
    public static final String ERA_FUTURE = "future";

    /**
     * Get all available planets
     */
    public static List<Planet> getAllPlanets() {
        List<Planet> planets = new ArrayList<>();

        // Prehistoric Era Planets (Easy - Ages 5-7)
        planets.add(createAnimalPlanet());
        planets.add(createColorPlanet());
        planets.add(createNumberPlanet());

        // Medieval Era Planets (Medium - Ages 7-9)
        planets.add(createFoodPlanet());
        planets.add(createFamilyPlanet());
        planets.add(createBodyPlanet());

        // Modern Era Planets (Medium-Hard - Ages 8-10)
        planets.add(createSchoolPlanet());
        planets.add(createNaturePlanet());
        planets.add(createHomePlanet());

        // Future Era Planets (Hard - Ages 10-12)
        planets.add(createActionPlanet());
        planets.add(createEmotionPlanet());
        planets.add(createTravelPlanet());

        return planets;
    }

    /**
     * ANIMAL PLANET - Prehistoric Era
     * Theme: Dinosaurs and animals
     */
    private static Planet createAnimalPlanet() {
        Planet planet = new Planet("animal", "Animal Planet", "Hành tinh Động vật", "🐾", 0xFF4ADE80, ERA_PREHISTORIC);
        planet.setUnlocked(true);
        planet.setRequiredStars(0);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Farm Animals
        Zone farmZone = new Zone("farm", "Farm Animals", "Động vật nông trại", "🐄");
        farmZone.setUnlocked(true);
        farmZone.setWords(Arrays.asList(
            createWord("dog", "con chó", "🐕", "The dog is brown.", "Con chó màu nâu."),
            createWord("cat", "con mèo", "🐱", "The cat is sleeping.", "Con mèo đang ngủ."),
            createWord("cow", "con bò", "🐄", "The cow gives milk.", "Con bò cho sữa."),
            createWord("pig", "con heo", "🐷", "The pig is pink.", "Con heo màu hồng."),
            createWord("chicken", "con gà", "🐔", "The chicken lays eggs.", "Con gà đẻ trứng."),
            createWord("duck", "con vịt", "🦆", "The duck can swim.", "Con vịt biết bơi."),
            createWord("horse", "con ngựa", "🐴", "The horse runs fast.", "Con ngựa chạy nhanh."),
            createWord("sheep", "con cừu", "🐑", "The sheep has wool.", "Con cừu có lông.")
        ));
        farmZone.setSentences(Arrays.asList(
            new Sentence("This is a dog.", "Đây là con chó.", new String[]{"dog"}),
            new Sentence("I see a cat.", "Tôi thấy con mèo.", new String[]{"cat"}),
            new Sentence("The cow is big.", "Con bò to lớn.", new String[]{"cow", "big"})
        ));
        zones.add(farmZone);

        // Zone 2: Wild Animals
        Zone wildZone = new Zone("wild", "Wild Animals", "Động vật hoang dã", "🦁");
        wildZone.setWords(Arrays.asList(
            createWord("lion", "sư tử", "🦁", "The lion is the king.", "Sư tử là vua."),
            createWord("tiger", "con hổ", "🐅", "The tiger has stripes.", "Con hổ có sọc."),
            createWord("elephant", "con voi", "🐘", "The elephant is big.", "Con voi rất to."),
            createWord("monkey", "con khỉ", "🐒", "The monkey likes bananas.", "Con khỉ thích chuối."),
            createWord("bear", "con gấu", "🐻", "The bear is strong.", "Con gấu rất khỏe."),
            createWord("snake", "con rắn", "🐍", "The snake is long.", "Con rắn dài."),
            createWord("crocodile", "cá sấu", "🐊", "The crocodile has teeth.", "Cá sấu có răng."),
            createWord("giraffe", "hươu cao cổ", "🦒", "The giraffe is tall.", "Hươu cao cổ rất cao.")
        ));
        wildZone.setSentences(Arrays.asList(
            new Sentence("This is a lion.", "Đây là sư tử.", new String[]{"lion"}),
            new Sentence("It is big.", "Nó to lớn.", new String[]{"big"}),
            new Sentence("The elephant has a trunk.", "Con voi có vòi.", new String[]{"elephant", "trunk"})
        ));
        zones.add(wildZone);

        // Zone 3: Sea Animals
        Zone seaZone = new Zone("sea", "Sea Animals", "Động vật biển", "🐠");
        seaZone.setWords(Arrays.asList(
            createWord("fish", "con cá", "🐟", "The fish can swim.", "Con cá biết bơi."),
            createWord("shark", "cá mập", "🦈", "The shark is scary.", "Cá mập đáng sợ."),
            createWord("whale", "cá voi", "🐋", "The whale is huge.", "Cá voi rất lớn."),
            createWord("dolphin", "cá heo", "🐬", "The dolphin is smart.", "Cá heo thông minh."),
            createWord("octopus", "bạch tuộc", "🐙", "The octopus has eight arms.", "Bạch tuộc có tám tay."),
            createWord("crab", "con cua", "🦀", "The crab walks sideways.", "Con cua đi ngang."),
            createWord("turtle", "con rùa", "🐢", "The turtle is slow.", "Con rùa chậm chạp."),
            createWord("starfish", "sao biển", "⭐", "The starfish has five arms.", "Sao biển có năm cánh.")
        ));
        zones.add(seaZone);

        // Zone 4: Birds
        Zone birdZone = new Zone("bird", "Birds", "Các loài chim", "🦅");
        birdZone.setWords(Arrays.asList(
            createWord("bird", "con chim", "🐦", "The bird can fly.", "Con chim biết bay."),
            createWord("eagle", "đại bàng", "🦅", "The eagle flies high.", "Đại bàng bay cao."),
            createWord("owl", "con cú", "🦉", "The owl sleeps by day.", "Con cú ngủ ban ngày."),
            createWord("parrot", "con vẹt", "🦜", "The parrot can talk.", "Con vẹt biết nói."),
            createWord("penguin", "chim cánh cụt", "🐧", "The penguin lives in ice.", "Chim cánh cụt sống ở băng."),
            createWord("peacock", "con công", "🦚", "The peacock is beautiful.", "Con công rất đẹp.")
        ));
        zones.add(birdZone);

        // Zone 5: Insects
        Zone insectZone = new Zone("insect", "Insects", "Côn trùng", "🦋");
        insectZone.setWords(Arrays.asList(
            createWord("butterfly", "con bướm", "🦋", "The butterfly is pretty.", "Con bướm rất đẹp."),
            createWord("bee", "con ong", "🐝", "The bee makes honey.", "Con ong làm mật."),
            createWord("ant", "con kiến", "🐜", "The ant is small.", "Con kiến nhỏ."),
            createWord("spider", "con nhện", "🕷️", "The spider has eight legs.", "Con nhện có tám chân."),
            createWord("ladybug", "bọ rùa", "🐞", "The ladybug is red.", "Bọ rùa màu đỏ."),
            createWord("dragonfly", "chuồn chuồn", "🪰", "The dragonfly flies fast.", "Chuồn chuồn bay nhanh.")
        ));
        zones.add(insectZone);

        // Zone 6: Dinosaurs (Special)
        Zone dinoZone = new Zone("dino", "Dinosaurs", "Khủng long", "🦕");
        dinoZone.setWords(Arrays.asList(
            createWord("dinosaur", "khủng long", "🦕", "Dinosaurs lived long ago.", "Khủng long sống cách đây lâu."),
            createWord("T-Rex", "khủng long bạo chúa", "🦖", "T-Rex was scary.", "T-Rex đáng sợ."),
            createWord("egg", "quả trứng", "🥚", "Dinosaurs laid eggs.", "Khủng long đẻ trứng."),
            createWord("bone", "xương", "🦴", "We find dinosaur bones.", "Chúng ta tìm thấy xương khủng long."),
            createWord("fossil", "hóa thạch", "🪨", "Fossils are very old.", "Hóa thạch rất cổ.")
        ));
        zones.add(dinoZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * COLOR PLANET - Prehistoric Era
     */
    private static Planet createColorPlanet() {
        Planet planet = new Planet("color", "Color Planet", "Hành tinh Màu sắc", "🌈", 0xFFF472B6, ERA_PREHISTORIC);
        planet.setUnlocked(true);
        planet.setRequiredStars(0);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Basic Colors
        Zone basicZone = new Zone("basic_color", "Basic Colors", "Màu cơ bản", "🎨");
        basicZone.setUnlocked(true);
        basicZone.setWords(Arrays.asList(
            createWord("red", "màu đỏ", "🔴", "The apple is red.", "Quả táo màu đỏ."),
            createWord("blue", "màu xanh dương", "🔵", "The sky is blue.", "Bầu trời màu xanh."),
            createWord("yellow", "màu vàng", "🟡", "The sun is yellow.", "Mặt trời màu vàng."),
            createWord("green", "màu xanh lá", "🟢", "Grass is green.", "Cỏ màu xanh lá."),
            createWord("orange", "màu cam", "🟠", "The orange is orange.", "Quả cam màu cam."),
            createWord("purple", "màu tím", "🟣", "Grapes are purple.", "Nho màu tím."),
            createWord("pink", "màu hồng", "💗", "The flower is pink.", "Bông hoa màu hồng."),
            createWord("black", "màu đen", "⬛", "Night is black.", "Đêm màu đen."),
            createWord("white", "màu trắng", "⬜", "Snow is white.", "Tuyết màu trắng."),
            createWord("brown", "màu nâu", "🟤", "The tree is brown.", "Cây màu nâu.")
        ));
        basicZone.setSentences(Arrays.asList(
            new Sentence("What color is it?", "Nó màu gì?", new String[]{"color"}),
            new Sentence("It is red.", "Nó màu đỏ.", new String[]{"red"}),
            new Sentence("I like blue.", "Tôi thích màu xanh.", new String[]{"like", "blue"})
        ));
        zones.add(basicZone);

        // Zone 2: Rainbow
        Zone rainbowZone = new Zone("rainbow", "Rainbow", "Cầu vồng", "🌈");
        rainbowZone.setWords(Arrays.asList(
            createWord("rainbow", "cầu vồng", "🌈", "The rainbow has seven colors.", "Cầu vồng có bảy màu."),
            createWord("violet", "màu tím violet", "💜", "Violet is in the rainbow.", "Màu tím có trong cầu vồng."),
            createWord("indigo", "màu chàm", "💙", "Indigo is dark blue.", "Màu chàm là xanh đậm.")
        ));
        zones.add(rainbowZone);

        // Zone 3: Color Mixing
        Zone mixZone = new Zone("mix_color", "Color Mixing", "Pha màu", "🎭");
        mixZone.setWords(Arrays.asList(
            createWord("light", "nhạt", "☀️", "Light blue is pretty.", "Xanh nhạt rất đẹp."),
            createWord("dark", "đậm", "🌙", "Dark red is nice.", "Đỏ đậm rất đẹp."),
            createWord("bright", "sáng", "✨", "Yellow is bright.", "Màu vàng sáng."),
            createWord("colorful", "nhiều màu", "🎨", "The picture is colorful.", "Bức tranh nhiều màu.")
        ));
        zones.add(mixZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * NUMBER PLANET - Prehistoric Era
     */
    private static Planet createNumberPlanet() {
        Planet planet = new Planet("number", "Number Planet", "Hành tinh Số", "🔢", 0xFF60A5FA, ERA_PREHISTORIC);
        planet.setRequiredStars(10);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Numbers 1-10
        Zone num10Zone = new Zone("num_1_10", "Numbers 1-10", "Số 1-10", "1️⃣");
        num10Zone.setUnlocked(true);
        num10Zone.setWords(Arrays.asList(
            createWord("one", "một", "1️⃣", "I have one apple.", "Tôi có một quả táo."),
            createWord("two", "hai", "2️⃣", "I have two hands.", "Tôi có hai tay."),
            createWord("three", "ba", "3️⃣", "There are three cats.", "Có ba con mèo."),
            createWord("four", "bốn", "4️⃣", "A dog has four legs.", "Chó có bốn chân."),
            createWord("five", "năm", "5️⃣", "I have five fingers.", "Tôi có năm ngón tay."),
            createWord("six", "sáu", "6️⃣", "There are six eggs.", "Có sáu quả trứng."),
            createWord("seven", "bảy", "7️⃣", "Seven days in a week.", "Bảy ngày trong tuần."),
            createWord("eight", "tám", "8️⃣", "Octopus has eight arms.", "Bạch tuộc có tám tay."),
            createWord("nine", "chín", "9️⃣", "Nine is before ten.", "Chín đứng trước mười."),
            createWord("ten", "mười", "🔟", "I count to ten.", "Tôi đếm đến mười.")
        ));
        zones.add(num10Zone);

        // Zone 2: Numbers 11-20
        Zone num20Zone = new Zone("num_11_20", "Numbers 11-20", "Số 11-20", "🔢");
        num20Zone.setWords(Arrays.asList(
            createWord("eleven", "mười một", "1️⃣1️⃣", "Eleven plus one is twelve.", "Mười một cộng một là mười hai."),
            createWord("twelve", "mười hai", "1️⃣2️⃣", "There are twelve months.", "Có mười hai tháng."),
            createWord("thirteen", "mười ba", "1️⃣3️⃣", "Thirteen is a number.", "Mười ba là một số."),
            createWord("fourteen", "mười bốn", "1️⃣4️⃣", "Fourteen days is two weeks.", "Mười bốn ngày là hai tuần."),
            createWord("fifteen", "mười lăm", "1️⃣5️⃣", "Fifteen minutes.", "Mười lăm phút."),
            createWord("twenty", "hai mươi", "2️⃣0️⃣", "I can count to twenty.", "Tôi đếm được đến hai mươi.")
        ));
        zones.add(num20Zone);

        // Zone 3: Shapes
        Zone shapeZone = new Zone("shape", "Shapes", "Hình dạng", "🔷");
        shapeZone.setWords(Arrays.asList(
            createWord("circle", "hình tròn", "⭕", "The sun is a circle.", "Mặt trời hình tròn."),
            createWord("square", "hình vuông", "⬜", "The box is square.", "Hộp hình vuông."),
            createWord("triangle", "hình tam giác", "🔺", "A triangle has three sides.", "Tam giác có ba cạnh."),
            createWord("rectangle", "hình chữ nhật", "🟦", "The door is a rectangle.", "Cửa hình chữ nhật."),
            createWord("star", "ngôi sao", "⭐", "I see a star.", "Tôi thấy ngôi sao."),
            createWord("heart", "trái tim", "❤️", "Love is a heart.", "Tình yêu là trái tim.")
        ));
        zones.add(shapeZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * FOOD PLANET - Medieval Era
     */
    private static Planet createFoodPlanet() {
        Planet planet = new Planet("food", "Food Planet", "Hành tinh Đồ ăn", "🍎", 0xFFFB923C, ERA_MEDIEVAL);
        planet.setRequiredStars(30);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Fruits
        Zone fruitZone = new Zone("fruit", "Fruits", "Trái cây", "🍎");
        fruitZone.setWords(Arrays.asList(
            createWord("apple", "táo", "🍎", "I eat an apple.", "Tôi ăn táo."),
            createWord("banana", "chuối", "🍌", "Monkeys like bananas.", "Khỉ thích chuối."),
            createWord("orange", "cam", "🍊", "Orange juice is good.", "Nước cam ngon."),
            createWord("grape", "nho", "🍇", "Grapes are sweet.", "Nho ngọt."),
            createWord("watermelon", "dưa hấu", "🍉", "Watermelon is red.", "Dưa hấu màu đỏ."),
            createWord("strawberry", "dâu tây", "🍓", "I like strawberries.", "Tôi thích dâu tây."),
            createWord("mango", "xoài", "🥭", "Mango is yellow.", "Xoài màu vàng."),
            createWord("pineapple", "dứa", "🍍", "Pineapple is sweet.", "Dứa ngọt.")
        ));
        zones.add(fruitZone);

        // Zone 2: Vegetables
        Zone vegZone = new Zone("vegetable", "Vegetables", "Rau củ", "🥕");
        vegZone.setWords(Arrays.asList(
            createWord("carrot", "cà rốt", "🥕", "Rabbits eat carrots.", "Thỏ ăn cà rốt."),
            createWord("tomato", "cà chua", "🍅", "Tomato is red.", "Cà chua màu đỏ."),
            createWord("potato", "khoai tây", "🥔", "I like potatoes.", "Tôi thích khoai tây."),
            createWord("corn", "bắp", "🌽", "Corn is yellow.", "Bắp màu vàng."),
            createWord("cucumber", "dưa leo", "🥒", "Cucumber is green.", "Dưa leo màu xanh."),
            createWord("onion", "hành", "🧅", "Onion makes me cry.", "Hành làm tôi khóc.")
        ));
        zones.add(vegZone);

        // Zone 3: Drinks
        Zone drinkZone = new Zone("drink", "Drinks", "Đồ uống", "🥤");
        drinkZone.setWords(Arrays.asList(
            createWord("water", "nước", "💧", "I drink water.", "Tôi uống nước."),
            createWord("milk", "sữa", "🥛", "Milk is white.", "Sữa màu trắng."),
            createWord("juice", "nước ép", "🧃", "I like orange juice.", "Tôi thích nước cam."),
            createWord("tea", "trà", "🍵", "Tea is hot.", "Trà nóng.")
        ));
        zones.add(drinkZone);

        // Zone 4: Meals
        Zone mealZone = new Zone("meal", "Meals", "Bữa ăn", "🍽️");
        mealZone.setWords(Arrays.asList(
            createWord("breakfast", "bữa sáng", "🍳", "I eat breakfast.", "Tôi ăn sáng."),
            createWord("lunch", "bữa trưa", "🥪", "Lunch is at noon.", "Bữa trưa lúc trưa."),
            createWord("dinner", "bữa tối", "🍝", "Dinner is at night.", "Bữa tối vào buổi tối."),
            createWord("rice", "cơm", "🍚", "I eat rice.", "Tôi ăn cơm."),
            createWord("bread", "bánh mì", "🍞", "Bread is yummy.", "Bánh mì ngon."),
            createWord("egg", "trứng", "🥚", "I eat eggs.", "Tôi ăn trứng.")
        ));
        zones.add(mealZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * FAMILY PLANET - Medieval Era
     */
    private static Planet createFamilyPlanet() {
        Planet planet = new Planet("family", "Family Planet", "Hành tinh Gia đình", "👨‍👩‍👧‍👦", 0xFFA78BFA, ERA_MEDIEVAL);
        planet.setRequiredStars(50);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Family Members
        Zone familyZone = new Zone("family_member", "Family Members", "Thành viên gia đình", "👨‍👩‍👧");
        familyZone.setWords(Arrays.asList(
            createWord("mother", "mẹ", "👩", "My mother loves me.", "Mẹ yêu tôi."),
            createWord("father", "bố", "👨", "My father is strong.", "Bố tôi khỏe."),
            createWord("sister", "chị/em gái", "👧", "My sister is nice.", "Chị gái tôi tốt bụng."),
            createWord("brother", "anh/em trai", "👦", "My brother is tall.", "Anh trai tôi cao."),
            createWord("grandmother", "bà", "👵", "Grandma tells stories.", "Bà kể chuyện."),
            createWord("grandfather", "ông", "👴", "Grandpa is wise.", "Ông thông thái."),
            createWord("baby", "em bé", "👶", "The baby is cute.", "Em bé dễ thương."),
            createWord("family", "gia đình", "👨‍👩‍👧‍👦", "I love my family.", "Tôi yêu gia đình.")
        ));
        familyZone.setSentences(Arrays.asList(
            new Sentence("This is my mother.", "Đây là mẹ tôi.", new String[]{"mother"}),
            new Sentence("I have a brother.", "Tôi có một anh trai.", new String[]{"brother"}),
            new Sentence("We are a family.", "Chúng tôi là một gia đình.", new String[]{"family"})
        ));
        zones.add(familyZone);

        // Zone 2: Jobs
        Zone jobZone = new Zone("job", "Jobs", "Nghề nghiệp", "👨‍⚕️");
        jobZone.setWords(Arrays.asList(
            createWord("teacher", "giáo viên", "👨‍🏫", "My teacher is kind.", "Thầy giáo tôi tốt bụng."),
            createWord("doctor", "bác sĩ", "👨‍⚕️", "The doctor helps people.", "Bác sĩ giúp mọi người."),
            createWord("police", "cảnh sát", "👮", "Police keep us safe.", "Cảnh sát bảo vệ chúng ta."),
            createWord("firefighter", "lính cứu hỏa", "👨‍🚒", "Firefighters are brave.", "Lính cứu hỏa dũng cảm."),
            createWord("farmer", "nông dân", "👨‍🌾", "The farmer grows food.", "Nông dân trồng thức ăn."),
            createWord("chef", "đầu bếp", "👨‍🍳", "The chef cooks food.", "Đầu bếp nấu ăn."),
            createWord("pilot", "phi công", "👨‍✈️", "The pilot flies planes.", "Phi công lái máy bay."),
            createWord("astronaut", "phi hành gia", "👨‍🚀", "I want to be an astronaut.", "Tôi muốn làm phi hành gia.")
        ));
        zones.add(jobZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * BODY PLANET - Medieval Era
     */
    private static Planet createBodyPlanet() {
        Planet planet = new Planet("body", "Body Planet", "Hành tinh Cơ thể", "🤸", 0xFFF87171, ERA_MEDIEVAL);
        planet.setRequiredStars(70);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Face
        Zone faceZone = new Zone("face", "Face", "Khuôn mặt", "😊");
        faceZone.setWords(Arrays.asList(
            createWord("head", "đầu", "🗣️", "I nod my head.", "Tôi gật đầu."),
            createWord("eye", "mắt", "👁️", "I have two eyes.", "Tôi có hai mắt."),
            createWord("ear", "tai", "👂", "I hear with my ears.", "Tôi nghe bằng tai."),
            createWord("nose", "mũi", "👃", "I smell with my nose.", "Tôi ngửi bằng mũi."),
            createWord("mouth", "miệng", "👄", "I eat with my mouth.", "Tôi ăn bằng miệng."),
            createWord("hair", "tóc", "💇", "My hair is black.", "Tóc tôi màu đen."),
            createWord("teeth", "răng", "🦷", "I brush my teeth.", "Tôi đánh răng.")
        ));
        zones.add(faceZone);

        // Zone 2: Body Parts
        Zone bodyZone = new Zone("body_part", "Body Parts", "Các bộ phận", "💪");
        bodyZone.setWords(Arrays.asList(
            createWord("hand", "tay", "✋", "I wave my hand.", "Tôi vẫy tay."),
            createWord("arm", "cánh tay", "💪", "I have two arms.", "Tôi có hai cánh tay."),
            createWord("leg", "chân", "🦵", "I run with my legs.", "Tôi chạy bằng chân."),
            createWord("foot", "bàn chân", "🦶", "I have two feet.", "Tôi có hai bàn chân."),
            createWord("finger", "ngón tay", "👆", "I have ten fingers.", "Tôi có mười ngón tay."),
            createWord("knee", "đầu gối", "🦵", "I bend my knees.", "Tôi gập đầu gối.")
        ));
        zones.add(bodyZone);

        // Zone 3: Actions
        Zone actionZone = new Zone("body_action", "Body Actions", "Hành động cơ thể", "🏃");
        actionZone.setWords(Arrays.asList(
            createWord("run", "chạy", "🏃", "I can run fast.", "Tôi chạy nhanh."),
            createWord("walk", "đi bộ", "🚶", "I walk to school.", "Tôi đi bộ đến trường."),
            createWord("jump", "nhảy", "🤸", "I can jump high.", "Tôi nhảy cao."),
            createWord("swim", "bơi", "🏊", "I swim in the pool.", "Tôi bơi trong hồ."),
            createWord("dance", "nhảy múa", "💃", "I love to dance.", "Tôi thích nhảy múa."),
            createWord("sleep", "ngủ", "😴", "I sleep at night.", "Tôi ngủ ban đêm.")
        ));
        zones.add(actionZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * SCHOOL PLANET - Modern Era
     */
    private static Planet createSchoolPlanet() {
        Planet planet = new Planet("school", "School Planet", "Hành tinh Trường học", "📚", 0xFFFBBF24, ERA_MODERN);
        planet.setRequiredStars(100);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Classroom
        Zone classZone = new Zone("classroom", "Classroom", "Lớp học", "🏫");
        classZone.setWords(Arrays.asList(
            createWord("school", "trường học", "🏫", "I go to school.", "Tôi đi học."),
            createWord("teacher", "giáo viên", "👨‍🏫", "The teacher teaches.", "Thầy giáo dạy học."),
            createWord("student", "học sinh", "👨‍🎓", "I am a student.", "Tôi là học sinh."),
            createWord("classroom", "lớp học", "🏫", "My classroom is big.", "Lớp học tôi to."),
            createWord("desk", "bàn", "🪑", "I sit at my desk.", "Tôi ngồi ở bàn."),
            createWord("chair", "ghế", "🪑", "The chair is blue.", "Ghế màu xanh."),
            createWord("board", "bảng", "📋", "Teacher writes on the board.", "Thầy viết lên bảng.")
        ));
        classZone.setSentences(Arrays.asList(
            new Sentence("I go to school.", "Tôi đi học.", new String[]{"go", "school"}),
            new Sentence("I study English.", "Tôi học Tiếng Anh.", new String[]{"study", "English"}),
            new Sentence("School is fun.", "Trường học vui.", new String[]{"school", "fun"})
        ));
        zones.add(classZone);

        // Zone 2: School Supplies
        Zone supplyZone = new Zone("supply", "School Supplies", "Đồ dùng học tập", "✏️");
        supplyZone.setWords(Arrays.asList(
            createWord("book", "sách", "📚", "I read a book.", "Tôi đọc sách."),
            createWord("pencil", "bút chì", "✏️", "I write with a pencil.", "Tôi viết bằng bút chì."),
            createWord("pen", "bút mực", "🖊️", "The pen is blue.", "Bút mực màu xanh."),
            createWord("eraser", "tẩy", "🧽", "I use an eraser.", "Tôi dùng tẩy."),
            createWord("ruler", "thước kẻ", "📏", "I measure with a ruler.", "Tôi đo bằng thước."),
            createWord("bag", "cặp sách", "🎒", "My bag is heavy.", "Cặp sách tôi nặng."),
            createWord("notebook", "vở", "📓", "I write in my notebook.", "Tôi viết vào vở.")
        ));
        zones.add(supplyZone);

        // Zone 3: Subjects
        Zone subjectZone = new Zone("subject", "Subjects", "Môn học", "📖");
        subjectZone.setWords(Arrays.asList(
            createWord("English", "Tiếng Anh", "🇬🇧", "I learn English.", "Tôi học Tiếng Anh."),
            createWord("Math", "Toán", "🔢", "Math is interesting.", "Toán thú vị."),
            createWord("Science", "Khoa học", "🔬", "I like Science.", "Tôi thích Khoa học."),
            createWord("Art", "Mỹ thuật", "🎨", "Art is creative.", "Mỹ thuật sáng tạo."),
            createWord("Music", "Âm nhạc", "🎵", "I love Music.", "Tôi yêu Âm nhạc."),
            createWord("PE", "Thể dục", "⚽", "PE is fun.", "Thể dục vui.")
        ));
        zones.add(subjectZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * NATURE PLANET - Modern Era
     */
    private static Planet createNaturePlanet() {
        Planet planet = new Planet("nature", "Nature Planet", "Hành tinh Thiên nhiên", "🌳", 0xFF34D399, ERA_MODERN);
        planet.setRequiredStars(130);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Weather
        Zone weatherZone = new Zone("weather", "Weather", "Thời tiết", "🌤️");
        weatherZone.setWords(Arrays.asList(
            createWord("sun", "mặt trời", "☀️", "The sun is bright.", "Mặt trời sáng."),
            createWord("rain", "mưa", "🌧️", "It is raining.", "Trời đang mưa."),
            createWord("cloud", "mây", "☁️", "The clouds are white.", "Mây trắng."),
            createWord("wind", "gió", "💨", "The wind is strong.", "Gió mạnh."),
            createWord("snow", "tuyết", "❄️", "Snow is cold.", "Tuyết lạnh."),
            createWord("rainbow", "cầu vồng", "🌈", "I see a rainbow.", "Tôi thấy cầu vồng."),
            createWord("hot", "nóng", "🔥", "Today is hot.", "Hôm nay nóng."),
            createWord("cold", "lạnh", "🥶", "Winter is cold.", "Mùa đông lạnh.")
        ));
        weatherZone.setSentences(Arrays.asList(
            new Sentence("How is the weather?", "Thời tiết thế nào?", new String[]{"weather"}),
            new Sentence("It is sunny today.", "Hôm nay trời nắng.", new String[]{"sunny", "today"}),
            new Sentence("I like rainy days.", "Tôi thích ngày mưa.", new String[]{"rainy", "days"})
        ));
        zones.add(weatherZone);

        // Zone 2: Seasons
        Zone seasonZone = new Zone("season", "Seasons", "Mùa", "🍂");
        seasonZone.setWords(Arrays.asList(
            createWord("spring", "mùa xuân", "🌸", "Spring has flowers.", "Mùa xuân có hoa."),
            createWord("summer", "mùa hè", "☀️", "Summer is hot.", "Mùa hè nóng."),
            createWord("autumn", "mùa thu", "🍂", "Leaves fall in autumn.", "Lá rụng mùa thu."),
            createWord("winter", "mùa đông", "❄️", "Winter is cold.", "Mùa đông lạnh.")
        ));
        zones.add(seasonZone);

        // Zone 3: Nature Objects
        Zone natureObjZone = new Zone("nature_obj", "Nature Objects", "Vật thiên nhiên", "🌲");
        natureObjZone.setWords(Arrays.asList(
            createWord("tree", "cây", "🌳", "The tree is tall.", "Cây cao."),
            createWord("flower", "hoa", "🌸", "The flower is pretty.", "Hoa đẹp."),
            createWord("grass", "cỏ", "🌿", "Grass is green.", "Cỏ xanh."),
            createWord("mountain", "núi", "🏔️", "The mountain is high.", "Núi cao."),
            createWord("river", "sông", "🏞️", "Fish live in rivers.", "Cá sống trong sông."),
            createWord("ocean", "đại dương", "🌊", "The ocean is big.", "Đại dương rộng lớn."),
            createWord("sky", "bầu trời", "🌌", "The sky is blue.", "Bầu trời xanh."),
            createWord("star", "ngôi sao", "⭐", "Stars shine at night.", "Sao sáng ban đêm.")
        ));
        zones.add(natureObjZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * HOME PLANET - Modern Era
     */
    private static Planet createHomePlanet() {
        Planet planet = new Planet("home", "Home Planet", "Hành tinh Ngôi nhà", "🏠", 0xFF818CF8, ERA_MODERN);
        planet.setRequiredStars(160);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Rooms
        Zone roomZone = new Zone("room", "Rooms", "Phòng trong nhà", "🚪");
        roomZone.setWords(Arrays.asList(
            createWord("house", "nhà", "🏠", "I live in a house.", "Tôi sống trong nhà."),
            createWord("bedroom", "phòng ngủ", "🛏️", "I sleep in my bedroom.", "Tôi ngủ trong phòng ngủ."),
            createWord("kitchen", "nhà bếp", "🍳", "Mom cooks in the kitchen.", "Mẹ nấu ăn trong bếp."),
            createWord("bathroom", "phòng tắm", "🚿", "I wash in the bathroom.", "Tôi tắm trong phòng tắm."),
            createWord("living room", "phòng khách", "🛋️", "We watch TV in the living room.", "Chúng tôi xem TV trong phòng khách."),
            createWord("garden", "vườn", "🌻", "Flowers grow in the garden.", "Hoa mọc trong vườn.")
        ));
        zones.add(roomZone);

        // Zone 2: Furniture
        Zone furnitureZone = new Zone("furniture", "Furniture", "Đồ nội thất", "🛋️");
        furnitureZone.setWords(Arrays.asList(
            createWord("bed", "giường", "🛏️", "I sleep on the bed.", "Tôi ngủ trên giường."),
            createWord("table", "bàn", "🪑", "Food is on the table.", "Đồ ăn ở trên bàn."),
            createWord("chair", "ghế", "🪑", "I sit on a chair.", "Tôi ngồi trên ghế."),
            createWord("sofa", "ghế sofa", "🛋️", "The sofa is soft.", "Ghế sofa êm."),
            createWord("lamp", "đèn", "💡", "The lamp gives light.", "Đèn cho ánh sáng."),
            createWord("TV", "ti vi", "📺", "I watch TV.", "Tôi xem ti vi."),
            createWord("clock", "đồng hồ", "🕐", "The clock shows time.", "Đồng hồ chỉ giờ.")
        ));
        zones.add(furnitureZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * ACTION PLANET - Future Era
     */
    private static Planet createActionPlanet() {
        Planet planet = new Planet("action", "Action Planet", "Hành tinh Hành động", "🏃", 0xFFF472B6, ERA_FUTURE);
        planet.setRequiredStars(200);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Daily Actions
        Zone dailyZone = new Zone("daily_action", "Daily Actions", "Hành động hàng ngày", "🌅");
        dailyZone.setWords(Arrays.asList(
            createWord("wake up", "thức dậy", "⏰", "I wake up early.", "Tôi thức dậy sớm."),
            createWord("eat", "ăn", "🍽️", "I eat breakfast.", "Tôi ăn sáng."),
            createWord("drink", "uống", "🥤", "I drink water.", "Tôi uống nước."),
            createWord("brush", "đánh răng", "🪥", "I brush my teeth.", "Tôi đánh răng."),
            createWord("wash", "rửa", "🧼", "I wash my hands.", "Tôi rửa tay."),
            createWord("dress", "mặc đồ", "👔", "I get dressed.", "Tôi mặc đồ."),
            createWord("study", "học", "📖", "I study hard.", "Tôi học chăm."),
            createWord("play", "chơi", "🎮", "I play games.", "Tôi chơi game.")
        ));
        dailyZone.setSentences(Arrays.asList(
            new Sentence("I wake up at 7.", "Tôi thức dậy lúc 7 giờ.", new String[]{"wake up"}),
            new Sentence("I go to bed at 9.", "Tôi đi ngủ lúc 9 giờ.", new String[]{"go", "bed"}),
            new Sentence("I study every day.", "Tôi học mỗi ngày.", new String[]{"study", "every day"})
        ));
        zones.add(dailyZone);

        // Zone 2: Sports
        Zone sportZone = new Zone("sport", "Sports", "Thể thao", "⚽");
        sportZone.setWords(Arrays.asList(
            createWord("soccer", "bóng đá", "⚽", "I play soccer.", "Tôi chơi bóng đá."),
            createWord("basketball", "bóng rổ", "🏀", "Basketball is fun.", "Bóng rổ vui."),
            createWord("swimming", "bơi lội", "🏊", "I like swimming.", "Tôi thích bơi."),
            createWord("running", "chạy bộ", "🏃", "Running is good.", "Chạy bộ tốt."),
            createWord("cycling", "đạp xe", "🚴", "I go cycling.", "Tôi đi đạp xe."),
            createWord("tennis", "quần vợt", "🎾", "Tennis is exciting.", "Quần vợt hấp dẫn.")
        ));
        zones.add(sportZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * EMOTION PLANET - Future Era
     */
    private static Planet createEmotionPlanet() {
        Planet planet = new Planet("emotion", "Emotion Planet", "Hành tinh Cảm xúc", "😊", 0xFFFCD34D, ERA_FUTURE);
        planet.setRequiredStars(250);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Feelings
        Zone feelingZone = new Zone("feeling", "Feelings", "Cảm xúc", "💭");
        feelingZone.setWords(Arrays.asList(
            createWord("happy", "vui", "😊", "I am happy.", "Tôi vui."),
            createWord("sad", "buồn", "😢", "I feel sad.", "Tôi buồn."),
            createWord("angry", "giận", "😠", "Don't be angry.", "Đừng giận."),
            createWord("scared", "sợ", "😨", "I am scared.", "Tôi sợ."),
            createWord("excited", "hào hứng", "🤩", "I am excited.", "Tôi hào hứng."),
            createWord("tired", "mệt", "😴", "I am tired.", "Tôi mệt."),
            createWord("hungry", "đói", "🍽️", "I am hungry.", "Tôi đói."),
            createWord("thirsty", "khát", "💧", "I am thirsty.", "Tôi khát.")
        ));
        feelingZone.setSentences(Arrays.asList(
            new Sentence("How are you?", "Bạn khỏe không?", new String[]{"How", "are"}),
            new Sentence("I am fine.", "Tôi khỏe.", new String[]{"fine"}),
            new Sentence("Are you happy?", "Bạn vui không?", new String[]{"happy"})
        ));
        zones.add(feelingZone);

        // Zone 2: Descriptions
        Zone descZone = new Zone("describe", "Descriptions", "Mô tả", "📝");
        descZone.setWords(Arrays.asList(
            createWord("big", "to", "🐘", "The elephant is big.", "Con voi to."),
            createWord("small", "nhỏ", "🐜", "The ant is small.", "Con kiến nhỏ."),
            createWord("tall", "cao", "🦒", "The giraffe is tall.", "Hươu cao cổ cao."),
            createWord("short", "thấp", "🐕", "The dog is short.", "Con chó thấp."),
            createWord("fast", "nhanh", "🐆", "The cheetah is fast.", "Báo nhanh."),
            createWord("slow", "chậm", "🐢", "The turtle is slow.", "Rùa chậm."),
            createWord("beautiful", "đẹp", "🌸", "The flower is beautiful.", "Bông hoa đẹp."),
            createWord("cute", "dễ thương", "🐱", "The kitten is cute.", "Mèo con dễ thương.")
        ));
        zones.add(descZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * TRAVEL PLANET - Future Era
     */
    private static Planet createTravelPlanet() {
        Planet planet = new Planet("travel", "Travel Planet", "Hành tinh Du lịch", "✈️", 0xFF38BDF8, ERA_FUTURE);
        planet.setRequiredStars(300);

        List<Zone> zones = new ArrayList<>();

        // Zone 1: Vehicles
        Zone vehicleZone = new Zone("vehicle", "Vehicles", "Phương tiện", "🚗");
        vehicleZone.setWords(Arrays.asList(
            createWord("car", "xe hơi", "🚗", "I go by car.", "Tôi đi bằng xe hơi."),
            createWord("bus", "xe buýt", "🚌", "I take the bus.", "Tôi đi xe buýt."),
            createWord("train", "tàu hỏa", "🚂", "The train is fast.", "Tàu hỏa nhanh."),
            createWord("plane", "máy bay", "✈️", "The plane flies.", "Máy bay bay."),
            createWord("ship", "tàu thuyền", "🚢", "The ship sails.", "Tàu thuyền đi."),
            createWord("bicycle", "xe đạp", "🚲", "I ride a bicycle.", "Tôi đạp xe."),
            createWord("rocket", "tên lửa", "🚀", "The rocket goes to space.", "Tên lửa đi vào vũ trụ."),
            createWord("helicopter", "trực thăng", "🚁", "The helicopter hovers.", "Trực thăng bay lơ lửng.")
        ));
        vehicleZone.setSentences(Arrays.asList(
            new Sentence("I want to travel.", "Tôi muốn đi du lịch.", new String[]{"want", "travel"}),
            new Sentence("I go by plane.", "Tôi đi bằng máy bay.", new String[]{"go", "plane"}),
            new Sentence("Let's explore!", "Hãy khám phá nào!", new String[]{"explore"})
        ));
        zones.add(vehicleZone);

        // Zone 2: Places
        Zone placeZone = new Zone("place", "Places", "Địa điểm", "🗺️");
        placeZone.setWords(Arrays.asList(
            createWord("city", "thành phố", "🌆", "The city is busy.", "Thành phố nhộn nhịp."),
            createWord("beach", "bãi biển", "🏖️", "I play at the beach.", "Tôi chơi ở biển."),
            createWord("park", "công viên", "🏞️", "I run in the park.", "Tôi chạy trong công viên."),
            createWord("zoo", "vườn thú", "🦁", "Animals live in the zoo.", "Động vật sống ở vườn thú."),
            createWord("museum", "bảo tàng", "🏛️", "I visit the museum.", "Tôi thăm bảo tàng."),
            createWord("library", "thư viện", "📚", "I read at the library.", "Tôi đọc ở thư viện."),
            createWord("hospital", "bệnh viện", "🏥", "Doctors work at hospitals.", "Bác sĩ làm việc ở bệnh viện."),
            createWord("restaurant", "nhà hàng", "🍽️", "We eat at restaurants.", "Chúng tôi ăn ở nhà hàng.")
        ));
        zones.add(placeZone);

        // Zone 3: Space
        Zone spaceZone = new Zone("space", "Space", "Không gian", "🚀");
        spaceZone.setWords(Arrays.asList(
            createWord("space", "không gian", "🌌", "Space is dark.", "Không gian tối."),
            createWord("planet", "hành tinh", "🪐", "Earth is a planet.", "Trái Đất là hành tinh."),
            createWord("moon", "mặt trăng", "🌙", "The moon is bright.", "Mặt trăng sáng."),
            createWord("Earth", "Trái Đất", "🌍", "I live on Earth.", "Tôi sống trên Trái Đất."),
            createWord("Mars", "Sao Hỏa", "🔴", "Mars is red.", "Sao Hỏa màu đỏ."),
            createWord("astronaut", "phi hành gia", "👨‍🚀", "Astronauts go to space.", "Phi hành gia đi vào vũ trụ."),
            createWord("spaceship", "tàu vũ trụ", "🛸", "The spaceship flies.", "Tàu vũ trụ bay.")
        ));
        zones.add(spaceZone);

        planet.setZones(zones);
        return planet;
    }

    /**
     * Helper method to create a word
     */
    private static Word createWord(String english, String vietnamese, String emoji, String example, String exampleVi) {
        Word word = new Word(english, vietnamese, emoji);
        word.setExampleSentence(example);
        word.setExampleTranslation(exampleVi);
        return word;
    }

    /**
     * Get planets by era
     */
    public static List<Planet> getPlanetsByEra(String era) {
        List<Planet> result = new ArrayList<>();
        for (Planet planet : getAllPlanets()) {
            if (planet.getTimeEra().equals(era)) {
                result.add(planet);
            }
        }
        return result;
    }

    /**
     * Get planet by ID
     */
    public static Planet getPlanetById(String id) {
        for (Planet planet : getAllPlanets()) {
            if (planet.getId().equals(id)) {
                return planet;
            }
        }
        return null;
    }

    /**
     * Get all time eras
     */
    public static List<TimeEra> getAllTimeEras() {
        List<TimeEra> eras = new ArrayList<>();
        eras.add(new TimeEra(ERA_PREHISTORIC, "Prehistoric", "Thời tiền sử", "🦕", 0xFF86EFAC));
        eras.add(new TimeEra(ERA_MEDIEVAL, "Medieval", "Thời Trung cổ", "🏰", 0xFFC4B5FD));
        eras.add(new TimeEra(ERA_MODERN, "Modern", "Thời Hiện đại", "🌆", 0xFF93C5FD));
        eras.add(new TimeEra(ERA_FUTURE, "Future", "Tương lai", "🚀", 0xFFF0ABFC));
        return eras;
    }

    /**
     * Helper class for Time Eras
     */
    public static class TimeEra {
        private String id;
        private String name;
        private String nameVi;
        private String emoji;
        private int color;

        public TimeEra(String id, String name, String nameVi, String emoji, int color) {
            this.id = id;
            this.name = name;
            this.nameVi = nameVi;
            this.emoji = emoji;
            this.color = color;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public String getNameVi() { return nameVi; }
        public String getEmoji() { return emoji; }
        public int getColor() { return color; }
    }
}

