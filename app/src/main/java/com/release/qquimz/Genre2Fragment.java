package com.release.qquimz;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.release.qquimz.databinding.FragmentGenre2Binding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Genre2Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Genre2Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";

    // TODO: Rename and change types of parameters
    private FragmentGenre2Binding binding;
    private List<String> selection;
    private String answer;
    private int inputSelect;    // 사용자의 선택. 타임아웃의 경우 -1로 설정
    private int answerNumber;   // 정답이 몇 번인지 알려줌
    private Long timeLimit;
    private String title;
    FirebaseFirestore myFirestore;
    LocalDate now;

    public Genre2Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Genre2Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Genre2Fragment newInstance(List<String> selection, Long timeLimit, String title) {
        Genre2Fragment fragment = new Genre2Fragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PARAM1, (ArrayList<String>) selection);
        args.putLong(ARG_PARAM2, timeLimit);
        args.putString(ARG_PARAM3, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selection = getArguments().getStringArrayList(ARG_PARAM1);
            timeLimit = getArguments().getLong(ARG_PARAM2);
            title = getArguments().getString(ARG_PARAM3);
        }
        myFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentGenre2Binding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        answer = selection.get(0);  // 맨 첫번째 원소를 정답으로 설정
        selection.remove(0);
        Collections.shuffle(selection);

        double rand = Math.random();
        answerNumber = (int)(rand*4);

        binding.select1value.setText(answerNumber == 0 ? answer : selection.get(0));
        binding.select2value.setText(answerNumber == 1 ? answer : selection.get(1));
        binding.select3value.setText(answerNumber == 2 ? answer : selection.get(2));
        binding.select4value.setText(answerNumber == 3 ? answer : selection.get(3));

        // timeLimit 타이머 스레드
        Thread timeOutCheck = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<=timeLimit;i++){
                    try{
                        if(i==timeLimit){
                            inputSelect = -1;
                            showResult(inputSelect);
                            return;   // 타임아웃 된 상황
                        } else Thread.sleep(1000);
                    }catch(InterruptedException e){
                        return; // return : 정상적인 종료 (선택지를 눌러서 interrupt해서 멈춘거니까)
                    }
                }
            }
        });
        timeOutCheck.start();

        binding.select1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputSelect = 0;
                timeOutCheck.interrupt();
                showResult(inputSelect);
            }
        });
        binding.select2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputSelect = 1;
                timeOutCheck.interrupt();
                showResult(inputSelect);
            }
        });
        binding.select3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputSelect = 2;
                timeOutCheck.interrupt();
                showResult(inputSelect);
            }
        });
        binding.select4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputSelect = 3;
                timeOutCheck.interrupt();
                showResult(inputSelect);
            }
        });
    }

    public void showResult(int inputSelect){
        int resultArg; // 0 : 정답, 1 : 오답
        String result;
        Map<String, Object> data = new HashMap<>();
        if(answerNumber == inputSelect){
            resultArg = 0;
            result = "정답";
        }else {
            resultArg = 1;
            result = "오답";
        }
        data.put("result", result);
        myFirestore.collection("History").document(title).update(data);

        getParentFragmentManager().beginTransaction()
                .add(R.id.main, ResultFragment.newInstance(resultArg,title))
                .addToBackStack(null)
                .commit();
    }
}