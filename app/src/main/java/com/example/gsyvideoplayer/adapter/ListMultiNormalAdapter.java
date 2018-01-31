package com.example.gsyvideoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.VideoModel;
import com.example.gsyvideoplayer.video.MultiSampleVideo;
import com.shuyu.gsyvideoplayer.listener.GSYSampleCallBack;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * 多个播放的listview adapter
 * Created by shuyu on 2016/11/12.
 */

public class ListMultiNormalAdapter extends BaseAdapter {

    public static final String TAG = "ListMultiNormalAdapter";

    private List<VideoModel> list = new ArrayList<>();
    private LayoutInflater inflater = null;
    private Context context;

    private String fullKey = "null";

    public ListMultiNormalAdapter(Context context) {
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
            convertView = inflater.inflate(R.layout.list_video_item_mutli, null);
            holder.gsyVideoPlayer = (MultiSampleVideo) convertView.findViewById(R.id.video_item_player);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }


        final String url = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";

        //多个播放时必须在setUpLazy、setUp和getGSYVideoManager()等前面设置
        holder.gsyVideoPlayer.setPlayTag(TAG);
        holder.gsyVideoPlayer.setPlayPosition(position);

        boolean isPlaying = holder.gsyVideoPlayer.getCurrentPlayer().isInPlayingState();

        if (!isPlaying) {
            holder.gsyVideoPlayer.setUpLazy(url, false, null, null, "这是title");
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
        holder.gsyVideoPlayer.setRotateViewAuto(true);
        holder.gsyVideoPlayer.setLockLand(true);
        holder.gsyVideoPlayer.setReleaseWhenLossAudio(false);
        holder.gsyVideoPlayer.setShowFullAnimation(true);
        holder.gsyVideoPlayer.setIsTouchWiget(false);

        holder.gsyVideoPlayer.setNeedLockFull(true);

        if (position % 2 == 0) {
            holder.gsyVideoPlayer.loadCoverImage(url, R.mipmap.xxx1);
        } else {
            holder.gsyVideoPlayer.loadCoverImage(url, R.mipmap.xxx2);
        }

        holder.gsyVideoPlayer.setStandardVideoAllCallBack(new GSYSampleCallBack() {


            @Override
            public void onQuitFullscreen(String url, Object... objects) {
                super.onQuitFullscreen(url, objects);
                fullKey = "null";
            }

            @Override
            public void onEnterFullscreen(String url, Object... objects) {
                super.onEnterFullscreen(url, objects);
                holder.gsyVideoPlayer.getCurrentPlayer().getTitleTextView().setText((String) objects[0]);
                fullKey = holder.gsyVideoPlayer.getKey();
            }

            @Override
            public void onAutoComplete(String url, Object... objects) {
                super.onAutoComplete(url, objects);
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

    class ViewHolder {
        MultiSampleVideo gsyVideoPlayer;
    }


    public String getFullKey() {
        return fullKey;
    }

}
