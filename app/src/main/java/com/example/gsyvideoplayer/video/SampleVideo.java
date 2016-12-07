package com.example.gsyvideoplayer.video;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.SwitchVideoModel;
import com.example.gsyvideoplayer.view.SwitchVideoTypeDialog;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuyu on 2016/12/7.
 */

public class SampleVideo extends StandardGSYVideoPlayer {

    private TextView mMoreScale;
    private TextView mSwitchSize;
    private List<SwitchVideoModel> urlList = new ArrayList<>();
    private int type = 0;
    private int sourcePosition = 0;

    public SampleVideo(Context context) {
        super(context);
        initView();
    }

    public SampleVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        mMoreScale = (TextView) findViewById(R.id.moreScale);
        mSwitchSize = (TextView) findViewById(R.id.switchSize);

        mMoreScale.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == 0) {
                    type = 1;
                    mMoreScale.setText("16:9");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_16_9);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                } else if (type == 1) {
                    type = 2;
                    mMoreScale.setText("4:3");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_4_3);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                } else if (type == 2) {
                    type = 0;
                    mMoreScale.setText("默认比例");
                    GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_DEFAULT);
                    if (mTextureView != null)
                        mTextureView.requestLayout();
                }
            }
        });

        //切换视频清晰度
        mSwitchSize.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                showSwitchDialog();
            }
        });

    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param objects       object[0]目前为title
     * @return
     */
    public boolean setUp(List<SwitchVideoModel> url, boolean cacheWithPlay, Object... objects) {
        urlList = url;
        return setUp(url.get(0).getUrl(), cacheWithPlay, objects);
    }

    /**
     * 设置播放URL
     *
     * @param url           播放url
     * @param cacheWithPlay 是否边播边缓存
     * @param cachePath     缓存路径，如果是M3U8或者HLS，请设置为false
     * @param objects       object[0]目前为title
     * @return
     */
    public boolean setUp(List<SwitchVideoModel> url, boolean cacheWithPlay, File cachePath, Object... objects) {
        urlList = url;
        return setUp(url.get(0).getUrl(), cacheWithPlay, cachePath, objects);
    }

    @Override
    public int getLayoutId() {
        return R.layout.sample_video;
    }


    private void showSwitchDialog() {
        SwitchVideoTypeDialog switchVideoTypeDialog = new SwitchVideoTypeDialog(getContext());
        switchVideoTypeDialog.initList(urlList, new SwitchVideoTypeDialog.OnListItemClickListener() {
            @Override
            public void onItemClick(int position) {
                final String name = urlList.get(position).getName();
                if (sourcePosition != position) {
                    final String url = urlList.get(position).getUrl();
                    onVideoPause();
                    final long currentPosition = mCurrentPosition;
                    GSYVideoManager.instance().releaseMediaPlayer();
                    cancelProgressTimer();
                    hideAllWidget();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            setUp(url, mCache, mCachePath, mObjects);
                            setSeekOnStart(currentPosition);
                            startPlayLogic();
                            cancelProgressTimer();
                            hideAllWidget();
                        }
                    }, 500);
                    mSwitchSize.setText(name);
                    sourcePosition = position;
                } else {
                    Toast.makeText(getContext(), "已经是 " + name, Toast.LENGTH_LONG).show();
                }
            }
        });
        switchVideoTypeDialog.show();
    }

}
