package com.example.myfirstapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * 权限请求页面
 * 在主界面启动前检查并请求位置和存储权限，
 * 避免应用因缺少权限而直接崩溃。
 */
public class PermissionActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1001;

    private ImageView ivLocationStatus;
    private ImageView ivStorageStatus;
    private TextView tvPermissionHint;
    private Button btnRequestPermissions;
    private Button btnGoSettings;

    private String[] requiredPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions);

        ivLocationStatus = findViewById(R.id.iv_location_status);
        ivStorageStatus = findViewById(R.id.iv_storage_status);
        tvPermissionHint = findViewById(R.id.tv_permission_hint);
        btnRequestPermissions = findViewById(R.id.btn_request_permissions);
        btnGoSettings = findViewById(R.id.btn_go_settings);

        // Android 13+ 不需要存储权限即可写入应用专属目录和公共媒体目录
        // 低版本需要传统存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            };
        } else {
            requiredPermissions = new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
        }

        btnRequestPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestNeededPermissions();
            }
        });

        btnGoSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAppSettings();
            }
        });

        // 页面加载时立即刷新权限状态
        updatePermissionStatus();

        // 自动尝试请求缺少的权限
        requestNeededPermissions();
    }

    /**
     * 检查哪些权限还未授予，发起请求
     */
    private void requestNeededPermissions() {
        java.util.List<String> neededList = new java.util.ArrayList<>();
        for (String perm : requiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                neededList.add(perm);
            }
        }

        if (neededList.isEmpty()) {
            launchMainActivity();
            return;
        }

        String[] needed = neededList.toArray(new String[0]);
        ActivityCompat.requestPermissions(this, needed, PERMISSION_REQUEST_CODE);
    }

    /**
     * 更新权限状态图标
     */
    private void updatePermissionStatus() {
        // 位置权限状态
        boolean locationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        ivLocationStatus.setImageResource(locationGranted
                ? android.R.drawable.ic_menu_info_details
                : android.R.drawable.ic_menu_close_clear_cancel);
        if (locationGranted) {
            ivLocationStatus.setColorFilter(0xFF4CAF50);
        } else {
            ivLocationStatus.setColorFilter(0xFFFF5252);
        }

        // 存储权限状态
        boolean storageGranted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 不需要存储权限即可写入外部存储
            storageGranted = true;
        } else {
            storageGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED;
        }
        ivStorageStatus.setImageResource(storageGranted
                ? android.R.drawable.ic_menu_info_details
                : android.R.drawable.ic_menu_close_clear_cancel);
        if (storageGranted) {
            ivStorageStatus.setColorFilter(0xFF4CAF50);
        } else {
            ivStorageStatus.setColorFilter(0xFFFF5252);
        }

        // 所有权限都授予后自动跳转
        boolean allGranted = locationGranted && storageGranted;
        if (allGranted) {
            tvPermissionHint.setVisibility(View.VISIBLE);
            tvPermissionHint.setText("所有权限已授予，正在进入主界面...");
            tvPermissionHint.setTextColor(0xFF4CAF50);
            btnRequestPermissions.setText("进入应用");
            btnRequestPermissions.postDelayed(new Runnable() {
                @Override
                public void run() {
                    launchMainActivity();
                }
            }, 800);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != PERMISSION_REQUEST_CODE) {
            return;
        }

        boolean shouldShowSettings = false;
        boolean allGranted = true;

        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                String perm = permissions[i];
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, perm)) {
                    if (perm.equals(Manifest.permission.ACCESS_FINE_LOCATION)
                            || perm.equals(Manifest.permission.ACCESS_COARSE_LOCATION)
                            || perm.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            || perm.equals(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        shouldShowSettings = true;
                    }
                }
            }
        }

        updatePermissionStatus();

        if (allGranted) {
            launchMainActivity();
        } else if (shouldShowSettings) {
            tvPermissionHint.setVisibility(View.VISIBLE);
            tvPermissionHint.setText("部分权限被拒绝且不再提示，请前往系统设置手动开启");
            tvPermissionHint.setTextColor(0xFFFF6666);
            btnGoSettings.setVisibility(View.VISIBLE);
            btnRequestPermissions.setText("再次请求权限");
        } else {
            tvPermissionHint.setVisibility(View.VISIBLE);
            tvPermissionHint.setText("部分权限未授予，请再次点击按钮授权");
            tvPermissionHint.setTextColor(0xFFFFAA00);
            btnRequestPermissions.setText("再次请求权限");
        }
    }

    private void launchMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void openAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开系统设置", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updatePermissionStatus();
    }
}
