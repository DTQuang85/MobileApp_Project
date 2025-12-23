package com.example.engapp;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.*;

import java.util.HashMap;
import java.util.Map;

public class FirestoreRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // === LEVELS & UNITS (Public read) ===
    public Task<QuerySnapshot> getLevels() {
        return db.collection("levels").orderBy("order").get();
    }

    public Task<QuerySnapshot> getUnits(String levelId) {
        return db.collection("levels")
                .document(levelId)
                .collection("units")
                .orderBy("order")
                .get();
    }

    public Task<QuerySnapshot> getQuestions(String levelId, String unitId) {
        return db.collection("levels")
                .document(levelId)
                .collection("units")
                .document(unitId)
                .collection("questions")
                .orderBy("order")
                .get();
    }

    // === PROGRESS & USER DATA ===
    public Task<DocumentSnapshot> getUserProgress(String uid) {
        return db.collection("users").document(uid).get();
    }

    public Task<Void> saveQuizResult(String uid, String levelId, String unitId, int score, int totalQuestions) {
        String progressId = uid + "_" + levelId + "_" + unitId;
        boolean passed = score >= 70;

        Map<String, Object> progressData = new HashMap<>();
        progressData.put("userId", uid);
        progressData.put("levelId", levelId);
        progressData.put("unitId", unitId);
        progressData.put("score", score);
        progressData.put("totalQuestions", totalQuestions);
        progressData.put("percentage", (score * 100.0) / totalQuestions);
        progressData.put("passed", passed);
        progressData.put("completedAt", FieldValue.serverTimestamp());

        return db.collection("progress").document(progressId).set(progressData, SetOptions.merge());
    }

    public Task<DocumentSnapshot> getUserData(String uid) {
        return db.collection("users").document(uid).get();
    }

    public Task<Void> updateUserLevel(String uid, int newLevelUnlocked) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("levelUnlocked", newLevelUnlocked);
        userData.put("lastActive", FieldValue.serverTimestamp());

        return db.collection("users").document(uid).set(userData, SetOptions.merge());
    }
}