package com.example.gsyvideoplayer;

import android.content.res.Configuration;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gsyvideoplayer.databinding.ActivityDetailMoreTypeBinding;
import com.example.gsyvideoplayer.model.SwitchVideoModel;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by guoshuyu on 2017/6/18.
 * sampleVideo支持全屏与非全屏切换的清晰度，旋转，镜像等功能.
 */

public class DetailMoreTypeActivity extends AppCompatActivity {

    private boolean isPlay;
    private boolean isPause;
    private boolean isRelease;

    private OrientationUtils orientationUtils;

    private MediaMetadataRetriever mCoverMedia;

    private ImageView coverImageView;

    private ActivityDetailMoreTypeBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailMoreTypeBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        String source1 = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        //String source1 = "https://res.exexm.com/cw_145225549855002";
        String name = "普通";
        SwitchVideoModel switchVideoModel = new SwitchVideoModel(name, source1);

        String source2 = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f30.mp4";
        String name2 = "清晰";
        SwitchVideoModel switchVideoModel2 = new SwitchVideoModel(name2, source2);

        List<SwitchVideoModel> list = new ArrayList<>();
        list.add(switchVideoModel);
        list.add(switchVideoModel2);

        binding.detailPlayer.setUp(list, true, "");

        //增加封面
        coverImageView = new ImageView(this);
        coverImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //coverImageView.setImageResource(R.mipmap.xxx1);
        binding.detailPlayer.setThumbImageView(coverImageView);

        resolveNormalVideoUI();

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, binding.detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

        binding.detailPlayer.setIsTouchWiget(true);
        //detailPlayer.setIsTouchWigetFull(false);
        //关闭自动旋转
        binding.detailPlayer.setRotateViewAuto(false);
        binding.detailPlayer.setLockLand(false);

        //打开  实现竖屏全屏动画
        binding.detailPlayer.setShowFullAnimation(true);

        binding.detailPlayer.setNeedLockFull(true);
        binding.detailPlayer.setSeekRatio(1);
        //detailPlayer.setOpenPreView(false);
        binding.detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //屏蔽，实现竖屏全屏
                //orientationUtils.resolveByClick();

                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                binding.detailPlayer.startWindowFullscreen(DetailMoreTypeActivity.this, true, true);
            }
        });

        binding.detailPlayer.setVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                //开始播放了才能旋转和全屏
                //orientationUtils.setEnable(true);
                orientationUtils.setEnable(binding.detailPlayer.isRotateWithSystem());
                isPlay = true;
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
            }

            @Override
            public void onClickStartError(String url, Object... objects) {
                super.onClickStartError(url, objects);
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                //屏蔽，实现竖屏全屏
                //if (orientationUtils != null) {
                //orientationUtils.backToProtVideo();
                //}
            }
        });

        binding.detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                //屏蔽，实现竖屏全屏
                //if (orientationUtils != null) {
                //配合下方的onConfigurationChanged
                //orientationUtils.setEnable(!lock);
                //}
            }
        });

        loadFirstFrameCover(source1);
    }

    @Override
    public void onBackPressed() {

        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }

        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }


    @Override
    protected void onPause() {
        getCurPlay().onVideoPause();
        super.onPause();
        isPause = true;
    }

    @Override
    protected void onResume() {
        getCurPlay().onVideoResume();
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRelease = true;
        if (isPlay) {
            getCurPlay().release();
        }
        //GSYPreViewManager.instance().releaseMediaPlayer();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
        if (mCoverMedia != null) {
            mCoverMedia.release();
            mCoverMedia = null;
        }
    }

    /**
     * orientationUtils 和  detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            binding.detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
        //竖屏全屏
        orientationUtils.setEnable(false);
    }


    private GSYVideoPlayer getCurPlay() {
        if (binding.detailPlayer.getFullWindowPlayer() != null) {
            return binding.detailPlayer.getFullWindowPlayer();
        }
        return binding.detailPlayer;
    }


    private void resolveNormalVideoUI() {
        //增加title
        binding.detailPlayer.getTitleTextView().setVisibility(View.GONE);
        binding.detailPlayer.getBackButton().setVisibility(View.GONE);
    }


    /**
     * 这里只是演示，并不建议直接这么做
     * MediaMetadataRetriever最好做一个独立的管理器
     * 使用缓存
     * 注意资源的开销和异步等
     *
     * @param url
     */
    public void loadFirstFrameCover(String url) {

        //原始方法
        /*final MediaMetadataRetriever mediaMetadataRetriever = getMediaMetadataRetriever(url);
        //获取帧图片
        if (getMediaMetadataRetriever(url) != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    final Bitmap bitmap = mediaMetadataRetriever
                            .getFrameAtTime(1000, MediaMetadataRetriever.OPTION_CLOSEST);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (bitmap != null && !isRelease) {
                                Debuger.printfLog("time " + System.currentTimeMillis());
                                //显示
                                coverImageView.setImageBitmap(bitmap);
                            }
                        }
                    });
                }
            }).start();
        }*/

        //可以参考Glide，内部也是封装了MediaMetadataRetriever
        Glide.with(this.getApplicationContext())
            .setDefaultRequestOptions(
                new RequestOptions()
                    .frame(1000000)
                    .centerCrop()
                    .error(R.mipmap.xxx2)
                    .placeholder(R.mipmap.xxx1))
            .load(url)
            .into(coverImageView);
    }

    public MediaMetadataRetriever getMediaMetadataRetriever(String url) {
        if (mCoverMedia == null) {
            mCoverMedia = new MediaMetadataRetriever();
        }
        mCoverMedia.setDataSource(url, new HashMap<String, String>());
        return mCoverMedia;
    }
}
