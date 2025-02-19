package com.release.qquimz;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;


public class SettingFragment extends Fragment {

    private FirebaseAuth mFirebaseAuth;
    private FirebaseFirestore db;
    private TextView nicknameTextView; // 닉네임을 표시할 TextView
    private TextView pointsView;

    // SettingFragment의 인스턴스를 생성하는 메서드
    public static SettingFragment newInstance() {
        return new SettingFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 프래그먼트의 레이아웃을 inflate
        return inflater.inflate(R.layout.fragment_setting, container, false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Firestore 인증과 Firestore 인스턴스 초기화
        mFirebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // UI 요소 초기화
        ImageButton renameButton = view.findViewById(R.id.rename); // 닉네임 변경 버튼
        ImageButton setAlarmButton = view.findViewById(R.id.set_alarm); // 알람 설정 버튼
        Button secessionButton = view.findViewById(R.id.secession); // 계정 탈퇴 버튼
        nicknameTextView = view.findViewById(R.id.logged_in_user_name);
        pointsView = view.findViewById(R.id.logged_in_user_points);
        // 버튼 클릭 리스너 설정
        renameButton.setOnClickListener(v -> showRenameDialog()); // 닉네임 변경 다이얼로그 표시
        setAlarmButton.setOnClickListener(v -> showAlarmBottomSheet()); // 알람 설정 BottomSheet 표시
        secessionButton.setOnClickListener(v -> showSecessionBottomSheet()); // 계정 탈퇴 BottomSheet 표시
        requestNotificationPermission();
        loadUserData();
    }

    private void loadUserData() {
        String userId = mFirebaseAuth.getCurrentUser().getUid(); // 로그인한 사용자의 UID 가져오기

        // Firestore에서 사용자 정보 불러오기
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.exists()) {
                            String nickname = documentSnapshot.getString("nickname"); // Firestore에서 닉네임 가져오기
                            String points = documentSnapshot.getString("points"); // Firestore에서 points 가져오기
                            nicknameTextView.setText(nickname != null ? nickname : "닉네임 없음");
                            pointsView.setText(points != null ? points : "포인트 불러오는데 실패");
                        }
                    } else {
                        Toast.makeText(getContext(), "사용자 정보를 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // 닉네임 변경 다이얼로그 표시
    private void showRenameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        EditText editText = new EditText(requireContext());
        editText.setHint("새 닉네임 입력"); // 힌트 텍스트 설정

        builder.setTitle("닉네임 변경") // 다이얼로그 제목 설정
                .setView(editText) // EditText 추가
                .setPositiveButton("확인", (dialog, which) -> {
                    String newNickname = editText.getText().toString().trim();
                    if (!newNickname.isEmpty()) {
                        // 닉네임이 입력되었을 경우 Firestore에 닉네임 업데이트
                        updateNicknameInFirestore(newNickname);
                    } else {
                        // 닉네임이 비어있을 경우
                        Toast.makeText(requireContext(), "닉네임을 입력하세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss()) // 취소 버튼 클릭 시 다이얼로그 닫기
                .show();
    }

    private void updateNicknameInFirestore(String newNickname) {
        String userId = mFirebaseAuth.getCurrentUser().getUid(); // 현재 로그인한 사용자 ID 가져오기

        // Firestore에서 해당 사용자의 닉네임을 업데이트
        db.collection("users").document(userId)
                .update("nickname", newNickname) // nickname 필드 업데이트
                .addOnSuccessListener(aVoid -> {
                    // 업데이트 성공
                    Toast.makeText(requireContext(), "닉네임이 변경되었습니다.", Toast.LENGTH_SHORT).show();
                    // 변경된 닉네임을 바로 반영하기 위해 TextView 업데이트
                    nicknameTextView.setText(newNickname);
                })
                .addOnFailureListener(e -> {
                    // 업데이트 실패
                    Toast.makeText(requireContext(), "닉네임 변경에 실패했습니다.", Toast.LENGTH_SHORT).show();
                });
    }


    // 계정 탈퇴 BottomSheet 표시
    private void showSecessionBottomSheet() {
        // BottomSheet 레이아웃 inflate
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_secession, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(bottomSheetView);

        // UI 요소 초기화
        Button confirmButton = bottomSheetView.findViewById(R.id.confirm_secession); // 탈퇴 확인 버튼
        Button cancelButton = bottomSheetView.findViewById(R.id.cancel_secession); // 탈퇴 취소 버튼

        // 탈퇴 확인 버튼 클릭 리스너 설정
        confirmButton.setOnClickListener(v -> {
            deleteUserAccount(); // 회원 탈퇴 함수 호출
            bottomSheetDialog.dismiss(); // BottomSheet 닫기
        });

        // 탈퇴 취소 버튼 클릭 리스너 설정
        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss()); // BottomSheet 닫기

        bottomSheetDialog.show(); // BottomSheet 표시
    }

    private void deleteUserAccount() {
        String userId = mFirebaseAuth.getCurrentUser().getUid(); // 로그인한 사용자의 UID 가져오기

        // Firestore에서 사용자 데이터 삭제
        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DeleteAccount", "Firestore 사용자 정보 삭제 성공");
                    // Firestore 삭제가 성공하면 Authentication 계정 삭제
                    deleteUserFromAuth();
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteAccount", "Firestore 사용자 정보 삭제 실패", e);
                    Toast.makeText(requireContext(), "회원탈퇴 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                });
    }


    private void deleteUserFromAuth() {
        mFirebaseAuth.getCurrentUser().delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("DeleteAccount", "Firebase Authentication 계정 삭제 성공");
                    Toast.makeText(requireContext(), "계정이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();

                    // 회원 탈퇴 후 로그인 화면으로 이동 (또는 앱 종료)
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                })
                .addOnFailureListener(e -> {
                    Log.e("DeleteAccount", "Firebase Authentication 계정 삭제 실패", e);
                    Toast.makeText(requireContext(), "계정 삭제 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                });
    }

    private void showAlarmBottomSheet() {
        View bottomSheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_alaarm, null);
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(requireContext());
        bottomSheetDialog.setContentView(bottomSheetView);

        NumberPicker npHour = bottomSheetView.findViewById(R.id.np_hour);
        NumberPicker npMinute = bottomSheetView.findViewById(R.id.np_minute);
        Button saveButton = bottomSheetView.findViewById(R.id.btn_set_alarm);

        npHour.setMinValue(0);
        npHour.setMaxValue(23);
        npMinute.setMinValue(0);
        npMinute.setMaxValue(59);

        saveButton.setOnClickListener(v -> {
            int selectedHour = npHour.getValue();
            int selectedMinute = npMinute.getValue();

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
            calendar.set(Calendar.MINUTE, selectedMinute);
            calendar.set(Calendar.SECOND, 0);

            if (calendar.before(Calendar.getInstance())) {
                calendar.add(Calendar.DAY_OF_YEAR, 1); // 이미 지난 시간이면 다음 날로 설정
            }

            if (checkExactAlarmPermission()) {
                setAlarm(calendar.getTimeInMillis()); // 알람 설정
                Toast.makeText(requireContext(), "알림이 설정되었습니다: " + String.format("%02d:%02d", selectedHour, selectedMinute), Toast.LENGTH_SHORT).show();
                bottomSheetDialog.dismiss();
            } else {
                requestExactAlarmPermission(); // 권한 요청
            }
        });

        bottomSheetDialog.show();
    }

    private boolean checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
            return alarmManager != null && alarmManager.canScheduleExactAlarms();
        }
        return true; // Android 12 미만에서는 권한 필요 없음
    }

    private void requestExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
            startActivity(intent);
        }
    }

    private void setAlarm(long triggerTime) {
        AlarmManager alarmManager = (AlarmManager) requireContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(requireContext(), AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);

        if (alarmManager != null) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Log.d("Alarm", "정확한 알람이 설정되었습니다.");
        } else {
            Log.e("Alarm", "AlarmManager가 null입니다. 알람 설정에 실패했습니다.");
        }
    }


    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

}