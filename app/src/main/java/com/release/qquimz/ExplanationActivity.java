package com.release.qquimz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.release.qquimz.databinding.ActivityExplanationBinding;

public class ExplanationActivity extends AppCompatActivity {
    ActivityExplanationBinding binding;
    FirebaseFirestore myFirestore;
    String tq, et, ec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityExplanationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        myFirestore = FirebaseFirestore.getInstance();
        tq=getIntent().getStringExtra("titleQuery");
        Log.d("QQuiMZ", "현재 퀴즈 : " + tq);

        myFirestore.collection("explanation").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("QQuiMZ", document.getId() + " => " + document.getData());
                                if(document.getId().toString().equals(tq)) {
                                    et = document.getString("title");
                                    ec = document.getString("content");
                                    Log.d("QQuiMZ", et + "|" + ec);
                                    binding.title.setText(et);
                                    binding.content.setText(ec);
                                    break;
                                }
                            }
                        } else {
                            Log.w("QQuiMZ", "Error getting documents.", task.getException());
                        }
                    }
                });
        binding.goPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }
}