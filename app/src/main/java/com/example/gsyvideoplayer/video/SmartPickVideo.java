package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.SwitchVideoModel;
import com.example.gsyvideoplayer.view.LoadingDialog;
import com.example.gsyvideoplayer.view.SwitchVideoTypeDialog;
import com.shuyu.gsyvideoplayer.GSYVideoBaseManager;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYMediaPlayerListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 无缝切换视频的DEMO
 * 这里是切换清晰度，稍微修改下也可以作为切换下一集等
 */

public class SmartPickVideo extends StandardGSYVideoPlayer {


    private TextView mSwitchSize;

    private List<SwitchVideoModel> mUrlList = new ArrayList<>();

    //记住切换数据源类型
    private int mType = 0;
    //数据源
    private int mSourcePosition = 0;
    private int mPreSourcePosition = 0;

    private String mTypeText = "标准";

    private GSYVideoManager mTmpManager;

    //切换过程中最好弹出loading，不给其他任何操作
    private LoadingDialog mLoadingDialog;

    private boolean isChanging;

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public SmartPickVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public SmartPickVideo(Context context) {
        super(context);
    }

    public SmartPickVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        initView();
    }

    private void initView() {
        mSwitchSize = (TextView) findViewById(R.id.switchSize);
        //切换视频清晰度
        mSwitchSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mHadPlay && !isChanging) {
                    showSwitchDialog();
                }
            }
        });
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param title         title
     * @return
     */
    public boolean setUp(List<SwitchVideoModel> url, boolean cacheWithPlay, String title) {
        mUrlList = url;
        return setUp(url.get(mSourcePosition).getUrl(), cacheWithPlay, title);
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param title         title
     * @return
     */
    public boolean setUp(List<SwitchVideoModel> url, boolean cacheWithPlay, File cachePath, String title) {
        mUrlList = url;
        return setUp(url.get(mSourcePosition).getUrl(), cacheWithPlay, cachePath, title);
    }

    @Override
    public int getLayoutId() {
        return R.layout.sample_video_pick;
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
        SmartPickVideo sampleVideo = (SmartPickVideo) super.startWindowFullscreen(context, actionBar, statusBar);
        sampleVideo.mSourcePosition = mSourcePosition;
        sampleVideo.mListItemRect = mListItemRect;
        sampleVideo.mListItemSize = mListItemSize;
        sampleVideo.mType = mType;
        sampleVideo.mUrlList = mUrlList;
        sampleVideo.mTypeText = mTypeText;
        sampleVideo.mSwitchSize.setText(mTypeText);
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
            SmartPickVideo sampleVideo = (SmartPickVideo) gsyVideoPlayer;
            mSourcePosition = sampleVideo.mSourcePosition;
            mType = sampleVideo.mType;
            mTypeText = sampleVideo.mTypeText;
            mSwitchSize.setText(mTypeText);
            setUp(mUrlList, mCache, mCachePath, mTitle);
        }
    }

    @Override
    public void onAutoCompletion() {
        super.onAutoCompletion();
        releaseTmpManager();
    }

    @Override
    public void onCompletion() {
        super.onCompletion();
        releaseTmpManager();
    }

    /**
     * 弹出切换清晰度
     */
    private void showSwitchDialog() {
        if (!mHadPlay) {
            return;
        }
        SwitchVideoTypeDialog switchVideoTypeDialog = new SwitchVideoTypeDialog(getContext());
        switchVideoTypeDialog.initList(mUrlList, new SwitchVideoTypeDialog.OnListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                resolveStartChange(position);
            }
        });
        switchVideoTypeDialog.show();
    }


    private void resolveChangeUrl(boolean cacheWithPlay, File cachePath, String url) {
        if (mTmpManager != null) {
            mCache = cacheWithPlay;
            mCachePath = cachePath;
            mOriginUrl = url;
            this.mUrl = url;
        }
    }


    private GSYMediaPlayerListener gsyMediaPlayerListener = new GSYMediaPlayerListener() {
        @Override
        public void onPrepared() {
            if (mTmpManager != null) {
                mTmpManager.start();
                mTmpManager.seekTo(getCurrentPositionWhenPlaying());
            }
        }

        @Override
        public void onAutoCompletion() {

        }

        @Override
        public void onCompletion() {

        }

        @Override
        public void onBufferingUpdate(int percent) {

        }

        @Override
        public void onSeekComplete() {
            if (mTmpManager != null) {
                GSYVideoBaseManager manager = GSYVideoManager.instance();
                GSYVideoManager.changeManager(mTmpManager);
                mTmpManager.setLastListener(manager.lastListener());
                mTmpManager.setListener(manager.listener());

                manager.setDisplay(null);

                Debuger.printfError("**** showDisplay onSeekComplete ***** " + mSurface);
                Debuger.printfError("**** showDisplay onSeekComplete isValid***** " + mSurface.isValid());
                mTmpManager.setDisplay(mSurface);
                changeUiToPlayingClear();
                resolveChangedResult();
                manager.releaseMediaPlayer();
            }
        }

        @Override
        public void onError(int what, int extra) {
            mSourcePosition = mPreSourcePosition;
            if (mTmpManager != null) {
                mTmpManager.releaseMediaPlayer();
            }
            post(new Runnable() {
                @Override
                public void run() {
                    resolveChangedResult();
                    Toast.makeText(mContext, "change Fail", Toast.LENGTH_LONG).show();
                }
            });
        }

        @Override
        public void onInfo(int what, int extra) {

        }

        @Override
        public void onVideoSizeChanged() {

        }

        @Override
        public void onBackFullscreen() {

        }

        @Override
        public void onVideoPause() {

        }

        @Override
        public void onVideoResume() {

        }

        @Override
        public void onVideoResume(boolean seek) {

        }
    };

    private void resolveStartChange(int position) {
        final String name = mUrlList.get(position).getName();
        if (mSourcePosition != position) {
            if ((mCurrentState == GSYVideoPlayer.CURRENT_STATE_PLAYING
                    || mCurrentState == GSYVideoPlayer.CURRENT_STATE_PAUSE)) {
                showLoading();
                final String url = mUrlList.get(position).getUrl();
                cancelProgressTimer();
                hideAllWidget();
                if (mTitle != null && mTitleTextView != null) {
                    mTitleTextView.setText(mTitle);
                }
                mPreSourcePosition = mSourcePosition;
                isChanging = true;
                mTypeText = name;
                mSwitchSize.setText(name);
                mSourcePosition = position;
                //创建临时管理器执行加载播放
                mTmpManager = GSYVideoManager.tmpInstance(gsyMediaPlayerListener);
                mTmpManager.initContext(getContext().getApplicationContext());
                resolveChangeUrl(mCache, mCachePath, url);
                mTmpManager.prepare(mUrl, mMapHeadData, mLooping, mSpeed, mCache, mCachePath, null);
                changeUiToPlayingBufferingShow();
            }
        } else {
            Toast.makeText(getContext(), "已经是 " + name, Toast.LENGTH_LONG).show();
        }
    }

    private void resolveChangedResult() {
        isChanging = false;
        mTmpManager = null;
        final String name = mUrlList.get(mSourcePosition).getName();
        final String url = mUrlList.get(mSourcePosition).getUrl();
        mTypeText = name;
        mSwitchSize.setText(name);
        resolveChangeUrl(mCache, mCachePath, url);
        hideLoading();
    }

    private void releaseTmpManager() {
        if (mTmpManager != null) {
            mTmpManager.releaseMediaPlayer();
            mTmpManager = null;
        }
    }

    private void showLoading() {
        hideLoading();
        mLoadingDialog = new LoadingDialog(mContext);
        mLoadingDialog.show();
    }

    private void hideLoading() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
            mLoadingDialog = null;
        }
    }

    @Override
    public boolean onSurfaceDestroyed(Surface surface) {
        //清空释放
        setDisplay(null);
        //同一消息队列中去release
        //todo 需要处理为什么全屏时全屏的surface会被释放了
        //releaseSurface(surface);
        return true;
    }
}
