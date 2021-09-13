package com.example.gsyvideoplayer.exo;

import android.os.Bundle;

import androidx.core.widget.NestedScrollView;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.databinding.ActivityDeatilExoListPlayerBinding;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 使用自定义的ExoPlayer，实现无缝切换下一集功能
 */
public class DetailExoListPlayer extends GSYBaseActivityDetail<GSYExo2PlayerView> {

    ActivityDeatilExoListPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDeatilExoListPlayerBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        //GSYBaseActivityDetail 的 普通模式初始化
        initVideo();

        List<GSYVideoModel> urls = new ArrayList<>();

        urls.add(new GSYVideoModel("http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4", "标题1"));
        urls.add(new GSYVideoModel("https://media6.smartstudy.com/ae/07/3997/2/dest.m3u8", "标题3"));
        urls.add(new GSYVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4", "标题2"));
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

    }


    /**
     * 重载为GSYExoVideoManager的方法处理
     */
    @Override
    public void onBackPressed() {
        // ------- ！！！如果不需要旋转屏幕，可以不调用！！！-------
        // 不需要屏幕旋转，还需要设置 setNeedOrientationUtils(false)
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
        if (GSYExoVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
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
