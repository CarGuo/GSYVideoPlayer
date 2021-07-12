package com.example.gsyvideoplayer;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.example.gsyvideoplayer.databinding.ActivityDetailAdPlayerBinding;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.video.GSYSampleADVideoPlayer;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;



public class DetailADPlayer extends GSYBaseActivityDetail<ListGSYVideoPlayer> {

    ActivityDetailAdPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDetailAdPlayerBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        //普通模式
        initVideo();

        ArrayList<GSYSampleADVideoPlayer.GSYADVideoModel> urls = new ArrayList<>();
        //广告1
        urls.add(new GSYSampleADVideoPlayer.GSYADVideoModel("http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4",
                "", GSYSampleADVideoPlayer.GSYADVideoModel.TYPE_AD));
        //正式内容1
        urls.add(new GSYSampleADVideoPlayer.GSYADVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4",
                "正文1标题", GSYSampleADVideoPlayer.GSYADVideoModel.TYPE_NORMAL));
        //广告2
        urls.add(new GSYSampleADVideoPlayer.GSYADVideoModel("http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4",
                "", GSYSampleADVideoPlayer.GSYADVideoModel.TYPE_AD, true));
        //正式内容2
        urls.add(new GSYSampleADVideoPlayer.GSYADVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f30.mp4",
                "正文2标题", GSYSampleADVideoPlayer.GSYADVideoModel.TYPE_NORMAL));

        binding.adPlayer.setAdUp(urls, true, 0);

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        binding.adPlayer.setThumbImageView(imageView);

        resolveNormalVideoUI();

        binding.adPlayer.setIsTouchWiget(true);
        //关闭自动旋转
        binding.adPlayer.setRotateViewAuto(false);
        binding.adPlayer.setLockLand(false);
        binding.adPlayer.setShowFullAnimation(false);
        binding.adPlayer.setNeedLockFull(true);

        binding.adPlayer.setVideoAllCallBack(this);

        binding.adPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });

    }

    @Override
    public ListGSYVideoPlayer getGSYVideoPlayer() {
        return binding.adPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //不需要builder的
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
        binding.adPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        binding.adPlayer.getBackButton().setVisibility(View.VISIBLE);
    }
}

