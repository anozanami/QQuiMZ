package com.release.qquimz;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.release.qquimz.databinding.FragmentGenre5Binding;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Genre5Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Genre5Fragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";

    // TODO: Rename and change types of parameters
    private FragmentGenre5Binding binding;
    private List<String> selection;
    private List<String> strDiv;
    private Long timeLimit;
    private String title;
    FirebaseFirestore myFirestore;

    public Genre5Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Genre4Fragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Genre5Fragment newInstance(List<String> selection, List<String> strDiv, Long timeLimit, String title) {
        Genre5Fragment fragment = new Genre5Fragment();
        Bundle args = new Bundle();
        args.putStringArrayList(ARG_PARAM1, (ArrayList<String>) selection);
        args.putStringArrayList(ARG_PARAM2, (ArrayList<String>) strDiv);
        args.putLong(ARG_PARAM3, timeLimit);
        args.putString(ARG_PARAM4, title);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selection = getArguments().getStringArrayList(ARG_PARAM1);
            strDiv = getArguments().getStringArrayList(ARG_PARAM2);
            timeLimit = getArguments().getLong(ARG_PARAM3);
            title = getArguments().getString(ARG_PARAM4);
        }
        myFirestore = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentGenre5Binding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.divided1.setText(strDiv.get(0));
        binding.divided2.setText(strDiv.get(1));

        // timeLimit 타이머 스레드
        Thread timeOutCheck = new Thread(new Runnable() {
            @Override
            public void run() {
                for(int i=0;i<=timeLimit;i++){
                    try{
                        if(i==timeLimit){
                            showResult(1);
                            return;   // 타임아웃 된 상황
                        } else Thread.sleep(1000);
                    }catch(InterruptedException e){
                        return; // return : 정상적인 종료 (선택지를 눌러서 interrupt해서 멈춘거니까)
                    }
                }
            }
        });
        timeOutCheck.start();

        binding.answerInput.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                switch (keyCode){
                    case KeyEvent.KEYCODE_ENTER:
                        if(selection.get(0).compareTo(String.valueOf(binding.answerInput.getText())) == 0){
                            timeOutCheck.interrupt();
                            showResult(0);  // 정답
                        }else{
                            timeOutCheck.interrupt();
                            showResult(1);  // 오답
                        }
                }
                return false;
            }
        });
    }

    public void showResult(int resultArg){
        Map<String, Object> data = new HashMap<>();
        String result;
        if(resultArg == 0) {
            result = "정답";
        }else{
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