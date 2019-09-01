package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;

import com.shuyu.gsyvideoplayer.GSYVideoADManager;

import java.util.Random;

public class RequestListADVideoPlayer extends ListADVideoPlayer {

    public RequestListADVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public RequestListADVideoPlayer(Context context) {
        super(context);
    }

    public RequestListADVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void startPlayLogic() {
        //模拟请求
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int min = 1;
                int max = 10;
                Random random = new Random();
                int i = random.nextInt(max) % (max - min + 1) + min;
                if (i % 3 == 0) {
                    mOriginUrl = "http://wdquan-space.b0.upaiyun.com/VIDEO/2018/11/22/ae0645396048_hls_time10.m3u8";
                    mUrl = "http://wdquan-space.b0.upaiyun.com/VIDEO/2018/11/22/ae0645396048_hls_time10.m3u8";
                    mTitle = "ggg2";
                } else if (i % 4 == 0) {
                    //模拟请求失败
                    onError(0, 0);
                } else {
                    mOriginUrl = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";
                    mUrl = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";
                    mTitle = "ffff1";
                }
                mCache = false;
                RequestListADVideoPlayer.super.startPlayLogic();
            }
        }, 2000);
    }
}
