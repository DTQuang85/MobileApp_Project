package com.example.engapp.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.engapp.models.Lesson;
import com.example.engapp.models.Question;
import com.example.engapp.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DatabaseHelper {
    private static final String TAG = "DatabaseHelper";
    private static DatabaseHelper instance;
    private DatabaseReference databaseRef;

    private DatabaseHelper() {
        databaseRef = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized DatabaseHelper getInstance() {
        if (instance == null) {
            instance = new DatabaseHelper();
        }
        return instance;
    }

    // User operations
    public void createOrUpdateUser(String uid, String email, String displayName) {
        DatabaseReference userRef = databaseRef.child("users").child(uid);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create new user
                    User user = new User(uid, email, displayName, 0, 0);
                    userRef.setValue(user)
                            .addOnSuccessListener(aVoid -> Log.d(TAG, "User created successfully"))
                            .addOnFailureListener(e -> Log.e(TAG, "Failed to create user", e));
                } else {
                    // Update existing user
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("email", email);
                    updates.put("displayName", displayName);
                    userRef.updateChildren(updates);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking user existence", error.toException());
            }
        });
    }

    public void getUserData(String uid, UserDataCallback callback) {
        databaseRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    callback.onUserDataReceived(user);
                } else {
                    callback.onError(new Exception("User not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void updateUserScore(String uid, int scoreToAdd) {
        DatabaseReference userRef = databaseRef.child("users").child(uid);
        userRef.child("totalScore").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer currentScore = snapshot.getValue(Integer.class);
                if (currentScore == null) currentScore = 0;
                userRef.child("totalScore").setValue(currentScore + scoreToAdd);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to update score", error.toException());
            }
        });
    }

    public void incrementLessonsCompleted(String uid) {
        DatabaseReference userRef = databaseRef.child("users").child(uid);
        userRef.child("lessonsCompleted").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Integer lessonsCompleted = snapshot.getValue(Integer.class);
                if (lessonsCompleted == null) lessonsCompleted = 0;
                userRef.child("lessonsCompleted").setValue(lessonsCompleted + 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to increment lessons", error.toException());
            }
        });
    }

    // Lesson operations
    public void initializeLessons() {
        DatabaseReference lessonsRef = databaseRef.child("lessons");

        lessonsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Create sample lessons
                    createSampleLessons();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Error checking lessons", error.toException());
            }
        });
    }

    private void createSampleLessons() {
        List<Lesson> lessons = new ArrayList<>();

        // Lesson 1: Greetings
        List<Question> lesson1Questions = Arrays.asList(
                new Question(
                        "How do you greet someone in the morning?",
                        Arrays.asList("Good night", "Good morning", "Good evening", "Goodbye"),
                        1,
                        "We use 'Good morning' to greet people in the morning."
                ),
                new Question(
                        "What does 'Hello' mean?",
                        Arrays.asList("Goodbye", "Thank you", "Greeting", "Sorry"),
                        2,
                        "'Hello' is a common greeting used at any time of day."
                )
        );
        lessons.add(new Lesson("1", "Greetings", "Learn basic greetings", lesson1Questions));

        // Lesson 2: Numbers
        List<Question> lesson2Questions = Arrays.asList(
                new Question(
                        "What comes after 'one'?",
                        Arrays.asList("three", "two", "four", "five"),
                        1,
                        "The number sequence is: one, two, three..."
                ),
                new Question(
                        "How do you spell the number 5?",
                        Arrays.asList("for", "fiv", "five", "fife"),
                        2,
                        "The correct spelling is 'five'."
                )
        );
        lessons.add(new Lesson("2", "Numbers", "Learn to count in English", lesson2Questions));

        // Lesson 3: Colors
        List<Question> lesson3Questions = Arrays.asList(
                new Question(
                        "What color is the sky?",
                        Arrays.asList("green", "red", "blue", "yellow"),
                        2,
                        "The sky is typically blue during the day."
                ),
                new Question(
                        "What color is grass?",
                        Arrays.asList("green", "blue", "red", "purple"),
                        0,
                        "Grass is usually green in color."
                )
        );
        lessons.add(new Lesson("3", "Colors", "Learn basic colors", lesson3Questions));

        // Save lessons to Firebase
        DatabaseReference lessonsRef = databaseRef.child("lessons");
        for (Lesson lesson : lessons) {
            lessonsRef.child(lesson.getId()).setValue(lesson)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "Lesson saved: " + lesson.getTitle()))
                    .addOnFailureListener(e -> Log.e(TAG, "Failed to save lesson", e));
        }
    }

    public void getAllLessons(LessonsCallback callback) {
        databaseRef.child("lessons").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Lesson> lessons = new ArrayList<>();
                for (DataSnapshot lessonSnapshot : snapshot.getChildren()) {
                    Lesson lesson = lessonSnapshot.getValue(Lesson.class);
                    if (lesson != null) {
                        lessons.add(lesson);
                    }
                }
                callback.onLessonsReceived(lessons);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    public void getLesson(String lessonId, LessonCallback callback) {
        databaseRef.child("lessons").child(lessonId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Lesson lesson = snapshot.getValue(Lesson.class);
                if (lesson != null) {
                    callback.onLessonReceived(lesson);
                } else {
                    callback.onError(new Exception("Lesson not found"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.toException());
            }
        });
    }

    // Callback interfaces
    public interface UserDataCallback {
        void onUserDataReceived(User user);
        void onError(Exception e);
    }

    public interface LessonsCallback {
        void onLessonsReceived(List<Lesson> lessons);
        void onError(Exception e);
    }

    public interface LessonCallback {
        void onLessonReceived(Lesson lesson);
        void onError(Exception e);
    }
}

