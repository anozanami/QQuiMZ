package com.release.qquimz;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.release.qquimz.QnaItem;
import com.release.qquimz.R;
import com.release.qquimz.databinding.FragmentQnaBinding;
import com.release.qquimz.databinding.QnaItemBinding;
import com.release.qquimz.MainActivity;
import com.release.qquimz.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QnaFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;
    FragmentQnaBinding binding;
    QnaItemBinding binding2;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    ArrayList<QnaItem> qnaItems = new ArrayList<QnaItem>();
    ArrayList<QnaItem> searchQnaItems = new ArrayList<QnaItem>();
    private SearchView searchView;
    QnaFragment.Myadapter adapter;
    FirebaseAuth auth;
    String quizTitle =" ";

    public QnaFragment() {
    }

    public static QnaFragment newInstance(String param1, String param2) {
        QnaFragment fragment = new QnaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        binding = FragmentQnaBinding.inflate(getLayoutInflater());
        binding2 = QnaItemBinding.inflate(getLayoutInflater());
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Q&A")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.get("quizTitle") != null) {
                                    quizTitle = document.getString("quizTitle");
                                }
                                Log.d("yyc", "quizTitle in db: " +quizTitle);
                                QnaItem qnaItem = new QnaItem(document.getString("date"), document.getString("title")
                                        , document.getString("content"), document.getString("likeCnt")
                                        , document.getLong("commentCnt"), document.getString("checked"), document.getString("nickname"), quizTitle);
                                qnaItems.add(qnaItem);
                                qnaItems.sort(Comparator.comparing(QnaItem::getDate).reversed());
                                adapter = new QnaFragment.Myadapter(qnaItems);
                                binding.recycler.setAdapter(adapter);
                                binding.recycler.setLayoutManager(new LinearLayoutManager(getActivity()));
                            }
                        } else {
                            Log.w("yyc", "Error getting documents.");
                        }
                    }
                });

        binding.btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), QnaPostActivity.class));
//                startActivity(new Intent(getActivity(), LoginActivity.class));
            }
        });

        searchView = binding.searchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // 검색 버튼을 눌렀을 때의 동작
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // 검색어가 변경될 때마다의 동작
                updateSearchResults(query);
                return false;
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return binding.getRoot();
    }

    private void performSearch(String query) {
        searchQnaItems.clear();
        if(query.length() == 0) {
            searchQnaItems.addAll(qnaItems);
            adapter.setItems(searchQnaItems);
        } else {
            for(int i = 0; i < qnaItems.size(); i++) {
                if(qnaItems.get(i).getTitle().contains(query)) {
                    searchQnaItems.add(qnaItems.get(i));
                }
                adapter.setItems(searchQnaItems);
            }
        }
        adapter.notifyDataSetChanged();
        // 검색 버튼을 눌렀을 때의 동작
        // TODO: 검색 로직 구현
    }

    private void updateSearchResults(String query) {
        // 검색어가 변경될 때마다 결과를 업데이트하는 메소드
        searchQnaItems.clear();
        if(query.length() == 0) {
            searchQnaItems.addAll(qnaItems);
            adapter.setItems(searchQnaItems);
        } else {
            for(int i = 0; i < qnaItems.size(); i++) {
                if(qnaItems.get(i).getTitle().contains(query)) {
                    searchQnaItems.add(qnaItems.get(i));
                    Log.d("yyc", "updateSearchResults: " + qnaItems.get(i).getTitle());
                }
                adapter.setItems(searchQnaItems);
            }
        }
        adapter.notifyDataSetChanged();
        // TODO: 실시간 검색 결과 업데이트 로직 구현
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private QnaItemBinding qnaItemBinding;

        private MyViewHolder(QnaItemBinding binding) {
            super(binding.getRoot());
            this.qnaItemBinding = binding;
        }
    }

    private class Myadapter extends RecyclerView.Adapter<QnaFragment.MyViewHolder> {
        private List<QnaItem> list;
        QnaItemBinding binding;
        String title;
        private Myadapter(List<QnaItem> list) {
            this.list = qnaItems;
        }

        // recycler view가 호출할 메소드들
        // 새로운 행을 만드는 메소드
        @NonNull
        @Override
        public QnaFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            binding = QnaItemBinding.inflate(LayoutInflater.from(parent.getContext()));
            return new QnaFragment.MyViewHolder(binding);
        }

        public void setItems(ArrayList<QnaItem> list) {
            this.list = list;
            notifyDataSetChanged();
        }

        // 특정 행에 있는 값을 바꿔주는 메소드
        @Override
        public void onBindViewHolder(@NonNull QnaFragment.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {

            holder.qnaItemBinding.title.setText(list.get(position).getTitle());
            holder.qnaItemBinding.content.setText(list.get(position).getContent());
            holder.qnaItemBinding.date.setText(list.get(position).getDate());
            holder.qnaItemBinding.likeCnt.setText(list.get(position).getLikeCnt());
            holder.qnaItemBinding.commentCnt.setText(list.get(position).getCommentCnt());
            holder.qnaItemBinding.nickname.setText(list.get(position).getNickname());
            if(list.get(position).getChecked().equals("1")) {
                holder.qnaItemBinding.iconLike.setImageResource(R.drawable.icon_like_fill);
            }

            holder.qnaItemBinding.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    startActivity(new Intent(getActivity(), QnaExplanation.class).putExtra("title", title));
                    title = holder.qnaItemBinding.title.getText().toString();
                    quizTitle = list.get(position).getQuizTitle();
                    Log.d("yyc", "QnaFragment, title: " + title);
                    Intent intent = new Intent(getActivity(), QnaExplanation.class)
                            .putExtra("quizTitle", quizTitle)
                            .putExtra("title", title);
//                            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivityForResult(intent, 1000);
                    startActivity(intent);
                }
            });

            holder.qnaItemBinding.iconLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> data = new HashMap<>();
                    if(holder.qnaItemBinding.iconLike.getDrawable().getConstantState() == getActivity().getDrawable(R.drawable.icon_like).getConstantState()){
                        holder.qnaItemBinding.iconLike.setImageResource(R.drawable.icon_like_fill);
                        int likeCnt = Integer.parseInt(holder.qnaItemBinding.likeCnt.getText().toString());
                        likeCnt++;
                        data.put("likeCnt", Integer.toString(likeCnt));
                        holder.qnaItemBinding.likeCnt.setText(Integer.toString(likeCnt));
                        db.collection("Q&A").document(holder.qnaItemBinding.title.getText().toString()).update(data)
                                .addOnCompleteListener(documentReference -> {
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("에러");
                                });
                        db.collection("Q&A").document(holder.qnaItemBinding.title.getText().toString()).update("checked", "1");
                    } else {
                        holder.qnaItemBinding.iconLike.setImageResource(R.drawable.icon_like);
                        int likeCnt = Integer.parseInt(holder.qnaItemBinding.likeCnt.getText().toString());
                        likeCnt--;
                        data.put("likeCnt", Integer.toString(likeCnt));
                        holder.qnaItemBinding.likeCnt.setText(Integer.toString(likeCnt));
                        db.collection("Q&A").document(holder.qnaItemBinding.title.getText().toString()).update(data)
                                .addOnCompleteListener(documentReference -> {
                                })
                                .addOnFailureListener(e -> {
                                    System.out.println("에러");
                                });
                        db.collection("Q&A").document(holder.qnaItemBinding.title.getText().toString()).update("checked", "0");
                    }
                }
            });
        }

        // 행의 개수를 반환하는 메소드
        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1000) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new QnaFragment())
                    .commit();
        }

    }
}