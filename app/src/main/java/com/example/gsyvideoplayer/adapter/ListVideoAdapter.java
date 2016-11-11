package com.example.gsyvideoplayer.adapter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.model.VideoModel;
import com.shuyu.gsyvideoplayer.utils.OrientationUtils;
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import static com.example.gsyvideoplayer.utils.CommonUtil.setViewHeight;

/**
 * Created by shuyu on 2016/11/11.
 */

public class ListVideoAdapter extends BaseAdapter {

    private List<VideoModel> list = new ArrayList<>();
    private LayoutInflater inflater = null;
    private Context context;

    private ViewGroup rootView;
    private OrientationUtils orientationUtils;

    private boolean isFullVideo;

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
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = inflater.inflate(R.layout.list_video_item, null);
            holder.standardGSYVideoPlayer = (StandardGSYVideoPlayer) convertView.findViewById(R.id.list_item_video);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final String url = "http://baobab.wdjcdn.com/14564977406580.mp4";
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
                resolveFullBtn(holder.standardGSYVideoPlayer);
            }
        });
        return convertView;
    }

    /**
     * 全屏幕按键处理
     */
    private void resolveFullBtn(final StandardGSYVideoPlayer standardGSYVideoPlayer) {
        if (orientationUtils != null) {
            orientationUtils.setEnable(false);
        }
        orientationUtils = new OrientationUtils((Activity) context, standardGSYVideoPlayer);
        if (isFullVideo) {
            orientationUtils.setEnable(false);
            int delay = orientationUtils.backToProtVideo();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        TransitionManager.beginDelayedTransition(rootView);
                    }
                    /*standardGSYVideoPlayer.getBackButton().setVisibility(View.GONE);
                    setViewHeight(standardGSYVideoPlayer, ViewGroup.LayoutParams.MATCH_PARENT,
                            (int) context.getResources().getDimension(R.dimen.post_media_height));*/

                    //standardGSYVideoPlayer.clearFullscreenLayout();
                    standardGSYVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_enlarge);
                    isFullVideo = false;
                }
            }, delay);
        } else {
            orientationUtils.setEnable(true);
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                TransitionManager.beginDelayedTransition(rootView);
            }
            standardGSYVideoPlayer.getBackButton().setVisibility(View.VISIBLE);
            setViewHeight(standardGSYVideoPlayer, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);*/
            //standardGSYVideoPlayer.startWindowFullscreen();
            standardGSYVideoPlayer.getFullscreenButton().setImageResource(R.drawable.video_shrink);
            isFullVideo = true;
        }
    }

    class ViewHolder {
        StandardGSYVideoPlayer standardGSYVideoPlayer;
    }

    public void setRootView(ViewGroup rootView) {
        this.rootView = rootView;
    }
}
