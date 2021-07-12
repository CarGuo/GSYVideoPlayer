package com.example.gsyvideoplayer.simple;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Toast;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.databinding.ActivitySimpleBinding;

import permissions.dispatcher.PermissionUtils;

public class SimpleActivity extends AppCompatActivity implements View.OnClickListener {

    final String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
    ActivitySimpleBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        binding = ActivitySimpleBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        binding.simpleDetail1.setOnClickListener(this);
        binding.simpleDetail2.setOnClickListener(this);
        binding.simpleList1.setOnClickListener(this);
        binding.simpleList2.setOnClickListener(this);
        binding.simplePlayer.setOnClickListener(this);

        boolean hadPermission = PermissionUtils.hasSelfPermissions(this, permissions);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !hadPermission) {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            requestPermissions(permissions, 1110);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean sdPermissionResult = PermissionUtils.verifyPermissions(grantResults);
        if (!sdPermissionResult) {
            Toast.makeText(this, "没获取到sd卡权限，无法播放本地视频哦", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.simple_player:
                startActivity(new Intent(this, SimplePlayer.class));
                break;
            case R.id.simple_list_1:
                startActivity(new Intent(this, SimpleListVideoActivityMode1.class));
                break;
            case R.id.simple_list_2:
                startActivity(new Intent(this, SimpleListVideoActivityMode2.class));
                break;
            case R.id.simple_detail_1:
                startActivity(new Intent(this, SimpleDetailActivityMode1.class));
                break;
            case R.id.simple_detail_2:
                startActivity(new Intent(this, SimpleDetailActivityMode2.class));
                break;
        }
    }
}
