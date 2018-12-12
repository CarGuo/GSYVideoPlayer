package com.example.gsyvideoplayer.switchplay;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.VideoModel;
import com.shuyu.gsyvideoplayer.GSYVideoManager;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

public class SwitchListVideoAdapter extends BaseAdapter {

    public static final String TAG = "SwitchListVideoAdapter";

    private List<VideoModel> list = new ArrayList<>();
    private LayoutInflater inflater;
    private Context context;

    private StandardGSYVideoPlayer curPlayer;

    protected OrientationUtils orientationUtils;

    protected boolean isPlay;

    protected boolean isFull;

    public SwitchListVideoAdapter(Context context) {
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
            convertView = inflater.inflate(R.layout.switch_list_video_item, null);
            holder.gsyVideoPlayer = (SwitchVideo) convertView.findViewById(R.id.video_item_player);
            holder.imageView = new ImageView(context);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        final String urlH = "http://9890.vod.myqcloud.com/9890_4e292f9a3dd011e6b4078980237cc3d3.f20.mp4";
        final String urlV = "http://wdquan-space.b0.upaiyun.com/VIDEO/2018/11/22/ae0645396048_hls_time10.m3u8";
        final String url = (position % 2 == 0) ? urlH : urlV;
        final int coverId = (position % 2 == 0) ? R.mipmap.xxx1 : R.mipmap.xxx2;


        //防止错位设置
        holder.gsyVideoPlayer.setPlayTag(TAG);
        holder.gsyVideoPlayer.setPlayPosition(position);
        SwitchUtil.optionPlayer(holder.gsyVideoPlayer, url, true, "这是title");
        holder.gsyVideoPlayer.setUpLazy(url, true, null, null, "这是title");

        //增加封面
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        holder.imageView.setImageResource(coverId);
        if (holder.imageView.getParent() != null) {
            ViewGroup viewGroup = (ViewGroup) holder.imageView.getParent();
            viewGroup.removeView(holder.imageView);
        }
        holder.gsyVideoPlayer.setThumbImageView(holder.imageView);

        if (GSYVideoManager.instance().getPlayTag().equals(SwitchListVideoAdapter.TAG)
                && (position == GSYVideoManager.instance().getPlayPosition())) {
            holder.gsyVideoPlayer.getThumbImageViewLayout().setVisibility(View.GONE);
        } else {
            holder.gsyVideoPlayer.getThumbImageViewLayout().setVisibility(View.VISIBLE);
        }

        return convertView;

    }

    class ViewHolder {
        SwitchVideo gsyVideoPlayer;
        ImageView imageView;
    }

}
