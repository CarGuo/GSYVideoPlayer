package com.example.gsyvideoplayer.holder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.gsyvideoplayer.adapter.RecyclerBaseAdapter;
import com.shuyu.gsyvideoplayer.utils.ListVideoUtil;

/**
 * Created by shuyu on 2016/12/3.
 */

public class RecyclerItemBaseHolder extends RecyclerView.ViewHolder {

    RecyclerView.Adapter recyclerBaseAdapter;

    public RecyclerItemBaseHolder(View itemView) {
        super(itemView);
    }

    public RecyclerView.Adapter getRecyclerBaseAdapter() {
        return recyclerBaseAdapter;
    }

    public void setRecyclerBaseAdapter(RecyclerView.Adapter recyclerBaseAdapter) {
        this.recyclerBaseAdapter = recyclerBaseAdapter;
    }
}
