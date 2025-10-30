package com.example.gsyvideoplayer.exo;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;

import android.view.View;
import android.widget.ImageView;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.databinding.ActivityDeatilExoListPlayerBinding;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用自定义的ExoPlayer，实现无缝切换下一集功能
 */
public class DetailExoListPlayer extends GSYBaseActivityDetail<GSYExo2PlayerView> {

    ActivityDeatilExoListPlayerBinding binding;

    private int type = GSYVideoType.getRenderType();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDeatilExoListPlayerBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        /// 保持当前设置
        type = GSYVideoType.getRenderType();
        ///暂停切换不出现步进，需要使用 SurfaceView
        GSYVideoType.setRenderType(GSYVideoType.SURFACE);


        //GSYBaseActivityDetail 的 普通模式初始化
        initVideo();

        List<GSYVideoModel> urls = new ArrayList<>();

        urls.add(new GSYVideoModel("https://flipfit-cdn.akamaized.net/flip_hls/6656423247ffe600199e8363-15125d/video_h1.m3u8", "标题1"));
        urls.add(new GSYVideoModel("https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8", "标题3"));
        urls.add(new GSYVideoModel("https://www.w3schools.com/html/mov_bbb.mp4", "标题2"));
        //binding.detailPlayer.setUp(urls, 1);
        binding.detailPlayer.setUp(urls, 0);

        //使用 exo 的 CacheDataSourceFactory 实现
        binding.detailPlayer.setExoCache(false);
        //binding.detailPlayer.setOverrideExtension("m3u8");

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        binding.detailPlayer.setThumbImageView(imageView);

        resolveNormalVideoUI();

        binding.detailPlayer.setIsTouchWiget(true);
        //关闭自动旋转
        binding.detailPlayer.setRotateViewAuto(false);
        binding.detailPlayer.setAutoFullWithSize(true);

        binding.detailPlayer.setLockLand(false);
        binding.detailPlayer.setShowFullAnimation(false);
        binding.detailPlayer.setNeedLockFull(true);

        binding.detailPlayer.setVideoAllCallBack(this);

        binding.detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });

        binding.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GSYExoVideoManager.instance().next();
                ((GSYExo2PlayerView) binding.detailPlayer.getCurrentPlayer()).nextUI();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (orientationUtils != null) {
                    orientationUtils.backToProtVideo();
                }
                if (GSYExoVideoManager.backFromWindowFull(DetailExoListPlayer.this)) {
                    return;
                }
                finish();
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        /// 恢复设置
        GSYVideoType.setRenderType(type);
    }

    @Override
    public GSYExo2PlayerView getGSYVideoPlayer() {
        return binding.detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //不用builder的模式
        return null;
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

    @Override
    public void onEnterFullscreen(String url, Object... objects) {
        super.onEnterFullscreen(url, objects);
        //隐藏调全屏对象的返回按键
        GSYVideoPlayer gsyVideoPlayer = (GSYVideoPlayer) objects[1];
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
    }


    private void resolveNormalVideoUI() {
        //增加title
        binding.detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        binding.detailPlayer.getBackButton().setVisibility(View.VISIBLE);
    }

    private GSYVideoPlayer getCurPlay() {
        if (binding.detailPlayer.getFullWindowPlayer() != null) {
            return binding.detailPlayer.getFullWindowPlayer();
        }
        return binding.detailPlayer;
    }


}
