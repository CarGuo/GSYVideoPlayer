package com.example.gsyvideoplayer;

import android.graphics.Point;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;

import android.view.View;
import android.webkit.WebSettings;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gsyvideoplayer.databinding.ActivityWebDetailBinding;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.CommonUtil;

/**
 * Created by shuyu on 2016/12/26.
 */

public class WebDetailActivity extends GSYBaseActivityDetail<StandardGSYVideoPlayer> {


    private boolean isSmall;

    private int backupRendType;
    private ActivityWebDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityWebDetailBinding.inflate(getLayoutInflater());
        View rootView = binding.getRoot();
        setContentView(rootView);

        backupRendType = GSYVideoType.getRenderType();

        //设置为Surface播放模式，注意此设置是全局的
        GSYVideoType.setRenderType(GSYVideoType.SUFRACE);

        resolveNormalVideoUI();

        initVideoBuilderMode();


        binding.webPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                    binding.webPlayer.getCurrentPlayer().setRotateViewAuto(!lock);
                }
            }
        });


        WebSettings settings = binding.scrollWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        binding.scrollWebView.loadUrl("https://www.baidu.com");


        orientationUtils.setRotateWithSystem(false);

        binding.webTopLayout.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (!binding.webPlayer.isIfCurrentIsFullscreen() && scrollY >= 0 && isPlay) {
                    if (scrollY > binding.webPlayer.getHeight()) {
                        //如果是小窗口就不需要处理
                        if (!isSmall) {
                            isSmall = true;
                            int size = CommonUtil.dip2px(WebDetailActivity.this, 150);
                            binding.webPlayer.showSmallVideo(new Point(size, size), true, true);
                            orientationUtils.setEnable(false);
                        }
                    } else {
                        if (isSmall) {
                            isSmall = false;
                            orientationUtils.setEnable(true);
                            //必须
                            binding.webTopLayoutVideo.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    binding.webPlayer.hideSmallVideo();
                                }
                            }, 50);
                        }
                    }
                    binding.webTopLayoutVideo.setTranslationY((scrollY <= binding.webTopLayoutVideo.getHeight()) ? -scrollY : -binding.webTopLayoutVideo.getHeight());
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //设置为GL播放模式，才能支持滤镜，注意此设置是全局的
        GSYVideoType.setRenderType(backupRendType);
    }

    @Override
    public StandardGSYVideoPlayer getGSYVideoPlayer() {
        return binding.webPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        //String url = "https://d131x7vzzf85jg.cloudfront.net/upload/documents/paper/b2/61/00/00/20160420_115018_b544.mp4";
        //增加封面。内置封面可参考SampleCoverVideo
        ImageView imageView = new ImageView(this);
        loadCover(imageView, url);
        return new GSYVideoOptionBuilder()
            .setThumbImageView(imageView)
            .setUrl(url)
            .setCacheWithPlay(false)
            .setRotateWithSystem(false)
            .setVideoTitle("测试视频")
            .setIsTouchWiget(true)
            .setRotateViewAuto(false)
            .setLockLand(false)
            .setShowFullAnimation(false)
            .setNeedLockFull(true);
    }

    @Override
    public void clickForFullScreen() {

    }

    /**
     * 是否启动旋转横屏，true表示启动
     *
     * @return true
     */
    @Override
    public boolean getDetailOrientationRotateAuto() {
        return true;
    }

    private void loadCover(ImageView imageView, String url) {

        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        Glide.with(this.getApplicationContext())
            .setDefaultRequestOptions(
                new RequestOptions()
                    .frame(3000000)
                    .centerCrop()
                    .error(R.mipmap.xxx2)
                    .placeholder(R.mipmap.xxx1))
            .load(url)
            .into(imageView);
    }


    private void resolveNormalVideoUI() {
        //增加title
        binding.webPlayer.getTitleTextView().setVisibility(View.GONE);
        binding.webPlayer.getBackButton().setVisibility(View.GONE);
    }
}
