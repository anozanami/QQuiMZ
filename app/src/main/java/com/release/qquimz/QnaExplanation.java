package com.release.qquimz;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.release.qquimz.R;
import com.release.qquimz.databinding.CommentItemBinding;
import com.release.qquimz.databinding.QnaExplanationBinding;
import com.release.qquimz.ExplanationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QnaExplanation extends AppCompatActivity {
    QnaExplanationBinding binding;
    MyAdapter adapter;
    RecyclerView recyclerView;
    List<CommentItem> commentItems = new ArrayList<>();
    EditText comment;
    long commentCnt;
    FirebaseAuth auth;
    FirebaseFirestore db;
    String uid;
    String nickname;
    LocalDate now;
    String quizTitle;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = QnaExplanationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        comment = binding.comment;
        String title = getIntent().getStringExtra("title");
        Log.d("yyc", "title: " + title);
        auth = FirebaseAuth.getInstance();

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        uid = auth.getCurrentUser().getUid();
        quizTitle = getIntent().getStringExtra("quizTitle");
        Log.d("yyc", "quizTitle: " + quizTitle);

        db.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                nickname = task.getResult().getString("nickname");
            }
        });

        db.collection("Q&A")
                .document(title)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        binding.title.setText(task.getResult().getString("title"));
                        binding.content.setText(task.getResult().getString("content"));
                        binding.likeCnt.setText(task.getResult().getString("likeCnt"));
                        binding.nickname.setText(task.getResult().getString("nickname"));
                        binding.date.setText(task.getResult().getString("date"));
                        String checked = task.getResult().getString("checked");
                        Log.d("yyc", "checkes: " + checked);
                        if(checked.equals("1")) {
                            binding.iconLike.setImageResource(R.drawable.icon_like_fill);
                        }
                    }
                });

        db.collection("Comment")
                .document(title)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            commentCnt = document.getLong("commentCnt");
                            if(commentCnt > 0) {
                                Log.d("yyc", "onComplete: " + commentCnt);
                                for (int i = 1; i <= commentCnt; i++) {
                                    commentItems.add(new CommentItem(document.getString("comment" + i)
                                            , document.getString("nickname" + i), document.getString("date" + i)));
                                    Log.d("yyc", "onComplete: " + document.getString("comment" + i));
                                }
                                commentItems.sort(Comparator.comparing(CommentItem::getDate).reversed());
                                binding.recyclerView.setLayoutManager(new LinearLayoutManager(QnaExplanation.this));
                                binding.recyclerView.setAdapter(new MyAdapter(commentItems));
                            }
                        } else {
                            Log.d("yyc", "Error getting documents: ", task.getException());
                        }
                    }
                });


        binding.btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Map<String, Object> data = new HashMap<>();
        binding.btnComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    now = LocalDate.now();
                }
                String comment = binding.comment.getText().toString();
                commentCnt++;
                data.put("comment"+commentCnt, comment);
                data.put("commentCnt", commentCnt);
                data.put("nickname"+commentCnt, nickname);
                data.put("date"+commentCnt, now.toString());
                db.collection("Comment").document(title).update(data)
                        .addOnSuccessListener(documentReference -> {
                        })
                        .addOnFailureListener(e -> {
                            System.out.println("에러");
                        });
                db.collection("Q&A").document(title).update("commentCnt", commentCnt);

                binding.comment.setText("");
                Intent intent = new Intent(QnaExplanation.this, QnaExplanation.class);
                intent.putExtra("title", title);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });

        binding.btnGoToExplanation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(QnaExplanation.this, ExplanationActivity.class);
                intent.putExtra("titleQuery", quizTitle);
                Log.d("yyc", "quizTitle: " + quizTitle);
                // title에 해당하는 explanation로 이동
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        });
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private CommentItemBinding binding; // ItemBinding은 item.xml의 바인딩 클래스, ViewHolder는 참조만 가지고 있으면 됨

        private MyViewHolder(CommentItemBinding binding) { // ItemBinding은 item.xml의 바인딩 클래스
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private List<CommentItem> list;
        CommentItemBinding binding2;
        private MyAdapter(List<CommentItem> list) {
            this.list = list;
        }

        // recycler view가 호출할 메소드
        @NonNull
        @Override
        // 새로운 행을 만드는 메소드
        public QnaExplanation.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            binding2 = CommentItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MyViewHolder(binding2);
        }

        //특정 행에 있는 값을 바꿔주는 메소드
        @Override
        public void onBindViewHolder(@NonNull QnaExplanation.MyViewHolder holder, int position) {
            binding2.comment.setText(list.get(position).getComment());
            binding2.nickname.setText(list.get(position).getNickname());
            binding2.date.setText(list.get(position).getDate());
        } // position : 나한테 몇번쨰 행을 줄거야

        @Override
        public int getItemCount() {
            return list.size();
        }
    }
}