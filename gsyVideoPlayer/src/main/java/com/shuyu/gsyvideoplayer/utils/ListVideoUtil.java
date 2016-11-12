package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.media.Image;
import android.os.Build;
import android.os.Handler;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

/**
 * Created by shuyu on 2016/11/12.
 */

public class ListVideoUtil {


    private static ListVideoUtil listVideoUtil;

    public static synchronized ListVideoUtil getInstance(Context context) {
        if (listVideoUtil == null) {
            listVideoUtil = new ListVideoUtil(context);
        }
        return listVideoUtil;
    }

    private int playPosition = -1; // 播放的位置
    private String TAG = "NULL"; //播放的标志
    private StandardGSYVideoPlayer gsyVideoPlayer;
    private OrientationUtils orientationUtils;

    public ListVideoUtil(Context context) {
        gsyVideoPlayer = new StandardGSYVideoPlayer(context);
    }

    public void addVideoPlayer(Context context, final int position, int imgId, String tag,
                               ViewGroup container, View playBtn) {
        container.removeAllViews();
        if (isCurrentViewPlaying(position, tag)) {
            ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
            if (viewGroup != null)
                viewGroup.removeAllViews();
            container.addView(gsyVideoPlayer);
            playBtn.setVisibility(View.INVISIBLE);
        } else {
            playBtn.setVisibility(View.VISIBLE);
            container.removeAllViews();   //增加封面
            ImageView imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(imgId);
            container.addView(imageView);
        }
    }

    public void setPlayPositionAndTag(int playPosition, String tag) {
        this.playPosition = playPosition;
        this.TAG = tag;
    }

    public void startPlay(String url) {
        gsyVideoPlayer.release();

        gsyVideoPlayer.setUp(url, true, "");

        //增加title
        gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);

        //设置返回键
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);

        //设置全屏按键功能
        gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //resolveFullBtn(holder.standardGSYVideoPlayer);
            }
        });

        gsyVideoPlayer.startPlayLogic();
    }


    /**
     * 全屏幕按键处理
     */
    private void resolveFullBtn(final StandardGSYVideoPlayer standardGSYVideoPlayer) {
        /*if (orientationUtils != null) {
            orientationUtils.setEnable(false);
        }
        orientationUtils = new OrientationUtils((Activity) context, standardGSYVideoPlayer);
        if (isFullVideo) {
            orientationUtils.setEnable(false);
            int delay = orientationUtils.backToProtVideo();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        TransitionManager.beginDelayedTransition(rootView);
                    }
                    standardGSYVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_enlarge);
                    isFullVideo = false;
                }
            }, delay);
        } else {
            orientationUtils.setEnable(true);
            standardGSYVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_shrink);
            isFullVideo = true;
        }*/
    }


    public void releaseVideoPlayer() {
        ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
        if (viewGroup != null)
            viewGroup.removeAllViews();
        playPosition = -1;
        TAG = "NULL";

    }

    private boolean isPlayView(int position, String tag) {
        return playPosition == position && TAG.equals(tag);
    }

    private boolean isCurrentViewPlaying(int position, String tag) {
        return isPlayView(position, tag);
    }
}
