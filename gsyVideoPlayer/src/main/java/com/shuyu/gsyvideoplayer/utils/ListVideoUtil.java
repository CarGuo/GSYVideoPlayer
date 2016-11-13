package com.shuyu.gsyvideoplayer.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
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

    private int playPosition = -1; // 播放的位置
    private String TAG = "NULL"; //播放的标志
    private StandardGSYVideoPlayer gsyVideoPlayer;
    private ViewGroup fullViewContainer;
    private ViewGroup listParent;
    private OrientationUtils orientationUtils;
    private Context context;
    private boolean isFull;

    public ListVideoUtil(Context context) {
        gsyVideoPlayer = new StandardGSYVideoPlayer(context);
        this.context = context;
    }

    public void addVideoPlayer(Context context, final int position, int imgId, String tag,
                               ViewGroup container, View playBtn) {
        container.removeAllViews();
        if (isCurrentViewPlaying(position, tag)) {
            if (!isFull) {
                ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
                if (viewGroup != null)
                    viewGroup.removeAllViews();
                container.addView(gsyVideoPlayer);
                playBtn.setVisibility(View.INVISIBLE);
            }
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
                resolveFullBtn();
            }
        });

        gsyVideoPlayer.startPlayLogic();
    }


    public void resolveFullBtn() {
        if (fullViewContainer == null) {
            return;
        }
        if (!isFull) {
            resolveToFull();
        } else {
            resolveToNormal();
        }
    }

    private void resolveToFull() {
        isFull = true;
        ViewGroup viewGroup = (ViewGroup) gsyVideoPlayer.getParent();
        if (viewGroup != null) {
            listParent = viewGroup;
            viewGroup.removeView(gsyVideoPlayer);
        }
        fullViewContainer.setBackgroundColor(Color.BLACK);
        fullViewContainer.addView(gsyVideoPlayer);
        gsyVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_shrink);
        gsyVideoPlayer.getBackButton().setVisibility(View.VISIBLE);
        //设置旋转
        orientationUtils = new OrientationUtils((Activity) context, gsyVideoPlayer);
        gsyVideoPlayer.getBackButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveToNormal();
            }
        });
    }

    private void resolveToNormal() {
        int delay = orientationUtils.backToProtVideo();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                isFull = false;
                fullViewContainer.removeAllViews();
                orientationUtils.setEnable(false);
                fullViewContainer.setBackgroundColor(Color.TRANSPARENT);
                listParent.addView(gsyVideoPlayer);
                gsyVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_enlarge);
                gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
            }
        }, delay);
    }


    public boolean backFromFull() {
        boolean isFull = false;
        if (fullViewContainer.getChildCount() > 0) {
            isFull = true;
            resolveToNormal();
        }
        return isFull;
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

    public void setFullViewContainer(ViewGroup fullViewContainer) {
        this.fullViewContainer = fullViewContainer;
    }
}
