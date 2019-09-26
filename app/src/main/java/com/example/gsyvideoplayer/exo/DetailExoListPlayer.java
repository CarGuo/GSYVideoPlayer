package com.example.gsyvideoplayer.exo;

import android.os.Bundle;
import androidx.core.widget.NestedScrollView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.model.GSYVideoModel;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 使用自定义的ExoPlayer，实现无缝切换下一集功能
 */
public class DetailExoListPlayer extends GSYBaseActivityDetail<GSYExo2PlayerView> {


    @BindView(R.id.post_detail_nested_scroll)
    NestedScrollView postDetailNestedScroll;
    @BindView(R.id.detail_player)
    GSYExo2PlayerView detailPlayer;
    @BindView(R.id.activity_detail_player)
    RelativeLayout activityDetailPlayer;
    @BindView(R.id.next)
    View next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deatil_exo_list_player);
        ButterKnife.bind(this);

        //GSYBaseActivityDetail 的 普通模式初始化
        initVideo();

        List<GSYVideoModel> urls = new ArrayList<>();

        urls.add(new GSYVideoModel("http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4", "标题1"));
        urls.add(new GSYVideoModel("https://media6.smartstudy.com/ae/07/3997/2/dest.m3u8", "标题3"));
        urls.add(new GSYVideoModel("http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4", "标题2"));
        detailPlayer.setUp(urls, 0);

        //使用 exo 的 CacheDataSourceFactory 实现
        detailPlayer.setExoCache(false);
        //detailPlayer.setOverrideExtension("m3u8");

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        detailPlayer.setThumbImageView(imageView);

        resolveNormalVideoUI();

        detailPlayer.setIsTouchWiget(true);
        //关闭自动旋转
        detailPlayer.setRotateViewAuto(false);
        detailPlayer.setLockLand(false);
        detailPlayer.setShowFullAnimation(false);
        detailPlayer.setNeedLockFull(true);

        detailPlayer.setVideoAllCallBack(this);

        detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GSYExoVideoManager.instance().next();
            }
        });

    }





    /**
     * 重载为GSYExoVideoManager的方法处理
     */
    @Override
    public void onBackPressed() {
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
        return detailPlayer;
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
        GSYVideoPlayer gsyVideoPlayer = (GSYVideoPlayer)objects[1];
        gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
    }


    private void resolveNormalVideoUI() {
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.VISIBLE);
        detailPlayer.getBackButton().setVisibility(View.VISIBLE);
    }

    private GSYVideoPlayer getCurPlay() {
        if (detailPlayer.getFullWindowPlayer() != null) {
            return  detailPlayer.getFullWindowPlayer();
        }
        return detailPlayer;
    }


}
