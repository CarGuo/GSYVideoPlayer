package com.example.gsyvideoplayer.simple.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.VideoModel;
import com.example.gsyvideoplayer.video.SampleCoverVideo;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

public class SimpleListVideoModeAdapter extends BaseAdapter {

    public static final String TAG = "ListNormalAdapter22";

    private List<VideoModel> list = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;

    private StandardGSYVideoPlayer curPlayer;

    protected OrientationUtils orientationUtils;

    protected boolean isPlay;

    protected boolean isFull;

    public SimpleListVideoModeAdapter(Context context) {
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
            convertView = inflater.inflate(R.layout.list_video_simple_mode1, null);
            holder.gsyVideoPlayer = (SampleCoverVideo) convertView.findViewById(R.id.video_item_player);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String urlH = "https://www.w3schools.com/html/mov_bbb.mp4";
        final String urlV = "https://test-streams.mux.dev/x36xhzz/x36xhzz.m3u8";
        final String url = (position % 2 == 0) ? urlH : urlV;

        if (position % 2 == 0) {
            holder.gsyVideoPlayer.loadCoverImage(url, R.mipmap.xxx1);
        } else {
            holder.gsyVideoPlayer.loadCoverImage(url, R.mipmap.xxx2);
        }

        holder.gsyVideoPlayer.setUpLazy(url, true, null, null, "这是title");
        //增加title
        holder.gsyVideoPlayer.getTitleTextView().setVisibility(View.GONE);
        //设置返回键
        holder.gsyVideoPlayer.getBackButton().setVisibility(View.GONE);
        //设置全屏按键功能
        holder.gsyVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.gsyVideoPlayer.startWindowFullscreen(context, false, true);
            }
        });
        //防止错位设置
        holder.gsyVideoPlayer.setPlayTag(TAG);
        holder.gsyVideoPlayer.setLockLand(true);
        holder.gsyVideoPlayer.setPlayPosition(position);
        //是否根据视频尺寸，自动选择竖屏全屏或者横屏全屏，这个标志为和 setLockLand 冲突，需要和 orientationUtils 使用
        holder.gsyVideoPlayer.setAutoFullWithSize(false);
        //音频焦点冲突时是否释放
        holder.gsyVideoPlayer.setReleaseWhenLossAudio(false);
        //全屏动画
        holder.gsyVideoPlayer.setShowFullAnimation(true);
        //小屏时不触摸滑动
        holder.gsyVideoPlayer.setIsTouchWiget(false);
        //全屏是否需要lock功能
        return convertView;

    }

    class ViewHolder {
        SampleCoverVideo gsyVideoPlayer;
    }

}
