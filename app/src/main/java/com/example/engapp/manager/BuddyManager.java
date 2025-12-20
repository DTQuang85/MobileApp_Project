package com.example.engapp.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;

import com.example.engapp.model.BuddyState;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Singleton manager for Buddy companion behavior across the app.
 * Handles state transitions, speech, and reactions.
 */
public class BuddyManager implements TextToSpeech.OnInitListener {

    private static BuddyManager instance;
    private Context context;
    private SharedPreferences prefs;
    private Gson gson;
    private TextToSpeech tts;
    private boolean ttsReady = false;

    private BuddyState buddyState;
    private List<BuddyEventListener> listeners;
    private Handler handler;
    private Random random;

    // Speech contexts
    public static final String CONTEXT_GREETING_MORNING = "greeting_morning";
    public static final String CONTEXT_GREETING_AFTERNOON = "greeting_afternoon";
    public static final String CONTEXT_GREETING_EVENING = "greeting_evening";
    public static final String CONTEXT_GREETING_RETURN = "greeting_return";
    public static final String CONTEXT_CORRECT_ANSWER = "correct_answer";
    public static final String CONTEXT_WRONG_ANSWER = "wrong_answer";
    public static final String CONTEXT_HINT = "hint";
    public static final String CONTEXT_TRAVEL_START = "travel_start";
    public static final String CONTEXT_TRAVEL_DURING = "travel_during";
    public static final String CONTEXT_TRAVEL_ARRIVE = "travel_arrive";
    public static final String CONTEXT_CELEBRATION = "celebration";
    public static final String CONTEXT_ENCOURAGEMENT = "encouragement";
    public static final String CONTEXT_LEVEL_UP = "level_up";
    public static final String CONTEXT_UNLOCK_PLANET = "unlock_planet";
    public static final String CONTEXT_GOODBYE = "goodbye";
    public static final String CONTEXT_IDLE_TAP = "idle_tap";

    private static final String PREFS_NAME = "buddy_prefs";
    private static final String KEY_BUDDY_STATE = "buddy_state";

    public interface BuddyEventListener {
        void onStateChanged(String newState, String previousState);
        void onBuddySpeak(String message);
        void onMoodChanged(int newMood);
        void onBuddyLevelUp(int newLevel);
    }

    private BuddyManager(Context context) {
        this.context = context.getApplicationContext();
        this.prefs = this.context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.gson = new Gson();
        this.listeners = new ArrayList<>();
        this.handler = new Handler(Looper.getMainLooper());
        this.random = new Random();

        loadBuddyState();
        initTTS();
    }

    public static synchronized BuddyManager getInstance(Context context) {
        if (instance == null) {
            instance = new BuddyManager(context);
        }
        return instance;
    }

    private void initTTS() {
        tts = new TextToSpeech(context, this);
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.US);
            ttsReady = (result != TextToSpeech.LANG_MISSING_DATA &&
                        result != TextToSpeech.LANG_NOT_SUPPORTED);
        }
    }

    // State Management
    public void loadBuddyState() {
        String json = prefs.getString(KEY_BUDDY_STATE, null);
        if (json != null) {
            buddyState = gson.fromJson(json, BuddyState.class);
        } else {
            buddyState = new BuddyState();
            saveBuddyState();
        }
    }

    public void saveBuddyState() {
        String json = gson.toJson(buddyState);
        prefs.edit().putString(KEY_BUDDY_STATE, json).apply();
    }

    public BuddyState getBuddyState() {
        return buddyState;
    }

    public String getCurrentState() {
        return buddyState.getCurrentState();
    }

    public void transitionToState(String newState) {
        String previousState = buddyState.getCurrentState();
        if (!previousState.equals(newState)) {
            buddyState.setCurrentState(newState);
            buddyState.recordInteraction();
            saveBuddyState();

            for (BuddyEventListener listener : listeners) {
                listener.onStateChanged(newState, previousState);
            }
        }
    }

    // Event Handlers
    public void onCorrectAnswer() {
        transitionToState(BuddyState.STATE_HAPPY);
        buddyState.increaseMood(5);
        String speech = getSpeechForContext(CONTEXT_CORRECT_ANSWER);
        speak(speech);

        // Return to idle after delay
        handler.postDelayed(() -> transitionToState(BuddyState.STATE_IDLE), 2000);
    }

    public void onWrongAnswer() {
        transitionToState(BuddyState.STATE_ENCOURAGING);
        String speech = getSpeechForContext(CONTEXT_WRONG_ANSWER);
        speak(speech);

        handler.postDelayed(() -> transitionToState(BuddyState.STATE_IDLE), 2000);
    }

    public void onHintRequested() {
        transitionToState(BuddyState.STATE_THINKING);
        String speech = getSpeechForContext(CONTEXT_HINT);
        speak(speech);

        handler.postDelayed(() -> transitionToState(BuddyState.STATE_IDLE), 3000);
    }

    public void onTravelStart() {
        transitionToState(BuddyState.STATE_TRAVELING);
        String speech = getSpeechForContext(CONTEXT_TRAVEL_START);
        speak(speech);
    }

    public void onTravelArrive(String planetName) {
        transitionToState(BuddyState.STATE_CELEBRATING);
        String speech = getSpeechForContext(CONTEXT_TRAVEL_ARRIVE)
            .replace("{planet}", planetName);
        speak(speech);

        handler.postDelayed(() -> transitionToState(BuddyState.STATE_IDLE), 3000);
    }

    public void onZoneComplete() {
        transitionToState(BuddyState.STATE_CELEBRATING);
        buddyState.increaseMood(10);
        String speech = getSpeechForContext(CONTEXT_CELEBRATION);
        speak(speech);

        checkBuddyLevelUp();
        handler.postDelayed(() -> transitionToState(BuddyState.STATE_IDLE), 3000);
    }

    public void onPlanetUnlock(String planetName) {
        transitionToState(BuddyState.STATE_CELEBRATING);
        String speech = getSpeechForContext(CONTEXT_UNLOCK_PLANET)
            .replace("{planet}", planetName);
        speak(speech);

        handler.postDelayed(() -> transitionToState(BuddyState.STATE_IDLE), 4000);
    }

    public void onUserTapBuddy() {
        buddyState.recordInteraction();
        buddyState.increaseMood(2);
        String speech = getSpeechForContext(CONTEXT_IDLE_TAP);
        speak(speech);
        saveBuddyState();
    }

    public void onAppOpen() {
        buddyState.recordInteraction();
        String greetingContext = getTimeBasedGreetingContext();
        String speech = getSpeechForContext(greetingContext);
        speak(speech);
        transitionToState(BuddyState.STATE_HAPPY);

        handler.postDelayed(() -> transitionToState(BuddyState.STATE_IDLE), 3000);
    }

    public void onAppClose() {
        String speech = getSpeechForContext(CONTEXT_GOODBYE);
        speak(speech);
        saveBuddyState();
    }

    // Speech System
    public void speak(String message) {
        for (BuddyEventListener listener : listeners) {
            listener.onBuddySpeak(message);
        }

        // Only speak English words with TTS
        if (ttsReady && containsEnglish(message)) {
            String englishPart = extractEnglish(message);
            if (!englishPart.isEmpty()) {
                tts.speak(englishPart, TextToSpeech.QUEUE_FLUSH, null, "buddy_speech");
            }
        }
    }

    public String getSpeechForContext(String context) {
        String buddyId = buddyState.getCurrentBuddyId();
        String[][] speeches = getSpeechesForBuddy(buddyId);

        int contextIndex = getContextIndex(context);
        if (contextIndex >= 0 && contextIndex < speeches.length) {
            String[] options = speeches[contextIndex];
            return options[random.nextInt(options.length)];
        }

        return "...";
    }

    private String getTimeBasedGreetingContext() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= 5 && hour < 12) {
            return CONTEXT_GREETING_MORNING;
        } else if (hour >= 12 && hour < 18) {
            return CONTEXT_GREETING_AFTERNOON;
        } else {
            return CONTEXT_GREETING_EVENING;
        }
    }

    private int getContextIndex(String context) {
        switch (context) {
            case CONTEXT_GREETING_MORNING: return 0;
            case CONTEXT_GREETING_AFTERNOON: return 1;
            case CONTEXT_GREETING_EVENING: return 2;
            case CONTEXT_CORRECT_ANSWER: return 3;
            case CONTEXT_WRONG_ANSWER: return 4;
            case CONTEXT_HINT: return 5;
            case CONTEXT_TRAVEL_START: return 6;
            case CONTEXT_TRAVEL_ARRIVE: return 7;
            case CONTEXT_CELEBRATION: return 8;
            case CONTEXT_ENCOURAGEMENT: return 9;
            case CONTEXT_GOODBYE: return 10;
            case CONTEXT_IDLE_TAP: return 11;
            case CONTEXT_UNLOCK_PLANET: return 12;
            case CONTEXT_LEVEL_UP: return 13;
            default: return -1;
        }
    }

    private String[][] getSpeechesForBuddy(String buddyId) {
        switch (buddyId) {
            case BuddyState.BUDDY_ROBOT:
                return getRobotSpeeches();
            case BuddyState.BUDDY_ALIEN:
                return getAlienSpeeches();
            case BuddyState.BUDDY_CAT:
                return getCatSpeeches();
            case BuddyState.BUDDY_FOX:
                return getFoxSpeeches();
            default:
                return getRobotSpeeches();
        }
    }

    private String[][] getRobotSpeeches() {
        return new String[][] {
            // Morning
            {"Beep boop! Good morning, Captain! ☀️", "Rise and shine! Systems online! 🤖", "Good morning! Ready to learn? 🌟"},
            // Afternoon
            {"Hello, Captain! Afternoon check! 🤖", "Beep! Good afternoon! Let's explore! 🚀", "Systems ready! Time to learn! ⚡"},
            // Evening
            {"Good evening, Captain! 🌙", "Evening mode activated! Let's study! 🤖", "Night learning time! Stars await! ✨"},
            // Correct
            {"Correct! Processing... AMAZING! 🎉", "Beep beep! Perfect answer! 💯", "Yes! You're super smart! 🌟", "Woohoo! That's right! 🤖"},
            // Wrong
            {"Oops! Let's try again! You can do it! 💪", "Almost! Don't give up! 🤖", "Recalculating... Try once more! 🔧"},
            // Hint
            {"Computing hint... Here's a clue! 💡", "Let me help! Look carefully... 🤖", "Analyzing... Maybe try this? 🔍"},
            // Travel Start
            {"Engines activated! Let's go! 🚀", "Blast off! Hold tight! 🤖", "Adventure time! Wheee! ⚡"},
            // Travel Arrive
            {"Landing on {planet}! How exciting! 🌟", "We made it to {planet}! 🤖", "Welcome to {planet}! Let's explore! 🚀"},
            // Celebration
            {"AMAZING! You're incredible! 🎉", "Celebration protocol activated! 🤖", "Fantastic job, Captain! 🌟"},
            // Encouragement
            {"You're doing great! Keep going! 💪", "I believe in you! 🤖", "Don't give up! You're awesome! 🌟"},
            // Goodbye
            {"Goodbye! I'll guard the ship! 👋", "See you soon, Captain! 🤖", "Sleep mode... Miss you! 💤"},
            // Idle Tap
            {"Beep! Hello there! 🤖", "Yes, Captain? 👋", "I'm here to help! ⚡", "Whirr... That tickles! 😊"},
            // Unlock Planet
            {"WOW! New planet {planet} unlocked! 🌟", "Alert! {planet} is now available! 🚀", "Exciting! Let's visit {planet}! 🤖"},
            // Level Up
            {"LEVEL UP! You're getting stronger! 🎉", "New level! Amazing progress! 🤖", "Wow! You're so smart! 🌟"}
        };
    }

    private String[][] getAlienSpeeches() {
        return new String[][] {
            // Morning
            {"Zog zog! Good morning! ☀️", "Greetings, Earth friend! 👽", "Morning from space! 🛸"},
            // Afternoon
            {"Hello, Earth Captain! 👽", "Afternoon vibes! Let's learn! 🛸", "Zog! Ready for fun? ✨"},
            // Evening
            {"Good evening, friend! 🌙", "Night sky is beautiful! 👽", "Stars are out! Like home! 🛸"},
            // Correct
            {"Zog zog! Correct! 🎉", "Woohoo! Smart human! 👽", "Yes yes! Amazing! 🛸"},
            // Wrong
            {"Zog... Try again! You'll get it! 💪", "On my planet, we try many times! 👽", "Almost there! Keep going! 🛸"},
            // Hint
            {"Zog gives hint! Listen... 💡", "Let me whisper a secret... 👽", "Here's alien wisdom... 🔮"},
            // Travel Start
            {"Wheee! Space travel! 🛸", "Like going home! Exciting! 👽", "Zog zog! Blast off! 🚀"},
            // Travel Arrive
            {"{planet}! Beautiful! 🌟", "Zog! We're at {planet}! 👽", "New world to explore! 🛸"},
            // Celebration
            {"ZOG ZOG ZOG! Amazing! 🎉", "Happy dance time! 👽", "You're a star, friend! 🌟"},
            // Encouragement
            {"Zog believes in you! 💪", "You're amazing! Keep going! 👽", "Earth friends are the best! 🛸"},
            // Goodbye
            {"Zog zog! See you! 👋", "Bye bye, friend! 👽", "Until next time! 🛸"},
            // Idle Tap
            {"Zog? Yes? 👽", "Hello, friend! 👋", "Zog is happy to see you! 💚", "Tickle tickle! 😊"},
            // Unlock Planet
            {"ZOG! New planet {planet}! 🌟", "Exciting! {planet} awaits! 🛸", "Let's visit {planet}! 👽"},
            // Level Up
            {"LEVEL UP! So proud! 🎉", "You're becoming space master! 👽", "Amazing progress! 🌟"}
        };
    }

    private String[][] getCatSpeeches() {
        return new String[][] {
            // Morning
            {"Meow! Good morning! ☀️", "Purr~ Wake up time! 🐱", "Meow meow! Let's play! 🌟"},
            // Afternoon
            {"Mew! Afternoon! 🐱", "Purr~ Learning time! 🎀", "Meow! Ready to explore? ✨"},
            // Evening
            {"Meow~ Good evening! 🌙", "Purr~ Night adventure! 🐱", "Stars are pretty! Meow! ✨"},
            // Correct
            {"Purrrr! Correct! 🎉", "Meow meow! Amazing! 🐱", "Yes! You're purrfect! 💕"},
            // Wrong
            {"Mew... Try again! 💪", "It's okay! Cats try many times! 🐱", "Purr~ You can do it! 💕"},
            // Hint
            {"Meow~ Here's a hint! 💡", "Let Kitty help! 🐱", "Purr~ Look here... 🔍"},
            // Travel Start
            {"Meow! Adventure! 🚀", "Whee! So fun! 🐱", "Purr~ Let's go! ✨"},
            // Travel Arrive
            {"Meow! We're at {planet}! 🌟", "Purr~ {planet} is pretty! 🐱", "New place to nap! Meow! 💕"},
            // Celebration
            {"MEOW MEOW! So happy! 🎉", "Purrrr! You did it! 🐱", "Dancing time! 💕"},
            // Encouragement
            {"Purr~ You're doing great! 💪", "Kitty believes in you! 🐱", "Keep going! Meow! 💕"},
            // Goodbye
            {"Bye bye! Meow! 👋", "See you soon! Purr~ 🐱", "Nap time... Miss you! 💤"},
            // Idle Tap
            {"Meow? 🐱", "Purr~ Hi! 👋", "Pet pet! Nice! 💕", "Meow meow! 😊"},
            // Unlock Planet
            {"MEOW! New planet {planet}! 🌟", "Purr~ {planet} looks fun! 🐱", "Let's explore {planet}! 💕"},
            // Level Up
            {"LEVEL UP! Meow! 🎉", "So proud of you! Purr~ 🐱", "You're amazing! 🌟"}
        };
    }

    private String[][] getFoxSpeeches() {
        return new String[][] {
            // Morning
            {"Yip yip! Good morning! ☀️", "Rise and shine, friend! 🦊", "Morning adventure awaits! 🌟"},
            // Afternoon
            {"Hello, friend! 🦊", "Afternoon exploring! ✨", "Yip! Let's learn! 🌳"},
            // Evening
            {"Good evening! 🌙", "Night time wisdom! 🦊", "Stars guide us! ✨"},
            // Correct
            {"Yip yip! Correct! 🎉", "Smart as a fox! 🦊", "Yes! Amazing! 🌟"},
            // Wrong
            {"Yip... Try again! 💪", "Foxes are persistent! 🦊", "You'll get it! Keep going! 🍃"},
            // Hint
            {"Fox wisdom says... 💡", "Here's a clever hint! 🦊", "Let me guide you... 🔍"},
            // Travel Start
            {"Quick like a fox! 🚀", "Adventure time! 🦊", "Yip yip! Let's go! ✨"},
            // Travel Arrive
            {"We're at {planet}! 🌟", "{planet}! New territory! 🦊", "Time to explore {planet}! 🍃"},
            // Celebration
            {"YIP YIP! Victory! 🎉", "Fox dance time! 🦊", "You're incredible! 🌟"},
            // Encouragement
            {"Be brave! You can do it! 💪", "Fox believes in you! 🦊", "Keep going, friend! 🌟"},
            // Goodbye
            {"Farewell, friend! 👋", "Until next adventure! 🦊", "Rest well! 💤"},
            // Idle Tap
            {"Yip? 🦊", "Hello there! 👋", "Fox is happy! 🧡", "What shall we do? 😊"},
            // Unlock Planet
            {"YIP! New planet {planet}! 🌟", "{planet} discovered! 🦊", "New adventure awaits at {planet}! 🍃"},
            // Level Up
            {"LEVEL UP! Yip yip! 🎉", "Growing stronger! 🦊", "Wise like a fox! 🌟"}
        };
    }

    // Helper methods
    private boolean containsEnglish(String text) {
        return text.matches(".*[a-zA-Z]+.*");
    }

    private String extractEnglish(String text) {
        // Extract English words for TTS
        StringBuilder english = new StringBuilder();
        String[] words = text.split("\\s+");
        for (String word : words) {
            if (word.matches("[a-zA-Z]+[!?.]*")) {
                english.append(word).append(" ");
            }
        }
        return english.toString().trim();
    }

    private void checkBuddyLevelUp() {
        int interactions = buddyState.getTotalInteractions();
        int expectedLevel = (interactions / 50) + 1;

        if (expectedLevel > buddyState.getBuddyLevel()) {
            buddyState.setBuddyLevel(expectedLevel);
            saveBuddyState();

            for (BuddyEventListener listener : listeners) {
                listener.onBuddyLevelUp(expectedLevel);
            }

            String speech = getSpeechForContext(CONTEXT_LEVEL_UP);
            speak(speech);
        }
    }

    // Buddy Selection
    public void selectBuddy(String buddyId) {
        if (buddyState.isBuddyUnlocked(buddyId)) {
            buddyState.setCurrentBuddyId(buddyId);
            saveBuddyState();
        }
    }

    public void unlockBuddy(String buddyId) {
        buddyState.unlockBuddy(buddyId);
        saveBuddyState();
    }

    public String getCurrentBuddyEmoji() {
        return buddyState.getBuddyEmoji();
    }

    public String getCurrentBuddyName() {
        return buddyState.getBuddyName();
    }

    // Listeners
    public void addListener(BuddyEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(BuddyEventListener listener) {
        listeners.remove(listener);
    }

    // Cleanup
    public void shutdown() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        handler.removeCallbacksAndMessages(null);
        saveBuddyState();
    }
}

