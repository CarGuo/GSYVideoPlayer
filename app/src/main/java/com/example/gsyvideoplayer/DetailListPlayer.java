package com.example.gsyvideoplayer;

import android.os.Bundle;

import androidx.core.widget.NestedScrollView;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.databinding.ActivityDeatilListPlayerBinding;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.video.ListGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity可以继承GSYBaseActivityDetail实现详情模式的页面
 * 或者参考DetailPlayer、DetailListPlayer实现
 * Created by shuyu on 2016/12/20.
 */

public class DetailListPlayer extends GSYBaseActivityDetail<ListGSYVideoPlayer> {

    ActivityDeatilListPlayerBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityDeatilListPlayerBinding.inflate(getLayoutInflater());

        View rootView = binding.getRoot();
        setContentView(rootView);


        //普通模式
        initVideo();

        //String url = "http://baobab.wd jcdn.com/14564977406580.mp4";
        List<GSYVideoModel> urls = new ArrayList<>();
        urls.add(new GSYVideoModel("http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4", "标题1"));
        urls.add(new GSYVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4", "标题2"));
        urls.add(new GSYVideoModel("https://res.exexm.com/cw_145225549855002", "标题3"));
        urls.add(new GSYVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4", "标题4"));
        binding.detailPlayer.setUp(urls, true, 0);

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
        //detailPlayer.setNeedLockFull(true);
        binding.detailPlayer.setAutoFullWithSize(false);

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
                ((ListGSYVideoPlayer) binding.detailPlayer.getCurrentPlayer()).playNext();
            }
        });

    }

    @Override
    public ListGSYVideoPlayer getGSYVideoPlayer() {
        return binding.detailPlayer;
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
        return false;
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
