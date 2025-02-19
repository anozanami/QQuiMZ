package com.release.qquimz;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.release.qquimz.databinding.FragmentHistoryBinding;
import com.release.qquimz.databinding.HistoryItemBinding;
import com.release.qquimz.ExplanationActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class HistoryFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;


    FragmentHistoryBinding binding;
    HistoryItemBinding binding2;
    HistoryFragment.MyAdapter adapter;
    RecyclerView recyclerView;
    ArrayList<HistoryItem> original_list = new ArrayList<>();
    ArrayList<HistoryItem> search_list = new ArrayList<>();
    EditText editText;
    private SearchView searchView;
    Menu menu;
    LocalDate now;
    String date;
    String title;
    String content;
    String result;
    Long categoryNumber;
    String category;
    HistoryItem historyItem;
    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String param1, String param2) {
        HistoryFragment fragment = new HistoryFragment();
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

        binding = FragmentHistoryBinding.inflate(getLayoutInflater());
        binding2 = HistoryItemBinding.inflate(getLayoutInflater());
        recyclerView = binding.recycler;


        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("History")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                date = document.getString("date");
                                title = document.getString("title");
                                content = document.getString("content");
                                result = document.getString("result");
//                                categoryNumber = document.getLong("category");
                                category = document.getString("category");
                                historyItem = new HistoryItem(date, title, content, result, category);
                                original_list.add(historyItem);

                                original_list.sort(Comparator.comparing(HistoryItem::getDate).reversed());
                                adapter = new HistoryFragment.MyAdapter(original_list);
                                recyclerView.setAdapter(adapter);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                            }
                        } else {
                            Log.w("yyc", "Error getting documents.");
                        }
                    }
                });

        // 정상적으로 객체에 보내짐
        // 리사이클러뷰에 LinearLayoutManager 객체 지정.
        // 리사이클러뷰에 Adapter 객체 지정.


        // result를 사용할 지 안 할 지 확인


        binding.btnCalender.setOnClickListener(view -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog dateDialog = new DatePickerDialog(getContext(),
                    new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear,
                                              int dayOfMonth) {
                            String s = (String.valueOf(year)+ "-" + String.valueOf(monthOfYear + 1) + "-" + String.valueOf(dayOfMonth));
                            dateSearch(s);
                            Log.d("yyc", "onDateSet: " + s);
                        }
                    }, year, month, day);
            dateDialog.show();
        });

        binding.btnFilter.setOnClickListener(view -> {
            //현재 날짜로 dialog를 띄우기 위해 날짜를 구함
            ArrayList<String> temp = new ArrayList<>();
            ArrayList<String> filter = new ArrayList<>();
            temp.add("국어");
            temp.add("문화");
            temp.add("영어");
            temp.add("사회");
            temp.add("과학");
            temp.add("정답");
            temp.add("오답");


            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("카테고리를 선택하세요")
                    .setMultiChoiceItems(temp.toArray(new String[0]), null, (dialog, which, isChecked) -> {
                        if (isChecked) {
                            filter.add(temp.get(which));
                        } else {
                            filter.remove(filter.get(which));
                        }
                        Log.d("yyc", "onClick: " + filter.toString());
                    })
                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Log.d("yyc", "onClick: " + filter.toString());
                            filterSearch(filter);
                        }
                    })
                    .setNegativeButton("취소", null)
                    .create();
            AlertDialog dialog = builder.show();
        });

        searchView = binding.searchView;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
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

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    private void filterSearch(ArrayList<String> filter) {
        search_list.clear();
        Log.d("yyc", "filterSearch: " + filter.toString());
        Log.d("yyc", "filterSearch: " + filter.size());
        if(filter.size() == 0) {
            search_list.addAll(original_list);
            adapter.setItems(search_list);
        } else {
            for(int i = 0; i < original_list.size(); i++) {
                for(int j = 0; j < filter.size(); j++) {
                    if(original_list.get(i).getCategory().contains(filter.get(j)) ||
                            original_list.get(i).getResult().contains(filter.get(j)) ||
                            original_list.get(i).getDate().contains(filter.get(j))) {
                        search_list.add(original_list.get(i));
                    }
                }
                adapter.setItems(search_list);
            }
        }
        adapter.notifyDataSetChanged();
    }

    private void dateSearch(String string) {
        search_list.clear();

        for(int i = 0; i < original_list.size(); i++) {
            if (original_list.get(i).getDate().equals(string)) {
                search_list.add(original_list.get(i));
            }
        }
        adapter.setItems(search_list);
        adapter.notifyDataSetChanged();
    }

    private void performSearch(String query) {
        search_list.clear();
        if(query.length() == 0) {
            search_list.addAll(original_list);
            adapter.setItems(search_list);
        } else {
            for(int i = 0; i < original_list.size(); i++) {
                if(original_list.get(i).getTitle().contains(query)) {
                    search_list.add(original_list.get(i));
                }
                adapter.setItems(search_list);
            }
        }
        adapter.notifyDataSetChanged();
        // 검색 버튼을 눌렀을 때의 동작
        // TODO: 검색 로직 구현
    }

    private void updateSearchResults(String query) {
        // 검색어가 변경될 때마다 결과를 업데이트하는 메소드
        search_list.clear();
        if(query.length() == 0) {
            search_list.addAll(original_list);
            adapter.setItems(search_list);
        } else {
            for(int i = 0; i < original_list.size(); i++) {
                if(original_list.get(i).getTitle().contains(query)) {
                    search_list.add(original_list.get(i));
                }
                adapter.setItems(search_list);
            }
        }
        adapter.notifyDataSetChanged();
        // TODO: 실시간 검색 결과 업데이트 로직 구현
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        private HistoryItemBinding binding; // ItemBinding은 item.xml의 바인딩 클래스, ViewHolder는 참조만 가지고 있으면 됨

        private MyViewHolder(HistoryItemBinding binding) { // ItemBinding은 item.xml의 바인딩 클래스
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    private class MyAdapter extends RecyclerView.Adapter<HistoryFragment.MyViewHolder> {
        private List<HistoryItem> list;

        private MyAdapter(List<HistoryItem> list) {
            this.list = list;
        }

        // recycler view가 호출할 메소드
        @NonNull
        @Override
        // 새로운 행을 만드는 메소드
        public HistoryFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            HistoryItemBinding binding = HistoryItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);

            binding.title.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getActivity(), ExplanationActivity.class)
                            .putExtra("titleQuery", binding.title.getText()));
                }
            });
            return new HistoryFragment.MyViewHolder(binding);
        }

        public void setItems(List<HistoryItem> list) {
            this.list = list;
            notifyDataSetChanged();
        };

        //특정 행에 있는 값을 바꿔주는 메소드
        @Override
        public void onBindViewHolder(@NonNull HistoryFragment.MyViewHolder holder, int position) {
            holder.binding.title.setText(list.get(position).getTitle()); // 한줄로 끝냄, date가 아니라 나머지 정답, 문제 제목도 이렇게 setText로 해줄 수 있음
            holder.binding.content.setText(list.get(position).getContent());
            holder.binding.result.setText(list.get(position).getResult());
            holder.binding.category.setText(list.get(position).getCategory());
            holder.binding.date.setText(list.get(position).getDate());
        } // position : 나한테 몇번쨰 행을 줄거야

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        Log.d("yyc", "onActivityResult()");
        super.onActivityResult(requestCode, resultCode, data);
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