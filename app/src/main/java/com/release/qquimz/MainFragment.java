package com.release.qquimz;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.release.qquimz.databinding.FragmentMainBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MainFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MainFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private FragmentMainBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String mParam1;
    private String mParam2;

    public MainFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MainFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MainFragment newInstance(String param1, String param2) {
        MainFragment fragment = new MainFragment();
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
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String uid = auth.getCurrentUser().getUid();
        db.collection("users").document(uid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // 닉네임 또는 ID 표시
                    String nickname = document.getString("nickname");
                    if (nickname == null || nickname.isEmpty()) {
                        nickname = auth.getCurrentUser().getEmail(); // 이메일 사용
                    }
                    binding.userName.setText(nickname);

                    // 포인트 표시
                    String points = document.getString("points");

                    if (points != null) {
                        int point = Integer.parseInt(points);
                        binding.rankNum.setText(point + "점");

                        // 엠블럼 설정
                        binding.rankingIcon.setImageResource(getEmblemResource(point));
                    }
                }
            }
        });
        binding.goSuggestion.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), SuggestionActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
        });
        binding.quizStart.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), QuizActivity.class));
        });
    }

    private int getEmblemResource(int points) {
        if (points < 500) {
            return R.drawable.bronze;
        } else if (points < 1000) {
            return R.drawable.silver;
        } else if (points < 2000) {
            return R.drawable.gold;
        } else if (points < 5000) {
            return R.drawable.ruby;
        } else {
            return R.drawable.pioneer;
        }
    }
}