package com.shuyu.gsyvideoplayer.utils;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

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
        gsyVideoPlayer.getmFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //resolveFullBtn(holder.standardGSYVideoPlayer);
            }
        });

        gsyVideoPlayer.startPlayLogic();
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
