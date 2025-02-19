package com.release.qquimz;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RankingFragment extends Fragment {
    // ranking fragmenet 에서 firestore 변수를 nickname으로 바꾸기
    // UI 컴포넌트 선언
    private RecyclerView recyclerView;
    private FriendAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    // 사용자 데이터 리스트
    private List<User> allUsers = new ArrayList<>(); // 모든 사용자 리스트
    private List<User> friendUsers = new ArrayList<>(); // 친구 리스트
    private User pivotUser; // 현재 사용자 데이터

    // 현재 사용자 UI 요소
    private TextView loggedInUserName;
    private TextView loggedInUserPoints;
    private ImageView loggedInUserIcon;
    private Switch toggleSwitch;
    private String currentUserId;

    private static final int REQUEST_ADD_FRIEND = 100; // 친구 추가 요청 코드

    // 기본 생성자
    public RankingFragment() {
    }

    // 새로운 인스턴스 생성
    public static RankingFragment newInstance() {
        return new RankingFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        // 로그인한 사용자 확인 및 Firestore 데이터 로드
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser != null) {
            // 로그인된 사용자의 UID 가져오기
            currentUserId = currentUser.getUid();
            Log.d("RankingFragment", "Current user ID: " + currentUserId);
            loadFirestoreData(currentUserId); // 사용자 ID로 데이터 로드
        } else {
            Log.e("RankingFragment", "No user is currently logged in.");
            // 로그인 페이지로 리다이렉트 등의 처리를 할 수 있음
        }
    }

    private void loadFirestoreData(String currentUserId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        // 현재 사용자 데이터 파싱
                        String token = document.getId();
                        String id = document.getString("nickname");

                        int points = parsePoints(document.getString("points"));

                        // 친구 목록 가져오기
                        List<String> isFriend = new ArrayList<>();
                        Object isFriendObj = document.get("isFriend");
                        if (isFriendObj instanceof List<?>) {
                            for (Object item : (List<?>) isFriendObj) {
                                if (item instanceof String) {
                                    isFriend.add((String) item);
                                }
                            }
                        }

                        // 사용자 아이콘 설정
                        String rankingIcon = document.getString("rankingIcon");
                        if (rankingIcon == null) rankingIcon = "default";
                        String nickname = document.getString("nickname");
                        // pivotUser 객체 생성 id, isFriend, nickname, points만 firestore field에 있음
                        pivotUser = new User(token, nickname ,points, isFriend, id, rankingIcon);

                        // 친구 목록 로드
                        loadFriendUsers();
                    } else {
                        Log.w("Firestore", "Failed to find current user: " + currentUserId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading user data", e));
    }


    // 모든 사용자 데이터와 친구 목록 데이터 불러오기
    private void loadFriendUsers() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // 기존 데이터 초기화
                    allUsers.clear();
                    friendUsers.clear();

                    // Firestore에서 사용자 데이터를 가져와 리스트에 추가
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String token = document.getId();
                        String id = document.getString("id");
                        String nickname = document.getString("nickname");
                        int points = parsePoints(document.getString("points"));
                        //초기엔 nickname이 없음. user가 직접 정해야하기 때문에 초기 displayNickname은 UID로 지정
                        String displayNickname = (nickname == null || nickname.isEmpty()) ? id : nickname;

                        List<String> isFriend = new ArrayList<>();
                        Object isFriendObj = document.get("isFriend");
                        if (isFriendObj instanceof List<?>) {
                            for (Object item : (List<?>) isFriendObj) {
                                if (item instanceof String) {
                                    isFriend.add((String) item);
                                }
                            }
                        }

                        String rankingIcon = document.getString("rankingIcon");
                        if (rankingIcon == null) rankingIcon = "default";

                        User user = new User(token, displayNickname ,points, isFriend, id, rankingIcon);
                        allUsers.add(user);

                        // 현재 사용자의 친구라면 friendUsers 리스트에 추가
                        if (pivotUser != null && pivotUser.getIsFriend().contains(token)) {
                            friendUsers.add(user);

                        }
                    }

                    // UI 업데이트
                    updateUIWithLoadedData();
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error loading all users", e));
    }

    // 포인트 문자열을 정수로 변환
    private int parsePoints(String pointsStr) {
        if (pointsStr != null) {
            try {
                return Integer.parseInt(pointsStr);
            } catch (NumberFormatException e) {
                Log.e("Firestore", "Failed to parse points: " + pointsStr, e);
            }
        }
        return 0;
    }

    // UI 업데이트 메서드
    private void updateUIWithLoadedData() {
        if (pivotUser != null) {
            // 현재 사용자 정보를 UI에 표시
            loggedInUserName.setText(pivotUser.getNickname());
            loggedInUserPoints.setText("Points: " + pivotUser.getPoint());

            int iconRes = getIconResource(pivotUser.getRankingIcon());
            loggedInUserIcon.setImageResource(iconRes);
        } else {
            Log.w("UI Update", "pivotUser is null");
        }

        // 사용자를 포인트 내림차순으로 정렬
        rankUsers();

        // RecyclerView 업데이트
        adapter.notifyDataSetChanged();
    }

    // 사용자 아이콘 리소스 가져오기
    private int getIconResource(String rankingIcon) {
        // rankingIcon이 null이거나 숫자가 아니면 기본 아이콘 반환
        if (rankingIcon == null || !isNumeric(rankingIcon)) {
            Log.e("IconResource", "Invalid rankingIcon value: " + rankingIcon);
            return R.drawable.default_icon; // 기본 아이콘
        }

        // 숫자 변환 후 조건에 따라 리소스 반환
        int points = Integer.parseInt(rankingIcon);
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

    // 숫자인지 확인하는 유틸리티 메서드
    private boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // 사용자 랭킹 정렬
    private void rankUsers() {
        Collections.sort(allUsers, Comparator.comparingInt(User::getPoint).reversed());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // 레이아웃 inflate
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        // UI 요소 초기화
        toggleSwitch = view.findViewById(R.id.toggle_switch);
        loggedInUserName = view.findViewById(R.id.logged_in_user_name);
        loggedInUserPoints = view.findViewById(R.id.logged_in_user_points);
        loggedInUserIcon = view.findViewById(R.id.logged_in_user_icon);
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new FriendAdapter(allUsers, getContext(), false);
        recyclerView.setAdapter(adapter);

        // SwipeRefresh 동작 설정
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadFirestoreData(currentUserId);
            swipeRefreshLayout.setRefreshing(false);
        });

        // Toggle Switch 동작 설정
        toggleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.setData(friendUsers); // 친구 목록 표시
            } else {
                adapter.setData(allUsers); // 전체 사용자 목록 표시
            }
        });

        // 친구 추가 버튼 클릭 리스너
        ImageButton addFriendButton = view.findViewById(R.id.add_friend);
        addFriendButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), AddFriendActivity.class);
            startActivityForResult(intent, REQUEST_ADD_FRIEND);
        });

        return view;
    }
}
