package com.example.gsyvideoplayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.gsyvideoplayer.utils.JumpUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.open_btn)
    Button openBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.open_btn, R.id.list_btn})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.open_btn:
                JumpUtils.goToVideoPlayer(this, openBtn);
                break;
            case R.id.list_btn:
                JumpUtils.goToVideoPlayer(this);
                break;
        }
    }
}
