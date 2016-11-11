package com.example.gsyvideoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.VideoModel;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuyu on 2016/11/11.
 */

public class ListVideoAdapter extends BaseAdapter {

    private List<VideoModel> list = new ArrayList<>();
    private LayoutInflater inflater = null;
    private Context context;

    public ListVideoAdapter(Context context) {
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_video_item, null);
            holder.standardGSYVideoPlayer = (StandardGSYVideoPlayer) convertView.findViewById(R.id.list_item_video);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String url = "http://baobab.wdjcdn.com/14564977406580.mp4";
        holder.standardGSYVideoPlayer.setUp(url, true, "");

        //增加封面
        ImageView imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);
        holder.standardGSYVideoPlayer.setThumbImageView(imageView);

        //增加title
        holder.standardGSYVideoPlayer.getTitleTextView().setVisibility(View.GONE);

        //设置返回键
        holder.standardGSYVideoPlayer.getBackButton().setVisibility(View.GONE);

        //设置全屏按键功能
        holder.standardGSYVideoPlayer.getFullscreenButton().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        return convertView;
    }

    class ViewHolder {
        StandardGSYVideoPlayer standardGSYVideoPlayer;
    }
}
