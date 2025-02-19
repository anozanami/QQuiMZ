package com.release.qquimz;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.release.qquimz.databinding.ActivitySuggestionBinding;

import java.util.HashMap;
import java.util.Map;

public class SuggestionActivity extends AppCompatActivity {
    private ActivitySuggestionBinding binding;
    FirebaseFirestore myFirestore;
    String sgTitle, sgContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySuggestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.goMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.summit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> sg = new HashMap<>();
                sgTitle = binding.sgTitle.getText().toString();
                sgContent = binding.sgContent.getText().toString();

                sg.put("title",sgTitle);
                sg.put("content",sgContent);

                myFirestore=FirebaseFirestore.getInstance();
                myFirestore.collection("suggestion")
                        .add(sg)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("QQuiMZ", "DocumentSnapshot added with ID: " + documentReference.getId());
                                Toast.makeText(SuggestionActivity.this, "퀴즈 제안이 완료되었습니다!", Toast.LENGTH_SHORT).show();
                                binding.sgTitle.setText("");
                                binding.sgContent.setText("");
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("QQuiMZ", "Error adding document", e);
                                Toast.makeText(SuggestionActivity.this, "오류가 발생했습니다. 다시 시도해 주세요", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }
}