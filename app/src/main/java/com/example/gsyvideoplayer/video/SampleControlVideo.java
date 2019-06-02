package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;


/**
 * Created by shuyu on 2016/12/7.
 * 注意
 * 这个播放器的demo配置切换到全屏播放器
 * 这只是单纯的作为全屏播放显示，如果需要做大小屏幕切换，请记得在这里耶设置上视频全屏的需要的自定义配置
 */

public class SampleControlVideo extends StandardGSYVideoPlayer {

    private TextView mMoreScale;

    private TextView mChangeRotate;

    private TextView mChangeTransform;

    //记住切换数据源类型
    private int mType = 0;

    private int mTransformSize = 0;

    //数据源
    private int mSourcePosition = 0;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public SampleControlVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SampleControlVideo(Context context) {
        super(context);
    }

    public SampleControlVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initView();
    }

    private void initView() {
        mMoreScale = (TextView) findViewById(R.id.moreScale);
        mChangeRotate = (TextView) findViewById(R.id.change_rotate);
        mChangeTransform = (TextView) findViewById(R.id.change_transform);

        //切换清晰度
        mMoreScale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHadPlay) {
                    return;
                }
                if (mType == 0) {
                    mType = 1;
                } else if (mType == 1) {
                    mType = 2;
                } else if (mType == 2) {
                    mType = 3;
                } else if (mType == 3) {
                    mType = 4;
                } else if (mType == 4) {
                    mType = 0;
                }
                resolveTypeUI();
            }
        });

        //旋转播放角度
        mChangeRotate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHadPlay) {
                    return;
                }
                if ((mTextureView.getRotation() - mRotate) == 270) {
                    mTextureView.setRotation(mRotate);
                    mTextureView.requestLayout();
                } else {
                    mTextureView.setRotation(mTextureView.getRotation() + 90);
                    mTextureView.requestLayout();
                }
            }
        });

        //镜像旋转
        mChangeTransform.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mHadPlay) {
                    return;
                }
                if (mTransformSize == 0) {
                    mTransformSize = 1;
                } else if (mTransformSize == 1) {
                    mTransformSize = 2;
                } else if (mTransformSize == 2) {
                    mTransformSize = 0;
                }
                resolveTransform();
            }
        });

    }

    @Override
    public void onSurfaceSizeChanged(Surface surface, int width, int height) {
        super.onSurfaceSizeChanged(surface, width, height);
        resolveTransform();
    }

    /**
     * 处理显示逻辑
     */
    @Override
    public void onSurfaceAvailable(Surface surface) {
        super.onSurfaceAvailable(surface);
        resolveRotateUI();
        resolveTransform();
    }

    /**
     * 处理镜像旋转
     * 注意，暂停时
     */
    protected void resolveTransform() {
        switch (mTransformSize) {
            case 1: {
                Matrix transform = new Matrix();
                transform.setScale(-1, 1, mTextureView.getWidth() / 2, 0);
                mTextureView.setTransform(transform);
                mChangeTransform.setText("左右镜像");
                mTextureView.invalidate();
            }
            break;
            case 2: {
                Matrix transform = new Matrix();
                transform.setScale(1, -1, 0, mTextureView.getHeight() / 2);
                mTextureView.setTransform(transform);
                mChangeTransform.setText("上下镜像");
                mTextureView.invalidate();
            }
            break;
            case 0: {
                Matrix transform = new Matrix();
                transform.setScale(1, 1, mTextureView.getWidth() / 2, 0);
                mTextureView.setTransform(transform);
                mChangeTransform.setText("旋转镜像");
                mTextureView.invalidate();
            }
            break;
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.sample_video;
    }


    /**
     * 全屏时将对应处理参数逻辑赋给全屏播放器
     *
     * @param context
     * @param actionBar
     * @param statusBar
     * @return
     */
    @Override
    public GSYBaseVideoPlayer startWindowFullscreen(Context context, boolean actionBar, boolean statusBar) {
        SampleControlVideo sampleVideo = (SampleControlVideo) super.startWindowFullscreen(context, actionBar, statusBar);
        sampleVideo.mSourcePosition = mSourcePosition;
        sampleVideo.mType = mType;
        sampleVideo.mTransformSize = mTransformSize;
        //sampleVideo.resolveTransform();
        sampleVideo.resolveTypeUI();
        //sampleVideo.resolveRotateUI();
        //这个播放器的demo配置切换到全屏播放器
        //这只是单纯的作为全屏播放显示，如果需要做大小屏幕切换，请记得在这里耶设置上视频全屏的需要的自定义配置
        //比如已旋转角度之类的等等
        //可参考super中的实现
        return sampleVideo;
    }

    /**
     * 推出全屏时将对应处理参数逻辑返回给非播放器
     *
     * @param oldF
     * @param vp
     * @param gsyVideoPlayer
     */
    @Override
    protected void resolveNormalVideoShow(View oldF, ViewGroup vp, GSYVideoPlayer gsyVideoPlayer) {
        super.resolveNormalVideoShow(oldF, vp, gsyVideoPlayer);
        if (gsyVideoPlayer != null) {
            SampleControlVideo sampleVideo = (SampleControlVideo) gsyVideoPlayer;
            mSourcePosition = sampleVideo.mSourcePosition;
            mType = sampleVideo.mType;
            mTransformSize = sampleVideo.mTransformSize;
            resolveTypeUI();
        }
    }

    /**
     * 旋转逻辑
     */
    private void resolveRotateUI() {
        if (!mHadPlay) {
            return;
        }
        mTextureView.setRotation(mRotate);
        mTextureView.requestLayout();
    }

    /**
     * 显示比例
     * 注意，GSYVideoType.setShowType是全局静态生效，除非重启APP。
     */
    private void resolveTypeUI() {
        if (!mHadPlay) {
            return;
        }
        if (mType == 1) {
            mMoreScale.setText("16:9");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);
        } else if (mType == 2) {
            mMoreScale.setText("4:3");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
        } else if (mType == 3) {
            mMoreScale.setText("全屏");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL);
        } else if (mType == 4) {
            mMoreScale.setText("拉伸全屏");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_MATCH_FULL);
        } else if (mType == 0) {
            mMoreScale.setText("默认比例");
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
        }
        changeTextureViewShowType();
        if (mTextureView != null)
            mTextureView.requestLayout();
    }


}
