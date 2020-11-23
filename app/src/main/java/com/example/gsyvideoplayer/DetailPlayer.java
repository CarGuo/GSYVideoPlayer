package com.example.gsyvideoplayer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.appcompat.app.AppCompatActivity;

import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.gsyvideoplayer.video.LandLayoutVideo;
import com.google.android.exoplayer2.SeekParameters;
import com.google.android.exoplayer2.upstream.RawResourceDataSource;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.listener.GSYVideoProgressListener;
import com.shuyu.gsyvideoplayer.model.VideoOptionModel;
import com.shuyu.gsyvideoplayer.video.base.GSYVideoPlayer;

import com.shuyu.gsyvideoplayer.builder.GSYVideoOptionBuilder;
import com.shuyu.gsyvideoplayer.listener.LockClickListener;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;


import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import tv.danmaku.ijk.media.exo2.Exo2PlayerManager;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class DetailPlayer extends AppCompatActivity {

    @BindView(R.id.post_detail_nested_scroll)
    NestedScrollView postDetailNestedScroll;

    //推荐使用StandardGSYVideoPlayer，功能一致
    //CustomGSYVideoPlayer部分功能处于试验阶段
    @BindView(R.id.detail_player)
    LandLayoutVideo detailPlayer;

    @BindView(R.id.activity_detail_player)
    RelativeLayout activityDetailPlayer;

    @BindView(R.id.open_btn)
    Button button;


    private boolean isPlay;
    private boolean isPause;

    private OrientationUtils orientationUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_player);
        ButterKnife.bind(this);

        String url = getUrl();

        //detailPlayer.setUp(url, false, null, "测试视频");
        //detailPlayer.setLooping(true);
        //detailPlayer.setShowPauseCover(false);

        //如果视频帧数太高导致卡画面不同步
        //VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 30);
        //如果视频seek之后从头播放
        //VideoOptionModel videoOptionModel = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1);
        //list<VideoOptionModel> list = new ArrayList<>();
        //list.add(videoOptionModel);
        //GSYVideoManager.instance().setOptionModelList(list);

        //GSYVideoManager.instance().setTimeOut(4000, true);

//        VideoOptionModel videoOptionModel =
//                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1);
//        List<VideoOptionModel> list = new ArrayList<>();
//        list.add(videoOptionModel);
//        VideoOptionModel videoOptionModel2 = new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_timeout", -1);
//        list.add(videoOptionModel2);
//        GSYVideoManager.instance().setOptionModelList(list);


        /// ijk rtmp
       /*VideoOptionModel videoOptionModel =
                new VideoOptionModel(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "protocol_whitelist", "crypto,file,http,https,tcp,tls,udp,rtmp,rtsp");
        List<VideoOptionModel> list = new ArrayList<>();
        list.add(videoOptionModel);
        GSYVideoManager.instance().setOptionModelList(list);*/

        //增加封面
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        //detailPlayer.setThumbImageView(imageView);

        resolveNormalVideoUI();

        //外部辅助的旋转，帮助全屏
        orientationUtils = new OrientationUtils(this, detailPlayer);
        //初始化不打开外部的旋转
        orientationUtils.setEnable(false);

        /**仅仅横屏旋转，不变直*/
        //orientationUtils.setOnlyRotateLand(true);

        Map<String, String> header = new HashMap<>();
        header.put("ee", "33");
        header.put("allowCrossProtocolRedirects", "true");
        header.put("User-Agent", "GSY");
        GSYVideoOptionBuilder gsyVideoOption = new GSYVideoOptionBuilder();
        gsyVideoOption.setThumbImageView(imageView)
                .setIsTouchWiget(true)
                .setRotateViewAuto(false)
                //仅仅横屏旋转，不变直
                //.setOnlyRotateLand(true)
                .setRotateWithSystem(true)
                .setLockLand(true)
                .setAutoFullWithSize(true)
                .setShowFullAnimation(false)
                .setNeedLockFull(true)
                .setUrl(url)
                .setMapHeadData(header)
                .setCacheWithPlay(false)
                .setVideoTitle("测试视频")
                .setVideoAllCallBack(new GSYSampleCallBack() {
                    @Override
                    public void onPrepared(String url, Object... objects) {
                        Debuger.printfError("***** onPrepared **** " + objects[0]);
                        Debuger.printfError("***** onPrepared **** " + objects[1]);
                        super.onPrepared(url, objects);
                        //开始播放了才能旋转和全屏
                        orientationUtils.setEnable(detailPlayer.isRotateWithSystem());
                        isPlay = true;


                        //设置 seek 的临近帧。
                        if (detailPlayer.getGSYVideoManager().getPlayer() instanceof Exo2PlayerManager) {
                            ((Exo2PlayerManager) detailPlayer.getGSYVideoManager().getPlayer()).setSeekParameter(SeekParameters.NEXT_SYNC);
                            Debuger.printfError("***** setSeekParameter **** ");
                        }
                    }

                    @Override
                    public void onEnterFullscreen(String url, Object... objects) {
                        super.onEnterFullscreen(url, objects);
                        Debuger.printfError("***** onEnterFullscreen **** " + objects[0]);//title
                        Debuger.printfError("***** onEnterFullscreen **** " + objects[1]);//当前全屏player
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
                        Debuger.printfError("***** onQuitFullscreen **** " + objects[0]);//title
                        Debuger.printfError("***** onQuitFullscreen **** " + objects[1]);//当前非全屏player
                        if (orientationUtils != null) {
                            orientationUtils.backToProtVideo();
                        }
                    }
                })
                .setLockClickListener(new LockClickListener() {
                    @Override
                    public void onClick(View view, boolean lock) {
                        if (orientationUtils != null) {
                            //配合下方的onConfigurationChanged
                            orientationUtils.setEnable(!lock);
                        }
                    }
                })
                .setGSYVideoProgressListener(new GSYVideoProgressListener() {
                    @Override
                    public void onProgress(int progress, int secProgress, int currentPosition, int duration) {
                        Debuger.printfLog(" progress " + progress + " secProgress " + secProgress + " currentPosition " + currentPosition + " duration " + duration);
                    }
                })
                .build(detailPlayer);

        detailPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //直接横屏
                orientationUtils.resolveByClick();

                //第一个true是否需要隐藏actionbar，第二个true是否需要隐藏statusbar
                detailPlayer.startWindowFullscreen(DetailPlayer.this, true, true);
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fileSearch();
            }
        });
    }

    @Override
    public void onBackPressed() {

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
        getCurPlay().onVideoResume(false);
        super.onResume();
        isPause = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isPlay) {
            getCurPlay().release();
        }
        //GSYPreViewManager.instance().releaseMediaPlayer();
        if (orientationUtils != null)
            orientationUtils.releaseListener();
    }


    /**
     * orientationUtils 和  detailPlayer.onConfigurationChanged 方法是用于触发屏幕旋转的
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        //如果旋转了就全屏
        if (isPlay && !isPause) {
            detailPlayer.onConfigurationChanged(this, newConfig, orientationUtils, true, true);
        }
    }


    private void resolveNormalVideoUI() {
        //增加title
        detailPlayer.getTitleTextView().setVisibility(View.GONE);
        detailPlayer.getBackButton().setVisibility(View.GONE);
    }

    private GSYVideoPlayer getCurPlay() {
        if (detailPlayer.getFullWindowPlayer() != null) {
            return detailPlayer.getFullWindowPlayer();
        }
        return detailPlayer;
    }


    private String getUrl() {

        //String url = "android.resource://" + getPackageName() + "/" + R.raw.test;
        //注意，用ijk模式播放raw视频，这个必须打开
        GSYVideoManager.instance().enableRawPlay(getApplicationContext());

        ///exo 播放 raw
        //String url = "rawresource://" + getPackageName() + "/" + R.raw.test;

        ///exo raw 支持
        //String url =  RawResourceDataSource.buildRawResourceUri(R.raw.test).toString();

        //断网自动重新链接，url前接上ijkhttphook:
        //String url = "ijkhttphook:https://res.exexm.com/cw_145225549855002";

        //String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        //String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        //String url = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";
        //String url = "http://hjq-1257036536.cos.ap-shanghai.myqcloud.com/m3u8/m1/out2.m3u8";
        //String url = "http://223.110.243.138/PLTV/2510088/224/3221227177/index.m3u8";
        //String url = "http://qiniu.carmmi.com/image/132451525666042.mp4";
        //String url = "http://ucp.wn.sunmath.cn/file-upload/gYQJHxK9iNQKJeWyS/V80418-103803.mp4?rc_uid=7sCFCGoaF2iTc9vH9&rc_token=prJK-xGutKmy2LDQO-OZASjob0o1u_s3e5SgMHmgjtn";
        //String url = "http://7xse1z.com1.z0.glb.clouddn.com/1491813192";
        //String url = "file://"+ Environment.getExternalStorageDirectory().getPath() + "Download/132451525666042.mp4";

        //String url =  "http://ipsimg-huabei2.speiyou.cn/010/video/other/20180427/40288b156241ec6301624243bdf7021e/40288b156290270d0162a3e7eb2e0726/1524814477/movie.mp4";
        //String url =  "http://ipsimg-huabei2.speiyou.cn/010/video/other/20180424/40288b156290270d0162a3db8cdd033e/40288b156290270d0162a3e8207f074f/e787a64c-f2d0-48fe-896d-246af05f111a.mp4";

        //String url =  "http://video.7k.cn/app_video/20171202/6c8cf3ea/v.m3u8.mp4";
        //String url =  "http://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/bipbop_4x3_variant.m3u8";
        //String url = "rtmp://ctc-zhenjiang04.rt1.gensee.com/5324e855b28b453db7b0ec226598b76c_171391_0_8801038305_1591077225_205d01b8/video";
        //String url = "http://video1.dgtle.com/backend%2F2020%2F3%2F0%2F%E6%88%91%E6%B2%A1%E6%9C%89%E7%BB%99%E4%B8%80%E5%8A%A08Pro%E5%81%9A%E8%AF%84%E6%B5%8B_%E5%8D%B4%E5%B8%A6%E7%9D%80%E5%AE%83%E6%BC%82%E6%B5%81.mp4_1080.mp4";
        //String url = "http://yongtaizx.xyz/20191230/t2Axgh3k/index.m3u8";
        //String url = "http://123.56.109.212:8035/users/bfe52074fba74247853caa764b522731/films/orig/aa4c3451-0468-452a-a189-bd064a1963e5-鹿鼎记下.mp4";
        //String url = "http://static.hnyequ.cn/yequ_iOS/4940735da1227890e6a261937223e0d2_828x1472.mp4";
        String url = "http://39.104.119.42/elevator-1.0/api/downFile?path=demo.ogv";
        //String url = "http://pointshow.oss-cn-hangzhou.aliyuncs.com/transcode/ORIGINAL/Mnbc61586842828593.mp4";
        //ssl error
        //String url =  "https://file.shftz.cn:8443/filesystem/download/10/2019/3/26/ce2c7c66-e9eb-42be-adf6-f9008385ea8c.mov/play";
        //String url =  "https://us-4.wl-cdn.com/hls/20200225/fde4f8ef394731f38d68fe6d601cfd56/index.m3u8";
        //String url =  "https://cdn61.ytbbs.tv/cn/tv/55550/55550-1/play.m3u8?md5=v4sI4lWlo4XojzeAjgBGaQ&expires=1521204012&token=55550";
        //String url =  "http://1253492636.vod2.myqcloud.com/2e5fc148vodgzp1253492636/d08af82d4564972819086152830/plHZZoSkje0A.mp4";

        //String url = "rtsp://ajj:12345678@218.21.217.122:65523/h264/ch40/sub/av_stream";
        //String url = "rtsp://ajj:ajj12345678@218.21.217.122:65522/h264/ch15/sub/av_stream";//String url =  "rtsp://cloud.easydarwin.org:554/stream0.sdp";
        //String url = "http://s.swao.cn/o_1c4gm8o1nniu1had13bk1t0l1rq64m.mov";
        //String url = "http://api.ciguang.tv/avideo/play?num=02-041-0491&type=flv&v=1&client=android";
        //String url = "http://video.7k.cn/app_video/20171213/276d8195/v.m3u8.mp4";
        //String url = "http://103.233.191.21/riak/riak-bucket/6469ac502e813a4c1df7c99f364e70c1.mp4";
        //String url = "http://7xjmzj.com1.z0.glb.clouddn.com/20171026175005_JObCxCE2.mp4";
        //String url = "https://media6.smartstudy.com/ae/07/3997/2/dest.m3u8";
        //String url = "http://cdn.tiaobatiaoba.com/Upload/square/2017-11-02/1509585140_1279.mp4";

        //String url = "http://hcjs2ra2rytd8v8np1q.exp.bcevod.com/mda-hegtjx8n5e8jt9zv/mda-hegtjx8n5e8jt9zv.m3u8";
        //String url = "http://7xse1z.com1.z0.glb.clouddn.com/1491813192";
        //String url = "http://ocgk7i2aj.bkt.clouddn.com/17651ac2-693c-47e9-b2d2-b731571bad37";
        //String url = "http://111.198.24.133:83/yyy_login_server/pic/YB059284/97778276040859/1.mp4";
        //String url = "http://vr.tudou.com/v2proxy/v?sid=95001&id=496378919&st=3&pw=";
        //String url = "http://pl-ali.youku.com/playlist/m3u8?type=mp4&ts=1490185963&keyframe=0&vid=XMjYxOTQ1Mzg2MA==&ep=ciadGkiFU8cF4SvajD8bYyuwJiYHXJZ3rHbN%2FrYDAcZuH%2BrC6DPcqJ21TPs%3D&sid=04901859548541247bba8&token=0524&ctype=12&ev=1&oip=976319194";
        //String url = "http://hls.ciguang.tv/hdtv/video.m3u8";
        //String url = "https://res.exexm.com/cw_145225549855002";
        //String url = "http://storage.gzstv.net/uploads/media/huangmeiyan/jr05-09.mp4";//mepg
        //String url = "https://zh-files.oss-cn-qingdao.aliyuncs.com/20170808223928mJ1P3n57.mp4";//90度
        return url;
    }

    private static final int READ_REQUEST_CODE = 42;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null || resultCode != Activity.RESULT_OK) return;
        if (requestCode == READ_REQUEST_CODE) {
            getPathForSearch(data.getData());
        }
    }


    private void getPathForSearch(Uri uri) {
        String[] selectionArgs = new String[]{DocumentsContract.getDocumentId(uri).split(":")[1]};
        Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + "=?",
                selectionArgs, null);
        if (null != cursor) {
            if (cursor.moveToFirst()) {
                int index = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                if (index > -1) {
                    detailPlayer.setUp(uri.toString(), false, "File");
                    detailPlayer.startPlayLogic();
                }
            }
            cursor.close();
        }
    }

    protected void fileSearch() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("video/*");
        startActivityForResult(intent, READ_REQUEST_CODE);
    }
}
