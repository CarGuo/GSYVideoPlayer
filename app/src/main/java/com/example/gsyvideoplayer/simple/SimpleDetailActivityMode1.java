package com.example.gsyvideoplayer.simple;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gsyvideoplayer.R;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

/**
 * 简单详情实现模式1
 */
public class SimpleDetailActivityMode1 extends GSYBaseActivityDetail<StandardGSYVideoPlayer> {

    StandardGSYVideoPlayer detailPlayer;

//    private String url = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";
    private String url = "http://alvideo.ippzone.com/zyvd/98/90/b753-55fe-11e9-b0d8-00163e0c0248";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simple_detail_player);

        detailPlayer = (StandardGSYVideoPlayer) findViewById(R.id.detail_player);
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);

        initVideoBuilderMode();

    }

    @Override
    public StandardGSYVideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //内置封面可参考SampleCoverVideo
        ImageView imageView = new ImageView(this);
        //loadCover(imageView, url);
        return new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setUrl(url)
                .setCacheWithPlay(true)
                .setVideoTitle("这里是一个竖直方向的视频")
                .setIsTouchWiget(true)
                //.setAutoFullWithSize(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)//打开动画
                .setNeedLockFull(true)
                .setSeekRatio(1);
    }

    @Override
    public void clickForFullScreen() {

    }


    /**
     * 是否启动旋转横屏，true表示启动
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

}
