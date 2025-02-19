package com.release.qquimz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private EditText mEtEmail, mEtPwd;
    private Button mBtnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mEtEmail = findViewById(R.id.editTextTextEmailAddress);
        mEtPwd = findViewById(R.id.editTextTextPassword);
        mBtnRegister = findViewById(R.id.btn_register);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmail = mEtEmail.getText().toString();
                String strPwd = mEtPwd.getText().toString();

                if (!strEmail.isEmpty() && !strPwd.isEmpty()) {
                    registerUser(strEmail, strPwd);
                } else {
                    Toast.makeText(RegisterActivity.this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void registerUser(String email, String password) {
        mFirebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            if (firebaseUser != null) {
                                String uid = firebaseUser.getUid();
                                // Firestore에 사용자 정보 추가
                                addUserToFirestore(uid);
                            }
                            Toast.makeText(RegisterActivity.this, "회원가입에 성공하였습니다.", Toast.LENGTH_SHORT).show();

                            // 회원가입 후 로그인 화면으로 이동
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, "회원가입에 실패하였습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


    private void addUserToFirestore(String uid) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 초기화된 사용자 정보 생성
        UserAccount userAccount = new UserAccount();
        userAccount.setId(uid); // Firebase UID 저장
        userAccount.getIsFriend(); // Friend field 초기화
        userAccount.setPoints("0"); //  points
        userAccount.setIsFriend(new ArrayList<>()); // 빈 배열로 초기화

        // Firestore에 데이터 추가
        db.collection("users").document(uid).set(userAccount)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "사용자 등록 성공"))
                .addOnFailureListener(e -> Log.e("Firestore", "사용자 등록 실패", e));
    }

}
