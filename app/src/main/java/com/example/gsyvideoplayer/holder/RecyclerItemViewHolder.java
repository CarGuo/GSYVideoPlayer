package com.example.gsyvideoplayer.holder;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.adapter.ListVideoAdapter;
import com.example.gsyvideoplayer.model.VideoModel;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by GUO on 2015/12/3.
 */
public class RecyclerItemViewHolder extends RecyclerItemBaseHolder {

    public final static String TAG = "RecyclerView2List";

    protected Context context = null;
    @BindView(R.id.list_item_container)
    FrameLayout listItemContainer;
    @BindView(R.id.list_item_btn)
    ImageView listItemBtn;

    ImageView imageView;

    public RecyclerItemViewHolder(Context context, View v) {
        super(v);
        this.context = context;
        ButterKnife.bind(this, v);
        imageView = new ImageView(context);
    }

    public void onBind(final int position, VideoModel videoModel) {

        //增加封面
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(R.mipmap.xxx1);

        listVideoUtil.addVideoPlayer(position, imageView, TAG, listItemContainer, listItemBtn);

        listItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRecyclerBaseAdapter().notifyDataSetChanged();
                //listVideoUtil.setLoop(true);
                listVideoUtil.setPlayPositionAndTag(position, TAG);
                listVideoUtil.setTitle("title " + position);
                String url;
                if (position % 2 == 0) {
                    url = "http://baobab.wdjcdn.com/14564977406580.mp4";
                } else {
                    url = "http://7xse1z.com1.z0.glb.clouddn.com/1491813192";
                }
                //listVideoUtil.setCachePath(new File(FileUtils.getPath()));
                listVideoUtil.startPlay(url);

                //必须在startPlay之后设置才能生效
                //listVideoUtil.getGsyVideoPlayer().getTitleTextView().setVisibility(View.VISIBLE);
            }
        });
    }

}





