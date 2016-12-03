package com.example.gsyvideoplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.holder.RecyclerItemViewHolder;
import com.example.gsyvideoplayer.model.VideoModel;
import com.shuyu.gsyvideoplayer.utils.ListVideoUtil;

import java.util.List;

/**
 * Created by GUO on 2015/12/3.
 */

/**
 * Created by Nelson on 15/11/9.
 */
public class RecyclerBaseAdapter extends RecyclerView.Adapter {

    private final static String TAG = "RecyclerBaseAdapter";

    private List<VideoModel> itemDataList = null;
    private Context context = null;
    private ListVideoUtil listVideoUtil;

    public RecyclerBaseAdapter(Context context, List<VideoModel> itemDataList) {
        this.itemDataList = itemDataList;
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                      int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.list_video_item, parent, false);
        final RecyclerView.ViewHolder holder = new RecyclerItemViewHolder(context, v);
        return holder;

    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        RecyclerItemViewHolder recyclerItemViewHolder = (RecyclerItemViewHolder) holder;
        recyclerItemViewHolder.setListVideoUtil(listVideoUtil);
        recyclerItemViewHolder.setRecyclerBaseAdapter(this);
        recyclerItemViewHolder.onBind(position, itemDataList.get(position));
    }

    @Override
    public int getItemCount() {
        return itemDataList.size();
    }


    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public void setListData(List<VideoModel> data) {
        itemDataList = data;
        notifyDataSetChanged();
    }

    public ListVideoUtil getListVideoUtil() {
        return listVideoUtil;
    }

    public void setListVideoUtil(ListVideoUtil listVideoUtil) {
        this.listVideoUtil = listVideoUtil;
    }
}
