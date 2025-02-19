package com.release.qquimz;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.release.qquimz.databinding.ActivityQuizBinding;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuizActivity extends AppCompatActivity {
    private ActivityQuizBinding binding;
    private FirebaseFirestore myFirestore;
    private String category;
    private String title;
    private Long genre;
    private String addition;
    private Long difficulty;
    private Long cate;
    private Long timeLimit;
    private List<String> selection;
    private List<String> stringDivided;
    private Long resultNumber;
    private String result;
    private String content;
    private List<HistoryItem> historyItems;
    private LocalDate now;
    Map<String, Object> data = new HashMap<>();
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            return true; // 뒤에 있는 함수를 호출하지 마라. false면 이후의 함수를 호출해라. 함
            // 함수의 type이 boolean이면 이후에 동작할 추가적인 함수(동작)이 있다.
        }
        return super.onKeyDown(keyCode, event); // back button이 눌린게 아니면 부모가 알아서 처리해라.
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityQuizBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            now = LocalDate.now();
        }
        myFirestore = FirebaseFirestore.getInstance();
        // 퀴즈를 가져옴
        myFirestore.collection("quiz")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            List<QueryDocumentSnapshot> documents = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                documents.add(document);
                            }

                            // 랜덤하게 데이터 섞기
                            Collections.shuffle(documents);

                            // 섞은 뒤, 퀴즈 데이터를 하나 가져옴
                            for (QueryDocumentSnapshot document : documents) {
                                // 각 필드 값을 개별적으로 가져오기
                                content = document.getString("content");
                                cate = document.getLong("category");
                                if(cate == 0){
                                    category = "국어";
                                }else if(cate == 1){
                                    category = "문화";
                                }else if(cate == 2){
                                    category = "영어";
                                }else if(cate == 3){
                                    category = "사회";
                                }else if(cate == 4){
                                    category = "과학";
                                }
                                binding.quizCategory.setText(category);
                                binding.quizQuestion.setText(document.getString("content"));
                                genre = document.getLong("genre");
                                title = document.getString("title");
                                addition = document.getString("addition");
                                if(addition.length() == 0){
                                    binding.quizAddition.setVisibility(View.GONE);
                                }else{
                                    binding.quizAddition.setText(document.getString("addition"));
                                }
                                difficulty = document.getLong("difficulty");
                                if(difficulty >= 1) binding.levelStar1.setImageResource(R.drawable.level_star_24_colored);
                                if(difficulty >= 2) binding.levelStar2.setImageResource(R.drawable.level_star_24_colored);
                                if(difficulty >= 3) binding.levelStar3.setImageResource(R.drawable.level_star_24_colored);
                                if(difficulty >= 4) binding.levelStar4.setImageResource(R.drawable.level_star_24_colored);
                                if(difficulty >= 5) binding.levelStar5.setImageResource(R.drawable.level_star_24_colored);

                                timeLimit = document.getLong("timeLimit");
                                binding.timeLeft.setText(timeLimit + "");
                                selection = (List<String>) document.get("selection");
                                stringDivided = (List<String>) document.get("stringDivided");
                                data.put("date", now.toString());
                                data.put("title", title);
                                data.put("content", content);
                                data.put("category", category);
                                break;
                            }

                            // 타이머 구현
                            Thread ProgThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for (int i=1 ; i<=100 ; i++){
                                        UpdateProg(i);
                                        SystemClock.sleep(timeLimit*10);
                                    }
                                }
                            });
                            ProgThread.start();
                            Thread TLThread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    for (Long i=timeLimit; i>0; i--){
                                        UpdateTL(i);
                                        SystemClock.sleep(1000);
                                    }
                                }
                            });
                            TLThread.start();
                            // 선택지 구현
                            if(genre == 2){
                                transferTo(Genre2Fragment.newInstance(selection, timeLimit, title));
                            }else if(genre == 5){
                                Genre5Fragment fragment = Genre5Fragment.newInstance(selection, stringDivided, timeLimit, title);
                                transferTo(fragment);
                            }
                        } else {
                            Log.w("QQuiMZ", "Error getting documents", task.getException());
                        }
                        myFirestore.collection("History").document(title).set(data);
                    }
                });
        binding.goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(QuizActivity.this, MainActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });
    }

    private void UpdateProg(int i){
        binding.timeLeftBar.post(new Runnable() {
            @Override
            public void run() {
                binding.timeLeftBar.setProgress(100-i,true);
            }
        });
    }
    private void UpdateTL(Long i){
        binding.timeLeft.post(new Runnable() {
            @Override
            public void run() {
                binding.timeLeft.setText(i>=10 ? "" + i : "0" + i);
            }
        });
    }
    // 실제로 fragment의 교체를 담당하는 구간
    private void transferTo(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.selection_container,fragment)
                .commit();
    }
    public String getCategory(Long categoryNumber) {
        if(categoryNumber == 1){
            return "문화";
        } else if(categoryNumber == 2){
            return "영어";
        } else if(categoryNumber == 3){
            return "사회";
        } else if(categoryNumber == 4){
            return "과학";
        } else if(categoryNumber == 0){
            return "국어";
        }
        return null;
    }
}