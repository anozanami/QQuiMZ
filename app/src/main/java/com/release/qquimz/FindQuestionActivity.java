package com.release.qquimz;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.release.qquimz.R;
import com.release.qquimz.databinding.ActivityFindQuestionBinding;
import com.release.qquimz.ExplanationActivity;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class FindQuestionActivity extends AppCompatActivity {
    ActivityFindQuestionBinding binding;
    List<question_item> data = new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFindQuestionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db.collection("quiz")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot document : task.getResult()) {
                            String title = document.getString("title");
                            String content = document.getString("content");
                            data.add(new question_item(title, content));
                        }

                        binding.recycler.setAdapter(new CustomAdapter(data));
                        binding.recycler.setLayoutManager(new LinearLayoutManager(this));
                    } else {
                        Log.d("yyc", "Error getting documents: ", task.getException());
                    }
                });

    }


    public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.ViewHolder> {

        private List<question_item> localDataSet;

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView titleView;
            TextView contentView;;

            public ViewHolder(View view) {
                super(view);
                titleView = view.findViewById(R.id.title);
                contentView = view.findViewById(R.id.content);
            }

            public TextView getTitleView() {
                return titleView;
            }

            public TextView getContentView() {
                return contentView;

            }
        }

        public CustomAdapter(List<question_item> dataSet) {
            localDataSet = dataSet;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.qna_post_item, viewGroup, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder viewHolder, @SuppressLint("RecyclerView") final int position) {

            viewHolder.getTitleView().setText(localDataSet.get(position).content);

            viewHolder.getTitleView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getApplicationContext(), ExplanationActivity.class);
                    intent.putExtra("content", localDataSet.get(position).content);
                    setResult(1000, intent);
                    finish();
                }
            });
        }

        @Override
        public int getItemCount() {
            return localDataSet.size();
        }

//        getAdapterPosition()
    }

    public class question_item {
        private String title;
        private String content;

        public question_item(String title, String content) {
            this.title = title;
            this.content = content;
        }

    }
}