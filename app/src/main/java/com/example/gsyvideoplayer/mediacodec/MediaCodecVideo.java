package com.example.gsyvideoplayer.mediacodec;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;

import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

public class MediaCodecVideo extends StandardGSYVideoPlayer {


    public MediaCodecVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public MediaCodecVideo(Context context) {
        super(context);
    }

    public MediaCodecVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        return super.startWindowFullscreen(context, actionBar, statusBar);
    }

    @Override
    public void onSurfaceAvailable(Surface surface) {
        super.onSurfaceAvailable(surface);
    }

    @Override
    public boolean onSurfaceDestroyed(Surface surface) {
        //清空释放
        //setDisplay(null);
        //同一消息队列中去release
        //releaseSurface(surface);
        return true;
    }

    @Override
    protected void addTextureView() {
        mTextureView = new MediaCodecRenderView();
        mTextureView.addView(getContext(), mTextureViewContainer, mRotate, this, this, mEffectFilter, mMatrixGL, mRenderer, mMode);
    }

}
