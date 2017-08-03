package com.shuyu.gsyvideoplayer.video;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.R;

/**
 * Created by guoshuyu on 2017/4/1.
 * 使用正常播放按键和loading的播放器
 */

public class NormalGSYVideoPlayer extends StandardGSYVideoPlayer {

    public NormalGSYVideoPlayer(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public NormalGSYVideoPlayer(Context context) {
        super(context);
    }

    public NormalGSYVideoPlayer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public int getLayoutId() {
        return R.layout.video_layout_normal;
    }

    @Override
    protected void updateStartImage() {
        if(mStartButton instanceof ImageView) {
            ImageView imageView = (ImageView) mStartButton;
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                imageView.setImageResource(R.drawable.video_click_pause_selector);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(R.drawable.video_click_play_selector);
            } else {
                imageView.setImageResource(R.drawable.video_click_play_selector);
            }
        }
    }
}
