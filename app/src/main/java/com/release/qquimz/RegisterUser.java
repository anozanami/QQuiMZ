package com.release.qquimz;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterUser {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public RegisterUser() {
        // Initialize Firebase Auth and Firestore
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void registerNewUser(String email, String password) {
        // Create a new user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // User registration successful
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String uid = user.getUid();
                                addUserToFirestore(uid);
                            }
                        } else {
                            // Handle registration failure
                            System.out.println("Registration failed: " + task.getException());
                        }
                    }
                });
    }

    public void addUserToFirestore(String uid) {
        // Create a map with initial field values
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", uid);
        userData.put("isFriend", "[]"); // Empty array as string representation
        userData.put("points", "0");


        // Add the user document to Firestore under the 'users' collection
        db.collection("users").document(uid)
                .set(userData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            System.out.println("User added to Firestore successfully!");
                        } else {
                            System.out.println("Failed to add user to Firestore: " + task.getException());
                        }
                    }
                });
    }
}

