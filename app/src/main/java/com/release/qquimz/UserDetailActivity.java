package com.release.qquimz;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class UserDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_detail);

        TextView userDetails = findViewById(R.id.user_detail);

        // 전달된 사용자 정보 가져오기
        String userToken = getIntent().getStringExtra("userToken");

        // 사용자 정보를 표시 (샘플 데이터로 대체 가능)
        userDetails.setText("User Token: " + userToken + "\nMore details to display...");
    }
}
