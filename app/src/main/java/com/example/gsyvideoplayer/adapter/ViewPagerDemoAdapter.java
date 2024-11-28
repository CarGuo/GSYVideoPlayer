package com.example.gsyvideoplayer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.gsyvideoplayer.R;
import com.example.gsyvideoplayer.holder.RecyclerItemPlayNormalHolder;
import com.example.gsyvideoplayer.holder.RecyclerPageItemViewHolder;
import com.example.gsyvideoplayer.model.VideoModel;

import java.util.List;

public class ViewPagerDemoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final static String TAG = "ViewPagerDemoAdapter";

    private List<VideoModel> itemDataList = null;
    private Context context = null;

    public ViewPagerDemoAdapter(Context context, List<VideoModel> itemDataList) {
        this.itemDataList = itemDataList;
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 2) {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_viewpager2_item, parent, false);
            final RecyclerView.ViewHolder holder = new RecyclerItemPlayNormalHolder(context, v);
            return holder;
        } else {
            View v = LayoutInflater.from(context).inflate(R.layout.layout_viewpager_demo_item, parent, false);
            final RecyclerView.ViewHolder holder = new RecyclerPageItemViewHolder(context, v);
            return holder;
        }

    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(position == 2) {
            RecyclerItemPlayNormalHolder recyclerItemViewHolder = (RecyclerItemPlayNormalHolder) holder;
            recyclerItemViewHolder.setRecyclerBaseAdapter(this);
            recyclerItemViewHolder.onBind(position, itemDataList.get(position));
        } else {
            RecyclerPageItemViewHolder recyclerItemViewHolder = (RecyclerPageItemViewHolder) holder;
            recyclerItemViewHolder.setRecyclerBaseAdapter(this);
            recyclerItemViewHolder.setText(position + "");
        }
    }

    @Override
    public int getItemCount() {
        return itemDataList.size();
    }


    @Override
    public int getItemViewType(int position) {
        if (position == 2) {
            return 2;
        }
        return 1;
    }

    public void setListData(List<VideoModel> data) {
        itemDataList = data;
        notifyDataSetChanged();
    }
}
