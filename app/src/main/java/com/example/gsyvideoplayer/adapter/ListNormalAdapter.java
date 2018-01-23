package com.example.gsyvideoplayer.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.VideoModel;
import com.example.gsyvideoplayer.video.SampleCoverVideo;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.utils.Debuger;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuyu on 2016/11/12.
 */

public class ListNormalAdapter extends BaseAdapter {

    public static final String TAG = "ListNormalAdapter";

    private List<VideoModel> list = new ArrayList<>();
    private LayoutInflater inflater = null;
    private Context context;

    private StandardGSYVideoPlayer curPlayer;

    protected OrientationUtils orientationUtils;

    protected boolean isPlay;

    public ListNormalAdapter(Context context) {
        super();
        this.context = context;
        inflater = LayoutInflater.from(context);
        for (int i = 0; i < 40; i++) {
            list.add(new VideoModel());
        }

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_video_item_normal, null);
            holder.gsyVideoPlayer = (SampleCoverVideo) convertView.findViewById(R.id.video_item_player);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        //final String url = "https://res.exexm.com/cw_145225549855002";
        final String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        //final String url = "http://7xse1z.com1.z0.glb.clouddn.com/1491813192";
        //final String url = "http://111.198.24.133:83/yyy_login_server/pic/YB059284/97778276040859/1.mp4";


        if (position % 2 == 0) {
            holder.gsyVideoPlayer.loadCoverImage(url, R.mipmap.xxx1);
        } else {
            holder.gsyVideoPlayer.loadCoverImage(url, R.mipmap.xxx2);
        }

        //防止错位，离开释放
        //holder.gsyVideoPlayer.initUIState();

        //默认缓存路径
        //使用lazy的set可以避免滑动卡的情况存在
        holder.gsyVideoPlayer.setUpLazy(url, true, null, null,"这是title");

        //holder.gsyVideoPlayer.setNeedShowWifiTip(false);

        /************************下方为其他路径************************************/
        //如果一个列表的缓存路劲都一一致
        //holder.gsyVideoPlayer.setUp(url, true, new File(FileUtils.getTestPath()), "");

        /************************下方为其他路径************************************/
        //如果一个列表里的缓存路劲不一致
        //int playPosition = GSYVideoManager.instance().getPlayPosition();
        //避免全屏返回的时候不可用了
        /*if (playPosition < 0 || playPosition != position ||
                !GSYVideoManager.instance().getPlayTag().equals(ListNormalAdapter.TAG)) {
            holder.gsyVideoPlayer.initUIState();
        }*/
        //如果设置了点击封面可以播放，如果缓存列表路径不一致，还需要设置封面点击
        /*holder.gsyVideoPlayer.setThumbPlay(true);

        holder.gsyVideoPlayer.getStartButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //需要切换缓存路径的
                holder.gsyVideoPlayer.setUp(url, true, new File(FileUtils.getTestPath(), ""));
                holder.gsyVideoPlayer.startPlayLogic();
            }
        });

        holder.gsyVideoPlayer.getThumbImageViewLayout().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //需要切换缓存路径的
                holder.gsyVideoPlayer.setUp(url, true, new File(FileUtils.getTestPath(), ""));
                holder.gsyVideoPlayer.startPlayLogic();
            }
        });*/

        //增加title
        holder.gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);

        //设置返回键
        holder.gsyVideoPlayer.getBackButton().setVisibility(View.GONE);

        //设置全屏按键功能
        holder.gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resolveFullBtn(holder.gsyVideoPlayer);
            }
        });
        holder.gsyVideoPlayer.setRotateViewAuto(!getListNeedAutoLand());
        holder.gsyVideoPlayer.setLockLand(!getListNeedAutoLand());
        holder.gsyVideoPlayer.setPlayTag(TAG);
        holder.gsyVideoPlayer.setShowFullAnimation(!getListNeedAutoLand());
        holder.gsyVideoPlayer.setIsTouchWiget(false);
        //循环
        //holder.gsyVideoPlayer.setLooping(true);
        holder.gsyVideoPlayer.setNeedLockFull(true);

        //holder.gsyVideoPlayer.setSpeed(2);

        holder.gsyVideoPlayer.setPlayPosition(position);

        holder.gsyVideoPlayer.setStandardVideoAllCallBack(new GSYSampleCallBack() {
            @Override
            public void onClickStartIcon(String url, Object... objects) {
                super.onClickStartIcon(url, objects);
            }

            @Override
            public void onPrepared(String url, Object... objects) {
                super.onPrepared(url, objects);
                Debuger.printfLog("onPrepared");
                if (!holder.gsyVideoPlayer.isIfCurrentIsFullscreen()) {
                    GSYVideoManager.instance().setNeedMute(true);
                }
                curPlayer = (StandardGSYVideoPlayer) objects[1];
                isPlay = true;
                if (getListNeedAutoLand()) {
                    //重力全屏工具类
                    initOrientationUtils(holder.gsyVideoPlayer);
                    ListNormalAdapter.this.onPrepared();
                }
            }

            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                GSYVideoManager.instance().setNeedMute(true);
                if (getListNeedAutoLand()) {
                    ListNormalAdapter.this.onQuitFullscreen();
                }
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
                super.onEnterFullscreen(url, objects);
                GSYVideoManager.instance().setNeedMute(false);
                holder.gsyVideoPlayer.getCurrentPlayer().getTitleTextView().setText((String)objects[0]);
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
                curPlayer = null;
                isPlay = false;
                if (getListNeedAutoLand()) {
                    ListNormalAdapter.this.onAutoComplete();
                }
            }
        });

        return convertView;
    }

    /**
     * 全屏幕按键处理
     */
    private void resolveFullBtn(final StandardGSYVideoPlayer standardGSYVideoPlayer) {
        if (getListNeedAutoLand() && orientationUtils != null) {
            resolveFull();
        }
        standardGSYVideoPlayer.startWindowFullscreen(context, false, true);
    }

    class ViewHolder {
        SampleCoverVideo gsyVideoPlayer;
    }

    public void clearCache() {
        if (curPlayer != null) {
            curPlayer.getCurrentPlayer().clearCurrentCache();
        }
    }


    /**************************支持全屏重力全屏的部分**************************/

    /**
     * 列表时是否需要支持重力旋转
     *
     * @return 返回true为支持列表重力全屏
     */
    public boolean getListNeedAutoLand() {
        return true;
    }

    private void initOrientationUtils(StandardGSYVideoPlayer standardGSYVideoPlayer) {
        orientationUtils = new OrientationUtils((Activity) context, standardGSYVideoPlayer);
        //是否需要跟随系统旋转设置
        //orientationUtils.setRotateWithSystem(false);
        orientationUtils.setEnable(false);
    }

    private void resolveFull() {
        if (getListNeedAutoLand() && orientationUtils != null) {
            //直接横屏
            orientationUtils.resolveByClick();
        }
    }

    private void onQuitFullscreen() {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
    }

    public void onAutoComplete() {
        if (orientationUtils != null) {
            orientationUtils.setEnable(false);
            orientationUtils.releaseListener();
            orientationUtils = null;
        }
        isPlay = false;
    }

    public void onPrepared() {
        if (orientationUtils == null) {
            return;
        }
        //开始播放了才能旋转和全屏
        orientationUtils.setEnable(true);
    }

    public void onConfigurationChanged(Activity activity, Configuration newConfig) {
        //如果旋转了就全屏
        if (isPlay && curPlayer != null && orientationUtils != null) {
            curPlayer.onConfigurationChanged(activity, newConfig, orientationUtils);
        }
    }

    public OrientationUtils getOrientationUtils() {
        return orientationUtils;
    }


    public void onBackPressed() {
        if (orientationUtils != null) {
            orientationUtils.backToProtVideo();
        }
    }

    public void onDestroy() {
        if (isPlay && curPlayer != null) {
            curPlayer.getCurrentPlayer().release();
        }
        if (orientationUtils != null) {
            orientationUtils.releaseListener();
            orientationUtils = null;
        }
    }

}
