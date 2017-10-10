package com.example.gsyvideoplayer;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.gsyvideoplayer.utils.CommonUtil;
import com.example.gsyvideoplayer.utils.JumpUtils;
import com.example.gsyvideoplayer.video.SampleControlVideo;
import com.shuyu.gsyvideoplayer.GSYBaseActivityDetail;
import com.shuyu.gsyvideoplayer.GSYRenderView;
import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.GSYVideoGifSaveListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotListener;
import com.shuyu.gsyvideoplayer.listener.GSYVideoShotSaveListener;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.FileUtils;
import com.shuyu.gsyvideoplayer.video.base.GSYBaseVideoPlayer;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * sampleVideo支持全屏与非全屏切换的清晰度，旋转，镜像等功能.
 * Activity可以继承GSYBaseActivityDetail实现详情模式的页面
 * 或者参考DetailPlayer、DetailListPlayer实现
 * <p>
 * Created by guoshuyu on 2017/6/18.
 */

public class DetailControlActivity extends GSYBaseActivityDetail {

    @BindView(R.id.post_detail_nested_scroll)
    NestedScrollView postDetailNestedScroll;

    @BindView(R.id.detail_player)
    SampleControlVideo detailPlayer;

    @BindView(R.id.activity_detail_player)
    RelativeLayout activityDetailPlayer;

    @BindView(R.id.change_speed)
    Button changeSpeed;


    @BindView(R.id.jump)
    Button jump;

    @BindView(R.id.shot)
    Button shot;

    @BindView(R.id.start_gif)
    Button startGif;

    @BindView(R.id.stop_gif)
    Button stopGif;

    @BindView(R.id.loadingView)
    View loadingView;

    private Timer timer = new Timer();

    private TaskLocal mTimerTask;

    private String url = "http://baobab.wdjcdn.com/14564977406580.mp4";

    private List<String> picList = new ArrayList<>();

    private float speed = 1;

    private boolean saveShotBitmapSuccess = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_control);
        ButterKnife.bind(this);

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

        changeSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveTypeUI();
            }
        });


        jump.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JumpUtils.gotoControl(DetailControlActivity.this);
                //startActivity(new Intent(DetailControlActivity.this, MainActivity.class));
            }
        });

        shot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shotImage(v);
            }
        });


        startGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startGif();
            }
        });

        stopGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopGif();
            }
        });

        loadingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //do nothing
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

    /**
     * 开始gif截图
     */
    private void startGif() {
        cancelTask();
        mTimerTask = new TaskLocal();
        timer.schedule(mTimerTask, 0, 50);
    }

    /**
     * 生成gif
     */
    private void stopGif() {
        saveShotBitmapSuccess = false;
        cancelTask();
        loadingView.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (picList.size() > 2) {
                    // 保存的文件路径，请确保文件夹目录已经创建
                    File file = new File(FileUtils.getPath(), "GSY-" + System.currentTimeMillis() + ".gif");
                    // inSampleSize  采样率，越大图片越小，越大图片越模糊，需要处理的时长越短
                    // scaleSize 缩减尺寸比例，对生成的截图进行缩减，越大图片越模糊，需要处理的时长越短
                    detailPlayer.createGif(file, picList, 0, 1, 5, new GSYVideoGifSaveListener() {
                        @Override
                        public void result(boolean success, File file) {
                            detailPlayer.post(new Runnable() {
                                @Override
                                public void run() {
                                    loadingView.setVisibility(View.GONE);
                                    Toast.makeText(detailPlayer.getContext(), "创建成功", Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void process(int curPosition, int total) {
                            Debuger.printfError(" current " + curPosition + " total " + total);
                        }
                    });
                } else {
                    detailPlayer.post(new Runnable() {
                        @Override
                        public void run() {
                            loadingView.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }).start();
    }

    /**
     * 开始保存帧图片
     */
    private void startSaveBitmap() {
        // 保存的文件路径，请确保文件夹目录已经创建
        File file = new File(FileUtils.getPath(), "GSY-" + System.currentTimeMillis() + ".tmp");
        detailPlayer.saveFrame(file, new GSYVideoShotSaveListener() {
            @Override
            public void result(boolean success, final File file) {
                saveShotBitmapSuccess = true;
                if (success) {
                    Debuger.printfError(" SUCCESS CREATE FILE " + file.getAbsolutePath());
                    picList.add(file.getAbsolutePath());
                }
            }
        });

    }

    /**
     * 保存帧图片定时任务
     */
    private class TaskLocal extends TimerTask {
        @Override
        public void run() {
            if (saveShotBitmapSuccess) {
                saveShotBitmapSuccess = false;
                startSaveBitmap();
            }
        }
    }

    /**
     * 取消帧图片定时任务
     */
    private void cancelTask() {
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
    }


    /**
     * 视频截图
     */
    private void shotImage(final View v) {
        //每次设置一个监听
        detailPlayer.setCurrentFrameBitmapListener(new GSYVideoShotListener() {
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
        detailPlayer.taskShotPic();

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
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);
    }

    /**
     * 显示比例
     * 注意，GSYVideoType.setShowType是全局静态生效，除非重启APP。
     */
    private void resolveTypeUI() {
        if (speed == 1) {
            speed = 1.5f;
        } else if (speed == 1.5f) {
            speed = 2f;
        } else if (speed == 2) {
            speed = 0.5f;
        } else if (speed == 0.5f) {
            speed = 0.25f;
        } else if (speed == 0.25f) {
            speed = 1;
        }
        changeSpeed.setText("播放速度：" + speed);
        detailPlayer.setSpeedPlaying(speed, true);
    }


    private void showToast(final String tip) {
        detailPlayer.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DetailControlActivity.this, tip, Toast.LENGTH_LONG).show();
            }
        });
    }

}
