package com.release.qquimz;

import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class AddFriendActivity extends AppCompatActivity {

    private List<User> searchResults = new ArrayList<>();
    private UserAccount loggedInUser;
    private FriendAdapter adapter;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        // Firebase Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Friend");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        // UI 초기화
        EditText etSearch = findViewById(R.id.et_search);
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        ImageView btnSearch = findViewById(R.id.btn_search);

        // RecyclerView 설정
        adapter = new FriendAdapter(searchResults, this, true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        // 로그인된 유저 정보 가져오기
        getLoggedInUser();

        // 친구 추가 콜백 설정
        adapter.setOnAddFriendListener(user -> {
            if (loggedInUser == null) {
                Toast.makeText(this, "로그인된 사용자 정보가 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            String friendId = user.getId(); // 친구로 추가할 사용자의 ID

            // 이미 친구인지 확인
            if (loggedInUser.getIsFriend().contains(friendId)) {
                Toast.makeText(this, user.getNickname() + "은(는) 이미 친구입니다!", Toast.LENGTH_SHORT).show();
                return; // 이미 친구라면 여기서 종료
            }

            // Firestore의 isFriend 필드에 친구 ID 추가 (arrayUnion 사용)
            db.collection("users").document(loggedInUser.getId())
                    .update("isFriend", com.google.firebase.firestore.FieldValue.arrayUnion(friendId))
                    .addOnSuccessListener(aVoid -> {
                        loggedInUser.getIsFriend().add(friendId);
                        Toast.makeText(this, user.getNickname() + "이(가) 친구로 추가되었습니다!", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "친구 추가 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                    });
        });

        // 검색 버튼 클릭 이벤트
        btnSearch.setOnClickListener(v -> {
            String keyword = etSearch.getText().toString().trim();
            searchFriendsByNickname(keyword);

            // 키보드 닫기
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
            }
        });

        // Enter 키로 검색
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String keyword = etSearch.getText().toString().trim();
                searchFriendsByNickname(keyword);

                // 키보드 닫기
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });
    }

    // Firestore에서 닉네임으로 유저 검색
    private void searchFriendsByNickname(String keyword) {
        db.collection("users")
                .whereEqualTo("nickname", keyword) // nickname이 일치하는 유저 검색
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        searchResults.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            searchResults.add(user);
                        }
                        adapter.setData(searchResults); // 검색 결과 RecyclerView에 업데이트
                    } else {
                        Toast.makeText(this, "유저 검색 실패", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 로그인된 사용자 정보 가져오기
    private void getLoggedInUser() {
        String loggedInUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 로그인된 사용자 UID 가져오기
        if (loggedInUserId == null) {
            Toast.makeText(this, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db.collection("users").document(loggedInUserId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        String id = document.getString("id");
                        String nickname = document.getString("nickname");
                        List<String> isFriend = (List<String>) document.get("isFriend");

                        if (isFriend == null) {
                            isFriend = new ArrayList<>();
                        }

                        loggedInUser = new UserAccount();
                        loggedInUser.setId(id);
                        loggedInUser.setNickname(nickname);
                        loggedInUser.setIsFriend(isFriend);
                    } else {
                        Toast.makeText(AddFriendActivity.this, "사용자 데이터가 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddFriendActivity.this, "사용자 정보 로드 실패", Toast.LENGTH_SHORT).show();
                });
    }


    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra("friendsListUpdated", true);
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
