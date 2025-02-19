package com.release.qquimz;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.release.qquimz.databinding.ActivityQnaPostBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class QnaPostActivity extends AppCompatActivity {
    ActivityQnaPostBinding binding;
    FirebaseFirestore db;
    QnaItem qnaItem;
    EditText title;
    EditText content;
    LocalDate now;
    FirebaseAuth auth;
    String uid;
    String nickname;
    String quizTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityQnaPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();

        title = binding.postTitle;
        content = binding.postContent;

        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            nickname = task.getResult().getString("nickname");
        });


        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.btnPost.setOnClickListener(new View.OnClickListener() {
            // 파이어 베이스로 보냄
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                now = LocalDate.now();
                Map<String, Object> data = new HashMap<>();
                data.put("title", title.getText().toString());
                data.put("content", content.getText().toString());
                data.put("likeCnt", "0");
                data.put("commentCnt", 0);
                data.put("date", QnaPostActivity.this.toString(now));
                data.put("nickname", nickname);
                data.put("checked", "0");
                data.put("quizTitle", quizTitle);
                db.collection("Q&A").document(title.getText().toString()).set(data)
                        .addOnSuccessListener(documentReference -> {
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            System.out.println("에러");
                        });
                Map<String, Object> comment = new HashMap<>();
                comment.put("commentCnt", 0);
                db.collection("Comment").document(title.getText().toString()).set(data);
                finish();
            }
        });

        binding.btnQuestion.setOnClickListener(view -> {
            Intent intent = new Intent(this, FindQuestionActivity.class);
            startActivityForResult(intent, 1000);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000 && resultCode == 1000) {
            binding.questionTitle.setText(data.getStringExtra("content"));
            quizTitle = data.getStringExtra("content");
        }
    }

    public String toString(LocalDate now) {
        String date = now.toString();
        return date;
    }
}