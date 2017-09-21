package com.example.gsyvideoplayer;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.Matrix;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gsyvideoplayer.effect.PixelationEffect;
import com.example.gsyvideoplayer.utils.CommonUtil;
import com.example.gsyvideoplayer.utils.JumpUtils;
import com.example.gsyvideoplayer.video.SampleControlVideo;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.GSYVideoGLView;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.effect.AutoFixEffect;
import com.shuyu.gsyvideoplayer.effect.BarrelBlurEffect;
import com.shuyu.gsyvideoplayer.effect.BlackAndWhiteEffect;
import com.shuyu.gsyvideoplayer.effect.BrightnessEffect;
import com.shuyu.gsyvideoplayer.effect.ContrastEffect;
import com.shuyu.gsyvideoplayer.effect.CrossProcessEffect;
import com.shuyu.gsyvideoplayer.effect.DocumentaryEffect;
import com.shuyu.gsyvideoplayer.effect.DuotoneEffect;
import com.shuyu.gsyvideoplayer.effect.FillLightEffect;
import com.shuyu.gsyvideoplayer.effect.GammaEffect;
import com.shuyu.gsyvideoplayer.effect.GaussianBlurEffect;
import com.shuyu.gsyvideoplayer.effect.GrainEffect;
import com.shuyu.gsyvideoplayer.effect.HueEffect;
import com.shuyu.gsyvideoplayer.effect.InvertColorsEffect;
import com.shuyu.gsyvideoplayer.effect.LamoishEffect;
import com.shuyu.gsyvideoplayer.effect.NoEffect;
import com.shuyu.gsyvideoplayer.effect.OverlayEffect;
import com.shuyu.gsyvideoplayer.effect.PosterizeEffect;
import com.shuyu.gsyvideoplayer.effect.SampleBlurEffect;
import com.shuyu.gsyvideoplayer.effect.SaturationEffect;
import com.shuyu.gsyvideoplayer.effect.SepiaEffect;
import com.shuyu.gsyvideoplayer.effect.SharpnessEffect;
import com.shuyu.gsyvideoplayer.effect.TemperatureEffect;
import com.shuyu.gsyvideoplayer.effect.TintEffect;
import com.shuyu.gsyvideoplayer.effect.VignetteEffect;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.FileUtils;
import com.shuyu.gsyvideoplayer.utils.GSYVideoType;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 滤镜
 * Created by guoshuyu on 2017/6/18.
 */

public class DetailFilterActivity extends GSYBaseActivityDetail {

    @BindView(R.id.post_detail_nested_scroll)
    NestedScrollView postDetailNestedScroll;

    @BindView(R.id.detail_player)
    SampleControlVideo detailPlayer;

    @BindView(R.id.activity_detail_player)
    RelativeLayout activityDetailPlayer;

    @BindView(R.id.change_filter)
    Button changeFilter;


    @BindView(R.id.jump)
    Button jump;

    @BindView(R.id.change_anima)
    Button anima;


    private int type = 0;

    private int backupRendType;

    private float deep = 0.8f;

    private String url = "http://baobab.wdjcdn.com/14564977406580.mp4";

    private Timer timer = new Timer();

    private TaskLocal mTimerTask;

    private int percentage = 1;

    private int percentageType = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_filter);
        ButterKnife.bind(this);

        backupRendType = GSYVideoType.getRenderType();

        GSYVideoType.setRenderType(GSYVideoType.GLSURFACE);

        resolveNormalVideoUI();

        initVideoBuilderMode();

        detailPlayer.setLockClickListener(new LockClickListener() {
            @Override
            public void onClick(View view, boolean lock) {
                if (orientationUtils != null) {
                    //配合下方的onConfigurationChanged
                    orientationUtils.setEnable(!lock);
                }
            }
        });

        changeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveTypeUI();
            }
        });


        jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shotImage(v);
                //JumpUtils.gotoControl(DetailFilterActivity.this);
                //startActivity(new Intent(DetailControlActivity.this, MainActivity.class));
            }
        });

        anima.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelTask();
                mTimerTask = new TaskLocal();
                timer.schedule(mTimerTask, 0, 50);
                percentageType++;
                if (percentageType > 4) {
                    percentageType = 1;
                }
            }
        });
    }

    @Override
    public GSYBaseVideoPlayer getGSYVideoPlayer() {
        return detailPlayer;
    }

    @Override
    public GSYVideoOptionBuilder getGSYVideoOptionBuilder() {
        //内置封面可参考SampleCoverVideo
        ImageView imageView = new ImageView(this);
        loadCover(imageView, url);
        return new GSYVideoOptionBuilder()
                .setThumbImageView(imageView)
                .setUrl(url)
                .setCacheWithPlay(true)
                .setVideoTitle(" ")
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                .setLockLand(false)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setSeekRatio(1);
    }

    @Override
    public void clickForFullScreen() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoType.setRenderType(backupRendType);
        cancelTask();
    }

    /**
     * 视频截图
     */
    private void shotImage(final View v) {
        if (detailPlayer.getCurrentPlayer().getRenderProxy() != null) {
            //每次设置一个监听
            detailPlayer.getCurrentPlayer().getRenderProxy().setCurrentFrameBitmapListener(new GSYVideoShotListener() {
                @Override
                public void getBitmap(Bitmap bitmap) {
                    if (bitmap != null) {
                        try {
                            CommonUtil.saveBitmap(bitmap);
                        } catch (FileNotFoundException e) {
                            showToast("save fail ");
                            e.printStackTrace();
                            return;
                        }
                        showToast("save success ");
                    } else {
                        showToast("get bitmap fail ");
                    }
                }
            });
            //获取截图
            detailPlayer.getCurrentPlayer().getRenderProxy().taskShotPic();
        }
    }

    /**
     * 加载第三秒的帧数作为封面
     */
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
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);
    }

    /**
     * 切换滤镜
     */
    private void resolveTypeUI() {
        GSYVideoGLView.ShaderInterface effect = new NoEffect();
        switch (type) {
            case 0:
                effect = new AutoFixEffect(deep);
                break;
            case 1:
                effect = new PixelationEffect();
                break;
            case 2:
                effect = new BlackAndWhiteEffect();
                break;
            case 3:
                effect = new ContrastEffect(deep);
                break;
            case 4:
                effect = new CrossProcessEffect();
                break;
            case 5:
                effect = new DocumentaryEffect();
                break;
            case 6:
                effect = new DuotoneEffect(Color.BLUE, Color.YELLOW);
                break;
            case 7:
                effect = new FillLightEffect(deep);
                break;
            case 8:
                effect = new GammaEffect(deep);
                break;
            case 9:
                effect = new GrainEffect(deep);
                break;
            case 10:
                effect = new GrainEffect(deep);
                break;
            case 11:
                effect = new HueEffect(deep);
                break;
            case 12:
                effect = new InvertColorsEffect();
                break;
            case 13:
                effect = new LamoishEffect();
                break;
            case 14:
                effect = new PosterizeEffect();
                break;
            case 15:
                effect = new BarrelBlurEffect();
                break;
            case 16:
                effect = new SaturationEffect(deep);
                break;
            case 17:
                effect = new SepiaEffect();
                break;
            case 18:
                effect = new SharpnessEffect(deep);
                break;
            case 19:
                effect = new TemperatureEffect(deep);
                break;
            case 20:
                effect = new TintEffect(Color.GREEN);
                break;
            case 21:
                effect = new VignetteEffect(deep);
                break;
            case 22:
                effect = new NoEffect();
                break;
            case 23:
                effect = new OverlayEffect();
                break;
            case 24:
                effect = new SampleBlurEffect(4.0f);
                break;
            case 25:
                effect = new GaussianBlurEffect(6.0f, GaussianBlurEffect.TYPEXY);
                break;
            case 26:
                effect = new BrightnessEffect(deep);
                break;
        }
        detailPlayer.setEffectFilter(effect);
        type++;
        if (type > 25) {
            type = 0;
        }
    }


    private void cancelTask() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }

    /**
     * 设置GLRender的VertexShader的transformMatrix
     * 注意，这是android.opengl.Matrix
     */
    private class TaskLocal extends TimerTask {
        @Override
        public void run() {
            float[] transform = new float[16];
            switch (percentageType) {
                case 1:
                    //给予x变化
                    Matrix.setRotateM(transform, 0, 360 * percentage / 100, 1.0f, 0, 0.0f);
                    break;
                case 2:
                    //给予y变化
                    Matrix.setRotateM(transform, 0, 360 * percentage / 100, 0.0f, 1.0f, 0.0f);
                    break;
                case 3:
                    //给予z变化
                    Matrix.setRotateM(transform, 0, 360 * percentage / 100, 0.0f, 0, 1.0f);
                    break;
                case 4:
                    Matrix.setRotateM(transform, 0, 360, 0.0f, 0, 1.0f);
                    break;
            }
            //设置渲染transform
            detailPlayer.setMatrixGL(transform);
            percentage++;
            if (percentage > 100) {
                percentage = 1;
            }
        }
    }


    private void showToast(final String tip) {
        detailPlayer.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DetailFilterActivity.this, tip, Toast.LENGTH_LONG).show();
            }
        });
    }
}
