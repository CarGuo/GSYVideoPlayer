package com.example.gsyvideoplayer;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.example.gsyvideoplayer.model.VideoModel;
import com.example.gsyvideoplayer.video.SampleCoverVideo;
import com.shuyu.gsyvideoplayer.GSYVideoADManager;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.video.GSYADVideoPlayer;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 带广告播放列表，支持中间插入广告模式
 */
public class ListADVideoActivity extends AppCompatActivity {

    @BindView(R.id.video_list)
    ListView videoList;

    ListADNormalAdapter listADNormalAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 设置一个exit transition
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
            getWindow().setEnterTransition(new Explode());
            getWindow().setExitTransition(new Explode());
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_video);
        ButterKnife.bind(this);

        listADNormalAdapter = new ListADNormalAdapter(this);
        videoList.setAdapter(listADNormalAdapter);

        videoList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int lastVisibleItem = firstVisibleItem + visibleItemCount;
                //大于0说明有播放
                if (GSYVideoManager.instance().getPlayPosition() >= 0) {
                    //当前播放的位置
                    int position = GSYVideoManager.instance().getPlayPosition();
                    //对应的播放列表TAG
                    if (GSYVideoManager.instance().getPlayTag().equals(ListADNormalAdapter.TAG)
                            && (position < firstVisibleItem || position > lastVisibleItem)) {
                        //如果滑出去了上面和下面就是否，和今日头条一样
                        //释放广告和视频
                        if (GSYVideoADManager.instance().listener() != null) {
                            GSYVideoADManager.instance().listener().onAutoCompletion();
                        }
                        GSYVideoADManager.releaseAllVideos();
                        GSYVideoManager.releaseAllVideos();
                        listADNormalAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }

    @Override
    public void onBackPressed() {
        if (GSYVideoADManager.backFromWindowFull(this)) {
            return;
        }
        if (GSYVideoManager.backFromWindowFull(this)) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GSYVideoManager.onPause();
        GSYVideoADManager.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GSYVideoManager.onResume();
        GSYVideoADManager.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        GSYVideoManager.releaseAllVideos();
        GSYVideoADManager.releaseAllVideos();
    }


    public class ListADNormalAdapter extends BaseAdapter {

        public static final String TAG = "ListADNormalAdapter";

        private List<VideoModel> list = new ArrayList<>();
        private LayoutInflater inflater = null;
        private Context context;

        public ListADNormalAdapter(Context context) {
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
                convertView = inflater.inflate(R.layout.list_video_item_ad, null);
                holder.gsyVideoPlayer = (SampleCoverVideo) convertView.findViewById(R.id.video_item_player);
                holder.adVideoPlayer = (GSYADVideoPlayer) convertView.findViewById(R.id.video_ad_player);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            final String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
            final String urlAD = "http://video.7k.cn/app_video/20171202/6c8cf3ea/v.m3u8.mp4";

            //多个播放时必须在setUpLazy、setUp和getGSYVideoManager()等前面设置
            holder.gsyVideoPlayer.setPlayTag(TAG);
            holder.gsyVideoPlayer.setPlayPosition(position);

            boolean isPlaying = holder.gsyVideoPlayer.getCurrentPlayer().isInPlayingState();

            if (!isPlaying) {
                holder.gsyVideoPlayer.setUpLazy(url, false, null, null, "这是title");
            }

            boolean isADPlaying = holder.adVideoPlayer.getCurrentPlayer().isInPlayingState();
            if (!isADPlaying) {
                holder.adVideoPlayer.setUpLazy(urlAD, false, null, null, "这是title");
            }


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
            holder.adVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    resolveFullBtn(holder.adVideoPlayer);
                }
            });
            holder.gsyVideoPlayer.setRotateViewAuto(false);
            holder.adVideoPlayer.setRotateViewAuto(false);
            holder.gsyVideoPlayer.setLockLand(true);
            holder.adVideoPlayer.setLockLand(true);
            holder.gsyVideoPlayer.setReleaseWhenLossAudio(false);
            holder.adVideoPlayer.setReleaseWhenLossAudio(false);
            holder.gsyVideoPlayer.setShowFullAnimation(false);
            holder.adVideoPlayer.setShowFullAnimation(false);
            holder.gsyVideoPlayer.setIsTouchWiget(false);
            holder.adVideoPlayer.setIsTouchWiget(false);

            holder.gsyVideoPlayer.setNeedLockFull(true);

            if (position % 2 == 0) {
                holder.gsyVideoPlayer.loadCoverImage(url, R.mipmap.xxx1);
            } else {
                holder.gsyVideoPlayer.loadCoverImage(url, R.mipmap.xxx2);
            }

            holder.gsyVideoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {


                @Override
                public void onPrepared(String url, Object... objects) {
                    super.onPrepared(url, objects);
                    if (isNeedAdOnStart())
                        startAdPlay(holder.adVideoPlayer, holder.gsyVideoPlayer);
                }

                @Override
                public void onQuitFullscreen(String url, Object... objects) {
                    super.onQuitFullscreen(url, objects);
                }

                @Override
                public void onEnterFullscreen(String url, Object... objects) {
                    super.onEnterFullscreen(url, objects);
                    holder.gsyVideoPlayer.getCurrentPlayer().getTitleTextView().setText((String) objects[0]);
                }

                @Override
                public void onAutoComplete(String url, Object... objects) {
                    super.onAutoComplete(url, objects);
                }
            });

            holder.adVideoPlayer.setVideoAllCallBack(new GSYSampleCallBack() {


                @Override
                public void onAutoComplete(String url, Object... objects) {
                    //广告结束，释放
                    holder.adVideoPlayer.getCurrentPlayer().release();
                    holder.adVideoPlayer.onVideoReset();
                    holder.adVideoPlayer.setVisibility(View.GONE);
                    //开始播放原视频，根据是否处于全屏状态判断
                    holder.gsyVideoPlayer.getCurrentPlayer().startAfterPrepared();
                    if (holder.adVideoPlayer.getCurrentPlayer().isIfCurrentIsFullscreen()) {
                        holder.adVideoPlayer.removeFullWindowViewOnly();
                        if (!holder.gsyVideoPlayer.getCurrentPlayer().isIfCurrentIsFullscreen()) {
                            resolveFullBtn(holder.gsyVideoPlayer);
                            holder.gsyVideoPlayer.setSaveBeforeFullSystemUiVisibility(holder.adVideoPlayer.getSaveBeforeFullSystemUiVisibility());
                        }
                    }
                }

                @Override
                public void onQuitFullscreen(String url, Object... objects) {
                    //退出全屏逻辑
                    if (holder.gsyVideoPlayer.isIfCurrentIsFullscreen()) {
                        holder.gsyVideoPlayer.onBackFullscreen();
                    }
                }

            });


            return convertView;
        }

        /**
         * 全屏幕按键处理
         */
        private void resolveFullBtn(final StandardGSYVideoPlayer standardGSYVideoPlayer) {
            standardGSYVideoPlayer.startWindowFullscreen(context, false, true);
        }


        /**
         * 显示播放广告
         */
        public void startAdPlay(GSYADVideoPlayer gsyadVideoPlayer, StandardGSYVideoPlayer normalPlayer) {
            gsyadVideoPlayer.setVisibility(View.VISIBLE);
            gsyadVideoPlayer.startPlayLogic();
            if (normalPlayer.getCurrentPlayer().isIfCurrentIsFullscreen()) {
                resolveFullBtn(gsyadVideoPlayer);
                gsyadVideoPlayer.setSaveBeforeFullSystemUiVisibility(normalPlayer.getSaveBeforeFullSystemUiVisibility());
            }
        }

        class ViewHolder {
            SampleCoverVideo gsyVideoPlayer;
            GSYADVideoPlayer adVideoPlayer;
        }
    }


    /**
     * 需要片头广告
     */
    public boolean isNeedAdOnStart() {
        return true;
    }

}
