package com.release.qquimz;

import static java.lang.Thread.sleep;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationBarView;
import com.release.qquimz.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private long initTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.bottomNavigation.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if(itemId==R.id.page_1){
                    transferTo(MainFragment.newInstance("param1","param2"));
                    return true;
                }
                if(itemId==R.id.page_2){
                    transferTo(QnaFragment.newInstance("param1","param2"));
                    return true;
                }
                if(itemId==R.id.page_3){
                    transferTo(RankingFragment.newInstance());
                    return true;
                }if(itemId==R.id.page_4){
                    transferTo(HistoryFragment.newInstance("param1","param2"));
                    return true;
                }if(itemId==R.id.page_5){
                    transferTo(SettingFragment.newInstance());
                    return true;
                }
                return false;
            }
        });
        // 아무 내용도 없는 setOnItemReselectedListener가 필요한 이유 :
        // 그게 없으면 선택된 메뉴를 (실수로) 또 선택할 때 화면을 또 초기화하게 되므로
        binding.bottomNavigation.setOnItemReselectedListener(new NavigationBarView.OnItemReselectedListener() {
            @Override
            public void onNavigationItemReselected(@NonNull MenuItem item) {}
        });

        // 초기 화면은 메인화면
        transferTo(MainFragment.newInstance("param1","param2"));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(System.currentTimeMillis() - initTime > 3000){
                // back button을 누른지 3초가 지난거라면..
                Toast.makeText(this, "종료하려면 한번 더 누르세요", Toast.LENGTH_SHORT).show();
                // 현재 시간 저장
                initTime = System.currentTimeMillis();
            } else{
                finish(); // MainActivity의 함수. activity 종료. 바로 종료가 되지 않음. 비동기 처리.
                // 함수를 실행하고 기다림 : 동기 처리, 함수를 실행하고 이어서 실행함 : 비동기 처리.
            }
            return true; // 뒤에 있는 함수를 호출하지 마라. false면 이후의 함수를 호출해라. 함
            // 함수의 type이 boolean이면 이후에 동작할 추가적인 함수(동작)이 있다.
        }
        return super.onKeyDown(keyCode, event); // back button이 눌린게 아니면 부모가 알아서 처리해라.
    }

    // 실제로 fragment의 교체를 담당하는 구간
    private void transferTo(Fragment fragment){
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container,fragment)
                .commit();
    }
}