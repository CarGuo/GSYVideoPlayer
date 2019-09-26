package com.example.gsyvideoplayer.switchplay;

import android.view.View;

import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;


public class SwitchUtil {

    private static SwitchVideo sSwitchVideo;
    private static GSYMediaPlayerListener sMediaPlayerListener;

    public static void optionPlayer(final SwitchVideo gsyVideoPlayer, String url, boolean cache, String title) {
        //增加title
        gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);
        //设置返回键
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
        //设置全屏按键功能
        gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gsyVideoPlayer.startWindowFullscreen(gsyVideoPlayer.getContext(), false, true);
            }
        });
        //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏
        gsyVideoPlayer.setAutoFullWithSize(true);
        //音频焦点冲突时是否释放
        gsyVideoPlayer.setReleaseWhenLossAudio(false);
        //全屏动画
        gsyVideoPlayer.setShowFullAnimation(false);
        //小屏时不触摸滑动
        gsyVideoPlayer.setIsTouchWiget(false);

        gsyVideoPlayer.setSwitchUrl(url);

        gsyVideoPlayer.setSwitchCache(cache);

        gsyVideoPlayer.setSwitchTitle(title);
    }


    public static void savePlayState(SwitchVideo switchVideo) {
        sSwitchVideo = switchVideo.saveState();
        sMediaPlayerListener = switchVideo;
    }

    public static void clonePlayState(SwitchVideo switchVideo) {
        switchVideo.cloneState(sSwitchVideo);
    }

    public static void release() {
        if (sMediaPlayerListener != null) {
            sMediaPlayerListener.onAutoCompletion();
        }
        sSwitchVideo = null;
        sMediaPlayerListener = null;
    }
}
