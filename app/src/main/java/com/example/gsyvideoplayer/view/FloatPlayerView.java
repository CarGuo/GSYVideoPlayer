package com.example.gsyvideoplayer.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.example.gsyvideoplayer.video.FloatingVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;

/**
 * 适配了悬浮窗的view
 * Created by guoshuyu on 2017/12/25.
 */

public class FloatPlayerView extends FrameLayout {

    FloatingVideo videoPlayer;

    public FloatPlayerView(Context context) {
        super(context);
        init();
    }

    public FloatPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FloatPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {

        videoPlayer = new FloatingVideo(getContext());

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;

        addView(videoPlayer, layoutParams);

        String source1 = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";

        videoPlayer.setUp(source1, true, "测试视频");

        //增加封面
        /*ImageView imageView = new ImageView(getContext());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        videoPlayer.setThumbImageView(imageView);*/

        //是否可以滑动调整
        videoPlayer.setIsTouchWiget(false);

    }


    public void onPause() {
        videoPlayer.getCurrentPlayer().onVideoPause();
    }

    public void onResume() {
        videoPlayer.getCurrentPlayer().onVideoResume();
    }

}
